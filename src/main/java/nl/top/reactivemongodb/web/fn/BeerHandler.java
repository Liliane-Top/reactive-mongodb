package nl.top.reactivemongodb.web.fn;

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

import static nl.top.reactivemongodb.web.fn.BeerRouterConfig.BEER_PATH_ID;

@Component
@RequiredArgsConstructor
public class BeerHandler {

    private final BeerService beerService;

    public Mono<ServerResponse> listBeers(ServerRequest request) {
        Flux<BeerDTO> flux;
        if(request.queryParam("beerStyle").isPresent()) {
            flux = beerService.findByBeerStyle(BeerStyle.valueOf(request.queryParam("beerStyle").get()));
        } else  {
            flux =beerService.listBeers();
        }
        return ServerResponse
                .ok()
                .body(flux, BeerDTO.class);
    }

    public Mono<ServerResponse> getBeerById(ServerRequest request) {
        return ServerResponse
                .ok()
                .body(beerService.getBeerById(request.pathVariable("beerId")), BeerDTO.class);
    }

    public Mono<ServerResponse> createNewBeer(ServerRequest request) {
        return request.bodyToMono(BeerDTO.class)
                .flatMap(beerDTO -> beerService.saveBeer(beerDTO))
                .flatMap(savedBeer -> ServerResponse.created(UriComponentsBuilder
                        .fromPath(BEER_PATH_ID)
                        .build(savedBeer.getId())).build());
    }

    public Mono<ServerResponse> updateBeerById(ServerRequest request) {
        return request.bodyToMono(BeerDTO.class)
                .flatMap(beerDTO -> beerService.updateBeer(request.pathVariable("beerId"), beerDTO))
                .flatMap(savedDTO -> ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> patchBeerById(ServerRequest request) {
        return request.bodyToMono(BeerDTO.class)
                .flatMap(beerDTO -> beerService.patchBeerById(request.pathVariable("beerId"), beerDTO))
                .flatMap(savedDTO -> ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> deleteBeerById(ServerRequest request) {
        return beerService.deleteBeerById(request.pathVariable("beerId"))
                .then(ServerResponse.noContent().build());
    }
}
