### Stream Combiner

* Check [homework](homework.md) for definition and requirements
* `Makefile` and `.local` directory are used for development, can be referred for example commands
* Running `make producer` first and then `make combiner` will start 3 producers (and consumers) with test xml data
* Default configuration packaged with jar will start 1 producer&consumer, emitting random xml data from infinite stream

```shell
git clone git@github.com:aliOzanDemirel/playground-examples.git
cd simple-stream-combiner
make producer
make combiner # in another shell
```

* Using library `jackson-dataformat-xml`, noticed later that this is not allowed
* Stream combiner handles an unnecessary case when single producer emit same timestamp consecutively, which is not a
  possible case by contract
* Would be very useful to have test suite to run both components and assert functionality with a predictable data
* There are some unit tests that do not cover socket connectivity, these tests can be improved upon to cover more

#### producer

* Multiple producers can be run with virtual threads in the same java process
* There can be only single consumer of a single xml data producer, data is sent to only one consumer
* Starts a new stream if an existing consumer disconnects
* There is no backpressure handling for the consumers which the data is being pushed to
* Every line corresponds to single xml data, there cannot be xml data indented with multiple lines
* Ignored capital letter in xml format example 'timeStamp'

#### combiner

* Does not retry to connect to configured producer, simply fails startup if producer is not running
* Timeouts if producer did not send any message for some configurable amount of time
* There is no buffer per consumer with configurable limits, data is immediately pushed to merging buffer
* Concurrent access to merging buffer is simply handled by a single mutex for adding to buffer
* Merging buffer is unbounded for simplicity
* Consumer tasks are started as virtual threads
