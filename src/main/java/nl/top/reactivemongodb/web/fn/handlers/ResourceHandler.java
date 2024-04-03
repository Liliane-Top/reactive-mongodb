package nl.top.reactivemongodb.web.fn.handlers;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public interface ResourceHandler<T> {
    Mono<ServerResponse> getList(ServerRequest request);
    Mono<ServerResponse> getById(ServerRequest request);
    Mono<ServerResponse> create(ServerRequest request);
    Mono<ServerResponse> updateById(ServerRequest request);
    Mono<ServerResponse> patchById(ServerRequest request);
    Mono<ServerResponse> deleteById(ServerRequest request);
}
