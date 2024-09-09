    {{/* vim: set filetype=mustache: */}}
{{/*
Environment variables for web and worker containers
*/}}
{{- define "deployment.envs" }}
env:
  - name: SERVER_PORT
    value: "{{ .Values.image.port }}"

  - name: JAVA_OPTS
    value: "{{ .Values.env.JAVA_OPTS }}"

  - name: SPRING_PROFILES_ACTIVE
    value: "{{ .Values.env.SPRING_PROFILES_ACTIVE }}"

  - name: INCLUDED_COURT_CODES
    value: "{{ .Values.env.INCLUDED_COURT_CODES }}"

  - name: APPLICATIONINSIGHTS_CONNECTION_STRING
    valueFrom:
      secretKeyRef:
        name: applicationinsights-connection-string
        key: applicationinsights_connection_string

  - name: AWS_SQS_QUEUE_NAME
    valueFrom:
      secretKeyRef:
        name: crime-portal-gateway-queue-credentials
        key: sqs_name

  - name: AWS_SQS_ENDPOINT_URL
    valueFrom:
      secretKeyRef:
        name: crime-portal-gateway-queue-credentials
        key: sqs_id

  - name: AWS_SNS_COURT_CASE_EVENTS_TOPIC
    valueFrom:
      secretKeyRef:
        name: court-case-events-topic
        key: topic_arn

  - name: AWS_S3_BUCKET_NAME
    valueFrom:
      secretKeyRef:
        name: crime-portal-gateway-s3-credentials
        key: bucket_name

  - name: AWS_S3_BUCKET_ARN
    valueFrom:
      secretKeyRef:
        name: crime-portal-gateway-s3-credentials
        key: bucket_arn

  - name: KEYSTORE_PASSWORD
    valueFrom:
      secretKeyRef:
        name: crime-portal-gateway-secrets
        key: KEYSTORE_PASSWORD

  - name: PRIVATE_KEY_PASSWORD
    valueFrom:
      secretKeyRef:
        name: crime-portal-gateway-secrets
        key: PRIVATE_KEY_PASSWORD

  - name: TRUSTED_CERT_ALIAS_NAME
    valueFrom:
      secretKeyRef:
        name: crime-portal-gateway-secrets
        key: TRUSTED_CERT_ALIAS_NAME

  - name: PRIVATE_KEY_ALIAS_NAME
    valueFrom:
      secretKeyRef:
        name: crime-portal-gateway-secrets
        key: PRIVATE_KEY_ALIAS_NAME

{{- end -}}
