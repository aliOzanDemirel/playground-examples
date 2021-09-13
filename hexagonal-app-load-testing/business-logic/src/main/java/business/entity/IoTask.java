package business.entity;

import business.error.InvariantViolationException;

import java.util.UUID;
import java.util.function.Function;

// entity here is actually domain object not the representation of persistence layer
public class IoTask<R> {

    private static final int DEFAULT_IO_TASK_DURATION = 1000;

    private IoTask(Builder<R> builder) {
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

    public static class Builder<R> {

        // some identifier
        private final UUID id;

        // minimum 1 seconds, maximum 2 minutes
        private long durationInMillis;

        // action taken by the IO task, behaviour here simulates different types of 'blocking' simulation
        private Function<Long, R> behaviour;

        public Builder(Function<Long, R> behaviour) {

            this(UUID.randomUUID(), behaviour);
        }

        public Builder(UUID id, Function<Long, R> behaviour) {

            this.id = id;
            this.behaviour = behaviour;

            // 1 seconds by default, lowest possible duration
            durationInMillis = DEFAULT_IO_TASK_DURATION;
        }

        public Builder<R> duration(long durationInMillis) {
            this.durationInMillis = durationInMillis;
            return this;
        }

        public IoTask<R> build() {

            if (id == null) {
                throw new InvariantViolationException("ID of io task is not provided!");
            }
            if (durationInMillis < 1000 || durationInMillis > 120_000) {
                throw new InvariantViolationException("Duration should be between 1000 and 120.000!");
            }
            if (behaviour == null) {
                throw new InvariantViolationException("Behaviour of io task is not defined!");
            }
            return new IoTask<>(this);
        }
    }
}
