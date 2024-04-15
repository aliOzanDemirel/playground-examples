package zlog

import (
	"github.com/rs/zerolog"
	"github.com/rs/zerolog/log"
	"os"
	"strings"
	"testing"
)

const testLogFilePath = "test.log"

func getLogConf() LogConf {
	return LogConf{
		FilePaths:   []string{testLogFilePath},
		DisableJson: false,
		Level:       zerolog.DebugLevel.String(),
		Fields: map[string]interface{}{
			"foo":    "bar",
			"number": 999,
		},
	}
}

func TestSetupGlobalLogger(t *testing.T) {

	t.Run("stdout writer without lumberjack logger", func(t *testing.T) {

		conf := getLogConf()
		conf.FilePaths = []string{"/dev/stdout"}
		writers := outputWriters(conf)
		if writers[0] != os.Stdout {
			t.Errorf("unexpected log writer, should get stdout writer")
		}
	})

	t.Run("stderr writer without structured logging", func(t *testing.T) {

		conf := getLogConf()
		conf.DisableJson = true
		conf.FilePaths = []string{"/dev/stderr"}
		writers := outputWriters(conf)
		writer, ok := writers[0].(zerolog.ConsoleWriter)
		if !ok {
			t.Errorf("unexpected log writer, should get console writer")
		}
		if writer.NoColor != false {
			t.Errorf("log writer should have colored output")
		}
		if writer.Out != os.Stderr {
			t.Errorf("log writer should have stderr output")
		}
	})

	t.Run("valid log setup", func(t *testing.T) {

		conf := getLogConf()
		if err := SetupGlobalLogger(conf); err != nil {
			t.Fatalf("failed to setup logger: %v", err)
		}
		defer func() {
			err := os.Remove(testLogFilePath)
			if err != nil {
				t.Logf("could not remove file '%s'", testLogFilePath)
			}
		}()

		expectedGlobalLoggerLevel := zerolog.DebugLevel
		if log.Logger.GetLevel() != expectedGlobalLoggerLevel {
			t.Errorf("unexpected logger specific level, actual: %v, expected: %v", log.Logger.GetLevel(), expectedGlobalLoggerLevel)
		}

		expectedGlobalOverrideLevel := zerolog.TraceLevel
		if zerolog.GlobalLevel() != expectedGlobalOverrideLevel {
			t.Errorf("unexpected global level, actual: %v, expected: %v", zerolog.GlobalLevel(), expectedGlobalOverrideLevel)
		}

		logMessage := "create log file test - random log message"
		log.Info().Msg(logMessage)

		data, err := os.ReadFile(testLogFilePath)
		if err != nil {
			t.Errorf("failed reading log file: %v", err)
		}

		loggedData := string(data)
		if !strings.Contains(loggedData, logMessage) {
			t.Errorf("did not find log message: %s", loggedData)
		}

		// expect to find global log fields in logger context
		if !strings.Contains(loggedData, "\"foo\":\"bar\"") {
			t.Errorf("did not find log field ['foo': 'bar']: %s", loggedData)
		}
		if !strings.Contains(loggedData, "\"number\":999") {
			t.Errorf("did not find log field ['number': 999]: %s", loggedData)
		}

		if strings.Contains(loggedData, "\"message\":") && !strings.Contains(loggedData, "\"msg\":") {
			t.Errorf("unexpected message field name: %s", loggedData)
		}
		if strings.Contains(loggedData, "\"time\":") && !strings.Contains(loggedData, "\"ts\":") {
			t.Errorf("unexpected timestamp field name: %s", loggedData)
		}
	})

}

func TestLogConfig(t *testing.T) {

	t.Run("default to info if log level is invalid", func(t *testing.T) {

		conf := LogConf{Level: "non_existing_level"}
		if err := conf.validateAndSetDefaults(); err != nil {
			t.Errorf("unexpected error: %v", err)
		}
		if conf.Level != "info" {
			t.Errorf("unexpected log level, actual: %s, expected: %s", conf.Level, "info")
		}
	})

	t.Run("check defaults", func(t *testing.T) {

		conf := LogConf{}
		if err := conf.validateAndSetDefaults(); err != nil {
			t.Errorf("unexpected error: %v", err)
		}

		// no log output should default to stdout
		if len(conf.FilePaths) != 1 {
			t.Errorf("unexpected output count, actual: %d, expected: %d", len(conf.FilePaths), 1)
		}
		if conf.FilePaths[0] != "/dev/stdout" {
			t.Errorf("unexpected output, actual: %s, expected: %s", conf.FilePaths[0], "/dev/stdout")
		}

		if conf.Level != "info" {
			t.Errorf("unexpected Level: %s", conf.Level)
		}
		if conf.MaxSize != 10 {
			t.Errorf("unexpected MaxSize: %d", conf.MaxSize)
		}
		if conf.MaxBackups != 10 {
			t.Errorf("unexpected MaxBackups: %d", conf.MaxBackups)
		}
		if conf.MaxAge != 3 {
			t.Errorf("unexpected MaxAge: %d", conf.MaxAge)
		}
	})

	t.Run("check multiple log outputs", func(t *testing.T) {

		conf := LogConf{FilePaths: []string{"/dup.log", "/dev/stderr", "", "/dup.log"}}
		if err := conf.validateAndSetDefaults(); err != nil {
			t.Errorf("unexpected error: %v", err)
		}

		if len(conf.FilePaths) != 3 {
			t.Errorf("wrong log file path count, actual: %d, expected: %d", len(conf.FilePaths), 3)
		}
		foundStdout, foundStderr, foundDupOnce := false, false, false
		for _, path := range conf.FilePaths {

			// empty string is converted to stdout
			if path == "/dev/stdout" {
				foundStdout = true
			} else if path == "/dev/stderr" {
				foundStderr = true
			} else if path == "/dup.log" {
				foundDupOnce = true
			}
		}
		if !foundStdout {
			t.Errorf("did not find /dev/stdout")
		} else if !foundStderr {
			t.Errorf("did not find /dev/stderr")
		} else if !foundDupOnce {
			t.Errorf("did not find /dup.log")
		}
	})
}

func TestLogConfigFromEnv(t *testing.T) {

	t.Run("check log config with custom prefix", func(t *testing.T) {

		t.Setenv("OZZY_LEVEL", "trace")
		t.Setenv("OZ_MAX_AGE", "29")
		t.Setenv("OZ_FIELD_ozzy", "tirrek")

		conf := LogConfFromEnv("OZ_")

		if conf.Level == "trace" {
			t.Errorf("found unexpected log level 'trace'")
		}
		if conf.MaxAge != 29 {
			t.Errorf("wrong max age, actual: %d, expected: %d", conf.MaxAge, 29)
		}
		if conf.Fields["ozzy"] != "tirrek" {
			t.Errorf("wrong global log field, actual: %s, expected: %s", conf.Fields["ozzy"], "tirrek")
		}
	})

	t.Run("read log config from environment vars", func(t *testing.T) {

		t.Setenv("LOG_DISABLE_JSON", "true")
		t.Setenv("LOG_FILE_PATHS", "/something/something|/another")
		t.Setenv("LOG_LEVEL", "warn")
		t.Setenv("LOG_MAX_SIZE", "333")
		t.Setenv("LOG_MAX_BACKUPS", "222")
		t.Setenv("LOG_MAX_AGE", "111")
		t.Setenv("LOG_FIELD_ozzy", "tirrek")

		conf := LogConfFromEnv("LOG_")

		if conf.DisableJson != true {
			t.Errorf("wrong disable json, actual: %t, expected: %t", conf.DisableJson, true)
		}
		if conf.Level != "warn" {
			t.Errorf("wrong log level, actual: %s, expected: %s", conf.Level, "warn")
		}
		if conf.MaxSize != 333 {
			t.Errorf("wrong max size, actual: %d, expected: %d", conf.MaxSize, 333)
		}
		if conf.MaxBackups != 222 {
			t.Errorf("wrong max backups, actual: %d, expected: %d", conf.MaxBackups, 222)
		}
		if conf.MaxAge != 111 {
			t.Errorf("wrong max age, actual: %d, expected: %d", conf.MaxAge, 111)
		}
		if conf.Fields["ozzy"] != "tirrek" {
			t.Errorf("wrong global log field, actual: %s, expected: %s", conf.Fields["ozzy"], "tirrek")
		}

		if len(conf.FilePaths) != 2 {
			t.Errorf("wrong log file path count, actual: %d, expected: %d", len(conf.FilePaths), 2)
		}
		foundSomething, foundAnother := false, false
		for _, path := range conf.FilePaths {
			if path == "/something/something" {
				foundSomething = true
			} else if path == "/another" {
				foundAnother = true
			}
		}
		if !foundSomething {
			t.Errorf("did not find /something/something")
		} else if !foundAnother {
			t.Errorf("did not find /another")
		}
	})
}
