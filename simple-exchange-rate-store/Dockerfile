# these are commons to share between docker builders
# where go source code is copied to later build it
ARG BUILDER_SOURCE_DIR=/source

# where built artifact is outputted to, with final name of absolute artifact path
ARG BUILDER_BINARY_DIR=$BUILDER_SOURCE_DIR/bin
ARG BUILDER_ARTIFACT_PATH=$BUILDER_BINARY_DIR/app-binary-in-builder

FROM golang:1.21.5-alpine AS builder

# declare shared docker args to be used and create build process directories
ARG BUILDER_SOURCE_DIR
ARG BUILDER_BINARY_DIR
ARG BUILDER_ARTIFACT_PATH
RUN mkdir -p $BUILDER_SOURCE_DIR && mkdir -p $BUILDER_BINARY_DIR

# setup environment for go build, explicitly build for linux/amd64
ENV GOOS=linux
ENV GOARCH=amd64
ENV CGO_ENABLED=0
ENV GO111MODULE=on

# log all go environment variables
RUN go version && go env

# copy source code from repository to builder image
COPY go.mod go.sum $BUILDER_SOURCE_DIR/
COPY vendor/ $BUILDER_SOURCE_DIR/vendor/
COPY main/ $BUILDER_SOURCE_DIR/main/
COPY pkg/ $BUILDER_SOURCE_DIR/pkg/

# run tests of all packages before building binary, will fail building image if returns non-zero code
ARG SKIP_GO_TEST
RUN if [[ $SKIP_GO_TEST == "1" ]]; then \
     echo "Skipping go test"; \
    else \
     cd $BUILDER_SOURCE_DIR && go test -cover ./pkg/...; \
    fi

# build final executable artifact
RUN cd $BUILDER_SOURCE_DIR && \
    go build -o $BUILDER_ARTIFACT_PATH -mod=vendor ./main/main.go

FROM alpine:3.19

# set docker workdir (-w) to where app specific things are
ENV APP_HOME=/opt/app
WORKDIR $APP_HOME

# copy binary artifact from builder image
ARG BUILDER_ARTIFACT_PATH
ENV APP_ARTIFACT_PATH=$APP_HOME/exchange-rate-store
COPY --from=builder $BUILDER_ARTIFACT_PATH $APP_ARTIFACT_PATH

# create non-root user
RUN addgroup -S app_runner -g 3000 && \
    adduser -S app_runner -G app_runner --uid 3000 --home /home/app_runner --disabled-password && \
    chown -R app_runner:app_runner $APP_HOME

USER app_runner:app_runner

ENTRYPOINT ["./exchange-rate-store"]
