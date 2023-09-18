package example.combiner;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * this merger is bounded buffer with capacity that combines multiple sorted streams to single sorted stream
 * items are pushed to this buffer, instead of being pulled by this merger
 * - this way the decision to write an item to a sorted stream is taken as fast as possible
 */
public class StreamMerger4 {

    public static class BufferOverCapacityException extends Exception {
        public BufferOverCapacityException(String msg) {
            super(msg);
        }
    }

    /**
     * concurrency optimization is ignored for simplicity, all operations of buffer are guarded by a single lock
     */
    private final Lock mergerLock = new ReentrantLock();

    /**
     * sorted map where all the amounts are merged per unique timestamp
     * tree is sorted so that there will be no filtering&sorting when items are flushed to be written
     */
    private SortedMap<Long, XmlData> mergedXmlDataBuffer = new TreeMap<>();

    /**
     * limit on how many records with unique timestamp can be kept
     */
    private final int mergedXmlDataBufferCapacity;

    /**
     * holds the last timestamp received per stream, last is same as 'earliest', 'latest' and 'max'
     * this is used to determine which timestamps can be written to output
     */
    private final Map<String, Long> streamToLastTimestamp = new HashMap<>();

    /**
     * used to determine if all streams started to send xml data, to decide if some timestamp is safe to be written
     */
    private final Set<String> registeredStreams = new HashSet<>();

    /**
     * where the sorted data will be written
     */
    private final JsonOutput jsonOutput;

    // NOTE: there is no validation of actual streams adding data against the registered ones
    // NOTE: can pass output stream
    // NOTE: can validate capacity
    public StreamMerger4(int mergedXmlDataBufferCapacity, JsonOutput jsonOutput) {
        this.mergedXmlDataBufferCapacity = mergedXmlDataBufferCapacity;
        this.jsonOutput = jsonOutput;
    }

    /**
     * XmlData records are buffered by sorting their timestamps in ascending order
     * records are written as json string when this buffer is flushed
     *
     * @throws BufferOverCapacityException if attempted to add record that is over the configured limit
     */
    public void add(String stream, XmlData xmlData) throws BufferOverCapacityException {
        try {
            mergerLock.lock();

            if (mergedXmlDataBuffer.size() > mergedXmlDataBufferCapacity) {
                throw new BufferOverCapacityException(String.format("buffer is over capacity: %d current size: %d",
                        mergedXmlDataBufferCapacity, mergedXmlDataBuffer.size()));
            }

            Long currentTs = xmlData.timestamp;

            // if timestamp is same, sum both amounts and use them
            // will do 'expensive timestamp compare' but only for logN times
            mergedXmlDataBuffer.merge(currentTs, xmlData, (oldValue, newValue) -> {
                newValue.amount = newValue.amount.add(oldValue.amount);
                return newValue;
            });

            // there might be another xml coming with this timestamp, so this timestamp at the head is not writable yet
            streamToLastTimestamp.put(stream, currentTs);

            if (canFlush()) {
                flush();
            }
        } finally {
            mergerLock.unlock();
        }
    }

    /**
     * max timestamp that can be written to output is the timestamp we are sure that we passed
     * meaning that there will never be another xml record coming at this and less than this timestamp
     */
    private long findMaxTimestampPushable() {

        if (streamToLastTimestamp.isEmpty()) {
            return Long.MIN_VALUE;
        }

        // this map keeps last received 'earliest' timestamps (max)
        // we should find last received 'oldest' timestamp (min) among all streams
        long minTimestampInStreamHead = Long.MAX_VALUE;
        for (long lastReceivedTimestamp : streamToLastTimestamp.values()) {

            // has 'expensive timestamp compare'
            if (lastReceivedTimestamp < minTimestampInStreamHead) {
                minTimestampInStreamHead = lastReceivedTimestamp;
            }
        }
        return minTimestampInStreamHead;
    }

    /**
     * check if all streams received at least one xml data
     * we cannot know if current timestamp can be written until we know all
     */
    private boolean canFlush() {
        return streamToLastTimestamp.size() == registeredStreams.size();
    }

    /**
     * flush writes the xml records that are old enough and then clears the buffer
     */
    private void flush() {

        // all events older than this are writable, including this timestamp -> less than or equal
        long maxPushableTimestamp = findMaxTimestampPushable();

        // negative timestamp means there is no data to be written (for some reason)
        if (maxPushableTimestamp < 0) {
            return;
        }

        // using exclusive number since headMap and tailMap argument is not inclusive
        long maxPushableTimestampExclusive = maxPushableTimestamp + 1;

        // headMap and tailMap will do 'expensive timestamp compare' but only for logN times
        // lower values (headMap) and then higher values (tailMap) will always satisfy the key ranges
        Collection<XmlData> xmlRecordsToWrite = mergedXmlDataBuffer.headMap(maxPushableTimestampExclusive).values();
        if (!xmlRecordsToWrite.isEmpty()) {
            xmlRecordsToWrite.forEach(jsonOutput::write);

            // cut out the older timestamps, only keep the ones that are not written yet
            // there will never be new records with these old timestamps
            mergedXmlDataBuffer = mergedXmlDataBuffer.tailMap(maxPushableTimestampExclusive);
        }
    }

    /**
     * flush all remaining xml data in buffer
     */
    public void flushAll() {
        try {
            mergerLock.lock();
            mergedXmlDataBuffer.values().forEach(jsonOutput::write);
        } finally {
            mergerLock.unlock();
        }
    }

    public void registerStream(String streamName) {
        try {
            mergerLock.lock();
            registeredStreams.add(streamName);
        } finally {
            mergerLock.unlock();
        }
    }

    /**
     * any xml data already buffered and belonging to this stream will be kept
     */
    public void deregisterStream(String streamName) {
        try {
            mergerLock.lock();
            streamToLastTimestamp.remove(streamName);
            registeredStreams.remove(streamName);
            if (canFlush()) {
                flush();
            }
        } finally {
            mergerLock.unlock();
        }
    }

}
