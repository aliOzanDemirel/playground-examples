package clothing.service.messaging.review;

import clothing.service.domain.Review;

public class ReviewAddedTransactionEvent {

    public ReviewAddedTransactionEvent() {
    }

    public ReviewAddedTransactionEvent(Review review) {
        rating = review.getRating();
        timestamp = System.currentTimeMillis();
    }

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
        return "ReviewAddedTransactionEvent{" +
                "rating=" + rating +
                ", timestamp=" + timestamp +
                '}';
    }
}
