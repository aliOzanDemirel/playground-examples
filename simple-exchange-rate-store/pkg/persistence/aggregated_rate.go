package persistence

import (
	"context"
	"database/sql"
	"exchange-rate-store/pkg/logger"
	"fmt"
	"time"

	_ "github.com/go-sql-driver/mysql"
)

type AggregatedRate struct {
	Id             uint64
	BaseCurrency   string
	QuoteCurrency  string
	AggregatedDate time.Time
	DailyAvg       float64
	DailyMax       float64
	DailyMin       float64
	MonthlyAvg     float64
	MonthlyMax     float64
	MonthlyMin     float64
	DataPointCount uint32
	CreatedDate    time.Time
}

func (p *Persistence) GetAggregatedRatesSinceRequestedDate(date time.Time, quoteCurrency string) ([]AggregatedRate, error) {

	// load aggregated rates of specific currency from the requested date until now
	query := "SELECT * FROM AggregatedRate WHERE aggregatedDate >= DATE(?) AND quoteCurrency = ?"
	rows, err := p.db.QueryContext(context.Background(), query, date, quoteCurrency)
	if err != nil {
		rateDate := date.Format(time.RFC3339)
		return nil, fmt.Errorf("failed to fetch aggregated rates from day '%s' -> %v", rateDate, err)
	}
	return readRowsToAggregatedRates(rows)
}

func (p *Persistence) getAggregatedRates(exchangeRateTimeUtc time.Time) ([]AggregatedRate, error) {

	// SELECT * FROM AggregatedRate WHERE aggregated < '2024-04-12 14:24:00' AND aggregated >= DATE_SUB('2024-04-12 14:24:00', INTERVAL 30 DAY)
	query := "SELECT * FROM AggregatedRate WHERE aggregatedDate <= ? AND aggregatedDate >= DATE_SUB(?, INTERVAL 30 DAY)"
	rows, err := p.db.QueryContext(context.Background(), query, exchangeRateTimeUtc, exchangeRateTimeUtc)
	if err != nil {
		rateDate := exchangeRateTimeUtc.Format(time.RFC3339)
		return nil, fmt.Errorf("failed to fetch aggregated rates of day '%s' -> %v", rateDate, err)
	}
	return readRowsToAggregatedRates(rows)
}

func readRowsToAggregatedRates(rows *sql.Rows) ([]AggregatedRate, error) {

	defer rows.Close()

	var aggRates []AggregatedRate
	for rows.Next() {
		var aggregated AggregatedRate

		// row columns are in table definition order
		err := rows.Scan(
			&aggregated.Id,
			&aggregated.BaseCurrency,
			&aggregated.QuoteCurrency,
			&aggregated.DataPointCount,
			&aggregated.AggregatedDate,
			&aggregated.DailyAvg,
			&aggregated.DailyMax,
			&aggregated.DailyMin,
			&aggregated.MonthlyAvg,
			&aggregated.MonthlyMax,
			&aggregated.MonthlyMin,
			&aggregated.CreatedDate,
		)
		if err != nil {
			return nil, fmt.Errorf("failed to read aggregated rate record to struct -> %v", err)
		}
		aggRates = append(aggRates, aggregated)
	}
	return aggRates, rows.Err()
}

func (p *Persistence) updateAverages(newRate Rate) error {

	aggRates, err := p.getAggregatedRates(newRate.ExchangeRateUtcTime)
	if err != nil {
		return err
	}
	logger.Debug(nil, "[persistence] loaded %d aggregated rate records for exchange time '%s'",
		len(aggRates), newRate.ExchangeRateUtcTime.Format(time.RFC3339))

	aggregated := getAggregatedRate(aggRates, newRate)
	return p.upsertAggregatedRate(aggregated)
}

func getAggregatedRate(previousAggregations []AggregatedRate, currentRate Rate) AggregatedRate {

	var aggRecordOfCurrent *AggregatedRate
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
		return AggregatedRate{
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

func (p *Persistence) upsertAggregatedRate(agg AggregatedRate) error {

	inTransaction, err := p.db.Begin()
	if err != nil {
		return fmt.Errorf("failed to begin transaction -> %v", err)
	}

	var resultErr error
	if agg.Id != 0 {
		query := "UPDATE AggregatedRate SET dataPointCount=?, dailyAverage=?, dailyMin=?, dailyMax=?, monthlyAverage=?, monthlyMin=?, monthlyMax=? WHERE id = ?"
		_, err = inTransaction.ExecContext(context.Background(), query,
			agg.DataPointCount,
			agg.DailyAvg,
			agg.DailyMin,
			agg.DailyMax,
			agg.MonthlyAvg,
			agg.MonthlyMin,
			agg.MonthlyMax,
			agg.Id,
		)
		if err != nil {
			resultErr = fmt.Errorf("failed to update row [Id: %d] -> %v", agg.Id, err)
		} else {
			logger.Debug(nil, "[persistence] updated aggregated rate [Id: %d] -> %v", agg.Id, agg)
		}
	} else {
		query := "INSERT INTO AggregatedRate (baseCurrency, quoteCurrency, dataPointCount, aggregatedDate, dailyAverage, dailyMin, dailyMax, monthlyAverage, monthlyMin, monthlyMax) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
		_, err = inTransaction.ExecContext(context.Background(), query,
			agg.BaseCurrency,
			agg.QuoteCurrency,
			agg.DataPointCount,
			agg.AggregatedDate,
			agg.DailyAvg,
			agg.DailyMin,
			agg.DailyMax,
			agg.MonthlyAvg,
			agg.MonthlyMin,
			agg.MonthlyMax,
		)
		if err != nil {
			resultErr = fmt.Errorf("failed to insert new row -> %v", err)
		} else {
			logger.Debug(nil, "[persistence] created new aggregated rate -> %v", agg)
		}
	}
	if resultErr != nil {
		rollbackErr := inTransaction.Rollback()
		if rollbackErr != nil {
			resultErr = fmt.Errorf("failed to rollback transaction -> %v -> caused by -> %v", rollbackErr, resultErr)
		}
		return resultErr
	}

	err = inTransaction.Commit()
	if err != nil {
		return fmt.Errorf("failed to commit transaction -> %v", err)
	}
	return nil
}
