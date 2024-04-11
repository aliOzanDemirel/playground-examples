package httpclient

import (
	"errors"
	"exchange-rate-store/pkg/logger"
	"fmt"
	"net/http"
	"net/http/httputil"
	"time"
)

const (
	LogFieldRequestDuration = "durationMillis"
	LogFieldStatus          = "responseStatus"
	LogFieldUrl             = "requestUrl"
	KeyHeaderUserAgent      = "User-Agent"
	KeyHeaderTraceId        = "X-Request-Trace-Id"
)

type LoggingClient struct {
	IsDumpAlwaysEnabled func() bool
	Delegate            *http.Client
}

func (c *LoggingClient) Do(req *http.Request) (*http.Response, error) {

	fields := logFieldsFromRequest(req)
	logger.Debug(fields, "[logging-client] starting '%s' request", req.Method)

	startTime := time.Now()
	resp, err := c.Delegate.Do(req)
	if err != nil {
		return nil, err
	}
	duration := time.Since(startTime)
	enrichLogFieldsWithResponse(fields, resp, duration)

	isErrorResponse := resp.StatusCode < 200 || resp.StatusCode >= 300
	if isErrorResponse {

		dumpStr := dumpRequestAndResponse(req, resp)
		err := errors.New("http request got error response, check log message for request and response dumps")
		logger.Error(fields, err, "[logging-client] %s", dumpStr)

	} else {

		dumpEnabled := c.IsDumpAlwaysEnabled != nil && c.IsDumpAlwaysEnabled()
		if dumpEnabled {
			dumpStr := dumpRequestAndResponse(req, resp)
			logger.Info(fields, "[logging-client] http request got successful response, dumping request and response: %s", dumpStr)
		} else {
			logger.Debug(fields, "[logging-client] http request got successful response")
		}
	}
	return resp, nil
}

func logFieldsFromRequest(req *http.Request) map[string]interface{} {
	return map[string]interface{}{
		LogFieldUrl:        req.URL.String(),
		KeyHeaderTraceId:   req.Header.Get(KeyHeaderTraceId),
		KeyHeaderUserAgent: req.Header.Get(KeyHeaderUserAgent),
	}
}

func enrichLogFieldsWithResponse(fields map[string]interface{}, httpResp *http.Response, duration time.Duration) map[string]interface{} {

	fields[LogFieldStatus] = httpResp.Status
	fields[LogFieldRequestDuration] = duration.Milliseconds()

	opcRequestId := httpResp.Header.Get(KeyHeaderTraceId)
	if opcRequestId != "" {
		fields[KeyHeaderTraceId] = opcRequestId
	}
	return fields
}

func dumpRequestAndResponse(httpReq *http.Request, httpResp *http.Response) string {

	var dumpReq, dumpResp string

	// this function is used after making the request, so restore request body before dump
	if httpReq.GetBody != nil {
		httpReq.Body, _ = httpReq.GetBody()
	}
	clonedReq := httpReq.Clone(httpReq.Context())
	clonedReq.Header.Del("Authorization")
	requestDump, err := httputil.DumpRequest(clonedReq, true)
	if err != nil {
		dumpReq = "failed to dump http request"
	} else {
		dumpReq = string(requestDump)
	}

	respDump, err := httputil.DumpResponse(httpResp, true)
	if err != nil {
		dumpResp = "failed to dump http response"
	} else {
		dumpResp = string(respDump)
	}

	return fmt.Sprintf("--- dump request start ---"+
		"\n%s"+
		"\n--- dump request end and response start ---"+
		"\n%s"+
		"\n--- dump response end ---", dumpReq, dumpResp)
}
