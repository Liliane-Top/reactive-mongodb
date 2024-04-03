package nl.top.reactivemongodb.web.fn.handlers;

import lombok.RequiredArgsConstructor;
import nl.top.reactivemongodb.domain.BeerStyle;
import nl.top.reactivemongodb.model.BeerDTO;
import nl.top.reactivemongodb.services.BeerService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static nl.top.reactivemongodb.web.fn.config.BeerRouterConfig.BEER_PATH_ID;

@Component
@RequiredArgsConstructor
public class BeerHandler implements ResourceHandler<BeerHandler> {

    private final BeerService beerService;

    @Override
    public Mono<ServerResponse> getList(ServerRequest request) {
        Flux<BeerDTO> flux;
        if (request.queryParam("beerStyle").isPresent()) {
            flux = beerService.findByBeerStyle(BeerStyle.valueOf(request.queryParam("beerStyle").get()));
        } else if (request.queryParam("beerName").isPresent()) {
            flux = beerService.findFirstByBeerName(request.queryParam("beerName").get()).flux();
        } else {
            flux = beerService.listBeers();
        }
        return ServerResponse
                .ok()
                .body(flux, BeerDTO.class);
    }

    @Override
    public Mono<ServerResponse> getById(ServerRequest request) {
        return ServerResponse
                .ok()
                .body(beerService.getBeerById(request.pathVariable("beerId")), BeerDTO.class);
    }
    @Override
    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(BeerDTO.class)
                .flatMap(beerService::saveBeer)
                .flatMap(savedBeer -> ServerResponse.created(UriComponentsBuilder
                        .fromPath(BEER_PATH_ID)
                        .build(savedBeer.getId())).build());
    }
    @Override
    public Mono<ServerResponse> updateById(ServerRequest request) {
        return request.bodyToMono(BeerDTO.class)
                .flatMap(beerDTO -> beerService.updateBeer(request.pathVariable("beerId"), beerDTO))
                .flatMap(savedDTO -> ServerResponse.noContent().build());
    }
    @Override
    public Mono<ServerResponse> patchById(ServerRequest request) {
        return request.bodyToMono(BeerDTO.class)
                .flatMap(beerDTO -> beerService.patchBeerById(request.pathVariable("beerId"), beerDTO))
                .flatMap(savedDTO -> ServerResponse.noContent().build());
    }
    @Override
    public Mono<ServerResponse> deleteById(ServerRequest request) {
        return beerService.deleteBeerById(request.pathVariable("beerId"))
                .then(ServerResponse.noContent().build());
    }
}
