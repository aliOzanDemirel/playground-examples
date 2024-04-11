package alphavantage

import (
	"exchange-rate-store/pkg/common"
	"exchange-rate-store/pkg/httpclient"
	"exchange-rate-store/pkg/logger"
	"fmt"
	"net/http"
	"runtime"
	"sync/atomic"
	"time"
)

// Api https://www.alphavantage.co/documentation/
type Api struct {
	conf                      *Config
	client                    *httpclient.LoggingClient
	exchangeRateQueryEndpoint string
	userAgent                 string
	failedRequestCount        atomic.Uint64
	successfulRequestCount    atomic.Uint64
}

type Config struct {
	BaseUrl string
	ApiKey  string
}

const alphaVantageExchangeRateQueryUrlFormat = "%s/query?function=CURRENCY_EXCHANGE_RATE"

func NewAlphaVantageApi(config Config) (Api, error) {

	if config.ApiKey == "" {
		return Api{}, fmt.Errorf("missing api key")
	}
	if config.BaseUrl == "" {
		return Api{}, fmt.Errorf("missing base url")
	}
	err := common.ValidateHttpUrl(config.BaseUrl, true)
	if err != nil {
		return Api{}, err
	}

	userAgent := fmt.Sprintf("Api go/%s/%s/%s", runtime.Version(), runtime.GOOS, runtime.GOARCH)
	queryUrl := fmt.Sprintf(alphaVantageExchangeRateQueryUrlFormat, config.BaseUrl)

	defaultHttpClient := &http.Client{
		Transport: http.DefaultTransport,
		Timeout:   45 * time.Second,
	}
	decoratedWithLoggingClient := &httpclient.LoggingClient{
		IsDumpAlwaysEnabled: logger.IsDebugOn,
		Delegate:            defaultHttpClient,
	}
	return Api{
		conf:                      &config,
		client:                    decoratedWithLoggingClient,
		exchangeRateQueryEndpoint: queryUrl,
		userAgent:                 userAgent,
	}, nil
}

func (a *Api) GetFailCount() uint64    { return a.failedRequestCount.Load() }
func (a *Api) GetSuccessCount() uint64 { return a.successfulRequestCount.Load() }
