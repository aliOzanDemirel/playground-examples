package agent.jfr;

public class AgentConfig {

    private AgentConfig() {
    }

    public static boolean isAttachedToClothingService() {
        String isEnabled = System.getProperty("jfr.enabled.clothing");
        return Boolean.parseBoolean(isEnabled);
    }

    public static boolean isAttachedToBondIssuer() {
        String isEnabled = System.getProperty("jfr.enabled.bond");
        return Boolean.parseBoolean(isEnabled);
    }
}
