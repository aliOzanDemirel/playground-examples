## Spring/JFR/InfluxDB/Grafana Monitoring Example

This is a simple demonstration project that integrates below pieces:

* Java/Spring service that was created as assignment project (2019) with a simple API to support requirements.
* JFR agent that is to be attached to java runtime of service.
* Timeseries database to record metrics, InfluxDB.
* Metric visualization tool, Grafana.

Adding a new review for a clothing (example with request body is in postman export) will fire up a JFR event that will then be captured by agent that
is attached to service process. Event will be enriched with some data to be tagged and then will be pushed to InfluxDB. Grafana running on container
can be configured to visualize metric data points recorded in InfluxDB.

* Building both the service and agent requires **java 16** as well as running gradle wrapper.
* Run only clothing-service with JFR agent: `gradlew runWithAgent`
* Run every piece together for full integration:
    1. `gradlew clean buildAndAssembleAll`
    2. `docker-compose up -d --build`

![birdview][birdview]

#### JFR Agent

* A simple java agent that will run alongside with clothing-service to enable&capture JFR events.
* Some JDK events are logged, only the custom application event to is written to timeseries database (InfluxDB) for demonstration purposes.
* For the sake of simplicity, there is no buffering events in memory, periodically dumping to disk to be collected by some telegraf agent, or any
  other more complex flows, agent writes directly to InfluxDB to keep it simple.

#### Grafana

* Access to UI via `http://localhost:3000`, username and password are `root`
* Configure InfluxDB data source:
    * HTTP URL: `http://influxdb:8086/`
    * Default database: `grafana_exposed_metrics`
    * Username and password are `root`
* Can import example graphs from `resources/grafana_json_model.json`

![dashboard_1]

---

![dashboard_2]

#### Simple Clothing Service

* There is example Postman collection to test API. For a basic API docs: `http://localhost:8080/swagger-ui.html`
* Shirts come in sizes small, medium, and large.
* Shirts come in the color options of red, black, blue, green, and white.
* Each article of clothing should also include a description of the clothing (any text is fine with a maximum of 2000 characters).
* Each article of clothing should also have a rating associated with it that any ordinary shopper can provide (the rating should come in the form of a
  text review no longer than 2000 characters and a star rating between 1 and 5). Both are required fields.
    * Once an article of clothing’s review has reached 5 reviews and the average review is 2 stars or below, reviewing that article of clothing should
      be prohibited and no future reviews should be accepted.
    * Once an article of clothing’s review has reached 5 reviews and the average review is 4 stars or above, the article of clothing should be labeled
      as “HOT”.
    * An article of clothing that was previously “HOT” can still become prohibited for future reviews if it drops below the minimum average star
      rating threshold (at least 5 reviews and 2 stars or below average).
* Articles of clothing should be searchable by their text description (prefix match), color, brand, size, “HOT” status, and average review stars
  rounded up to the nearest whole number (e.g. an article with an average review of 2.1 would be found when searching for items with 3 stars).
* The API does not need to allow creation of new brands or articles of clothing.

[birdview]: resources/jfr_to_grafana.jpg "Birdview"

[dashboard_1]: resources/dashboard_1.jpg

[dashboard_2]: resources/dashboard_2.jpg
