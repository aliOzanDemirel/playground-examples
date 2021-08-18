package bond;

import com.integralblue.log4jdbc.spring.Log4jdbcAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {Log4jdbcAutoConfiguration.class})
public class Main {
    public static void main(String... args) {
        SpringApplication.run(Main.class, args);
    }
}
