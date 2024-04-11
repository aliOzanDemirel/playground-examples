package common

import (
	random "crypto/rand"
	"encoding/hex"
	"exchange-rate-store/pkg/logger"
	"fmt"
	"math/rand"
	"net/url"
	"time"
)

func SleepDurationWithBackoff(sleepBase time.Duration) time.Duration {
	randomBackoff := time.Duration(rand.Int63n(int64(sleepBase)))
	return sleepBase + randomBackoff/2
}

func UniqueId() string {
	id, err := RandomId()
	if err != nil {
		logger.Info(nil, "[unique-id] failed to generate but ignoring error -> %v", err)
		return ""
	}
	return id
}

func RandomId() (string, error) {
	b := make([]byte, 16)
	_, err := random.Read(b)
	if err != nil {
		return "", err
	}
	return hex.EncodeToString(b), nil
}

// ValidateHttpUrl checks if string is absolute http URL
func ValidateHttpUrl(check string, onlyDomain bool) error {

	uri, err := url.ParseRequestURI(check)
	if err != nil {
		return err
	}

	isHttp := uri.Scheme == "http" || uri.Scheme == "https"
	if !isHttp {
		return fmt.Errorf("URL can only be http: %s", check)
	}

	if uri.Host == "" {
		return fmt.Errorf("URL should have host: %s", check)
	}

	if onlyDomain && uri.Path != "" {
		return fmt.Errorf("URL should not have path: %s", check)
	}
	return err
}
