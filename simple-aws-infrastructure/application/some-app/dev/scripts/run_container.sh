#!/usr/bin/env bash

while getopts ":i:h:u:p:c:" opt; do
  case ${opt} in
    i)
        IMAGE_URL="$OPTARG"
    ;;
    h)
        HOST_ARG="$OPTARG"
    ;;
    u)
        USERNAME_ARG="$OPTARG"
    ;;
    p)
        PASSWORD_ARG="$OPTARG"
    ;;
    c)
        CONTAINER_NAME_ARG="$OPTARG"
    ;;
    \?) echo "Invalid argument: -$OPTARG / Usage: [-i] [-h] [-u] [-p] [-c]" >&2
        exit 1
    ;;
  esac
done

if [[ -z "$IMAGE_URL" ]]; then
    echo $0: image url with repository is needed to run script!
    exit 1
fi

if [[ -z "$PASSWORD_ARG" ]]; then
    echo $3: database password is not provided!
    exit 1
fi
echo "DB_PASSWORD is set to '$PASSWORD_ARG'"

echo "Trying to get login for docker from ECR"
$(aws ecr get-login --no-include-email --region eu-west-1 --profile aws-profile-dev-account)

DB_HOST=${HOST_ARG:=some.db.dev.budget.local}
echo "DB_HOST is set to '$DB_HOST'"

DB_USERNAME=${USERNAME_ARG:=some}
echo "DB_USER is set to '$DB_USERNAME'"

CONTAINER_NAME=${CONTAINER_NAME_ARG:=some-container-name}

echo "Starting to run container '$CONTAINER_NAME' with image '$IMAGE_URL' at $(date +"%x %r %Z")"

# user running this command should have aws profile configured to be able to pull images from ECR
# also if there is already running container with the name provided, it should be pruned first
# -e SPRING_PROFILES_ACTIVE="dev" \
CONTAINER_ID=$(docker run -d -t -i \
-e DB_DB="some" \
-e DB_USERNAME="$DB_USERNAME" \
-e DB_PASSWORD="$PASSWORD_ARG" \
-e DB_HOST="$DB_HOST" \
-p 8080:8080 \
--name "$CONTAINER_NAME" "$IMAGE_URL")

if ! docker top $CONTAINER_ID &>/dev/null; then
    echo "Container could not be run..."
    exit 1
fi
