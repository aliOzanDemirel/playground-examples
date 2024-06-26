package app

import (
	"exchange-rate-store/pkg/logger"
	"exchange-rate-store/pkg/persistence"
	"sync"
	"time"
)

type InMemoryStore struct {
	QuoteCurrencyToRates map[string][]persistence.Rate
	mu                   sync.Mutex
}

func (r *InMemoryStore) Close() error { return nil }

func (r *InMemoryStore) InsertNewRate(newRate persistence.Rate) error {

	r.mu.Lock()
	defer r.mu.Unlock()

	rates, found := r.QuoteCurrencyToRates[newRate.QuoteCurrency]
	if !found {
		rates = []persistence.Rate{newRate}
		r.QuoteCurrencyToRates[newRate.QuoteCurrency] = rates
	} else {
		rates = append(rates, newRate)
	}

	logger.Debug(nil, "[in-memory-store] inserted new rate -> %+v", newRate)
	return nil
}

func (r *InMemoryStore) DeleteRatesOlderThanDate(date time.Time) (int64, error) {

	r.mu.Lock()
	defer r.mu.Unlock()

	count := int64(0)
	for currency := range r.QuoteCurrencyToRates {
		rates := r.QuoteCurrencyToRates[currency]
		var newRates []persistence.Rate
		for i := range rates {
			rate := rates[i]
			if rate.ExchangeRateUtcTime.Sub(date) >= 0 {
				newRates = append(newRates, rate)
			} else {
				count++
			}
		}
		r.QuoteCurrencyToRates[currency] = newRates
	}
	return count, nil
}

// GetAggregatedRatesSinceRequestedDate aggregates everything kept in memory, not meant to be correct
func (r *InMemoryStore) GetAggregatedRatesSinceRequestedDate(date time.Time, quoteCurrency string) ([]persistence.AggregatedRate, error) {

	r.mu.Lock()
	defer r.mu.Unlock()

	rates := r.QuoteCurrencyToRates[quoteCurrency]
	if len(rates) == 0 {
		return nil, nil
	}

	sum, maxRate, minRate := rates[0].ExchangeRate, rates[0].ExchangeRate, rates[0].ExchangeRate
	for i := 1; i < len(rates); i++ {
		r := rates[i].ExchangeRate
		sum += r
		if r > maxRate {
			maxRate = r
		}
		if r < minRate {
			minRate = r
		}
	}

	agg := persistence.AggregatedRate{
		BaseCurrency:   "-",
		QuoteCurrency:  quoteCurrency,
		AggregatedDate: time.Now(),
		DailyAvg:       sum / float64(len(rates)),
		DailyMax:       maxRate,
		DailyMin:       minRate,
		MonthlyAvg:     sum / float64(len(rates)),
		MonthlyMax:     maxRate,
		MonthlyMin:     minRate,
		DataPointCount: uint32(len(rates)),
		CreatedDate:    time.Now(),
	}
	return []persistence.AggregatedRate{agg}, nil
}

func (r *InMemoryStore) GetAggregatedRates(time.Time) ([]persistence.AggregatedRate, error) {
	return []persistence.AggregatedRate{{
		BaseCurrency:   "agg",
		QuoteCurrency:  "gaa",
		AggregatedDate: time.Now(),
		DailyAvg:       111,
		DailyMax:       999,
		DailyMin:       0,
		MonthlyAvg:     3333,
		MonthlyMax:     5555,
		MonthlyMin:     0,
		DataPointCount: 20,
		CreatedDate:    time.Now(),
	}}, nil
}

func (r *InMemoryStore) UpsertAggregatedRate(persistence.AggregatedRate) error {
	return nil
}
