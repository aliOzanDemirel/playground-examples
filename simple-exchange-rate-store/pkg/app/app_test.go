package app

import (
	"context"
	"exchange-rate-store/pkg/logger"
	"exchange-rate-store/pkg/persistence"
	"net/http"
	"sync"
	"testing"
	"time"
)

func TestReadConf(t *testing.T) {

	t.Setenv("TEST_DEV_MOCK_CRYPTO_API", "false")
	t.Setenv("TEST_DEV_MOCK_DATA_STORE", "true")

	t.Setenv("TEST_RATE_PULL_INTERVAL", "3m")
	t.Setenv("TEST_PORT", "1234")
	t.Setenv("TEST_BASIC_AUTH_USER", "server_user")
	t.Setenv("TEST_BASIC_AUTH_PASSWORD", "server_passwd")

	t.Setenv("TEST_DB_HOST", "db_host")
	t.Setenv("TEST_DB_PORT", "8855")
	t.Setenv("TEST_DB_NAME", "test_db")
	t.Setenv("TEST_DB_USER", "db_user")
	t.Setenv("TEST_DB_PASSWORD", "db_password")

	c := &Config{}
	c.ReadFromEnv("TEST_")

	if c.Dev.MockCryptoApi {
		t.Errorf("unexpected Dev.MockCryptoApi: %t", c.Dev.MockCryptoApi)
	}
	if !c.Dev.MockDataStore {
		t.Errorf("unexpected Dev.MockDataStore: %t", c.Dev.MockDataStore)
	}

	if c.RatePullInterval != time.Minute*3 {
		t.Errorf("unexpected RatePullInterval: %s", c.RatePullInterval)
	}
	if c.Port != 1234 {
		t.Errorf("unexpected Dev.Port: %d", c.Port)
	}
	if c.BasicAuthUser != "server_user" {
		t.Errorf("unexpected BasicAuthUser: %s", c.BasicAuthUser)
	}
	if c.BasicAuthPassword != "server_passwd" {
		t.Errorf("unexpected BasicAuthPassword: %s", c.BasicAuthPassword)
	}

	if c.Database.DbName != "test_db" {
		t.Errorf("unexpected Database.DbName: %s", c.Database.DbName)
	}
	if c.Database.User != "db_user" {
		t.Errorf("unexpected Database.User: %s", c.Database.User)
	}
	if c.Database.Password != "db_password" {
		t.Errorf("unexpected Database.Password: %s", c.Database.Password)
	}
	if c.Database.Host != "db_host" {
		t.Errorf("unexpected Database.Host: %s", c.Database.Host)
	}
	if c.Database.Port != 8855 {
		t.Errorf("unexpected Database.Port: %d", c.Database.Port)
	}
}
func TestValidateServerConf(t *testing.T) {

	t.Run("valid conf", func(t *testing.T) {
		s := &ExchangeRateStore{
			conf: &Config{
				Port:              9999,
				BasicAuthUser:     "admin",
				BasicAuthPassword: "admin",
			},
		}
		err := s.validateServerConf()
		if err != nil {
			t.Errorf("failed validation -> %v", err)
		}
	})
	t.Run("missing basic auth user", func(t *testing.T) {
		s := &ExchangeRateStore{
			conf: &Config{
				Port:              9999,
				BasicAuthUser:     "",
				BasicAuthPassword: "admin",
			},
		}
		err := s.validateServerConf()
		if err == nil {
			t.Errorf("expected to fail validation")
		}
	})
	t.Run("missing basic auth password", func(t *testing.T) {
		s := &ExchangeRateStore{
			conf: &Config{
				Port:              9999,
				BasicAuthUser:     "admin",
				BasicAuthPassword: "",
			},
		}
		err := s.validateServerConf()
		if err == nil {
			t.Errorf("expected to fail validation")
		}
	})
	t.Run("missing port", func(t *testing.T) {
		s := &ExchangeRateStore{
			conf: &Config{
				Port:              0,
				BasicAuthUser:     "admin",
				BasicAuthPassword: "admin",
			},
		}
		err := s.validateServerConf()
		if err == nil {
			t.Errorf("expected to fail validation")
		}
	})
}

type moduleTestCtx struct {
	t   *testing.T
	app *ExchangeRateStore
}

func runTestApp(t *testing.T, testApp *ExchangeRateStore) *moduleTestCtx {

	logger.SetGlobalLogger(&logger.StdoutLogger{DebugLevel: true})

	err := testApp.Start()
	if err != nil {
		t.Fatalf("could not start test app instance -> %v", err)
	}

	return &moduleTestCtx{
		t:   t,
		app: testApp,
	}
}

func defaultTestApp() *ExchangeRateStore {

	ctx, cancel := context.WithCancel(context.Background())
	wg := &sync.WaitGroup{}
	m := &metrics{}
	testStore := &InMemoryStore{
		QuoteCurrencyToRates: make(map[string][]persistence.Rate),
	}
	randomizer := &RandomRateGenerator{}
	interval := 2 * time.Second
	return &ExchangeRateStore{
		signalCtx:       ctx,
		signalCtxCancel: cancel,
		shutdownWait:    wg,
		conf: &Config{
			Dev: Dev{
				MockCryptoApi: true,
				MockDataStore: true,
			},
			RatePullInterval:  interval,
			Port:              3333,
			BasicAuthUser:     "test",
			BasicAuthPassword: "test",
		},
		pullJobs: []*ExchangeRatePullJob{{
			ctx:               ctx,
			waitJobGroup:      wg,
			metrics:           m,
			ratePersistence:   testStore,
			rateRetriever:     randomizer,
			rateBaseCurrency:  "a",
			rateQuoteCurrency: "b",
			pullInterval:      interval,
		}, {
			ctx:               ctx,
			waitJobGroup:      wg,
			metrics:           m,
			ratePersistence:   testStore,
			rateRetriever:     randomizer,
			rateBaseCurrency:  "a",
			rateQuoteCurrency: "c",
			pullInterval:      interval,
		}, {
			ctx:               ctx,
			waitJobGroup:      wg,
			metrics:           m,
			ratePersistence:   testStore,
			rateRetriever:     randomizer,
			rateBaseCurrency:  "b",
			rateQuoteCurrency: "c",
			pullInterval:      interval,
		}, {
			ctx:               ctx,
			waitJobGroup:      wg,
			metrics:           m,
			ratePersistence:   testStore,
			rateRetriever:     randomizer,
			rateBaseCurrency:  "c",
			rateQuoteCurrency: "b",
			pullInterval:      interval,
		}},
		metrics:         m,
		rateRetriever:   randomizer,
		ratePersistence: testStore,
	}
}
func TestExchangeRateStore_instantiate(t *testing.T) {

	testApp := defaultTestApp()
	tc := runTestApp(t, testApp)
	defer tc.app.Shutdown()

	wait := tc.app.conf.RatePullInterval + 100*time.Millisecond
	time.Sleep(wait)
	tc.app.Shutdown()

	if tc.app.metrics.pullJobSuccessCount.Load() != 4 {
		t.Errorf("unexpected pul job success count, expected: 2, actual: %d", tc.app.metrics.pullJobSuccessCount.Load())
	}
	if tc.app.metrics.pullJobFailCount.Load() != 0 {
		t.Errorf("unexpected pul job fail count, expected: 0, actual: %d", tc.app.metrics.pullJobFailCount.Load())
	}
}

func TestExchangeRateStore_checkHealthEndpoint(t *testing.T) {

	testApp := defaultTestApp()
	tc := runTestApp(t, testApp)
	defer tc.app.Shutdown()

	request, err := http.NewRequest("GET", "http://localhost:3333/health", nil)
	if err != nil {
		t.Fatalf("failed test -> %v", err)
	}
	resp, err := http.DefaultClient.Do(request)
	if err != nil {
		t.Fatalf("failed test -> %v", err)
	}
	if resp.StatusCode != 200 {
		t.Errorf("unexpected response status, actual: %d", resp.StatusCode)
	}
}
