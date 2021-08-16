## Scala/Spring Web App Example

This application is an example project written with Scala, Spring Boot/Security/Data/Test/Web (Undertow), Thymeleaf,
jQuery, H2, Maven and Docker. Below examples are implemented in the project:

- Creating login-logout by using Spring security
- Using Spring's CSRF protection and how to post ajax request with CSRF enabled
- Configuring im-memory users and url matchers up to user roles
- Modifying static files' path in local environment for hot reloading
- Configuring HTTPS in Spring Boot and adding explicit HTTP listener for Undertow
- Using import.sql to automatically insert data to db schema which is created by Hibernate
- Using Thymeleaf with jQuery and its Spring integration for form actions and field errors
- How to upload or download files with Spring
- How to add custom logic by overriding default ErrorController in Spring
- Using Hibernate validation and entity mappings, writing custom Spring Validator
- Writing both integration and unit tests by using Spring test utilities
- Using maven to build project by running tests
- Using docker to generate and run JAR file in a container

---

- Use `-DskipTests=true` to disable tests if there are failures in local machine (should not happen normally).

| steps | docker           | maven  |  
| :---: | :--------------: | :---:  |
| build | docker build -t archiver . | mvn clean install spring-boot:repackage
| run   | docker run -it -p 8080:8080 -p 8443:8443 --rm archiver | java -Dspring.profiles.active=docker -jar simple-file-archiver.jar

### Screenshots

![login][login]

---

![files][files]

---

![categories][categories]

---

![file-form][file-form]


[login]: readme-resources/login.jpg "Login Page"

[files]: readme-resources/files.jpg "Category List"

[categories]: readme-resources/categories.jpg "Category List"

[file-form]: readme-resources/file-form.jpg "Add-Edit File Form"