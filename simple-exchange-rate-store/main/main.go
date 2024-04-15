package main

import (
	"exchange-rate-store/pkg/app"
	"exchange-rate-store/pkg/logger"
	"exchange-rate-store/pkg/logger/zlog"
	"fmt"
	"os"
	"os/signal"
	"runtime"
	"syscall"
)

func main() {

	const startupFailureMsgFormat = "STARTUP FAILURE -> %v\n"

	logConf := zlog.LogConfFromEnv("LOG_")
	err := zlog.SetupGlobalLogger(logConf)
	if err != nil {
		errMsg := fmt.Sprintf("failed to setup logger -> %v", err)
		fmt.Printf(startupFailureMsgFormat, errMsg)
		os.Exit(1)
	}
	logger.Info(nil, "[startup] logger is ready, runtime -> %s - %s - %s", runtime.Version(), runtime.GOOS, runtime.GOARCH)

	conf := app.Config{}
	conf.ReadFromEnv("APP_")
	confHidden := conf.HideSecrets()
	logger.Info(nil, "[startup] loaded configuration -> %+v", confHidden)

	appInstance, err := app.NewExchangeRateStore(conf)
	if err != nil {
		errMsg := fmt.Sprintf("failed to prepare app instance -> %v", err)
		fmt.Printf(startupFailureMsgFormat, errMsg)
		os.Exit(1)
	}

	go func() {
		err := appInstance.Start()
		if err != nil {
			errMsg := fmt.Sprintf("failed to start app -> %v", err)
			fmt.Printf(startupFailureMsgFormat, errMsg)
			os.Exit(1)
		}
	}()
	defer appInstance.Shutdown()

	// wait to receive SIGINT or SIGTERM, then deferred shutdown method will handle whatever needs to be closed
	blockUntilSigintOrSigterm := make(chan os.Signal, 1)
	signal.Notify(blockUntilSigintOrSigterm, os.Interrupt, syscall.SIGTERM)
	<-blockUntilSigintOrSigterm
}
