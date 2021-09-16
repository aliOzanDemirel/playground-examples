#### InfluxDB / Grafana

* After running grafana container with docker-compose, access to UI via `http://localhost:3000`, username and password are `root`
* Configure InfluxDB data source:
    * HTTP URL: `http://influxdb:8086/`
    * Default database: `grafana_exposed_metrics`
    * Username and password are `root`
* Can import example graphs from `resources/grafana_json_model.json`

![dashboard_1]

---

![dashboard_2]

[dashboard_1]: resources/readme/dashboard_1.jpg

[dashboard_2]: resources/readme/dashboard_2.jpg
