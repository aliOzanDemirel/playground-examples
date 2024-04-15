## Exchange Rate Store

- Create and deploy a microservice where a client application will be able to:
    - retrieve the current price of Bitcoin (BTC) in both EUR and CZK;
    - retrieve locally calculated daily and monthly averages for the price mentioned above, obtained from locally stored
      data.
    - the data storage cadence should be a minimum of 1 request every 5 minutes.
    - the microservice may be left running for years, but only a retention of 12 months is necessary.
    - a credential is required to leverage the microservice.
    - The output of any request should:
        - include both prices per 1 BTC, their currency, the client request’s time, and the server data’s time (if
          available).
        - be JSON formatted.
- Containerize the microservice.
- Prepare a deployment into Kubernetes:
    - the deployment should be reproducible.
    - the microservice should auto-start and be reachable via appropriate calls (for example: curl, postman, etc..).
    - the microservice and any related additional resources should be deployed into appropriate custom namespaces.
    - use Helm for this deployment.

### How to run

- Developed with go 1.21.5 in osx/arm, targeting linux/amd container runtime, using rancher desktop's k8s cluster
- Can run in a k8s cluster or can run standalone binary
    - `dev/local.env` is kept as example config file used by make commands to run compiled artifact
    - or make sure to have required configuration setup in environment variables
    - `APP_DEV_MOCK_CRYPTO_API` -> for fast development of other parts without needing actual currency rate server
    - `APP_DEV_MOCK_DATA_STORE` -> for fast development of other parts without needing actual database
- `Makefile` is used for common commands, can be used to execute some ready commands
    - `make image_build` -> build binary and container image without needing golang installed
    - `make helm_release_mysql` -> release mysql with a volume dir in project folder
    - `make helm_release_app` -> release service configured against mysql released with make, node port is enabled by
      default
- You might need to adjust some configuration in makefile or default helm values
- Can use postman config `dev/exchange_rate_store_api.postman_collection` for request examples

---

- This example service configures and runs:
    - http server with multiple endpoints
        - endpoints prefixed with `/authorized` are protected with basic auth
            - there is a static user credential, configured with env vars
            - use `admin` for username and password when checking these endpoints
        - /health -> used to configure kubernetes probes
        - /authorized/rates/btc/czk -> query crypto API and return result in JSON
        - /authorized/averages?quoteCurrency=czk -> query database to get the average per currency for current day (or
          query param 'dateFrom')
        - /authorized/clean -> delete ingested exchange rates before last 12 months (or query param 'date')
    - jobs that pull data from crypto API and store it
        - runs periodically, cannot configure more than 5 minute interval
        - exchange rate data is ingested into mysql, storing raw records in a table
        - calculates average, minimum and maximum rates and stores them in a separate table
        - aggregations are calculated per day and previous 30 days (monthly)
        - so raw exchange rate records (can have 5 minute precision) are downsampled to 1 day
- Service can run in a container, can be deployed to kubernetes cluster with helm `chart/app`
    - should pass mandatory config `containerImage` when making helm release
    - should configure a valid `envVars.APP_DB_HOST`
    - database can be deployed with a separate helm chart (`chart/mysql`)
        - can configure release with a persistent volume or empty dir
            - `storage.usePersistentVolume` and `storage.hostPath`
        - can create nodeport service for development convenience
            - `database.nodePort.enable`
        - there is sql script packaged with helm chart, this will be run when database is first created
        - make sure to delete existing contents of host path dir if you use persistent volume and want to re-install
- Using a free tier of some crypto currency rate API to demo the usage
    - very easy to get a key but it only allows 25 request per day
    - https://www.alphavantage.co/support/#api-key
    - https://www.alphavantage.co/query?function=CURRENCY_EXCHANGE_RATE&from_currency=BTC&to_currency=CNY&apikey=demo

### Additional notes, possible improvements

- Secrets
    - checked in to repository since this is example/demo project
    - ideally we would use some platform level vault like solution
    - or follow some other standard to hide secrets (client side encryption or whatever exists)
- Database
    - simply initializing when db is created but ideally we would use some migration tool (like liquibase etc.)
    - ignoring possible concurrency issues, no optimistic locking or stricter transaction etc.
- Tests
    - can have more test coverage with blackbox tests
    - mock db driver or use proper in-memory db to run tests against
    - or can use testcontainers for module wide blackbox tests
- Observability
    - could use proper log fields where needed
    - use trace id for every flow, both jobs and requests
    - can emit prometheus samples for counted metrics
    - can deploy prometheus/grafana with helm, provisioning grafana with some default dashboards
- Others
    - ignoring possible floating number conversion errors like losing precision
    - http server does not have tls listener for simplicity, can configure self signed cert
    - can support arbitrary currency pair with more configuration logic
    - readiness/liveness probes can be added with better health check logic
    - deployed a single replica for simplicity, can add standby replica or run multiple instances with work queue