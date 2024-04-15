package zlog

import "github.com/rs/zerolog"

type zerologBind struct {
	zLogger *zerolog.Logger
}

func (l *zerologBind) IsDebugOn() bool {
	return l.zLogger.Debug().Enabled()
}
func (l *zerologBind) Debug(fields map[string]interface{}, format string, args ...interface{}) {
	l.zLogger.Debug().Fields(fields).Msgf(format, args...)
}
func (l *zerologBind) Info(fields map[string]interface{}, format string, args ...interface{}) {
	l.zLogger.Info().Fields(fields).Msgf(format, args...)
}
func (l *zerologBind) Error(fields map[string]interface{}, err error, format string, args ...interface{}) {
	l.zLogger.Err(err).Fields(fields).Msgf(format, args...)
}
