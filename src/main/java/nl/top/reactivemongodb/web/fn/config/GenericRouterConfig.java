package nl.top.reactivemongodb.web.fn.config;

import lombok.RequiredArgsConstructor;
import nl.top.reactivemongodb.web.fn.handlers.ResourceHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public abstract class GenericRouterConfig<T extends ResourceHandler<?>> {

    protected final String basePath;
    protected final String basePathWithId;
    protected final T handler;

    @Bean
    public RouterFunction<ServerResponse> routes() {
        return route()
                .GET(basePath, accept(APPLICATION_JSON), handler::getList)
                .GET(basePathWithId, accept(APPLICATION_JSON), handler::getById)
                .POST(basePath, accept(APPLICATION_JSON), handler::create)
                .PUT(basePathWithId, accept(APPLICATION_JSON), handler::updateById)
                .PATCH(basePathWithId, accept(APPLICATION_JSON), handler::patchById)
                .DELETE(basePathWithId, accept(APPLICATION_JSON), handler::deleteById)
                .build();


    }
}
