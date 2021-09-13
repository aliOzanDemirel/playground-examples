package business;

import java.util.UUID;

public class Util {

    private Util() {
    }

    public static String prepareHugeString(int bound) {

        String param = UUID.randomUUID().toString();
        while (true) {
            String temp = param + UUID.randomUUID();
            if (temp.length() < bound) {
                param = temp;
            } else {
                break;
            }
        }
        return param;
    }
}
