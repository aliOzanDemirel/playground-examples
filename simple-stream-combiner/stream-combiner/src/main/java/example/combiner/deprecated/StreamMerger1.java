package example.combiner.deprecated;

import example.combiner.JsonOutput;
import example.combiner.XmlData;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;


/**
 * written first to have something working (almost), although it does not fulfill the contract fully:
 * -- The data must be written as soon as possible
 * -- (e.g. when there is certainity that a record with the same timestamp cannot be received from any stream producer)
 */
@Deprecated
public class StreamMerger1 {

    private final Lock mergerLock = new ReentrantLock();
    private final Map<Long, XmlData> mergedXmlDataBuffer = new ConcurrentHashMap<>();

    // this basically means the last timestamp received for a given consumer
    private final Map<String, Long> consumerToEarliestTimestamp = new HashMap<>();
    private final JsonOutput jsonOutput;
    private final AtomicInteger totalExpectedConsumers;


    public StreamMerger1(JsonOutput jsonOutput, int totalExpectedConsumers) {
        this.jsonOutput = jsonOutput;
        this.totalExpectedConsumers = new AtomicInteger(totalExpectedConsumers);
    }

    public void add(String consumerName, XmlData xmlData) {
        try {
            mergerLock.lock();

            Long currentTs = xmlData.timestamp;

            // if timestamp is same, sum both amounts and use them
            BiFunction<XmlData, XmlData, XmlData> mergingFunc = (oldValue, newValue) -> {
                newValue.amount = newValue.amount.add(oldValue.amount);
                return newValue;
            };
            mergedXmlDataBuffer.merge(currentTs, xmlData, mergingFunc);

            // use previous timestamp to check if some xml can be written to output
            // we cannot write the previous xml if current timestamp is equal (cannot be less really)
            // there might be another xml coming with this timestamp
            boolean canWrite = false;
            Long previousTs = consumerToEarliestTimestamp.put(consumerName, currentTs);

            // check if current timestamp ahead of the previous timestamp for this consumer
            boolean currentConsumerTimestampIsNotSameAsPrevious = previousTs != null && currentTs > previousTs;
            boolean allConsumersPushed = consumerToEarliestTimestamp.size() == totalExpectedConsumers.get();
            if (currentConsumerTimestampIsNotSameAsPrevious && allConsumersPushed) {

                // can write previous timestamp data if there was only this consumer
                canWrite = true;

                // check if timestamp is older than all other consumers
                for (String key : consumerToEarliestTimestamp.keySet()) {

                    // check only for other consumers, skip current consumer
                    if (!key.equals(consumerName)) {

                        Long lastTimestampOfOther = consumerToEarliestTimestamp.get(key);

                        // we cannot write this data if any consumer's latest timestamp is not ahead of current
                        if (previousTs >= lastTimestampOfOther) {
                            canWrite = false;
                            break;
                        }
                    }
                }
            }

            if (canWrite) {
                long maxWritableTimestamp = previousTs;
                mergedXmlDataBuffer.keySet()
                        .stream()
                        .filter(it -> it <= maxWritableTimestamp)
                        .sorted()
                        .forEach(it -> {
                            XmlData xmlDataThatCanBeSent = mergedXmlDataBuffer.remove(it);
                            jsonOutput.write(xmlDataThatCanBeSent);
                        });
            }
        } finally {
            mergerLock.unlock();
        }
    }

    // flush all remaining xml data in buffer
    public void shutdown() {
        try {
            mergerLock.lock();

            mergedXmlDataBuffer.keySet()
                    .stream()
                    .sorted()
                    .forEach(it -> {
                        XmlData xmlDataThatCanBeSent = mergedXmlDataBuffer.remove(it);
                        jsonOutput.write(xmlDataThatCanBeSent);
                    });

        } finally {
            mergerLock.unlock();
        }
    }

    public void decreaseConsumerCount() {
        totalExpectedConsumers.getAndDecrement();
    }
}
