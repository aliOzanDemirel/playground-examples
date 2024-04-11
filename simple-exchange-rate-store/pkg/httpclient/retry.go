package httpclient

import (
	"exchange-rate-store/pkg/common"
	"exchange-rate-store/pkg/logger"
	"fmt"
	"net/http"
	"time"
)

type RetriableFunc func(traceId string) (*http.Response, error)

type RetryContext struct {
	BaseTraceId       string // shared trace id used by all possible retries
	RetryCount        uint   // how many retries will be attempted, excluding initial try
	RetryBackoffSleep time.Duration
	Retriable         RetriableFunc
}

func (r *RetryContext) DoWithRetry() (*http.Response, error) {

	// total attempt = 1 initial try + number of retries
	totalAttempt := 1 + r.RetryCount
	sleep := r.RetryBackoffSleep

	attempt := uint(0)
	for {
		traceSpecificAttempt := fmt.Sprintf("%s/%d", r.BaseTraceId, attempt)

		// request failed with error, do not attempt a retry
		response, err := r.Retriable(traceSpecificAttempt)
		if err != nil {
			return nil, err
		}

		// count the attempt executed just before
		attempt++

		// request had no error, retry request with backoff if can still retry
		canStillAttempt := attempt < totalAttempt
		shouldRetry := canStillAttempt && canRetryResponse(response.StatusCode)

		if shouldRetry {

			// determine new sleep time, old sleep + how much backoff to apply for this try
			sleep = common.SleepDurationWithBackoff(sleep)
			remainingAttempt := totalAttempt - attempt - 1

			fields := map[string]interface{}{KeyHeaderTraceId: r.BaseTraceId}
			logger.Info(fields, "[client-retry] will retry after sleep: %v [attempted: %d, remaining: %d]",
				sleep, attempt, remainingAttempt)

			// sleep with backoff before retry attempt
			time.Sleep(sleep)
		} else {

			// got response successfully, return it
			return response, nil
		}

		// request had no error and can no longer retry, break retry loop and return current response
		retryErr := fmt.Errorf("failed after %d attempts", attempt)
		if err != nil {
			retryErr = fmt.Errorf("%v -> %v", retryErr, err)
		}
		return nil, retryErr
	}
}

func canRetryResponse(responseStatus int) bool {

	// [429]
	retryIfThrottled := responseStatus == http.StatusTooManyRequests

	// [409]
	retryIfHadConflict := responseStatus == http.StatusConflict

	// [500 to 505] excluding [501]
	retryIfServerError := responseStatus != http.StatusNotImplemented && responseStatus >= http.StatusInternalServerError && responseStatus <= http.StatusHTTPVersionNotSupported

	return retryIfThrottled || retryIfHadConflict || retryIfServerError
}
