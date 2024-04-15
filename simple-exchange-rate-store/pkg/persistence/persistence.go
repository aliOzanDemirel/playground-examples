package persistence

import (
	"database/sql"
	"fmt"
	"time"

	_ "github.com/go-sql-driver/mysql"
)

type DbConfig struct {
	Host     string
	Port     uint
	Name     string
	User     string
	Password string
}

type Persistence struct {
	conf *DbConfig
	db   *sql.DB
}

const mysqlDriverName = "mysql"

func NewPersistence(config DbConfig) (*Persistence, error) {

	if config.Host == "" {
		return nil, fmt.Errorf("missing Host")
	}
	if config.Port == 0 {
		return nil, fmt.Errorf("missing Port")
	}
	if config.Name == "" {
		return nil, fmt.Errorf("missing Name")
	}
	if config.User == "" {
		return nil, fmt.Errorf("missing User")
	}
	if config.Password == "" {
		return nil, fmt.Errorf("missing Password")
	}

	dataSource := fmt.Sprintf("%s:%s@tcp(%s:%d)/%s?parseTime=true",
		config.User, config.Password, config.Host, config.Port, config.Name)
	db, err := sql.Open(mysqlDriverName, dataSource)
	if err != nil {
		return nil, err
	}

	db.SetConnMaxLifetime(time.Minute * 10)
	db.SetMaxOpenConns(5)
	db.SetMaxIdleConns(5)

	// NOTE: can move this out of here to healthcheck
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
