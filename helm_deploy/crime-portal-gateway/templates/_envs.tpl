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
    value: "logstash"

  - name: APPINSIGHTS_INSTRUMENTATIONKEY
    valueFrom:
      secretKeyRef:
        name: crime-portal-gateway-secrets
        key: APPINSIGHTS_INSTRUMENTATIONKEY

  - name: AWS_ACCESS_KEY_ID
    valueFrom:
      secretKeyRef:
        name: crime-portal-gateway-queue-credentials
        key: access_key_id

  - name: AWS_SECRET_ACCESS_KEY
    valueFrom:
      secretKeyRef:
        name: crime-portal-gateway-queue-credentials
        key: secret_access_key

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

  - name: KEYSTORE_PASSWORD
    valueFrom:
      secretKeyRef:
        name: crime-portal-gateway-secrets
        key: KEYSTORE_PASSWORD

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
