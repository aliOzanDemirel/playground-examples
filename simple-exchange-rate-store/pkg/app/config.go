package app

import (
	"exchange-rate-store/pkg/alphavantage"
	"exchange-rate-store/pkg/common"
	"exchange-rate-store/pkg/persistence"
	"time"
)

type Config struct {
	Dev               Dev
	RatePullInterval  time.Duration
	AlphaVantageApi   alphavantage.Config
	Database          persistence.Config
	Port              uint
	BasicAuthUser     string
	BasicAuthPassword string
}

type Dev struct {
	MockCryptoApi bool
	MockDataStore bool
}

func (c *Config) ReadFromEnv(configPrefix string) {

	c.Port = common.ReadEnvUint(configPrefix+"PORT", 0)
	c.BasicAuthUser = common.ReadEnvString(configPrefix+"BASIC_AUTH_USER", "")
	c.BasicAuthPassword = common.ReadEnvString(configPrefix+"BASIC_AUTH_PASSWORD", "")
	c.RatePullInterval = common.ReadEnvDuration(configPrefix+"RATE_PULL_INTERVAL", 0)

	alphaVantageConf := alphavantage.Config{}
	alphaVantageConf.ApiKey = common.ReadEnvString(configPrefix+"ALPHA_VANTAGE_API_KEY", "")
	alphaVantageConf.BaseUrl = common.ReadEnvString(configPrefix+"ALPHA_VANTAGE_BASE_URL", "")
	c.AlphaVantageApi = alphaVantageConf

	dbConf := persistence.Config{}
	dbConf.Host = common.ReadEnvString(configPrefix+"DB_HOST", "")
	dbConf.Port = common.ReadEnvUint(configPrefix+"DB_PORT", 0)
	dbConf.DbName = common.ReadEnvString(configPrefix+"DB_NAME", "")
	dbConf.User = common.ReadEnvString(configPrefix+"DB_USER", "")
	dbConf.Password = common.ReadEnvString(configPrefix+"DB_PASSWORD", "")
	c.Database = dbConf

	dev := Dev{}
	dev.MockCryptoApi = common.ReadEnvBool(configPrefix+"DEV_MOCK_CRYPTO_API", true)
	dev.MockDataStore = common.ReadEnvBool(configPrefix+"DEV_MOCK_DATA_STORE", true)
	c.Dev = dev
}

func (c *Config) HideSecrets() Config {

	cloned := *c
	cloned.Database.Password = "hidden"
	cloned.BasicAuthPassword = "hidden"
	cloned.AlphaVantageApi.ApiKey = "hidden"
	return cloned
}
