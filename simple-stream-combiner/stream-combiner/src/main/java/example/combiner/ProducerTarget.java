package example.combiner;

public class ProducerTarget {
    private final String host;
    private final int port;

    public ProducerTarget(String producer) throws Exception {

        String[] parts = producer.split(":");
        if (parts.length != 2) {
            throw new Exception(String.format("expected to find host:port parts in '%s'", producer));
        }

        host = parts[0];
        if (host.isBlank()) {
            throw new Exception("blank host is invalid");
        }

        try {
            port = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            throw new Exception(String.format("port is not a number -> %s", parts[1]), e);
        }

        if (port < 1024 || port > 65535) {
            throw new Exception(String.format("invalid port -> %d", port));
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return host + ':' + port;
    }
}