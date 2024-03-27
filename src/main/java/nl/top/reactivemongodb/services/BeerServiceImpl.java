package nl.top.reactivemongodb.services;

import lombok.RequiredArgsConstructor;
import nl.top.reactivemongodb.mapper.BeerMapper;
import nl.top.reactivemongodb.model.BeerDTO;
import nl.top.reactivemongodb.repositories.BeerRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.util.StringUtils.hasText;

@Service
@RequiredArgsConstructor
public class BeerServiceImpl implements BeerService {
    private final BeerMapper beerMapper;
    private final BeerRepository beerRepository;

    @Override
    public Flux<BeerDTO> listBeers() {
        return beerRepository.findAll().map(beerMapper::beerTobeerDTO);
    }

    @Override
    public Mono<BeerDTO> saveBeer(Mono<BeerDTO> beerDTO) {
        return beerDTO.map(beerMapper::beerDTOtoBeer)
                .flatMap(beerRepository::save)
                .map(beerMapper::beerTobeerDTO);
    }

    @Override
    public Mono<BeerDTO> saveBeer(BeerDTO beerDTO) {
        return beerRepository.save(beerMapper.beerDTOtoBeer(beerDTO))
                .map(beerMapper::beerTobeerDTO);
    }

    @Override
    public Mono<BeerDTO> getBeerById(String beerId) {
        return beerRepository.findById(beerId).map(beerMapper::beerTobeerDTO);
    }

    @Override
    public Mono<BeerDTO> updateBeer(String beerId, BeerDTO beerDTO) {
        return beerRepository.findById(beerId)
                .map(foundBeer -> {
                    foundBeer.setBeerName(beerDTO.getBeerName());
                    foundBeer.setBeerStyle(beerDTO.getBeerStyle());
                    foundBeer.setPrice(beerDTO.getPrice());
                    foundBeer.setUpc(beerDTO.getUpc());
                    foundBeer.setQuantityOnHand(beerDTO.getQuantityOnHand());
                    return foundBeer;
                })
                .flatMap(beerRepository::save)
                .map(beerMapper::beerTobeerDTO);
    }

    @Override
    public Mono<BeerDTO> patchBeer(String beerId, BeerDTO beerDTO) {

        return beerRepository.findById(beerId)
                .map(foundBeer -> {
                    if (hasText(beerDTO.getBeerName())) {
                        foundBeer.setBeerName(beerDTO.getBeerName());
                    }
                    if (beerDTO.getBeerStyle() != null) {
                        foundBeer.setBeerStyle(beerDTO.getBeerStyle());
                    }
                    if (beerDTO.getPrice() != null) {
                        foundBeer.setPrice(beerDTO.getPrice());
                    }
                    if (hasText(beerDTO.getUpc())) {
                        foundBeer.setUpc(beerDTO.getUpc());
                    }
                    if (beerDTO.getQuantityOnHand() != null) {
                        foundBeer.setQuantityOnHand(beerDTO.getQuantityOnHand());
                    }
                    return foundBeer;
                }).flatMap(beerRepository::save)
                .map(beerMapper::beerTobeerDTO);
    }

    @Override
    public Mono<Void> deleteBeerById(String beerId) {
        return beerRepository.deleteById(beerId);
    }
}
