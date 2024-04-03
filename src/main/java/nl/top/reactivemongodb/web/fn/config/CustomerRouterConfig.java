package nl.top.reactivemongodb.web.fn.config;

import nl.top.reactivemongodb.web.fn.handlers.CustomerHandler;
import nl.top.reactivemongodb.web.fn.handlers.ResourceHandler;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerRouterConfig extends GenericRouterConfig<ResourceHandler<CustomerHandler>> {

    public static final String CUSTOMER_PATH = "/api/v3/customer";
    public static final String CUSTOMER_PATH_ID = CUSTOMER_PATH + "/{customerId}";

    public CustomerRouterConfig(CustomerHandler handler) {
        super(CUSTOMER_PATH, CUSTOMER_PATH_ID, handler);
    }
}
