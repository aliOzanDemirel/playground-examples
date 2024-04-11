package app

import (
	"context"
	"exchange-rate-store/pkg/alphavantage"
	"math/rand"
	"sync/atomic"
	"time"
)

type RandomRateGenerator struct {
	count atomic.Uint64
}

func (r *RandomRateGenerator) FetchCurrentRate(ctx context.Context, fromCurrency, toCurrency string) (alphavantage.CurrencyExchangeRateResponse, error) {

	defer func() { r.count.Add(1) }()

	value := rand.Float64()
	ask, bid := value+0.000025, value-0.000025

	now := time.Now()
	zone, _ := now.Zone()
	lastRefreshed := now.Format(time.DateTime)
	return alphavantage.CurrencyExchangeRateResponse{RealtimeCurrencyExchangeRate: alphavantage.CurrencyExchangeRate{
		FromCurrencyCode:     fromCurrency,
		FromCurrencyName:     "RandomRateGenerator -> " + fromCurrency,
		ToCurrencyCode:       toCurrency,
		ToCurrencyName:       "RandomRateGenerator -> " + toCurrency,
		ExchangeRate:         value,
		LastRefreshed:        lastRefreshed,
		LastRefreshedUtcTime: now.UTC(),
		TimeZone:             zone,
		BidPrice:             bid,
		AskPrice:             ask,
	}}, nil
}

func (r *RandomRateGenerator) GetFailCount() uint64    { return 0 }
func (r *RandomRateGenerator) GetSuccessCount() uint64 { return r.count.Load() }
