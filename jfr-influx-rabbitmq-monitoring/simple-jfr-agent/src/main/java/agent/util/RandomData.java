package agent.util;

import java.util.List;

public class RandomData {

    private RandomData() {
    }

    private static final List<String> DUMMY_REGIONS = List.of("eu-central-1", "eu-west-1", "us-east-1");
    private static final List<String> DUMMY_PARTITIONS = List.of("eu-deployment", "us-deployment");

    // simulated region
    public static String dummyRegion() {

        int index = (int) Math.floor(Math.random() * DUMMY_REGIONS.size());
        return DUMMY_REGIONS.get(index);
    }

    // simulated application runtime partition
    public static String dummyPartition() {

        int index = (int) Math.floor(Math.random() * DUMMY_PARTITIONS.size());
        return DUMMY_PARTITIONS.get(index);
    }
}
