package nl.top.reactivemongodb.services;

import nl.top.reactivemongodb.domain.BeerStyle;
import nl.top.reactivemongodb.model.BeerDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BeerService {
    Flux<BeerDTO> listBeers();
    Flux<BeerDTO> findByBeerStyle(BeerStyle beerStyle);

    Mono<BeerDTO> findFirstByBeerName(String beerName);

    Mono<BeerDTO> saveBeer(Mono<BeerDTO> beerDTO);

    Mono<BeerDTO> saveBeer(BeerDTO beerDTO);

    Mono<BeerDTO> getBeerById(String beerId);

    Mono<BeerDTO> updateBeer(String beerId, BeerDTO beerDTO);

    Mono<BeerDTO> patchBeerById(String beerId, BeerDTO beerDTO);

    Mono<Void> deleteBeerById(String beerId);
}
