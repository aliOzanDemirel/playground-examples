package example.producer;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public class RandomDataProvider implements XmlDataProvider {

    private static final String XML_FORMAT = "<data> <timestamp>%d</timestamp> <amount>%.6f</amount> </data>";

    @Override
    public Stream<String> streamXmlData() {
        DoubleStream randomDoubleStream = ThreadLocalRandom.current().doubles();
        return streamXmlData(randomDoubleStream);
    }

    Stream<String> streamXmlData(DoubleStream doubleStream) {
        return doubleStream.mapToObj(this::xmlData);
    }

    // keeping it simple without passing timestamp or a non-default clock
    private String xmlData(double amount) {
        long timestamp = Instant.now().getEpochSecond();
        String xmlData = String.format(XML_FORMAT, timestamp, amount);
        double waitAtLeastOneSecond = 1 + amount;
        UtilFunction.simulateWait(waitAtLeastOneSecond);
        return xmlData;
    }
}
