package business.entity;

import java.util.UUID;
import java.util.function.Function;

// entity here is actually domain object not the representation of persistence layer
public class IoTask<R> {

    private IoTask(IoTaskBuilder<R> builder) {
        id = builder.id;
        durationInMillis = builder.durationInMillis;
        behaviour = builder.behaviour;
    }

    // some identifier
    private final UUID id;
    private final long durationInMillis;
    private final Function<Long, R> behaviour;

    public Metric getMetric() {
        return new Metric(Metric.Category.IO, "io_duration_ms", String.valueOf(durationInMillis));
    }

    public long getDurationInMillis() {
        return durationInMillis;
    }

    public UUID getId() {
        return id;
    }

    public R execute() {
        return behaviour.apply(getDurationInMillis());
    }

    // default behaviour is to block the OS thread
    public static Function<Long, Boolean> defaultBlockingBehaviour() {
        return (Long durationInMillis) -> {
            try {
                Thread.sleep(durationInMillis);
                return true;
            } catch (InterruptedException e) {
                return false;
            }
        };
    }

    @Override
    public String toString() {
        return "IoTask - id: " + id + ", durationInMillis: " + durationInMillis + ", behaviour hash: " + behaviour.hashCode();
    }

    public static class IoTaskBuilder<R> {

        // some identifier
        private final UUID id;

        // minimum 1 seconds, maximum 2 minutes
        private long durationInMillis;

        // action taken by the IO task
        private Function<Long, R> behaviour;

        public IoTaskBuilder(UUID id, Function<Long, R> behaviour) {

            this.id = id;
            this.behaviour = behaviour;

            // 1 seconds by default, lowest possible duration
            durationInMillis = 1000;
        }

        public IoTaskBuilder<R> duration(long durationInMillis) {
            this.durationInMillis = durationInMillis;
            return this;
        }

        public IoTask<R> build() {

            if (id == null
                    || durationInMillis < 1000 || durationInMillis > 120_000
                    || behaviour == null) {
                throw new IllegalStateException("Cannot build as internal state is invalid!");
            }
            return new IoTask<>(this);
        }
    }
}
