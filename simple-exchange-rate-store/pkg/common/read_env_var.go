package common

import (
	"exchange-rate-store/pkg/logger"
	"os"
	"strconv"
	"strings"
	"time"
)

func ReadEnvString(envVarName string, defaultValue string) string {
	val := os.Getenv(envVarName)
	if val == "" {
		logger.Info(nil, "[read-env-var] could not find '%s', returning default value: '%s'", envVarName, defaultValue)
		return defaultValue
	}
	return val
}

func ReadEnvBool(envVarName string, defaultValue bool) bool {
	val := os.Getenv(envVarName)
	parsedBool, err := strconv.ParseBool(val)
	if err != nil {
		logger.Info(nil, "[read-env-var] could not parse bool '%s', returning default value: '%t'", envVarName, defaultValue)
		return defaultValue
	}
	return parsedBool
}

func ReadEnvDuration(envVarName string, defaultValue time.Duration) time.Duration {
	val := os.Getenv(envVarName)
	duration, err := time.ParseDuration(val)
	if err != nil {
		logger.Info(nil, "[read-env-var] could not parse duration '%s', returning default value: '%v'", envVarName, defaultValue)
		return defaultValue
	}
	return duration
}

// ReadEnvUint truncates if necessary
func ReadEnvUint(envVarName string, defaultValue uint) uint {
	val := os.Getenv(envVarName)
	parsedUint, err := strconv.ParseUint(val, 10, 64)
	if err != nil {
		logger.Info(nil, "[read-env-var] could not parse unsigned int '%s', returning default value: '%d'", envVarName, defaultValue)
		return defaultValue
	}
	return uint(parsedUint)
}

// ReadEnvStringList uses separator '|'
func ReadEnvStringList(envVarName string, defaultValue []string) []string {
	val := os.Getenv(envVarName)
	if val == "" {
		logger.Info(nil, "[read-env-var] could not find '%s', returning default value: '%v'", envVarName, defaultValue)
		return defaultValue
	}
	return strings.Split(val, "|")
}

// PrefixedEnvKeyValues returns key=values extracted from env variables prefixed by given argument
// given env variable 'EXAMPLE_ENV_key1=val1' and given prefix 'EXAMPLE_ENV_', result will be {"key1":"val1"}
func PrefixedEnvKeyValues(prefix string) map[string]string {
	var foundVars = make(map[string]string)
	for _, envVar := range os.Environ() {
		if strings.HasPrefix(envVar, prefix) {
			envVar = envVar[len(prefix):]
			parts := strings.SplitN(envVar, "=", 2)
			if len(parts) == 2 {
				foundVars[parts[0]] = parts[1]
			}
		}
	}
	return foundVars
}
