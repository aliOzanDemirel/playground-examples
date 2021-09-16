package transaction.rabbit.model;

public class ReviewAddedEvent {

    private int rating;
    private long timestamp;

    public int getRating() {
        return rating;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "ReviewAddedEvent{" +
                "rating=" + rating +
                ", timestamp=" + timestamp +
                '}';
    }
}
