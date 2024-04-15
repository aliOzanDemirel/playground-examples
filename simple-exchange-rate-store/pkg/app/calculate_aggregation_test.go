package app

import (
	"exchange-rate-store/pkg/persistence"
	"fmt"
	"testing"
	"time"
)

func TestGetAggregatedRate(t *testing.T) {

	t.Run("no previous aggregations, new day", func(t *testing.T) {

		var prevAggs []persistence.AggregatedRate
		rate := 44.139845
		aggDate := time.Date(2024, 4, 13, 10, 10, 10, 0, time.UTC)
		current := persistence.Rate{
			BaseCurrency:        "TBA",
			QuoteCurrency:       "TQU",
			ExchangeRate:        rate,
			ExchangeRateUtcTime: aggDate,
		}

		agg := calculateAggregatedRateForNewRate(prevAggs, current)
		if agg.Id != 0 {
			t.Errorf("unexpected Id, should be new record, expected: 0, actual: %d", agg.Id)
		}
		if agg.AggregatedDate.Format(time.DateOnly) != aggDate.Format(time.DateOnly) {
			t.Errorf("unexpected AggregatedDate, expected: %s, actual: %s", aggDate.Format(time.DateOnly), agg.AggregatedDate.Format(time.DateOnly))
		}
		if agg.BaseCurrency != "TBA" {
			t.Errorf("unexpected BaseCurrency, expected: TBA, actual: %s", agg.BaseCurrency)
		}
		if agg.QuoteCurrency != "TQU" {
			t.Errorf("unexpected QuoteCurrency, expected: TQU, actual: %s", agg.QuoteCurrency)
		}
		if agg.DataPointCount != 1 {
			t.Errorf("unexpected DataPointCount, expected: 1, actual: %d", agg.DataPointCount)
		}
		if agg.DailyAvg != rate {
			t.Errorf("unexpected DailyAvg, expected: %f, actual: %f", rate, agg.DailyAvg)
		}
		if agg.DailyMin != rate {
			t.Errorf("unexpected DailyMin, expected: %f, actual: %f", rate, agg.DailyMin)
		}
		if agg.DailyMax != rate {
			t.Errorf("unexpected DailyMax, expected: %f, actual: %f", rate, agg.DailyMax)
		}
		if agg.MonthlyAvg != rate {
			t.Errorf("unexpected MonthlyAvg, expected: %f, actual: %f", rate, agg.MonthlyAvg)
		}
		if agg.MonthlyMin != rate {
			t.Errorf("unexpected MonthlyMin, expected: %f, actual: %f", rate, agg.MonthlyMin)
		}
		if agg.MonthlyMax != rate {
			t.Errorf("unexpected MonthlyMax, expected: %f, actual: %f", rate, agg.MonthlyMax)
		}
	})

	t.Run("there are previous aggregations but not for current rate's day, new day", func(t *testing.T) {

		day0 := time.Date(2024, 4, 11, 0, 0, 0, 0, time.UTC)
		day1 := day0.Add(time.Hour * 24)
		prevAggs := []persistence.AggregatedRate{{
			Id:             888,
			BaseCurrency:   "B",
			QuoteCurrency:  "Q",
			AggregatedDate: day0,
			DailyAvg:       3.3,
			DailyMax:       4.4,
			DailyMin:       2.2,
			MonthlyAvg:     12.66,
			MonthlyMax:     22.66,
			MonthlyMin:     10.66,
			DataPointCount: 5,
		}, {
			Id:             999,
			BaseCurrency:   "B",
			QuoteCurrency:  "Q",
			AggregatedDate: day1,
			DailyAvg:       2,
			DailyMax:       3,
			DailyMin:       1,
			MonthlyAvg:     48.1234,
			MonthlyMax:     88.1234,
			MonthlyMin:     28.1234,
			DataPointCount: 10,
		}}

		day2 := day1.Add(time.Hour * 30)
		rate := 99.8998
		current := persistence.Rate{
			BaseCurrency:        "B",
			QuoteCurrency:       "Q",
			ExchangeRate:        rate,
			ExchangeRateUtcTime: day2,
		}

		agg := calculateAggregatedRateForNewRate(prevAggs, current)
		if agg.Id != 0 {
			t.Errorf("unexpected Id, should be new record, expected: 0, actual: %d", agg.Id)
		}
		if agg.AggregatedDate.Format(time.DateOnly) != day2.Format(time.DateOnly) {
			t.Errorf("unexpected AggregatedDate, expected: %s, actual: %s", day2.Format(time.DateOnly), agg.AggregatedDate.Format(time.DateOnly))
		}
		if agg.BaseCurrency != "B" {
			t.Errorf("unexpected BaseCurrency, expected: B, actual: %s", agg.BaseCurrency)
		}
		if agg.QuoteCurrency != "Q" {
			t.Errorf("unexpected QuoteCurrency, expected: Q, actual: %s", agg.QuoteCurrency)
		}
		if agg.DataPointCount != 1 {
			t.Errorf("unexpected DataPointCount, expected: 1, actual: %d", agg.DataPointCount)
		}
		if agg.DailyAvg != rate {
			t.Errorf("unexpected DailyAvg, expected: %f, actual: %f", rate, agg.DailyAvg)
		}
		if agg.DailyMin != rate {
			t.Errorf("unexpected DailyMin, expected: %f, actual: %f", rate, agg.DailyMin)
		}
		if agg.DailyMax != rate {
			t.Errorf("unexpected DailyMax, expected: %f, actual: %f", rate, agg.DailyMax)
		}
		if agg.MonthlyAvg != rate {
			t.Errorf("unexpected MonthlyAvg, expected: %f, actual: %f", rate, agg.MonthlyAvg)
		}
		if agg.MonthlyMin != rate {
			t.Errorf("unexpected MonthlyMin, expected: %f, actual: %f", rate, agg.MonthlyMin)
		}
		if agg.MonthlyMax != rate {
			t.Errorf("unexpected MonthlyMax, expected: %f, actual: %f", rate, agg.MonthlyMax)
		}
	})

	t.Run("there is previous aggregation but not matching current rate's currencies, new day", func(t *testing.T) {

		day0 := time.Date(2024, 4, 11, 0, 0, 0, 0, time.UTC)
		prevAggs := []persistence.AggregatedRate{{
			Id:             888,
			BaseCurrency:   "B",
			QuoteCurrency:  "Q",
			AggregatedDate: day0,
		}}

		current := persistence.Rate{
			BaseCurrency:        "TBA",
			QuoteCurrency:       "TQU",
			ExchangeRate:        10.0,
			ExchangeRateUtcTime: day0,
		}

		agg := calculateAggregatedRateForNewRate(prevAggs, current)
		if agg.Id != 0 {
			t.Errorf("unexpected Id, should be new record, expected: 0, actual: %d", agg.Id)
		}
		if agg.BaseCurrency != "TBA" {
			t.Errorf("unexpected BaseCurrency, expected: TBA, actual: %s", agg.BaseCurrency)
		}
		if agg.QuoteCurrency != "TQU" {
			t.Errorf("unexpected QuoteCurrency, expected: TQU, actual: %s", agg.QuoteCurrency)
		}
	})

	t.Run("there are many previous aggregations including current day, update agg record for current day", func(t *testing.T) {

		day0 := time.Date(2024, 4, 11, 0, 0, 0, 0, time.UTC)
		day1 := day0.Add(time.Hour * 24)
		prevAggs := []persistence.AggregatedRate{{
			Id:             888,
			BaseCurrency:   "B",
			QuoteCurrency:  "Q",
			AggregatedDate: day0,
			DailyAvg:       2.0,
			DailyMax:       99.0,
			DailyMin:       25.0,
			MonthlyAvg:     12.66,
			MonthlyMax:     22.66,
			MonthlyMin:     10.66,
			DataPointCount: 9,
		}, {
			Id:             999,
			BaseCurrency:   "B",
			QuoteCurrency:  "Q",
			AggregatedDate: day1,
			DailyAvg:       18,
			DailyMax:       999,
			DailyMin:       101,
			MonthlyAvg:     48.1234,
			MonthlyMax:     88.1234,
			MonthlyMin:     28.1234,
			DataPointCount: 67,
		}}

		rate := 100.0
		current := persistence.Rate{
			BaseCurrency:        "B",
			QuoteCurrency:       "Q",
			ExchangeRate:        rate,
			ExchangeRateUtcTime: day1,
		}

		agg := calculateAggregatedRateForNewRate(prevAggs, current)
		if agg.Id != 999 {
			t.Errorf("unexpected Id, should find existing record, expected: 0, actual: %d", agg.Id)
		}
		if agg.AggregatedDate.Format(time.DateOnly) != day1.Format(time.DateOnly) {
			t.Errorf("unexpected AggregatedDate, expected: %s, actual: %s", day1.Format(time.DateOnly), agg.AggregatedDate.Format(time.DateOnly))
		}
		if agg.BaseCurrency != "B" {
			t.Errorf("unexpected BaseCurrency, expected: B, actual: %s", agg.BaseCurrency)
		}
		if agg.QuoteCurrency != "Q" {
			t.Errorf("unexpected QuoteCurrency, expected: Q, actual: %s", agg.QuoteCurrency)
		}
		if agg.DataPointCount != 68 {
			t.Errorf("unexpected DataPointCount, expected: 68, actual: %d", agg.DataPointCount)
		}

		actualDailyAvg := fmt.Sprintf("%.10f", agg.DailyAvg)
		expectedDailyAvg := "19.2058823529"
		if actualDailyAvg != expectedDailyAvg {
			t.Errorf("unexpected DailyAvg, expected: %s, actual: %s", expectedDailyAvg, actualDailyAvg)
		}

		expectedDailyMin := 100.0
		if agg.DailyMin != expectedDailyMin {
			t.Errorf("unexpected DailyMin, kept current, expected: %f, actual: %f", expectedDailyMin, agg.DailyMin)
		}

		expectedDailyMax := 999.0
		if agg.DailyMax != expectedDailyMax {
			t.Errorf("unexpected DailyMax, used previous, expected: %f, actual: %f", expectedDailyMax, agg.DailyMax)
		}

		expectedMonthlyAvg := 40.0
		if agg.MonthlyAvg != expectedMonthlyAvg {
			t.Errorf("unexpected MonthlyAvg, expected: %f, actual: %f", expectedMonthlyAvg, agg.MonthlyAvg)
		}

		expectedMonthlyMin := 25.0
		if agg.MonthlyMin != expectedMonthlyMin {
			t.Errorf("unexpected MonthlyMin, used previous, expected: %f, actual: %f", expectedMonthlyMin, agg.MonthlyMin)
		}

		expectedMonthlyMax := 999.0
		if agg.MonthlyMax != expectedMonthlyMax {
			t.Errorf("unexpected MonthlyMax, used previous, expected: %f, actual: %f", expectedMonthlyMax, agg.MonthlyMax)
		}
	})
}
