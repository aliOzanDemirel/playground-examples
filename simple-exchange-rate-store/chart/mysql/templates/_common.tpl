{{/*
use release name as common k8s resource identifier
*/}}
{{- define "fullname" -}}
{{ $nameLength := len .Release.Name }}
{{- if and (ge $nameLength 2) (le $nameLength 40) -}}
{{ .Release.Name }}
{{- else -}}
{{- required "release name (to be used as k8s resource name) should have >= 2 and <= 40 characters!" "" -}}
{{- end -}}
{{- end -}}
