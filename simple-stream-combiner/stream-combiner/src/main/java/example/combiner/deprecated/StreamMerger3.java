package example.combiner.deprecated;

import example.combiner.JsonOutput;
import example.combiner.XmlData;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;

/**
 * this implementation handles an unnecessary case that is against the producer contract:
 * - normally same producer can never generate duplicate timestamps
 * - this merger expects that same producer can send same timestamp consecutively [3 -> 3 -> 3 ...]
 */
@Deprecated
public class StreamMerger3 {

    /**
     * concurrency optimization is ignored for simplicity, all operations of buffer are guarded by a single lock
     */
    private final Lock mergerLock = new ReentrantLock();

    /**
     * has an unbounded buffer of sorted map, where all the amounts are merged per unique timestamp
     * tree is sorted so that there will be no filtering&sorting when items are flushed to be written
     */
    private SortedMap<Long, XmlData> mergedXmlDataBuffer = new TreeMap<>();

    /**
     * keep the latest timestamp received per consumer, this is used to determine which timestamps can be written to output stream
     */
    private final Map<String, Long> consumerToLastTimestamp = new HashMap<>();

    /**
     * used to determine if all consumers started to send xml data, to decide if some timestamp is safe to be written
     */
    private final int totalExpectedConsumers;

    private final JsonOutput jsonOutput;

    public StreamMerger3(JsonOutput jsonOutput, int totalExpectedConsumers) {
        this.jsonOutput = jsonOutput;
        this.totalExpectedConsumers = totalExpectedConsumers;
    }

    public void add(String consumerName, XmlData xmlData) {
        try {
            mergerLock.lock();

            // if timestamp is same, sum both amounts and use them
            BiFunction<XmlData, XmlData, XmlData> mergingFunc = (oldValue, newValue) -> {
                newValue.amount = newValue.amount.add(oldValue.amount);
                return newValue;
            };

            Long currentTs = xmlData.timestamp;

            // has expensive timestamp compare
            mergedXmlDataBuffer.merge(currentTs, xmlData, mergingFunc);

            // there might be another xml coming with this timestamp, so this timestamp at the head is not writable yet
            consumerToLastTimestamp.put(consumerName, currentTs);

            // check if all consumers received at least one xml data
            // we cannot know which timestamp can be written until this
            boolean allConsumersPushed = consumerToLastTimestamp.size() == totalExpectedConsumers;
            if (!allConsumersPushed) {
                return;
            }

            // all events older than this are writable, but this particular timestamp is not writable
            long minLastTimestamp = Long.MAX_VALUE;

            // consumerToLastTimestamp keeps last received 'earliest' timestamps
            // here we should find last received 'oldest' timestamp of all consumers
            for (String key : consumerToLastTimestamp.keySet()) {
                Long lastTimestamp = consumerToLastTimestamp.get(key);

                // has expensive timestamp compare
                if (lastTimestamp < minLastTimestamp) {
                    minLastTimestamp = lastTimestamp;
                }
            }

            // max timestamp that can be written is minLastTimestamp - 1
            mergedXmlDataBuffer.headMap(minLastTimestamp)
                    .values()
                    .forEach(jsonOutput::write);

            // cut out the older timestamps, only keep the ones that are not written yet
            mergedXmlDataBuffer = mergedXmlDataBuffer.tailMap(minLastTimestamp);
        } finally {
            mergerLock.unlock();
        }
    }

    // flush all remaining xml data in buffer
    public void flush() {
        try {
            mergerLock.lock();

            mergedXmlDataBuffer.values().forEach(jsonOutput::write);
        } finally {
            mergerLock.unlock();
        }
    }
}
