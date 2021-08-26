package business.entity;

import java.util.UUID;

// entity here is actually domain object not the representation of persistence layer
public class IoTask {

    private IoTask(IoTaskBuilder builder) {
        id = builder.id;
        durationInMillis = builder.durationInMillis;
    }

    // some identifier
    private final UUID id;
    private final int durationInMillis;

    public Metric getMetric() {
        return new Metric(Metric.Category.BLOCKING, "blocking_duration_ms", String.valueOf(durationInMillis));
    }

    public int getDurationInMillis() {
        return durationInMillis;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public String toString() {
        return "IoTask - id: " + id + ", durationInMillis: " + durationInMillis;
    }

    public static class IoTaskBuilder {

        // some identifier
        private final UUID id;

        // minimum 1 seconds, maximum 2 minutes
        private int durationInMillis;

        public IoTaskBuilder(UUID id) {

            this.id = id;

            // 1 seconds by default, lowest possible duration
            durationInMillis = 1000;
        }

        public IoTaskBuilder duration(int durationInMillis) {
            this.durationInMillis = durationInMillis;
            return this;
        }

        public IoTask build() {

            if (id == null || durationInMillis < 1000 || durationInMillis > 120_000) {
                throw new IllegalStateException("Cannot build as internal state is invalid!");
            }
            return new IoTask(this);
        }
    }
}
