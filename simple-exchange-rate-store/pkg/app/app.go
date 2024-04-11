package app

import (
	"context"
	"exchange-rate-store/pkg/alphavantage"
	"exchange-rate-store/pkg/logger"
	"exchange-rate-store/pkg/persistence"
	"fmt"
	"net/http"
	"os"
	"os/signal"
	"sync"
	"sync/atomic"
	"syscall"
	"time"
)

type metrics struct {
	pullJobSuccessCount atomic.Uint64 // cumulative counter
	pullJobFailCount    atomic.Uint64 // cumulative counter
}

type ExchangeRateStore struct {
	signalCtx       context.Context
	signalCtxCancel context.CancelFunc // to restore default behaviour to OS signals
	shutdownWait    *sync.WaitGroup
	metrics         *metrics
	conf            *Config
	pullJobs        []*ExchangeRatePullJob
	ratePersistence ExchangeRatePersistence
	rateRetriever   ExchangeRateRetriever
	server          *http.Server
}

func NewExchangeRateStore(config Config) (*ExchangeRateStore, error) {

	const maxPullInterval = 5 * time.Minute
	if config.RatePullInterval > maxPullInterval {
		return nil, fmt.Errorf("exchange rate pull interval '%v' cannot be greater than '%v'", config.RatePullInterval, maxPullInterval)
	}

	sigtermCtx, restoreDefaultBehaviour := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	waitGroup := &sync.WaitGroup{}
	metrics := &metrics{}

	var api ExchangeRateRetriever
	if config.Dev.MockCryptoApi {

		api = &RandomRateGenerator{}
	} else {

		alphaApi, err := alphavantage.NewAlphaVantageApi(config.AlphaVantageApi)
		if err != nil {
			return nil, fmt.Errorf("could not create alpha vantage api -> %v", err)
		}
		api = &alphaApi
	}

	var db ExchangeRatePersistence
	if config.Dev.MockDataStore {

		db = &InMemoryStore{
			QuoteCurrencyToRates: make(map[string][]persistence.Rate),
		}
	} else {

		persistenceDb, err := persistence.NewPersistence(config.Database)
		if err != nil {
			return nil, fmt.Errorf("could not create database client -> %v", err)
		}
		db = persistenceDb
	}

	var pullJobs []*ExchangeRatePullJob
	quoteCurrenciesToPull := []string{currencyEur, currencyCzk}
	for i := range quoteCurrenciesToPull {
		currency := quoteCurrenciesToPull[i]
		pullJobs = append(pullJobs, &ExchangeRatePullJob{
			ctx:               sigtermCtx,
			waitJobGroup:      waitGroup,
			metrics:           metrics,
			ratePersistence:   db,
			rateRetriever:     api,
			rateBaseCurrency:  currencyBtc,
			rateQuoteCurrency: currency,
			pullInterval:      config.RatePullInterval,
		})
	}

	return &ExchangeRateStore{
		signalCtx:       sigtermCtx,
		signalCtxCancel: restoreDefaultBehaviour,
		shutdownWait:    waitGroup,
		conf:            &config,
		metrics:         metrics,
		pullJobs:        pullJobs,
		ratePersistence: db,
		rateRetriever:   api,
	}, nil
}

func (b *ExchangeRateStore) Shutdown() {

	logger.Info(nil, "[shutdown] starting to wait for resources and goroutines")

	const shutdownTimeout = 5 * time.Second
	signalWaitDone := make(chan struct{})
	go func() {
		b.signalCtxCancel()

		if b.server != nil {
			ctx, cancel := context.WithTimeout(context.Background(), time.Second)
			defer cancel()

			err := b.server.Shutdown(ctx)
			if err != nil {
				logger.Error(nil, err, "[shutdown] error occurred while closing http server")
			}
		}

		if b.ratePersistence != nil {
			err := b.ratePersistence.Close()
			if err != nil {
				logger.Error(nil, err, "[shutdown] error occurred while closing persistence")
			}
		}

		b.shutdownWait.Wait()
		close(signalWaitDone)
	}()

	select {
	case <-signalWaitDone:
		logger.Info(nil, "[shutdown] done waiting")
	case <-time.After(shutdownTimeout):
		logger.Info(nil, "[shutdown] wait timed out after '%v'", shutdownTimeout)
	}
}

func (b *ExchangeRateStore) Start() error {

	// start pull jobs
	for i := range b.pullJobs {
		job := b.pullJobs[i]
		job.startAsync()
	}

	srv, err := b.startServerAsync()
	if err != nil {
		return err
	}

	b.server = srv
	return nil
}
