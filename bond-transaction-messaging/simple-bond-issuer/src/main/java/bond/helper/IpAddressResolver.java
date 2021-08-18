package bond.helper;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.StringTokenizer;

public class IpAddressResolver {

    private IpAddressResolver() {
    }

    private static final String FORWARDED_HEADER = "X-Forwarded-For";
    private static final String[] EXTRA_HEADERS_TO_CHECK = {
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    public static String getClientIpAddress(HttpServletRequest request) {

        var sourceIp = getIpFromForwardedForHeader(request).orElse(getIpFromExtraHeaders(request));

        if (sourceIp == null || sourceIp.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Source IP cannot be deducted!");
        }
        return sourceIp;
    }

    private static String getIpFromExtraHeaders(HttpServletRequest request) {

        for (String header : EXTRA_HEADERS_TO_CHECK) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }

    private static Optional<String> getIpFromForwardedForHeader(HttpServletRequest request) {
        String ip = request.getHeader(FORWARDED_HEADER);
        if (ip != null && ip.length() != 0) {
            return Optional.of(new StringTokenizer(ip, ",").nextToken().trim());
        }
        return Optional.empty();
    }

}
