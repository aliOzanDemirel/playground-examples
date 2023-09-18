### Stream Combiner

* Check [homework](homework.md) for definition and requirements
    * Only 3pt library used is for xml serialization: `jackson-dataformat-xml`
* Default configuration packaged with jar will start 1 producer&consumer, emitting random xml data from infinite stream
* `Makefile` and `.local` directory are used for development, can be referred for example commands
    * Running `make coverage` will build runnable jar artifacts by running all tests and generating coverage report
    * Running `make producer` first and then `make combiner` will start 3 producers (and consumers) with test xml data
    * Can configure a specific `JAVA_HOME` in `Makefile` to be used by its commands

```shell
git clone git@github.com:aliOzanDemirel/playground-examples.git
cd simple-stream-combiner
make producer
make combiner # in another shell
```

#### producer

* Producer tasks are started as virtual threads, multiple can be run in the same java process (for simplicity)
* There can be only single consumer of a single xml data producer, data is sent to only one consumer
* There is no backpressure handling for the consumers which the data is being pushed to
* Checks to see if consumer client is already disconnected from producer socket
    * Waits for another consumer to start a new stream if an existing consumer disconnects
* Every line corresponds to single xml data, there cannot be xml data indented with multiple lines
    * Ignored capital letter in xml format example 'timeStamp'

#### combiner

* Consumer tasks are started as virtual threads
* Does not retry to connect to configured producer, simply fails startup if producer is not running
* Timeouts if producer did not send any message for some configurable amount of time
* Concurrent access to merging buffer is synchronized by a single mutex for all operations
* Xml records will be dropped in cases:
    * If merging buffer capacity is exceeded (limit is configurable)
    * If received data could not be deserialized as expected
