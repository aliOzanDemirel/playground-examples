package httpclient_test

import (
	"exchange-rate-store/pkg/httpclient"
	"net/http"
	"net/http/httptest"
	"testing"
)

func setupTestServer(t *testing.T, responseStatus int) *httptest.Server {

	mux := http.NewServeMux()
	mux.HandleFunc("/testSomething", func(w http.ResponseWriter, r *http.Request) {
		t.Logf("[fake-server] received to /testSomething")
		w.WriteHeader(responseStatus)
	})
	return httptest.NewServer(mux)
}

func TestDoRequest_successResponse(t *testing.T) {

	server := setupTestServer(t, http.StatusOK)
	defer server.Close()

	client := &httpclient.LoggingClient{
		IsDumpAlwaysEnabled: func() bool { return true },
		Delegate:            http.DefaultClient,
	}
	endpoint := server.URL + "/testSomething"
	request, err := http.NewRequest("GET", endpoint, nil)
	if err != nil {
		t.Fatalf("failed test -> %v", err)
	}
	response, err := client.Do(request)
	if err != nil {
		t.Fatalf("failed test -> %v", err)
	}

	if response.StatusCode != http.StatusOK {
		t.Fatalf("unexpected StatusCode: %d", response.StatusCode)
	}
}

func TestDoRequest_errorResponse(t *testing.T) {

	server := setupTestServer(t, http.StatusInternalServerError)
	defer server.Close()

	client := &httpclient.LoggingClient{
		IsDumpAlwaysEnabled: func() bool { return true },
		Delegate:            http.DefaultClient,
	}
	endpoint := server.URL + "/testSomething"
	request, err := http.NewRequest("GET", endpoint, nil)
	if err != nil {
		t.Fatalf("failed test -> %v", err)
	}
	response, err := client.Do(request)
	if err != nil {
		t.Fatalf("failed test -> %v", err)
	}

	if response.StatusCode != http.StatusInternalServerError {
		t.Fatalf("unexpected StatusCode: %d", response.StatusCode)
	}
}
