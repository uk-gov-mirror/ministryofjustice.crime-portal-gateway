#!/usr/bin/env bash
set -e
export TERM=ansi
export AWS_ACCESS_KEY_ID=foobar
export AWS_SECRET_ACCESS_KEY=foobar
export AWS_DEFAULT_REGION=eu-west-2
export PAGER=

aws --endpoint-url http://localhost:4566 sqs create-queue --queue-name crime-portal-gateway-queue
aws --endpoint-url=http://localhost:4566 s3 mb s3://cpg-bucket
aws --endpoint-url=http://localhost:4566 sns create-topic --name court-case-events-topic
aws --endpoint-url=http://localhost:4566 sns subscribe --topic-arn "arn:aws:sns:eu-west-2:000000000000:court-case-events-topic" --protocol "sqs" --notification-endpoint "arn:aws:sns:eu-west-2:000000000000:crime-portal-gateway-queue"

echo "S3 Configured"
