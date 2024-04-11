package alphavantage

import (
	"context"
	"encoding/json"
	"exchange-rate-store/pkg/common"
	"exchange-rate-store/pkg/httpclient"
	"fmt"
	"io"
	"net/http"
	"time"
)

type CurrencyExchangeRateResponse struct {
	RealtimeCurrencyExchangeRate CurrencyExchangeRate `json:"Realtime Currency Exchange Rate"`
}

type CurrencyExchangeRate struct {
	FromCurrencyCode     string    `json:"1. From_Currency Code"`
	FromCurrencyName     string    `json:"2. From_Currency Name"`
	ToCurrencyCode       string    `json:"3. To_Currency Code"`
	ToCurrencyName       string    `json:"4. To_Currency Name"`
	ExchangeRate         float64   `json:"5. Exchange Rate,string"`
	LastRefreshed        string    `json:"6. Last Refreshed"`
	TimeZone             string    `json:"7. Time Zone"`
	BidPrice             float64   `json:"8. Bid Price,string"`
	AskPrice             float64   `json:"9. Ask Price,string"`
	LastRefreshedUtcTime time.Time `json:"-"`
}

func (c *CurrencyExchangeRate) setUtcTime() error {

	location, err := time.LoadLocation(c.TimeZone)
	if err != nil {
		return err
	}
	lastRefreshedWithZone, err := time.ParseInLocation(time.DateTime, c.LastRefreshed, location)
	if err != nil {
		return err
	}

	c.LastRefreshedUtcTime = lastRefreshedWithZone.UTC()
	return nil
}

func (a *Api) FetchCurrentRate(ctx context.Context, fromCurrency, toCurrency string) (CurrencyExchangeRateResponse, error) {

	response, err := a.getExchangeRateWithRetry(ctx, fromCurrency, toCurrency)
	if err != nil {
		a.failedRequestCount.Add(1)
		return CurrencyExchangeRateResponse{}, err
	}

	a.successfulRequestCount.Add(1)
	return response, nil
}

func (a *Api) getExchangeRateWithRetry(ctx context.Context, fromCurrency, toCurrency string) (CurrencyExchangeRateResponse, error) {

	const retryAttempt = 2
	const retrySleepBase = 2 * time.Second

	retryCtx := httpclient.RetryContext{
		BaseTraceId:       common.UniqueId(),
		RetryCount:        retryAttempt,
		RetryBackoffSleep: retrySleepBase,
		Retriable: func(traceId string) (*http.Response, error) {
			return a.doExchangeRateGetRequest(ctx, traceId, fromCurrency, toCurrency)
		},
	}

	httpResponse, err := retryCtx.DoWithRetry()
	if err != nil {
		return CurrencyExchangeRateResponse{}, err
	}

	var response CurrencyExchangeRateResponse
	err = json.NewDecoder(httpResponse.Body).Decode(&response)
	if err != nil && err != io.EOF {
		return CurrencyExchangeRateResponse{}, fmt.Errorf("failed to deserialize response to json -> %w", err)
	} else {
		isEmptyResponse := response.RealtimeCurrencyExchangeRate.ExchangeRate == 0.0 || response.RealtimeCurrencyExchangeRate.LastRefreshed == ""
		if isEmptyResponse {
			return CurrencyExchangeRateResponse{}, fmt.Errorf("received successful http status but empty response body")
		}
	}

	err = response.RealtimeCurrencyExchangeRate.setUtcTime()
	if err != nil {
		return CurrencyExchangeRateResponse{}, fmt.Errorf("failed to set UTC timestamp -> %w", err)
	}

	return response, nil
}

func (a *Api) doExchangeRateGetRequest(ctx context.Context, traceId, fromCurrency, toCurrency string) (*http.Response, error) {

	req, err := http.NewRequestWithContext(ctx, "GET", a.exchangeRateQueryEndpoint, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to prepare http request: %w", err)
	}

	params := req.URL.Query()
	params.Set("from_currency", fromCurrency)
	params.Set("to_currency", toCurrency)
	params.Set("apikey", a.conf.ApiKey)

	req.URL.RawQuery = params.Encode()
	req.Header.Set(httpclient.KeyHeaderTraceId, traceId)
	req.Header.Set(httpclient.KeyHeaderUserAgent, a.userAgent)
	req.Header.Set("Accept", "application/json")
	req.Header.Set("Content-Type", "application/json")

	return a.client.Do(req)
}
