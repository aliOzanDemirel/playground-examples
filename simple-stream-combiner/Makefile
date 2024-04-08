# commands here are run for local development with zsh

JAVA_HOME_DIR ?= /Users/ademirel/OzzyWorkspace/tools/java/21-rc-mac-aarch64.jdk/Contents/Home
MAVEN_CMD     := ./mvnw  # using maven wrapper by default
MAVEN_PROFILE := default # should have no proxy in maven profile
CURRENT_DIR   := $(shell pwd)

PRODUCER_DEV_CONFIG := $(CURRENT_DIR)/.local/local_producer.properties
COMBINER_DEV_CONFIG := $(CURRENT_DIR)/.local/local_combiner.properties

PRODUCER_ARTIFACT := stream-producer/target/stream-producer-1.jar
COMBINER_ARTIFACT := stream-combiner/target/stream-combiner-1-jar-with-dependencies.jar

PRODUCER_JACOCO_REPORT := stream-producer/target/site/jacoco/index.html
COMBINER_JACOCO_REPORT := stream-combiner/target/site/jacoco/index.html

.PHONY: clean
clean:
	$(MAVEN_CMD) clean

.PHONY: producer_package
producer_package:
	export JAVA_HOME=$(JAVA_HOME_DIR) && \
	$(MAVEN_CMD) -pl :stream-producer package -P $(MAVEN_PROFILE)

.PHONY: producer
producer:
	export JAVA_HOME=$(JAVA_HOME_DIR) && \
	$(MAVEN_CMD) -pl :stream-producer package -DskipTests -P $(MAVEN_PROFILE)
	export JAVA_HOME=$(JAVA_HOME_DIR) && \
	java -Dconfig.path.override=$(PRODUCER_DEV_CONFIG) -jar $(PRODUCER_ARTIFACT)

.PHONY: combiner_package
combiner_package:
	export JAVA_HOME=$(JAVA_HOME_DIR) && \
	$(MAVEN_CMD) -pl :stream-combiner package -P $(MAVEN_PROFILE)

.PHONY: combiner
combiner:
	export JAVA_HOME=$(JAVA_HOME_DIR) && \
	$(MAVEN_CMD) -pl :stream-combiner package -DskipTests -P $(MAVEN_PROFILE)
	export JAVA_HOME=$(JAVA_HOME_DIR) && \
	java -Dconfig.path.override=$(COMBINER_DEV_CONFIG) -jar $(COMBINER_ARTIFACT)

.PHONY: coverage
coverage:
	export JAVA_HOME=$(JAVA_HOME_DIR) && \
	$(MAVEN_CMD) package -P $(MAVEN_PROFILE)
	open $(PRODUCER_JACOCO_REPORT)
	open $(COMBINER_JACOCO_REPORT)
