package nl.top.reactivemongodb.web.fn.config;

import lombok.RequiredArgsConstructor;
import nl.top.reactivemongodb.web.fn.handlers.BeerHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class BeerRouterConfig {

    private final BeerHandler handler;
    public static final String BEER_PATH = "/api/v3/beer";
    public static final String BEER_PATH_ID = BEER_PATH + "/{beerId}";
    @Bean
    public RouterFunction<ServerResponse> beerRoutes() {
        return route()
                .GET(BEER_PATH, accept(APPLICATION_JSON), handler::getList)
                .GET(BEER_PATH_ID, accept(APPLICATION_JSON), handler::getById)
                .POST(BEER_PATH, accept(APPLICATION_JSON), handler::create)
                .PUT(BEER_PATH_ID, accept(APPLICATION_JSON), handler::updateById)
                .PATCH(BEER_PATH_ID, accept(APPLICATION_JSON), handler::patchById)
                .DELETE(BEER_PATH_ID, accept(APPLICATION_JSON), handler::deleteById)
                .build();
    }

}
