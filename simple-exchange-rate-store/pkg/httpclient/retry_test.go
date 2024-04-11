package httpclient

import (
	"fmt"
	"net/http"
	"strings"
	"testing"
	"time"
)

func TestRetry(t *testing.T) {

	t.Run("errored without retry", func(t *testing.T) {

		errMsg := "some error"
		retryCtx := RetryContext{
			RetryCount:        2,
			RetryBackoffSleep: 10 * time.Millisecond,
			Retriable: func(traceId string) (*http.Response, error) {
				return nil, fmt.Errorf(errMsg)
			},
		}
		response, err := retryCtx.DoWithRetry()
		if err == nil || err.Error() != errMsg {
			t.Errorf("expected error message: '%s'", errMsg)
		}
		if response != nil {
			t.Errorf("expected nil response")
		}
	})

	expectNoRetryStatuses := []int{200, 300, 400, 401, 403, 404, 501}
	for _, status := range expectNoRetryStatuses {

		t.Run("expect no retry", func(t *testing.T) {

			retryCtx := RetryContext{
				RetryCount:        2,
				RetryBackoffSleep: 10 * time.Millisecond,
				Retriable: func(traceId string) (*http.Response, error) {
					resp := http.Response{StatusCode: status}
					return &resp, nil
				},
			}
			response, err := retryCtx.DoWithRetry()
			if err != nil {
				t.Errorf("unexpected error: %v for status %d", err, status)
			}
			if response == nil {
				t.Errorf("expected non-nil response for status %d", status)
			}
		})
	}

	expectRetryStatuses := []int{409, 429, 500, 502, 503, 504, 505}
	for _, status := range expectRetryStatuses {
		t.Run("expect retry and failure for error responses", func(t *testing.T) {

			retryCtx := RetryContext{
				RetryCount:        4,
				RetryBackoffSleep: 10 * time.Millisecond,
				Retriable: func(traceId string) (*http.Response, error) {
					resp := http.Response{StatusCode: status}
					return &resp, nil
				},
			}
			response, err := retryCtx.DoWithRetry()
			errorStartsWith := "failed after "
			if err == nil {
				t.Errorf("expected error for status %d", status)
			} else if !strings.HasPrefix(err.Error(), errorStartsWith) {
				t.Errorf("expected error message '%v' to start with: '%s' for status %d", err, errorStartsWith, status)
			}
			if response != nil {
				t.Errorf("expected nil response for status %d", status)
			}
		})
	}
}
