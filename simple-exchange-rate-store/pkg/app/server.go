package app

import (
	"errors"
	"exchange-rate-store/pkg/logger"
	"exchange-rate-store/pkg/persistence"
	"fmt"
	"github.com/gin-gonic/gin"
	"net/http"
	"strings"
	"time"
)

func (b *ExchangeRateStore) validateServerConf() error {

	if b.conf.Port == 0 {
		return fmt.Errorf("missing config -> port")
	}
	if b.conf.BasicAuthUser == "" {
		return fmt.Errorf("missing config -> basic auth user")
	}
	if b.conf.BasicAuthPassword == "" {
		return fmt.Errorf("missing config -> basic auth password")
	}

	return nil
}

func (b *ExchangeRateStore) startServerAsync() (*http.Server, error) {

	err := b.validateServerConf()
	if err != nil {
		return nil, err
	}

	gin.SetMode("debug")
	gin.ForceConsoleColor()
	r := gin.New()
	r.Use(gin.Logger())
	r.Use(gin.Recovery())

	// http://localhost:9999/health
	r.GET("/health", b.handleHealth)

	// http://localhost:9999/metrics
	r.GET("/metrics", b.handleMetrics)

	basicAuthAccount := gin.Accounts{b.conf.BasicAuthUser: b.conf.BasicAuthPassword}
	authorized := r.Group("/authorized", gin.BasicAuth(basicAuthAccount))

	// http://localhost:9999/authorized/averages?dateFrom=2023-03-01&quoteCurrency=czk
	authorized.GET("/averages", b.handleGetCurrencyAverage)

	// http://localhost:9999/authorized/rates/btc/czk
	authorized.GET("/rates/:baseCurrency/:quoteCurrency", b.handleGetRate)

	// http://localhost:9999/clean?date=2022-05-05
	authorized.GET("/clean", b.handleCleanRates)

	address := fmt.Sprintf(":%d", b.conf.Port)
	server := &http.Server{
		Addr:    address,
		Handler: r,
	}

	logger.Info(nil, "[http-server] starting by listening on '%s'", address)
	go func() {
		err := server.ListenAndServe()
		if err != nil && !errors.Is(err, http.ErrServerClosed) {
			logger.Error(nil, err, "[http-server] stopped listening on '%s' -> %v", address, err)
		}
	}()

	return server, nil
}

func (b *ExchangeRateStore) handleHealth(c *gin.Context) {

	// NOTE: can ping database or other services to see if they are at least reachable
	c.Status(http.StatusOK)
}

func (b *ExchangeRateStore) handleMetrics(c *gin.Context) {

	// NOTE: can expose prometheus format timeseries
	c.IndentedJSON(http.StatusOK, gin.H{
		"pullJobSuccessCount":      b.metrics.pullJobSuccessCount.Load(),
		"pullJobFailCount":         b.metrics.pullJobFailCount.Load(),
		"rateRetrieveSuccessCount": b.rateRetriever.GetSuccessCount(),
		"rateRetrieveFailCount":    b.rateRetriever.GetFailCount(),
	})
}

func (b *ExchangeRateStore) handleGetRate(c *gin.Context) {

	user := c.MustGet(gin.AuthUserKey).(string)
	logger.Debug(map[string]interface{}{"user": user}, "handling request to protected endpoint")

	baseCurrency := strings.ToLower(c.Param("baseCurrency"))
	quoteCurrency := strings.ToLower(c.Param("quoteCurrency"))
	fetchTime := time.Now()
	rate, err := b.rateRetriever.FetchCurrentRate(c, baseCurrency, quoteCurrency)
	if err != nil {
		logger.Error(nil, err, "[http-server] failed to get rate '%s/%s'", baseCurrency, quoteCurrency)
		c.IndentedJSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	eRate := rate.RealtimeCurrencyExchangeRate
	c.IndentedJSON(http.StatusOK, gin.H{
		"clientFetchTime":   fetchTime.UTC().Format(time.RFC3339),
		"exchangeRateTime":  eRate.LastRefreshedUtcTime.Format(time.RFC3339), // this is from API, server's last modified time
		"exchangeRate":      eRate.ExchangeRate,
		"bidPrice":          eRate.BidPrice,
		"askPrice":          eRate.AskPrice,
		"baseCurrencyCode":  eRate.FromCurrencyCode,
		"quoteCurrencyCode": eRate.ToCurrencyCode,
	})
}

func (b *ExchangeRateStore) handleGetCurrencyAverage(c *gin.Context) {

	user := c.MustGet(gin.AuthUserKey).(string)
	logger.Debug(map[string]interface{}{"user": user}, "handling request to protected endpoint")

	// use current date by default
	dateFrom := time.Now()

	dateFromStr := c.Query("dateFrom")
	if dateFromStr != "" {
		parsed, err := time.Parse(time.DateOnly, dateFromStr)
		if err != nil {
			err = fmt.Errorf("query param 'dateFrom' error -> failed to parse '%s' -> %v", dateFromStr, err)
			logger.Error(nil, err, "[http-server] bad request")
			c.IndentedJSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}
		dateFrom = parsed
	}

	quoteCurrency := strings.ToLower(c.Query("quoteCurrency"))
	if quoteCurrency == "" {
		err := errors.New("query param 'quoteCurrency' error -> parameter is mandatory")
		logger.Error(nil, err, "[http-server] bad request")
		c.IndentedJSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	averages, err := b.ratePersistence.GetAggregatedRatesSinceRequestedDate(dateFrom, quoteCurrency)
	if err != nil {
		logger.Error(nil, err, "[http-server] failed to get averages -> currency: '%s', date: '%s'", quoteCurrency, dateFrom.Format(time.DateOnly))
		c.IndentedJSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	if len(averages) == 0 {
		averages = []persistence.AggregatedRate{}
	}
	c.IndentedJSON(http.StatusOK, averages)
}

func (b *ExchangeRateStore) handleCleanRates(c *gin.Context) {

	user := c.MustGet(gin.AuthUserKey).(string)
	logger.Debug(map[string]interface{}{"user": user}, "handling request to protected endpoint")

	// use 1 year ago by default
	dateOneYearAgo := time.Now().AddDate(-1, 0, 0)

	dateStr := c.Query("date")
	if dateStr != "" {
		parsed, err := time.Parse(time.DateOnly, dateStr)
		if err != nil {
			err = fmt.Errorf("query param 'date' error -> failed to parse '%s' -> %v", dateStr, err)
			logger.Error(nil, err, "[http-server] bad request")
			c.IndentedJSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}
		dateOneYearAgo = parsed
	}

	count, err := b.ratePersistence.DeleteRatesOlderThanDate(dateOneYearAgo)
	if err != nil {
		logger.Error(nil, err, "[http-server] failed to delete rates before '%s'", dateOneYearAgo.Format(time.DateOnly))
		c.IndentedJSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.IndentedJSON(http.StatusOK, gin.H{"deletedCount": count})
}
