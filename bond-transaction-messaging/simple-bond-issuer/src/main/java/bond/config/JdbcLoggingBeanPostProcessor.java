package bond.config;

import com.integralblue.log4jdbc.spring.Log4jdbcBeanPostProcessor;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;
import net.sf.log4jdbc.sql.Spy;
import net.sf.log4jdbc.sql.jdbcapi.DataSourceSpy;
import org.springframework.beans.BeansException;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * this is for logging sql queries with parameters set instead of bind arguments like '?'
 * this bean will be picked up automatically by Log4jdbcAutoConfiguration.class, log levels:
 * jdbc.sqlonly: only sql logs
 * jdbc.sqltiming: sql logs with timinig statistics
 */
@Component
public class JdbcLoggingBeanPostProcessor extends Log4jdbcBeanPostProcessor {

    @AllArgsConstructor
    private static class WrappedSpyDataSource extends HikariDataSource {

        @Delegate(types = {DataSource.class, Spy.class})
        DataSourceSpy dataSourceSpy;

        @Delegate(types = HikariDataSource.class, excludes = DataSource.class)
        HikariDataSource original;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof HikariDataSource) {
            return new WrappedSpyDataSource((DataSourceSpy) super.postProcessBeforeInitialization(bean, beanName), (HikariDataSource) bean);
        } else {
            return bean;
        }
    }
}
