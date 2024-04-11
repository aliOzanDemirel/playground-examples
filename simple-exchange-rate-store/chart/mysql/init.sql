CREATE DATABASE IF NOT EXISTS ExchangeRateDb;
USE ExchangeRateDb;

CREATE TABLE IF NOT EXISTS Rate
(
    id               INT                      NOT NULL AUTO_INCREMENT,
    baseCurrency     VARCHAR(5)               NOT NULL,
    quoteCurrency    VARCHAR(5)               NOT NULL,
    exchangeRateDate TIMESTAMP(6)             NOT NULL,
    exchangeRate     DOUBLE PRECISION(30, 15) NOT NULL,
    bidPrice         DOUBLE PRECISION(30, 15) NOT NULL,
    askPrice         DOUBLE PRECISION(30, 15) NOT NULL,
    createdDate      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX Rate_RateDate_BaseCurrency_QuoteCurrency ON Rate (exchangeRateDate, baseCurrency, quoteCurrency);

CREATE TABLE IF NOT EXISTS AggregatedRate
(
    id             INT                      NOT NULL AUTO_INCREMENT,
    baseCurrency   VARCHAR(5)               NOT NULL,
    quoteCurrency  VARCHAR(5)               NOT NULL,
    dataPointCount INT                      NOT NULL,
    aggregatedDate DATE                     NOT NULL,
    dailyAverage   DOUBLE PRECISION(30, 15) NOT NULL,
    dailyMax       DOUBLE PRECISION(30, 15) NOT NULL,
    dailyMin       DOUBLE PRECISION(30, 15) NOT NULL,
    monthlyAverage DOUBLE PRECISION(30, 15) NOT NULL,
    monthlyMax     DOUBLE PRECISION(30, 15) NOT NULL,
    monthlyMin     DOUBLE PRECISION(30, 15) NOT NULL,
    createdDate    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX AggregatedRate_AggregatedDate_QuoteCurrency ON AggregatedRate (aggregatedDate, quoteCurrency);

# populate some test data
INSERT INTO Rate (baseCurrency, quoteCurrency,
                  exchangeRateDate, exchangeRate,
                  bidPrice, askPrice)
VALUES ('BTC', 'EUR', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 YEAR), 26.1, 0, 0),
       ('BTC', 'CZK', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 YEAR), 26.2, 0, 0),
       ('BTC', 'EUR', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 YEAR), 26.3, 0, 0),
       ('BTC', 'CZK', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 YEAR), 26.4, 0, 0),
       ('BTC', 'EUR', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 YEAR), 26.5, 0, 0),
       ('BTC', 'CZK', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 YEAR), 26.6, 0, 0),

       ('BTC', 'EUR', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 364 DAY), 88.1, 0, 0),
       ('BTC', 'CZK', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 364 DAY), 88.2, 0, 0),
       ('BTC', 'EUR', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 11 MONTH), 93.1, 0, 0),
       ('BTC', 'CZK', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 11 MONTH), 93.2, 0, 0),
       ('BTC', 'USD', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 11 MONTH), 666.1, 0, 0),
       ('BTC', 'TRY', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 11 MONTH), 777.2, 0, 0),

       ('BTC', 'EUR', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY), 26.5, 0, 0),
       ('BTC', 'CZK', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY), 26.6, 0, 0);

INSERT INTO AggregatedRate (baseCurrency, quoteCurrency,
                            dataPointCount, aggregatedDate,
                            dailyAverage, dailyMin, dailyMax,
                            monthlyAverage, monthlyMin, monthlyMax)
VALUES ('BTC', 'EUR', 10, '2019-01-01', 99, 97, 101, 90, 97, 999),
       ('BTC', 'CZK', 10, '2019-01-01', 99, 97, 101, 90, 97, 999),
       ('BTC', 'EUR', 10, '2020-02-02', 99, 97, 101, 90, 97, 999),
       ('BTC', 'CZK', 10, '2020-02-02', 99, 97, 101, 90, 97, 999),
       ('BTC', 'EUR', 10, '2021-03-03', 99, 97, 101, 90, 97, 999),
       ('BTC', 'CZK', 10, '2021-03-03', 99, 97, 101, 90, 97, 999),

       ('BTC', 'EUR', 10, '2024-02-03', 99, 97, 101, 90, 97, 999),
       ('BTC', 'CZK', 10, '2024-02-03', 71, 70, 72, 71, 70, 72),

       ('BTC', 'EUR', 10, '2024-03-03', 49, 47, 51, 40, 7, 51),
       ('BTC', 'CZK', 10, '2024-03-03', 81, 80, 82, 81, 80, 82),

       ('BTC', 'EUR', 10, '2024-04-11', 10, 5, 15, 6, 3, 18),
       ('BTC', 'CZK', 10, '2024-04-11', 3.2, 0.9, 3.3, 3.2, 0.8, 3.4),

       ('BTC', 'EUR', 10, '2024-04-12', 1.2, 1.1, 1.3, 12, 5, 15),
       ('BTC', 'CZK', 10, '2024-04-12', 4.4, 4.2, 4.6, 5.5, 4.2, 6.5),
       ('BTC', 'TRY', 10, '2024-04-12', 30, 20, 40, 31, 19, 41),
       ('BTC', 'JPY', 10, '2024-04-12', 31, 21, 41, 32, 20, 42),
       ('BTC', 'CAD', 10, '2024-04-12', 32, 22, 42, 33, 21, 43),
       ('BTC', 'USD', 10, '2024-04-12', 33, 23, 43, 34, 22, 44),
       ('BTC', 'RUB', 10, '2024-04-12', 34, 24, 44, 35, 23, 45),
       ('BTC', 'SEK', 10, '2024-04-12', 35, 25, 45, 36, 24, 46);
