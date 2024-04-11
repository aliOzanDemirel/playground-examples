package logger

import (
	"fmt"
	"time"
)

type Logger interface {
	IsDebugOn() bool
	Debug(map[string]interface{}, string, ...interface{})
	Info(map[string]interface{}, string, ...interface{})
	Error(map[string]interface{}, error, string, ...interface{})
}

var globalLogger Logger = &StdoutLogger{DebugLevel: false}

func SetGlobalLogger(l Logger) { globalLogger = l }

func IsDebugOn() bool { return globalLogger.IsDebugOn() }
func Debug(fields map[string]interface{}, format string, args ...interface{}) {
	globalLogger.Debug(fields, format, args...)
}
func Info(fields map[string]interface{}, format string, args ...interface{}) {
	globalLogger.Info(fields, format, args...)
}
func Error(fields map[string]interface{}, err error, format string, args ...interface{}) {
	globalLogger.Error(fields, err, format, args...)
}

type StdoutLogger struct {
	DebugLevel bool
}

func (l *StdoutLogger) IsDebugOn() bool {
	return l.DebugLevel
}

func (l *StdoutLogger) Debug(fields map[string]interface{}, format string, args ...interface{}) {
	if !l.DebugLevel {
		return
	}
	if len(fields) == 0 {
		fields = map[string]interface{}{"time": time.Now().Format(time.RFC822Z)}
	}
	fieldStr := fmt.Sprintf("fields -> [%v]", fields)
	fmt.Printf("DEBUG: "+format+"\n"+fieldStr+"\n\n", args...)
}

func (l *StdoutLogger) Info(fields map[string]interface{}, format string, args ...interface{}) {
	if len(fields) == 0 {
		fields = map[string]interface{}{"time": time.Now().Format(time.RFC822Z)}
	}
	fieldStr := fmt.Sprintf("fields -> [%v]", fields)
	fmt.Printf("INFO: "+format+"\n"+fieldStr+"\n\n", args...)
}

func (l *StdoutLogger) Error(fields map[string]interface{}, err error, format string, args ...interface{}) {
	if len(fields) == 0 {
		fields = map[string]interface{}{"time": time.Now().Format(time.RFC822Z)}
	}
	if err != nil {
		fields["error"] = err.Error()
	}
	fieldStr := fmt.Sprintf("fields -> [%v]", fields)
	fmt.Printf("ERROR: "+format+"\n"+fieldStr+"\n\n", args...)
}
