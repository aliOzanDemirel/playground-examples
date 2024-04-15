LOCAL_USER  := $(shell whoami)
VERSION		:= 1-$(LOCAL_USER)

DOCKER_IMAGE              := exchange-rate-store:$(VERSION)
DOCKER_BUILD_SKIP_GO_TEST := 1

HELM_CHART_MYSQL	 := simple-mysql-$(VERSION).tgz
HELM_RELEASE_MYSQL	 := mysql-$(VERSION)
HELM_NAMESPACE_MYSQL := mysql
MYSQL_HOST           := $(HELM_RELEASE_MYSQL).$(HELM_NAMESPACE_MYSQL).svc.cluster.local

HELM_CHART_APP	   := simple-exchange-rate-store-$(VERSION).tgz
HELM_RELEASE_APP   := exchange-rate-store-$(VERSION)
HELM_NAMESPACE_APP := exchange-rate-store

LOCAL_MYSQL_VOLUME_DIR            := $(shell pwd)/mysqldata
LOCAL_MYSQL_USE_PERSISTENT_VOLUME := true

# load env file and export all for child shell processes
LOCAL_DEV_CONFIG := ./dev/local.env
include $(LOCAL_DEV_CONFIG)
export $(shell sed '/^\#/d; s/=.*//' $(LOCAL_DEV_CONFIG))

####################

.PHONY: clean
clean:
	go clean -x -i -testcache
	rm -rf ./bin/* || true
	rm -rf $(LOCAL_MYSQL_VOLUME_DIR)/* || true

.PHONY: test
test:
	go test -cover -coverprofile=coverage.out ./pkg/...
	go tool cover -html=coverage.out -o coverage.html

.PHONY: build_binary
build_binary:
	go build -o ./bin/exchange-rate-store ./main/main.go

.PHONY: run
run: build_binary
	./bin/exchange-rate-store

####################

# use --no-cache to ignore docker build cache
.PHONY: image_build
image_build:
	docker rmi $(DOCKER_IMAGE) || true
	docker build --platform=linux/x86_64 --build-arg="SKIP_GO_TEST=$(DOCKER_BUILD_SKIP_GO_TEST)" . -t $(DOCKER_IMAGE)

.PHONY: image_verify
image_verify:
	docker run --platform=linux/x86_64 -d --name test --entrypoint sleep $(DOCKER_IMAGE) infinity
	docker exec -it test sh -c "pwd; whoami; env | grep APP_; ls -la /opt/app/exchange-rate-store"
	docker stop -t 1 test | xargs docker rm

####################

.PHONY: helm_template_app
helm_template_app:
	helm template ./chart/app --namespace $(HELM_NAMESPACE_APP) --name-template $(HELM_RELEASE_APP);

.PHONY: helm_package_app
helm_package_app:
	helm package ./chart/app --version "$(VERSION)" --destination ./bin

.PHONY: helm_release_app
helm_release_app: helm_package_app
	helm upgrade $(HELM_RELEASE_APP) ./bin/$(HELM_CHART_APP) --install --create-namespace --namespace $(HELM_NAMESPACE_APP) \
	 --set containerImage=$(DOCKER_IMAGE) \
	 --set databaseResourceName=$(HELM_RELEASE_MYSQL) \
	 --set envVars.APP_DB_HOST=$(MYSQL_HOST);

.PHONY: helm_delete_app
helm_delete_app:
	helm uninstall $(HELM_RELEASE_APP) --namespace $(HELM_NAMESPACE_APP)

####################

# outputs final manifest to console, use --debug to debug
.PHONY: helm_template_mysql
helm_template_mysql:
	helm template ./chart/mysql --namespace $(HELM_NAMESPACE_MYSQL) --name-template $(HELM_RELEASE_MYSQL);

.PHONY: helm_package_mysql
helm_package_mysql:
	helm package ./chart/mysql --version "$(VERSION)" --destination ./bin

.PHONY: helm_release_mysql
helm_release_mysql: helm_package_mysql
	if [[ "true" == "$(LOCAL_MYSQL_USE_PERSISTENT_VOLUME)" ]]; then \
  	  	mkdir -p $(LOCAL_MYSQL_VOLUME_DIR) && chmod 777 $(LOCAL_MYSQL_VOLUME_DIR); \
	fi;
	helm upgrade $(HELM_RELEASE_MYSQL) ./bin/$(HELM_CHART_MYSQL) --install --create-namespace --namespace $(HELM_NAMESPACE_MYSQL) \
	 --set storage.hostPath=$(LOCAL_MYSQL_VOLUME_DIR) \
	 --set storage.usePersistentVolume=$(LOCAL_MYSQL_USE_PERSISTENT_VOLUME);

.PHONY: helm_delete_mysql
helm_delete_mysql:
	helm uninstall $(HELM_RELEASE_MYSQL) --namespace $(HELM_NAMESPACE_MYSQL)

.PHONY: recreate_volume_dir_mysql
recreate_volume_dir_mysql:
	rm -rf ./mysqldata/* || true
	mkdir -p mysqldata && chmod 777 mysqldata

####################

.PHONY: kubeforward_mysql
kubeforward_mysql:
	kubectl port-forward svc/$(HELM_RELEASE_MYSQL) 3306:3306 -n $(HELM_NAMESPACE_MYSQL)

.PHONY: kubeforward_app
kubeforward_app:
	kubectl port-forward svc/$(HELM_RELEASE_APP) 9999:9999 -n $(HELM_NAMESPACE_APP)
