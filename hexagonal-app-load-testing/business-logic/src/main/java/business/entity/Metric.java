package business.entity;

public class Metric {

    public Metric(Category category, String metricName, String value) {
        this.category = category;
        this.metricName = metricName;
        this.value = value;
    }

    private final Category category;
    private final String metricName;
    private final String value;

    public enum Category {
        BLOCKING, CPU_WORK
    }

    public Category getCategory() {
        return category;
    }

    public String getMetricName() {
        return metricName;
    }

    public String getValue() {
        return value;
    }
}
