## Pub/Sub Messaging Example

These transaction-processor and bond-issuer services are simple assignment projects (created at 2018 and 2019
respectively), that are now integrated through **a direct exchange with single queue** to demonstrate a basic example of
async communication between two remote processes with [RabbitMQ] (https://www.rabbitmq.com/tutorials/amqp-concepts.html)
as middleware.

When a new bond is created, bond service will send an AMQP message to the configured queue. Then transaction service (
after subscribing to exchange) will receive this message and create new transaction for newly issued bond. This flow can
be tested by importing api-postman.json to trigger API, create new bond (POST /bonds) and get transaction statistics (
GET /statistics) to watch the flow.

> Both java applications require jdk 11, transaction-processor also requires maven to be installed on the host.

- Build both services to use docker-compose to create image and run containers, docker-compose will also startup
  rabbitmq broker and **wait until this broker is ready to be connected** before starting service containers.
    1. simple-bond-issuer: `gradlew clean bootJar`
    2. simple-transaction-processor: `maven clean install`
    3. `docker-compose up -d --build`
- API of services can be explored via `/swagger-ui.html` for both services.
- You can run rabbitmq for local development with below:
    - `docker run -d -p 5672:5672 -p 15672:15672 --hostname rabbitmq --name rmq rabbitmq:3.8-management-alpine`
    - Login to rabbit management portal with guest as user and password: `http://localhost:15672/`

### Logic in simple-transaction-processor

* Save concurrent transaction requests in memory without using database.
* Statistics of incoming transactions in last 60 seconds can be fetched.
* All transactions can be purged from memory, while accepting new transactions.

### Logic in simple-bond-issuer

* Client can apply for a bond by providing his personal data, term and amount.
* Default bond coupon (interest rate) is 5% per year and minimal term is 5 years.
* The application follows regulations that have to be validated, a bond can’t be sold if:
  * The application is made between 10:00 PM and 06:00 AM with an amount higher than 1000
  * Reached max number of sold bonds (e.g. 5) per day from a single IP address
* The bond is sold if there are no violations of the regulatory requirements. The newly sold bond reference is returned
  to the client, otherwise the client receives a rejection message.
* Client should be able to adjust the term of his bond. Each term extension results in coupon decreased by 10% of its
  value. Shortening the term does not affect the coupon.
* Client should be able to retrieve whole history of his bonds, including adjustments.