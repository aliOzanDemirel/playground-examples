package persistence

import (
	"context"
	"database/sql"
	"exchange-rate-store/pkg/logger"
	"fmt"
	"time"

	_ "github.com/go-sql-driver/mysql"
)

type Config struct {
	Host     string
	Port     uint
	DbName   string
	User     string
	Password string
}

type Persistence struct {
	conf *Config
	db   *sql.DB
}

func NewPersistence(config Config) (*Persistence, error) {

	if config.Host == "" {
		return nil, fmt.Errorf("missing Host")
	}
	if config.Port == 0 {
		return nil, fmt.Errorf("missing Port")
	}
	if config.DbName == "" {
		return nil, fmt.Errorf("missing DbName")
	}
	if config.User == "" {
		return nil, fmt.Errorf("missing User")
	}
	if config.Password == "" {
		return nil, fmt.Errorf("missing Password")
	}

	dataSource := fmt.Sprintf("%s:%s@tcp(%s:%d)/%s?parseTime=true",
		config.User, config.Password, config.Host, config.Port, config.DbName)
	db, err := sql.Open("mysql", dataSource)
	if err != nil {
		return nil, err
	}

	db.SetConnMaxLifetime(time.Minute * 10)
	db.SetMaxOpenConns(5)
	db.SetMaxIdleConns(5)

	err = db.Ping()
	if err != nil {
		return nil, fmt.Errorf("failed to ping '%s' -> %v", dataSource, err)
	}

	return &Persistence{
		conf: &config,
		db:   db,
	}, nil
}

func (p *Persistence) Close() error {
	return p.db.Close()
}

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

func (p *Persistence) Save(newRate Rate) error {

	err := p.insertNewCurrencyRate(newRate)
	if err != nil {
		return err
	}

	// NOTE: this might fail depending on frequency of new rate's date, since there is unique index on table
	err = p.updateAverages(newRate)
	if err != nil {
		return err
	}
	return nil
}

func (p *Persistence) insertNewCurrencyRate(newRate Rate) error {

	inTransaction, err := p.db.Begin()
	if err != nil {
		return fmt.Errorf("failed to begin transaction -> %v", err)
	}

	query := "INSERT INTO Rate (BaseCurrency, QuoteCurrency, exchangeRate, exchangeRateDate, bidPrice, askPrice) VALUES (?, ?, ?, ?, ?, ?)"
	insertResult, err := inTransaction.ExecContext(context.Background(), query,
		newRate.BaseCurrency,
		newRate.QuoteCurrency,
		newRate.ExchangeRate,
		newRate.ExchangeRateUtcTime,
		newRate.BidPrice,
		newRate.AskPrice,
	)
	if err != nil {
		err = fmt.Errorf("failed to insert new row -> %v", err)
		rollbackErr := inTransaction.Rollback()
		if rollbackErr != nil {
			err = fmt.Errorf("failed to rollback transaction -> %v -> caused by -> %v", rollbackErr, err)
		}
		return err
	}

	id, err := insertResult.LastInsertId()
	if err != nil {
		err = fmt.Errorf("failed to read last inserted row Id -> %v", err)
		rollbackErr := inTransaction.Rollback()
		if rollbackErr != nil {
			err = fmt.Errorf("failed to rollback transaction -> %v -> caused by -> %v", rollbackErr, err)
		}
		return err
	}

	err = inTransaction.Commit()
	if err != nil {
		return fmt.Errorf("failed to commit transaction -> %v", err)
	}

	logger.Debug(nil, "[persistence] saved new exchange rate [Id: %d] -> %v", id, newRate)
	return nil
}

func (p *Persistence) DeleteRatesOlderThanDate(date time.Time) (int64, error) {

	inTransaction, err := p.db.Begin()
	if err != nil {
		return 0, fmt.Errorf("failed to begin transaction -> %v", err)
	}

	query := "DELETE FROM Rate WHERE exchangeRateDate < DATE(?)"
	res, err := inTransaction.ExecContext(context.Background(), query, date)
	if err != nil {
		err = fmt.Errorf("failed to insert new row -> %v", err)
		rollbackErr := inTransaction.Rollback()
		if rollbackErr != nil {
			err = fmt.Errorf("failed to rollback transaction -> %v -> caused by -> %v", rollbackErr, err)
		}
		return 0, err
	}

	affectedRows, err := res.RowsAffected()
	if err != nil {
		rollbackErr := inTransaction.Rollback()
		if rollbackErr != nil {
			err = fmt.Errorf("failed to rollback transaction -> %v -> caused by -> %v", rollbackErr, err)
		}
		return 0, err
	}

	err = inTransaction.Commit()
	if err != nil {
		return 0, fmt.Errorf("failed to commit transaction -> %v", err)
	}

	logger.Debug(nil, "[persistence] deleted %d rates older than '%s'", affectedRows, date.Format(time.RFC3339))
	return affectedRows, nil
}
