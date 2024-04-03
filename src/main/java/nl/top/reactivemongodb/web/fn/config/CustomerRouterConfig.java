package nl.top.reactivemongodb.web.fn.config;

import lombok.RequiredArgsConstructor;
import nl.top.reactivemongodb.web.fn.handlers.CustomerHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class CustomerRouterConfig {

    public static final String CUSTOMER_PATH = "/api/v3/customer";
    public static final String CUSTOMER_PATH_ID = CUSTOMER_PATH + "/{customerId}";
    private final CustomerHandler customerHandler;

    @Bean
    public RouterFunction<ServerResponse> customerRoutes() {
        return route()
                .GET(CUSTOMER_PATH, accept(APPLICATION_JSON), customerHandler::getList)
                .GET(CUSTOMER_PATH_ID, accept(APPLICATION_JSON), customerHandler::getById)
                .POST(CUSTOMER_PATH, accept(APPLICATION_JSON), customerHandler::create)
                .PUT(CUSTOMER_PATH_ID, accept(APPLICATION_JSON), customerHandler::updateById)
                .PATCH(CUSTOMER_PATH_ID, accept(APPLICATION_JSON), customerHandler::patchById)
                .DELETE(CUSTOMER_PATH_ID, accept(APPLICATION_JSON), customerHandler::deleteById)
                .build();
    }
}
