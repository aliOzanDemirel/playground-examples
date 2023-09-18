# Stream Combiner

## Description

*Stream Combiner* is an application that allows you to create a
*combined stream*. This stream combines / merges entries from all (original)
individual stream producers.

## Requirements

### Stream producers

There may be 1 to N stream producers

1. Each stream producer acts as a server (opens a socket on a host:port)
2. Each stream producer sends XML data in a stream, when the stream combiner connects to it
3. Each XML record is sent in an ascending timestamp order (e.g. you may never receive a record with older timestamp
   than the last received record)
4. One stream never produces duplicate timestamps
5. The stream may not be finite (and may send millions of records over the lifetime of the application)

### Stream combiner

1. Application should be configurable to read data from N hosts:ports (Stream producers)
3. Application writes data in JSON stream.
1. The data must be written as soon as possible (e.g. when there is certainity that a record with the same timestamp
   cannot be received from any stream producer)
2. Standard output is the expected destination of the output stream.
3. Output data is sorted by timestamp
4. If several inputs provide data with the same timestamp - amounts should be merged (see data format below)
5. Amounts could be positive/negative. And it's very sensitive data - like money. And nobody likes losing money.

### Bonus points

1. Add a solution in case a stream producer hangs (does not send any data for a long time)
2. Imagine that timestamps comparing operation is VERY expensive - try to minimize it's usage

## Implementation

1. Project lifecycyle:

* You must use [Maven 3.x](http://maven.apache.org/ "Maven") to build the project.
* You must use [JUnit](http://junit.org/ "JUnit") to write tests for the project. Use of any JUnit extensions is
  allowed.

2. Definition of XML format:<br/>
   `<data> <timestamp>123456789</timeStamp> <amount>1234.567890</amount> </data>`
3. XMLs in stream are separated by new line (`\n`)
4. Definition of JSON format:<br/>
   `{ "data": { "timestamp":123456789, "amount":"1234.567890" }}`