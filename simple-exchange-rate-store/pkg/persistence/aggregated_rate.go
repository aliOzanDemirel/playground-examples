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

func (p *Persistence) GetAggregatedRates(exchangeRateTimeUtc time.Time) ([]AggregatedRate, error) {

	query := "SELECT * FROM AggregatedRate WHERE aggregatedDate <= ? AND aggregatedDate >= DATE_SUB(?, INTERVAL 30 DAY)"
	rows, err := p.db.QueryContext(context.Background(), query, exchangeRateTimeUtc, exchangeRateTimeUtc)
	if err != nil {
		rateDate := exchangeRateTimeUtc.Format(time.RFC3339)
		return nil, fmt.Errorf("failed to fetch aggregated rates of day '%s' -> %v", rateDate, err)
	}

	aggRates, err := readRowsToAggregatedRates(rows)
	if err != nil {
		return nil, err
	}

	logger.Debug(nil, "[persistence] loaded %d aggregated rate records for exchange time '%s'",
		len(aggRates), exchangeRateTimeUtc.Format(time.RFC3339))
	return aggRates, nil
}

func readRowsToAggregatedRates(rows *sql.Rows) ([]AggregatedRate, error) {

	defer func() {
		err := rows.Close()
		if err != nil {
			err = fmt.Errorf("failed to close rows cursor -> %v", err)
		}
	}()

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

func (p *Persistence) UpsertAggregatedRate(agg AggregatedRate) error {

	tx, err := p.db.Begin()
	if err != nil {
		return fmt.Errorf("failed to begin transaction -> %v", err)
	}
	defer func() {
		switch err {
		case nil:
			err = tx.Commit()
		default:
			rollbackErr := tx.Rollback()
			if rollbackErr != nil {
				err = fmt.Errorf("failed to rollback transaction -> %v -> caused by -> %v", rollbackErr, err)
			}
		}
	}()

	if agg.Id != 0 {

		query := "UPDATE AggregatedRate SET dataPointCount=?, dailyAverage=?, dailyMin=?, dailyMax=?, monthlyAverage=?, monthlyMin=?, monthlyMax=? WHERE id=?"
		_, err = tx.ExecContext(context.Background(), query,
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
			return fmt.Errorf("failed to update row [Id: %d] -> %v", agg.Id, err)
		} else {
			logger.Debug(nil, "[persistence] updated aggregated rate [Id: %d] -> %v", agg.Id, agg)
		}
	} else {

		query := "INSERT INTO AggregatedRate (baseCurrency, quoteCurrency, dataPointCount, aggregatedDate, dailyAverage, dailyMin, dailyMax, monthlyAverage, monthlyMin, monthlyMax) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
		_, err = tx.ExecContext(context.Background(), query,
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
			return fmt.Errorf("failed to insert new row -> %v", err)
		} else {
			logger.Debug(nil, "[persistence] created new aggregated rate -> %v", agg)
		}
	}
	return nil
}
