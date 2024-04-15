package app

import (
	"encoding/json"
	"exchange-rate-store/pkg/persistence"
	"net/http"
	"testing"
)

func TestExchangeRateStore_checkHealthEndpoint(t *testing.T) {

	testApp := defaultTestApp()
	tc := runTestApp(t, testApp)
	defer tc.app.Shutdown()

	request, err := http.NewRequest("GET", "http://localhost:3333/health", nil)
	if err != nil {
		t.Fatalf("failed test -> %v", err)
	}
	resp, err := http.DefaultClient.Do(request)
	if err != nil {
		t.Fatalf("failed test -> %v", err)
	}
	if resp.StatusCode != 200 {
		t.Fatalf("unexpected response status, actual: %d", resp.StatusCode)
	}
}

func TestExchangeRateStore_rates_unauthorized(t *testing.T) {

	testApp := defaultTestApp()
	tc := runTestApp(t, testApp)
	defer tc.app.Shutdown()

	request, err := http.NewRequest("GET", "http://localhost:3333/authorized/rates/BTC/CZK", nil)
	if err != nil {
		t.Fatalf("failed test -> %v", err)
	}
	resp, err := http.DefaultClient.Do(request)
	if err != nil {
		t.Fatalf("failed test -> %v", err)
	}
	if resp.StatusCode != 401 {
		t.Fatalf("unexpected response status, epxected: 401, actual: %d", resp.StatusCode)
	}
}

func TestExchangeRateStore_rates_authorized(t *testing.T) {

	testApp := defaultTestApp()
	tc := runTestApp(t, testApp)
	defer tc.app.Shutdown()

	request, err := http.NewRequest("GET", "http://localhost:3333/authorized/rates/BTC/CZK", nil)
	if err != nil {
		t.Fatalf("failed test -> %v", err)
	}
	request.SetBasicAuth(testApp.conf.BasicAuthUser, testApp.conf.BasicAuthPassword)

	resp, err := http.DefaultClient.Do(request)
	if err != nil {
		t.Fatalf("failed test -> %v", err)
	}
	if resp.StatusCode != 200 {
		t.Fatalf("unexpected response status, expected: 200, actual: %d", resp.StatusCode)
	}

	var body map[string]interface{}
	err = json.NewDecoder(resp.Body).Decode(&body)
	if err != nil {
		t.Fatalf("failed test -> %v", err)
	}

	if body["clientFetchTime"] == nil {
		t.Errorf("did not find clientFetchTime")
	}
	if body["exchangeRateTime"] == nil {
		t.Errorf("did not find exchangeRateTime")
	}
	if body["exchangeRate"] == nil {
		t.Errorf("did not find exchangeRate")
	}
	if body["bidPrice"] == nil {
		t.Errorf("did not find bidPrice")
	}
	if body["askPrice"] == nil {
		t.Errorf("did not find askPrice")
	}
	if body["baseCurrencyCode"] != "btc" {
		t.Errorf("unexpected baseCurrencyCode, expected: 'btc', actual: %s", body["baseCurrencyCode"])
	}
	if body["quoteCurrencyCode"] != "czk" {
		t.Errorf("did not find quoteCurrencyCode, expected: 'czk', actual: %s", body["quoteCurrencyCode"])
	}
}

func TestExchangeRateStore_averages_badRequest(t *testing.T) {

	testApp := defaultTestApp()
	tc := runTestApp(t, testApp)
	defer tc.app.Shutdown()

	// missing query param quoteCurrency
	request, err := http.NewRequest("GET", "http://localhost:3333/authorized/averages", nil)
	if err != nil {
		t.Fatalf("failed test -> %v", err)
	}
	request.SetBasicAuth(testApp.conf.BasicAuthUser, testApp.conf.BasicAuthPassword)

	resp, err := http.DefaultClient.Do(request)
	if err != nil {
		t.Fatalf("failed test -> %v", err)
	}
	if resp.StatusCode != 400 {
		t.Fatalf("unexpected response status, expected: 400, actual: %d", resp.StatusCode)
	}
}

func TestExchangeRateStore_averages_successful(t *testing.T) {

	testApp := defaultTestApp()
	tc := runTestApp(t, testApp)
	defer tc.app.Shutdown()

	request, err := http.NewRequest("GET", "http://localhost:3333/authorized/averages?quoteCurrency=CZK", nil)
	if err != nil {
		t.Fatalf("failed test -> %v", err)
	}
	request.SetBasicAuth(testApp.conf.BasicAuthUser, testApp.conf.BasicAuthPassword)

	resp, err := http.DefaultClient.Do(request)
	if err != nil {
		t.Fatalf("failed test -> %v", err)
	}
	if resp.StatusCode != 200 {
		t.Fatalf("unexpected response status, expected: 200, actual: %d", resp.StatusCode)
	}

	var body []persistence.AggregatedRate
	err = json.NewDecoder(resp.Body).Decode(&body)
	if err != nil {
		t.Fatalf("failed test -> %v", err)
	}
	if len(body) != 0 {
		t.Fatalf("should not have found any AggregatedRate")
	}
}

func TestExchangeRateStore_clean_successful(t *testing.T) {

	testApp := defaultTestApp()
	tc := runTestApp(t, testApp)
	defer tc.app.Shutdown()

	request, err := http.NewRequest("GET", "http://localhost:3333/authorized/clean", nil)
	if err != nil {
		t.Fatalf("failed test -> %v", err)
	}
	request.SetBasicAuth(testApp.conf.BasicAuthUser, testApp.conf.BasicAuthPassword)

	resp, err := http.DefaultClient.Do(request)
	if err != nil {
		t.Fatalf("failed test -> %v", err)
	}
	if resp.StatusCode != 200 {
		t.Fatalf("unexpected response status, expected: 200, actual: %d", resp.StatusCode)
	}

	var body map[string]int
	err = json.NewDecoder(resp.Body).Decode(&body)
	if err != nil {
		t.Fatalf("failed test -> %v", err)
	}
	if body["count"] != 0 {
		t.Fatalf("unexpected response 'count', expected: 0, actual: %d", body["count"])
	}
}
