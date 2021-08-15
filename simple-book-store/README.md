## Vaadin Example

- This application is for trying out Vaadin to experience server-side frontend development with Java. User can list,
  create, update or delete books.
    - Spring Boot (Undertow), Vaadin, Gradle, Docker and H2 in-memory database are used. Vaadin Spring Extensions can be
      used for further improvement.
    - Some resources: [Vaadin Tutorial](https://vaadin.com/docs/v8/framework/tutorial.html)
      and [Vaadin with Spring Tutorial](http://vaadin.github.io/spring-tutorial)
- UndertowServletWebServerFactory is modified to add http listener. Default ports are 8080 for http and 8443 for https,
  pass in -DhttpPort or -DhttpsPort to override defaults.
- Keystore for ssl is generated for development with command:
  `/WHEREEVER_JAVA_IS/keytool -genkey -alias local -storetype PKCS12 -keyalg RSA -keysize 2048 -keystore localhost.p12 -validity 3650`

> Requires java 1.8 for gradle wrapper (4.9) to use as java runtime and build the application.

1. You can use docker to build image and run it:
    1. `docker build -t vaadin-example-app .`
    2. `docker run -it -p 8080:8080 -p 8443:8443 --rm vaadin-example-app`
2. You can build fat jar and run it directly:
    1. `gradlew bootJar`
    2. `java -jar -Dspring.profiles.active=local VaadBooks.jar`

### Pictures

![1][1]

---

![2][2]

[1]: readme-resources/1.jpg "List Books"

[2]: readme-resources/2.jpg "Not Found"
