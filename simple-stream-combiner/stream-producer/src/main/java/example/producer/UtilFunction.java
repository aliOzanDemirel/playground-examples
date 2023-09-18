package example.producer;

import java.io.*;
import java.util.List;

import static example.Log.logErr;

public class UtilFunction {

    private UtilFunction() {
    }

    public static void simulateWait(double seconds) {
        long waitMillis = (long) (seconds * 1000);
        try {
            Thread.sleep(waitMillis);
        } catch (InterruptedException e) {
            logErr(e, "[simulate-wait] ignoring interrupted thread");
        }
    }

    public static List<String> linesFromFile(String filePath) {

        try (BufferedReader reader = fileReader(filePath)) {
            return reader.lines().toList();
        } catch (IOException e) {
            String errMsg = String.format("could not read file -> '%s'", filePath);
            throw new RuntimeException(errMsg, e);
        }
    }

    public static BufferedReader fileReader(String filePath) {

        InputStream inputStream = fileInputStream(filePath);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        return new BufferedReader(inputStreamReader);
    }

    /**
     * tries classpath first
     * then looks to find as absolute path in system
     */
    public static InputStream fileInputStream(String filePath) {

        InputStream inputStream = ClassLoader.getSystemResourceAsStream(filePath);
        if (inputStream != null) {
            return inputStream;
        }

        try {
            return new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            String errMsg = String.format("could not find file -> '%s'", filePath);
            throw new RuntimeException(errMsg, e);
        }
    }
}
