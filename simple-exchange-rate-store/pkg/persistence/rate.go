package persistence

import (
	"context"
	"exchange-rate-store/pkg/logger"
	"fmt"
	"time"

	_ "github.com/go-sql-driver/mysql"
)

type Rate struct {
	Id                  uint64
	BaseCurrency        string
	QuoteCurrency       string
	ExchangeRate        float64
	ExchangeRateUtcTime time.Time
	BidPrice            float64
	AskPrice            float64
	CreatedDate         time.Time
}

func (p *Persistence) InsertNewRate(newRate Rate) error {

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

	query := "INSERT INTO Rate (BaseCurrency, QuoteCurrency, exchangeRate, exchangeRateDate, bidPrice, askPrice) VALUES (?, ?, ?, ?, ?, ?)"
	insertResult, err := tx.ExecContext(context.Background(), query,
		newRate.BaseCurrency,
		newRate.QuoteCurrency,
		newRate.ExchangeRate,
		newRate.ExchangeRateUtcTime,
		newRate.BidPrice,
		newRate.AskPrice,
	)
	if err != nil {
		return fmt.Errorf("failed to insert new Rate -> %v", err)
	}

	id, err := insertResult.LastInsertId()
	if err != nil {
		return fmt.Errorf("failed to read last inserted Rate Id -> %v", err)
	}

	logger.Debug(nil, "[persistence] inserted new rate [Id: %d] -> %v", id, newRate)
	return nil
}

func (p *Persistence) DeleteRatesOlderThanDate(date time.Time) (int64, error) {

	tx, err := p.db.Begin()
	if err != nil {
		return 0, fmt.Errorf("failed to begin transaction -> %v", err)
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

	query := "DELETE FROM Rate WHERE exchangeRateDate < DATE(?)"
	res, err := tx.ExecContext(context.Background(), query, date)
	if err != nil {
		err = fmt.Errorf("failed to insert new row -> %v", err)
		rollbackErr := tx.Rollback()
		if rollbackErr != nil {
			err = fmt.Errorf("failed to rollback transaction -> %v -> caused by -> %v", rollbackErr, err)
		}
		return 0, err
	}

	affectedRows, err := res.RowsAffected()
	if err != nil {
		rollbackErr := tx.Rollback()
		if rollbackErr != nil {
			err = fmt.Errorf("failed to rollback transaction -> %v -> caused by -> %v", rollbackErr, err)
		}
		return 0, err
	}

	logger.Debug(nil, "[persistence] deleted %d rates older than '%s'", affectedRows, date.Format(time.RFC3339))
	return affectedRows, nil
}
