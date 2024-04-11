package alphavantage

import (
	"context"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"
)

func fakeAlphaVantage(t *testing.T, shouldFail bool, fakeResponse CurrencyExchangeRateResponse) *httptest.Server {
	mux := http.NewServeMux()
	mux.HandleFunc("/query", func(w http.ResponseWriter, r *http.Request) {

		queryParams := r.URL.Query()
		expectedParams := []string{"function", "apikey", "from_currency", "to_currency"}
		for _, param := range expectedParams {
			if queryParams.Get(param) == "" {
				t.Errorf("[fake-alpha-vantage] missing query param '%s'", param)
			}
		}

		if shouldFail {
			w.WriteHeader(http.StatusInternalServerError)
			return
		}

		w.Header().Set("Content-Type", "application/json")
		err := json.NewEncoder(w).Encode(&fakeResponse)
		if err != nil {
			t.Errorf("[fake-alpha-vantage] unexpected error in test server -> %v", err)
		}
	})
	return httptest.NewServer(mux)
}

func TestNewAlphaVantageApi(t *testing.T) {

	t.Run("valid conf", func(t *testing.T) {
		api, err := NewAlphaVantageApi(Config{
			ApiKey: "test", BaseUrl: "https://test.host",
		})
		if err != nil {
			t.Errorf("failed to create alpha vantage api -> %v", err)
		}
		if api.client == nil {
			t.Errorf("failed to find non-nil alpha vantage api client")
		}
		if api.exchangeRateQueryEndpoint != "https://test.host/query?function=CURRENCY_EXCHANGE_RATE" {
			t.Errorf("unexpected query url '%s'", api.exchangeRateQueryEndpoint)
		}
	})

	t.Run("missing api key", func(t *testing.T) {
		_, err := NewAlphaVantageApi(Config{
			ApiKey: "", BaseUrl: "https://test.host",
		})
		if err == nil {
			t.Errorf("expected to find error")
		}
	})
	t.Run("missing base url", func(t *testing.T) {
		_, err := NewAlphaVantageApi(Config{
			ApiKey: "test", BaseUrl: "",
		})
		if err == nil {
			t.Errorf("expected to find error")
		}
	})

	t.Run("invalid base url", func(t *testing.T) {
		_, err := NewAlphaVantageApi(Config{
			ApiKey: "test", BaseUrl: "invalid.com",
		})
		if err == nil {
			t.Errorf("expected to find error")
		}
	})
}

func TestAlphaVantageQuery_success(t *testing.T) {

	resp := CurrencyExchangeRateResponse{
		RealtimeCurrencyExchangeRate: CurrencyExchangeRate{
			FromCurrencyCode: "test_from_code",
			ToCurrencyCode:   "test_to_code",
			ExchangeRate:     11.0123456789,
			LastRefreshed:    "2024-04-13 12:49:33",
			TimeZone:         "UTC",
			BidPrice:         10.00000009,
			AskPrice:         12.00000009,
		},
	}
	server := fakeAlphaVantage(t, false, resp)
	defer server.Close()

	api, err := NewAlphaVantageApi(Config{
		ApiKey: "test", BaseUrl: server.URL,
	})
	if err != nil {
		t.Fatalf("failed to create alpha vantage api -> %v", err)
	}

	response, err := api.FetchCurrentRate(context.Background(), "from", "to")
	if err != nil {
		t.Fatalf("failed to get exchange rate -> %v", err)
	}

	c := response.RealtimeCurrencyExchangeRate
	if c.FromCurrencyCode != "test_from_code" {
		t.Errorf("unexpected FromCurrencyCode: %s", c.FromCurrencyCode)
	}
	if c.ToCurrencyCode != "test_to_code" {
		t.Errorf("unexpected ToCurrencyCode: %s", c.ToCurrencyCode)
	}
	if c.ExchangeRate != 11.0123456789 {
		t.Errorf("unexpected ExchangeRate: %f", c.ExchangeRate)
	}
	if c.BidPrice != 10.00000009 {
		t.Errorf("unexpected BidPrice: %f", c.BidPrice)
	}
	if c.AskPrice != 12.00000009 {
		t.Errorf("unexpected AskPrice: %f", c.AskPrice)
	}
	if c.LastRefreshed != "2024-04-13 12:49:33" {
		t.Errorf("unexpected LastRefreshed: %s", c.LastRefreshed)
	}
	if c.TimeZone != "UTC" {
		t.Errorf("unexpected TimeZone: %s", c.TimeZone)
	}

	lastRefreshedUtc := c.LastRefreshedUtcTime.Format(time.RFC3339)
	if lastRefreshedUtc != "2024-04-13T12:49:33Z" {
		t.Errorf("unexpected LastRefreshedUtcTime: %s", lastRefreshedUtc)
	}
}

func TestAlphaVantageQuery_fail(t *testing.T) {

	server := fakeAlphaVantage(t, true, CurrencyExchangeRateResponse{})
	defer server.Close()

	api, err := NewAlphaVantageApi(Config{
		ApiKey: "test", BaseUrl: server.URL,
	})
	if err != nil {
		t.Fatalf("failed to create alpha vantage api -> %v", err)
	}

	_, err = api.FetchCurrentRate(context.Background(), "from", "to")
	if err == nil {
		t.Fatalf("expected to find error")
	}
}

func TestCurrencyExchangeRate_setUtcTime(t *testing.T) {

	prague := &CurrencyExchangeRate{
		LastRefreshed: "2024-04-13 12:49:33",
		TimeZone:      "Europe/Prague",
	}
	err := prague.setUtcTime()
	if err != nil {
		t.Fatalf("unexpected error -> %v", err)
	}

	istanbul := &CurrencyExchangeRate{
		LastRefreshed: "2024-04-13 12:49:33",
		TimeZone:      "Europe/Istanbul",
	}
	err = istanbul.setUtcTime()
	if err != nil {
		t.Fatalf("unexpected error -> %v", err)
	}

	if prague.LastRefreshedUtcTime.UnixMilli() <= istanbul.LastRefreshedUtcTime.UnixMilli() {
		t.Logf("expected to find prague time to be greater than istanbul after UTC conversion"+
			"\nprague: %s istanbul: %s", prague.LastRefreshedUtcTime.Format(time.RFC3339), istanbul.LastRefreshedUtcTime.Format(time.RFC3339))
	}
}
