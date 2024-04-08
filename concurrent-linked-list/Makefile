.PHONY: clean
clean:
	go clean -x -i -testcache

.PHONY: test
test:
	go test -cover -coverprofile=coverage.out ./list/...
	go tool cover -html=coverage.out -o coverage.html

.PHONY: run
run:
	go build -o ./concurrent-linked-list ./main/main.go
	./concurrent-linked-list

.PHONY: test_server
test_server:
	curl http://localhost:9999/push/10
	curl http://localhost:9999/push/20
	curl http://localhost:9999/push/30
	curl http://localhost:9999/insert/100/after/20
	curl http://localhost:9999/pop
	curl http://localhost:9999/insert/200/after/10
	curl http://localhost:9999/snapshot
