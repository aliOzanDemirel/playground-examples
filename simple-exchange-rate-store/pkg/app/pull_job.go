package app

import (
	"context"
	"exchange-rate-store/pkg/alphavantage"
	"exchange-rate-store/pkg/logger"
	"exchange-rate-store/pkg/persistence"
	"sync"
	"time"
)

const (
	currencyBtc = "BTC"
	currencyEur = "EUR"
	currencyCzk = "CZK"
)

type ExchangeRatePersistence interface {
	Close() error
	Save(persistence.Rate) error
	GetAggregatedRatesSinceRequestedDate(time.Time, string) ([]persistence.AggregatedRate, error)
	DeleteRatesOlderThanDate(time.Time) (int64, error)
}

type ExchangeRateRetriever interface {
	FetchCurrentRate(ctx context.Context, fromCurrency, toCurrency string) (alphavantage.CurrencyExchangeRateResponse, error)
	GetFailCount() uint64
	GetSuccessCount() uint64
}

type ExchangeRatePullJob struct {
	ctx               context.Context
	waitJobGroup      *sync.WaitGroup
	metrics           *metrics
	ratePersistence   ExchangeRatePersistence
	rateRetriever     ExchangeRateRetriever
	rateBaseCurrency  string // BTC/EUR -> BTC
	rateQuoteCurrency string // BTC/EUR -> EUR
	pullInterval      time.Duration
}

func (j *ExchangeRatePullJob) startAsync() {

	fields := map[string]interface{}{
		"baseCurrency":  j.rateBaseCurrency,
		"quoteCurrency": j.rateQuoteCurrency,
	}
	if j.pullInterval <= 0 {
		logger.Info(fields, "[pull-job] disabled by interval, will not run the job")
		return
	}
	logger.Info(fields, "[pull-job] starting job")

	j.waitJobGroup.Add(1)
	go func() {
		defer j.waitJobGroup.Done()

		flowTicker := time.NewTicker(j.pullInterval)
		for {
			select {
			case <-j.ctx.Done():
				logger.Info(fields, "[pull-job] exiting job")
				return

			case <-flowTicker.C:
				err := j.pullRateDataAndPersist()
				if err != nil {

					logger.Error(fields, err, "[pull-job] failed to pull and store exchange rate")
					j.metrics.pullJobFailCount.Add(1)

				} else {

					logger.Debug(fields, "[pull-job] completed exchange rate pull")
					j.metrics.pullJobSuccessCount.Add(1)
				}
			}
		}
	}()
}

func (j *ExchangeRatePullJob) pullRateDataAndPersist() error {

	rateResponse, err := j.rateRetriever.FetchCurrentRate(j.ctx, j.rateBaseCurrency, j.rateQuoteCurrency)
	if err != nil {
		return err
	}

	r := rateResponse.RealtimeCurrencyExchangeRate
	return j.ratePersistence.Save(persistence.Rate{
		BaseCurrency:        r.FromCurrencyCode,
		QuoteCurrency:       r.ToCurrencyCode,
		ExchangeRate:        r.ExchangeRate,
		ExchangeRateUtcTime: r.LastRefreshedUtcTime,
		BidPrice:            r.BidPrice,
		AskPrice:            r.AskPrice,
	})
}
