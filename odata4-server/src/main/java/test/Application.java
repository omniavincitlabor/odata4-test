package test;


import com.sdl.odata.controller.ODataController;
import com.sdl.odata.service.ODataServiceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;

/**
 *
 */
@SpringBootApplication(exclude = {
        MongoAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class}
)
@Import({
        ODataServiceConfiguration.class,
        ODataController.class,
        TestDataSourceConfiguration.class,
        TestController.class
})
public class Application extends SpringBootServletInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    public static void main(final String[] args) {
        LOG.info("Starting Example Service Application container");

        SpringApplication.run(Application.class, args);

        LOG.info("Example Service application container started");
    }
}