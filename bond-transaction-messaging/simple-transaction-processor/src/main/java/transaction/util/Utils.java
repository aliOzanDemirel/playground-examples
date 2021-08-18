package transaction.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
public class Utils {

    private Utils() {
    }

    public static void logAndThrowException(String errMsg, Class<? extends Exception> exc) throws Exception {
        log.error(errMsg);
        throw exc.getConstructor(String.class).newInstance(errMsg);
    }

    public static long getCurrentTimeMillis() {
        return LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

}
