package zlog

import (
	"exchange-rate-store/pkg/common"
	moduleLogger "exchange-rate-store/pkg/logger"
	"fmt"
	"github.com/rs/zerolog"
	"github.com/rs/zerolog/log"
	"gopkg.in/natefinch/lumberjack.v2"
	"io"
	"os"
)

const (
	// default log configs here mean that at most 100 MB of log files will exist, if logs are written to files
	defaultLogFileMaxSize    = 10
	defaultLogFileMaxBackups = 10
	defaultLogFileMaxAge     = 3
)

type LogConf struct {
	CurrentTest zerolog.TestingLog     `yaml:"-"`                     // uses test writer if configured with any test context, ignores any other file path or json disabling config
	DisableJson bool                   `yaml:"disableJson,omitempty"` // uses pretty formatter console writer instead of json writer
	FilePaths   []string               `yaml:"filePaths,omitempty"`   // by default logs are sent to stdout if no path is configured, can configure multiple paths if stdout is wanted for kubectl logs
	Level       string                 `yaml:"level,omitempty"`       // by default, info
	MaxSize     uint                   `yaml:"maxSize,omitempty"`     // by default, 10, size threshold of the log file before it gets rotated, in MB
	MaxBackups  uint                   `yaml:"maxBackups,omitempty"`  // by default, 10, number of old log files to retain
	MaxAge      uint                   `yaml:"maxAge,omitempty"`      // by default, 3, number of days to retain old log files
	Fields      map[string]interface{} `yaml:"fields,omitempty"`      // log fields configured in logger context, will be logged for every log statement
}

func LogConfFromEnv(envVarPrefix string) LogConf {

	lc := LogConf{}
	lc.DisableJson = common.ReadEnvBool(envVarPrefix+"DISABLE_JSON", false)
	lc.FilePaths = common.ReadEnvStringList(envVarPrefix+"FILE_PATHS", []string{})
	lc.Level = common.ReadEnvString(envVarPrefix+"LEVEL", zerolog.InfoLevel.String())
	lc.MaxSize = common.ReadEnvUint(envVarPrefix+"MAX_SIZE", defaultLogFileMaxSize)
	lc.MaxBackups = common.ReadEnvUint(envVarPrefix+"MAX_BACKUPS", defaultLogFileMaxBackups)
	lc.MaxAge = common.ReadEnvUint(envVarPrefix+"MAX_AGE", defaultLogFileMaxAge)

	// by default, env vars starting with 'FIELD_' will be searched
	logFieldSearchPrefix := envVarPrefix + "FIELD_"
	foundFields := common.PrefixedEnvKeyValues(logFieldSearchPrefix)
	if lc.Fields == nil {
		lc.Fields = map[string]interface{}{}
	}
	for k, v := range foundFields {
		lc.Fields[k] = v
	}

	return lc
}

func SetupGlobalLogger(conf LogConf) error {

	logger, err := NewZeroLogger(conf)
	if err != nil {
		return err
	}

	// NOTE: ignoring global log level override for simplicity, just using global logger
	log.Logger = logger

	// use global zerolog logger as global module logger
	moduleGlobalLogger := &zerologBind{zLogger: &log.Logger}
	moduleLogger.SetGlobalLogger(moduleGlobalLogger)
	return nil
}

func (lc *LogConf) validateAndSetDefaults() error {

	if len(lc.FilePaths) == 0 {

		// default to stdout writer if no log file path is configured
		lc.FilePaths = append(lc.FilePaths, os.Stdout.Name())
	} else {

		// if there are multiple log outputs configured, remove any duplicates by creating hash set
		logOutputs := map[string]struct{}{}
		for _, path := range lc.FilePaths {
			logOutputs[path] = struct{}{}
		}
		lc.FilePaths = []string{}
		for pathAsKey := range logOutputs {
			lc.FilePaths = append(lc.FilePaths, pathAsKey)
		}

		for i := range lc.FilePaths {

			path := lc.FilePaths[i]
			if path == "" {

				// treat empty string as stdout
				lc.FilePaths[i] = os.Stdout.Name()

			} else if isExistingDir(path) {

				// should warn if given path is a directory
				return fmt.Errorf("cannot use directory for log output file path: '%s'", path)
			}
		}
	}

	_, err := zerolog.ParseLevel(lc.Level)
	if lc.Level == "" || err != nil {
		lc.Level = zerolog.InfoLevel.String()
		fmt.Printf("[setup-zerolog] defaulted logger level to %s\n", zerolog.InfoLevel.String())
	}

	if lc.MaxSize <= 0 {
		lc.MaxSize = defaultLogFileMaxSize
		fmt.Printf("[setup-zerolog] defaulted logger max file size to %d\n", defaultLogFileMaxSize)
	}
	if lc.MaxBackups <= 0 {
		lc.MaxBackups = defaultLogFileMaxBackups
		fmt.Printf("[setup-zerolog] defaulted logger max backups to %d\n", defaultLogFileMaxBackups)
	}
	if lc.MaxAge <= 0 {
		lc.MaxAge = defaultLogFileMaxAge
		fmt.Printf("[setup-zerolog] defaulted logger max age to %d\n", defaultLogFileMaxAge)
	}
	return nil
}

func NewZeroLogger(conf LogConf) (zerolog.Logger, error) {

	zerolog.MessageFieldName = "msg"
	zerolog.TimestampFieldName = "ts"
	zerolog.TimeFieldFormat = zerolog.TimeFormatUnixMs

	err := conf.validateAndSetDefaults()
	if err != nil {
		return zerolog.Logger{}, err
	}
	level, err := zerolog.ParseLevel(conf.Level)
	if err != nil {
		return zerolog.Logger{}, err
	}

	fmt.Printf("[setup-zerolog] configuring logger with config: %+v\n", conf)

	writer := logOutputWriter(conf)
	loggerWithoutCustomFields := zerolog.New(writer).Level(level).With().Timestamp().Logger()
	logger := setupLogFields(loggerWithoutCustomFields, conf.Fields)
	return logger, nil
}

// enriches given zerolog logger with log fields and returns a new logger
// env var FIELD_env=prod will configure field with key 'env' and value 'prod'
// env var FIELD_caller=1 will configure caller field
func setupLogFields(logger zerolog.Logger, logFields map[string]interface{}) zerolog.Logger {

	if len(logFields) == 0 {
		return logger
	}

	callerLogFieldKey := zerolog.CallerFieldName
	if _, found := logFields[callerLogFieldKey]; found {
		logger = logger.With().Caller().Logger()
		delete(logFields, callerLogFieldKey)
	}
	return logger.With().Fields(logFields).Logger()
}

func logOutputWriter(conf LogConf) io.Writer {

	// ignores all the other writer configuring options
	if conf.CurrentTest != nil {
		consoleTestWriter := &zerolog.ConsoleWriter{}
		configure := zerolog.ConsoleTestWriter(conf.CurrentTest)
		configure(consoleTestWriter)
		return consoleTestWriter
	}

	writers := outputWriters(conf)
	if len(writers) == 1 {
		return writers[0]
	}

	return zerolog.MultiLevelWriter(writers...)
}

func outputWriters(conf LogConf) []io.Writer {

	var logOutWriters []io.Writer
	for _, path := range conf.FilePaths {

		// does not use lumberjack log writer if stdout or stderr is configured
		var logOut io.Writer
		if path == os.Stdout.Name() {
			logOut = os.Stdout
		} else if path == os.Stderr.Name() {
			logOut = os.Stderr
		} else {

			// lumberjack will auto create the parent directory if it does not already exist
			logOut = &lumberjack.Logger{
				Filename:   path,
				MaxSize:    int(conf.MaxSize),
				MaxBackups: int(conf.MaxBackups),
				MaxAge:     int(conf.MaxAge),
				Compress:   false,
				LocalTime:  false,
			}
		}
		logOutWriters = append(logOutWriters, logOut)
	}

	// decorate all writers with colored console writer if structured log is disabled
	if conf.DisableJson {
		var consoleWriters []io.Writer
		for _, writer := range logOutWriters {
			consoleWriters = append(consoleWriters, zerolog.ConsoleWriter{
				Out:     writer,
				NoColor: false,
			})
		}
		logOutWriters = consoleWriters
	}

	return logOutWriters
}

func isExistingDir(filePath string) bool {
	fileInfo, err := os.Stat(filePath)
	if err != nil {
		return false
	}
	return fileInfo.IsDir()
}
