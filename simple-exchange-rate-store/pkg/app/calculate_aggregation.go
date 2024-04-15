package app

import "exchange-rate-store/pkg/persistence"

func calculateAggregatedRateForNewRate(previousAggregations []persistence.AggregatedRate, currentRate persistence.Rate) persistence.AggregatedRate {

	var aggRecordOfCurrent *persistence.AggregatedRate
	sum, monthlyMax, monthlyMin := currentRate.ExchangeRate, currentRate.ExchangeRate, currentRate.ExchangeRate
	for i := range previousAggregations {
		agg := previousAggregations[i]

		sum += agg.DailyAvg
		if agg.DailyMax > monthlyMax {
			monthlyMax = agg.DailyMax
		}
		if agg.DailyMin < monthlyMin {
			monthlyMin = agg.DailyMin
		}

		// should not be necessary but make sure the currencies match
		if agg.BaseCurrency == currentRate.BaseCurrency &&
			agg.QuoteCurrency == currentRate.QuoteCurrency {

			// make sure to select aggregated record for the exact date
			aggYear, aggMonth, aggDay := agg.AggregatedDate.Date()
			curYear, curMonth, curDay := currentRate.ExchangeRateUtcTime.Date()
			if aggYear == curYear &&
				aggMonth == curMonth &&
				aggDay == curDay {

				aggRecordOfCurrent = &agg
			}
		}
	}

	// if there is no previous aggregation record at new rate's date, return new one using only current rate
	if aggRecordOfCurrent == nil {
		return persistence.AggregatedRate{
			BaseCurrency:   currentRate.BaseCurrency,
			QuoteCurrency:  currentRate.QuoteCurrency,
			AggregatedDate: currentRate.ExchangeRateUtcTime,
			DailyAvg:       currentRate.ExchangeRate,
			DailyMax:       currentRate.ExchangeRate,
			DailyMin:       currentRate.ExchangeRate,
			MonthlyAvg:     currentRate.ExchangeRate,
			MonthlyMax:     currentRate.ExchangeRate,
			MonthlyMin:     currentRate.ExchangeRate,
			DataPointCount: 1,
		}
	}

	// total sum is counted with this many rates
	monthlySumDataPointCount := float64(len(previousAggregations) + 1)
	monthlyAvg := sum / monthlySumDataPointCount
	aggRecordOfCurrent.MonthlyAvg = monthlyAvg
	aggRecordOfCurrent.MonthlyMax = monthlyMax
	aggRecordOfCurrent.MonthlyMin = monthlyMin

	aggRecordSum := aggRecordOfCurrent.DailyAvg * float64(aggRecordOfCurrent.DataPointCount)
	newSum := aggRecordSum + currentRate.ExchangeRate
	newTotalCount := aggRecordOfCurrent.DataPointCount + 1

	// count currently added new rate (+1) by adding to average
	aggRecordOfCurrent.DailyAvg = newSum / float64(newTotalCount)
	aggRecordOfCurrent.DataPointCount = newTotalCount

	if currentRate.ExchangeRate > aggRecordOfCurrent.DailyMax {
		aggRecordOfCurrent.DailyMax = currentRate.ExchangeRate
	}

	if currentRate.ExchangeRate < aggRecordOfCurrent.DailyMin {
		aggRecordOfCurrent.DailyMin = currentRate.ExchangeRate
	}

	return *aggRecordOfCurrent
}
