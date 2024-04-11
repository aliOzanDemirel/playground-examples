package main

import (
	"exchange-rate-store/pkg/app"
	"exchange-rate-store/pkg/logger"
	"os"
	"os/signal"
	"runtime"
	"syscall"
)

func main() {

	_, found := os.LookupEnv("LOG_DEBUG")
	logger.SetGlobalLogger(&logger.StdoutLogger{DebugLevel: found})
	logger.Info(nil, "[startup] runtime -> %s - %s - %s", runtime.Version(), runtime.GOOS, runtime.GOARCH)

	conf := app.Config{}
	conf.ReadFromEnv("APP_")
	confHidden := conf.HideSecrets()
	logger.Info(nil, "[startup] loaded configuration -> %+v", confHidden)

	appInstance, err := app.NewExchangeRateStore(conf)
	if err != nil {
		logger.Error(nil, err, "[startup] failed to prepare app instance")
		os.Exit(1)
	}

	go func() {
		err := appInstance.Start()
		if err != nil {
			logger.Error(nil, err, "[startup] failed to start app")
			os.Exit(1)
		}
	}()
	defer appInstance.Shutdown()

	// wait to receive SIGINT or SIGTERM, then deferred shutdown method will handle whatever needs to be closed
	blockUntilSigintOrSigterm := make(chan os.Signal, 1)
	signal.Notify(blockUntilSigintOrSigterm, os.Interrupt, syscall.SIGTERM)
	<-blockUntilSigintOrSigterm
}
