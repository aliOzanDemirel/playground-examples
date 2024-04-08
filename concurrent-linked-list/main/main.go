package main

import (
	"concurrent-linked-list/list"
	"errors"
	"fmt"
	"github.com/go-chi/chi/v5"
	"github.com/go-chi/chi/v5/middleware"
	"log"
	"net/http"
	"runtime"
	"strconv"
)

var linkedList list.SinglyLinkedList

func main() {

	log.Printf("[startup] runtime: %s - %s - %s", runtime.Version(), runtime.GOOS, runtime.GOARCH)

	err := startServer()
	if err != nil && !errors.Is(err, http.ErrServerClosed) {
		log.Printf("[startup] error occurred -> %v", err)
	}
}

func startServer() error {

	linkedList = new(list.SinglyLinkedListWithNodeMutex)

	r := chi.NewRouter()
	r.Use(middleware.Logger)
	r.Use(middleware.Recoverer)

	// http://localhost:9999/snapshot
	r.Get("/snapshot", func(writer http.ResponseWriter, request *http.Request) {
		values := linkedList.Snapshot()
		resultMsg := fmt.Sprintf("snapshot operation -> current list node count: %d", linkedList.Size())
		for i, value := range values {
			resultMsg += fmt.Sprintf("\nnode #%d -> %d", i+1, value)
		}
		writer.Write([]byte(resultMsg))
	})

	// http://localhost:9999/pop
	r.Get("/pop", func(writer http.ResponseWriter, request *http.Request) {
		success := linkedList.Pop()
		resultMsg := fmt.Sprintf("pop operation -> current list node count: %d", linkedList.Size())
		if success {
			resultMsg += " -> removed last node from list"
		} else {
			resultMsg += " -> could not remove last element, list is already empty"
		}
		writer.Write([]byte(resultMsg))
	})

	// http://localhost:9999/push/888
	r.Get("/push/{newNodeValue}", func(writer http.ResponseWriter, request *http.Request) {

		paramNewValue := chi.URLParam(request, "newNodeValue")
		paramNewValueInt, err := strconv.ParseUint(paramNewValue, 10, 64)
		if err != nil {
			writer.WriteHeader(http.StatusBadRequest)
			errMsg := fmt.Sprintf("error occurred -> %v -> node value should be positive integer", err)
			log.Printf("[http-server] %s", errMsg)
			writer.Write([]byte(errMsg))
			return
		}

		linkedList.Push(paramNewValueInt)
		resultMsg := fmt.Sprintf("pushed node value %d to list -> current list node count: %d", paramNewValueInt, linkedList.Size())
		writer.Write([]byte(resultMsg))
	})

	// http://localhost:9999/insert/999/after/888
	r.Get("/insert/{newNodeValue}/after/{targetNodeValue}", func(writer http.ResponseWriter, request *http.Request) {

		paramTargetAfterValue := chi.URLParam(request, "targetNodeValue")
		paramTargetAfterValueInt, err := strconv.ParseUint(paramTargetAfterValue, 10, 64)
		if err != nil {
			writer.WriteHeader(http.StatusBadRequest)
			errMsg := fmt.Sprintf("error occurred -> %v -> target node value should be positive integer", err)
			log.Printf("[http-server] %s", errMsg)
			writer.Write([]byte(errMsg))
			return
		}

		paramNewValue := chi.URLParam(request, "newNodeValue")
		paramNewValueInt, err := strconv.ParseUint(paramNewValue, 10, 64)
		if err != nil {
			writer.WriteHeader(http.StatusBadRequest)
			errMsg := fmt.Sprintf("error occurred -> %v -> new node value should be positive integer", err)
			log.Printf("[http-server] %s", errMsg)
			writer.Write([]byte(errMsg))
			return
		}

		success := linkedList.InsertAfter(paramNewValueInt, paramTargetAfterValueInt)
		resultMsg := fmt.Sprintf("insert after operation -> current list node count: %d", linkedList.Size())
		if success {
			resultMsg += fmt.Sprintf(" -> 'node %d' is added right after 'node %d'", paramNewValueInt, paramTargetAfterValueInt)
		} else {
			resultMsg += fmt.Sprintf(" -> could not add 'node %d' because could not find 'node %d'", paramNewValueInt, paramTargetAfterValueInt)
		}
		writer.Write([]byte(resultMsg))
	})

	address := ":9999"
	log.Printf("[http-server] starting by listening on '%s'", address)
	return http.ListenAndServe(address, r)
}
