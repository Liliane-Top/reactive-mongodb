package nl.top.reactivemongodb.web.fn.config;

import nl.top.reactivemongodb.web.fn.handlers.ResourceHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public abstract class GenericRouterConfig <T extends ResourceHandler<?>> {
    protected String basePath;
    protected String basePathWithId;
    protected T handler;

    public GenericRouterConfig(String basePath, String basePathWithId, T handler) {
        this.basePath = basePath;
        this.basePathWithId = basePathWithId;
        this.handler = handler;
    }

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
