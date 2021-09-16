package agent.influx;

public class InfluxConfig {

    private InfluxConfig() {
    }

    public static String getHost() {
        String host = System.getenv("INFLUX_HOST");
        if (host == null) {
            return "127.0.0.1";
        }
        return host;
    }

    public static String getPort() {
        String port = System.getenv("INFLUX_PORT");
        if (port == null) {
            return "8086";
        }
        return port;
    }

    public static String getUsername() {
        String username = System.getenv("INFLUX_USER");
        if (username == null) {
            return "root";
        }
        return username;
    }

    public static String getPassword() {
        String password = System.getenv("INFLUX_PASS");
        if (password == null) {
            return "root";
        }
        return password;
    }
}
