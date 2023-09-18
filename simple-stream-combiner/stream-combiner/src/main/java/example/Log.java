package example;

public class Log {

    private static final System.Logger LOG = System.getLogger("Stream-Combiner");

    public static void logInfo(String message, Object... args) {
        LOG.log(System.Logger.Level.INFO, logMsg(message, args));
    }

    public static void logErr(Exception e, String message, Object... args) {
        LOG.log(System.Logger.Level.ERROR, logMsg(message, args), e);
    }

    private static String logMsg(String message, Object... args) {
        long threadId = Thread.currentThread().threadId();
        String threadName = Thread.currentThread().getName();
        String formattedMsg = String.format("[%s] [%d] COMBINER -> %s", threadName, threadId, message);
        if (args != null) {
            return String.format(formattedMsg, args);
        }
        return formattedMsg;
    }
}