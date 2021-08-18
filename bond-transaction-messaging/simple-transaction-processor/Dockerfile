FROM azul/zulu-openjdk-alpine:11.0.10-jre

EXPOSE 8080

RUN addgroup -S app-runner -g 3000 && \
    adduser -S app-runner -G app-runner --uid 3000 --home /home/app-runner --disabled-password
USER app-runner:app-runner

WORKDIR /home/app-runner
COPY target/transaction-app.jar .

CMD ["java", "-XX:MaxRAMPercentage=80", "-jar", "./transaction-app.jar"]