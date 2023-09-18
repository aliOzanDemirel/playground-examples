package example.producer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.DoubleStream;

public class XmlDataProviderTest {

    @Test
    void testRandomXmlData() {

        RandomDataProvider provider = new RandomDataProvider();

        DoubleStream testStream = DoubleStream.of(0.1, 0.2, 0.15);
        Instant start = Instant.now();
        List<String> result = provider.streamXmlData(testStream).toList();
        Instant end = Instant.now();

        int expectedXmlDataCount = 3;
        Assertions.assertEquals(expectedXmlDataCount, result.size(),
                "unexpected amount of xml datas read from file");

        Duration timeElapsed = Duration.between(start, end);
        long expectedMinWaitMillis = (long) ((0.1 + 0.2 + 0.15) * 1000);
        Assertions.assertTrue(timeElapsed.toMillis() > expectedMinWaitMillis,
                "unexpected minimum duration of consuming the stream");

        String[] parts_0 = result.get(0).split("<timestamp>.*</timestamp>");
        Assertions.assertEquals("<data> ", parts_0[0], "mismatching start of xml data");
        Assertions.assertEquals(" <amount>0.100000</amount> </data>", parts_0[1], "mismatching end of xml data");

        String[] parts_1 = result.get(1).split("<timestamp>.*</timestamp>");
        Assertions.assertEquals("<data> ", parts_1[0], "mismatching start of xml data");
        Assertions.assertEquals(" <amount>0.200000</amount> </data>", parts_1[1], "mismatching end of xml data");

        String[] parts_2 = result.get(2).split("<timestamp>.*</timestamp>");
        Assertions.assertEquals("<data> ", parts_2[0], "mismatching start of xml data");
        Assertions.assertEquals(" <amount>0.150000</amount> </data>", parts_2[1], "mismatching end of xml data");
    }

    @Test
    void testFileXmlData() {

        String fileName = "test_stream.txt";
        FileDataProvider provider = new FileDataProvider(fileName);

        Instant start = Instant.now();
        List<String> result = provider.streamXmlData().toList();
        Instant end = Instant.now();

        int expectedXmlDataCount = 4;
        Assertions.assertEquals(expectedXmlDataCount, result.size(),
                "unexpected amount of xml datas read from file");

        Duration timeElapsed = Duration.between(start, end);
        long expectedMinWaitMillis = (long) ((0.5 * expectedXmlDataCount) * 1000);
        Assertions.assertTrue(timeElapsed.toMillis() > expectedMinWaitMillis,
                "unexpected minimum duration of consuming the stream");

        Assertions.assertEquals("<data> <timestamp>100</timestamp> <amount>3</amount> </data>",
                result.get(0), "mismatching xml data");
        Assertions.assertEquals("<data> <timestamp>101</timestamp> <amount>1.001</amount> </data>",
                result.get(1), "mismatching xml data");
        Assertions.assertEquals("<data> <timestamp>102</timestamp> <amount>88888.000008</amount> </data>",
                result.get(2), "mismatching xml data");
        Assertions.assertEquals("<data> <timestamp>103</timestamp> <amount>7.700000</amount> </data>",
                result.get(3), "mismatching xml data");
    }
}
