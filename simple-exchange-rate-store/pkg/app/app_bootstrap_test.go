package app

import (
	"testing"
	"time"
)

func TestNewExchangeRateStore(t *testing.T) {

	conf := Config{
		Dev: Dev{
			MockCryptoApi: true,
			MockDataStore: true,
		},
	}

	t.Run("zero value interval is accepted", func(t *testing.T) {
		_, err := NewExchangeRateStore(conf)
		if err != nil {
			t.Fatalf("could not init exchange rate store -> %v", err)
		}
	})

	validDurations := []time.Duration{0, 2 * time.Second, 5 * time.Minute}
	for _, tValue := range validDurations {
		t.Run("valid interval", func(t *testing.T) {
			c := conf
			c.RatePullInterval = tValue
			_, err := NewExchangeRateStore(c)
			if err != nil {
				t.Errorf("valid interval '%v' should have passed -> %v", tValue, err)
			}
		})
	}

	invalidDurations := []time.Duration{1 * time.Hour, 5*time.Minute + 1*time.Millisecond}
	for _, tValue := range invalidDurations {
		t.Run("invalid interval", func(t *testing.T) {
			c := conf
			c.RatePullInterval = tValue
			_, err := NewExchangeRateStore(c)
			if err == nil {
				t.Errorf("invalid value '%v' should have failed", tValue)
			}
		})
	}
}
