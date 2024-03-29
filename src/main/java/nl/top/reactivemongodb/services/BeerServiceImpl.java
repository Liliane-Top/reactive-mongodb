package nl.top.reactivemongodb.services;

import lombok.RequiredArgsConstructor;
import nl.top.reactivemongodb.domain.BeerStyle;
import nl.top.reactivemongodb.mapper.BeerMapper;
import nl.top.reactivemongodb.model.BeerDTO;
import nl.top.reactivemongodb.repositories.BeerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.util.StringUtils.hasText;

@Service
@RequiredArgsConstructor
public class BeerServiceImpl implements BeerService {
    private final BeerMapper beerMapper;
    private final BeerRepository beerRepository;
    private final Validator validator;

    private void validate(BeerDTO beerDTO) {
        Errors errors = new BeanPropertyBindingResult(beerDTO, "beerDTO");
        validator.validate(beerDTO, errors);
        if (errors.hasErrors()) {
            throw new ServerWebInputException(errors.toString());
        }
    }

    @Override
    public Flux<BeerDTO> listBeers() {
        return beerRepository.findAll().map(beerMapper::beerTobeerDTO);
    }

    @Override
    public Flux<BeerDTO> findByBeerStyle(BeerStyle beerStyle) {
        return beerRepository.findByBeerStyle(beerStyle).map(beerMapper::beerTobeerDTO);
    }

    @Override
    public Mono<BeerDTO> findFirstByBeerName(String beerName) {
        return beerRepository.findFirstByBeerName(beerName)
                .map(beerMapper::beerTobeerDTO);
    }

    @Override
    public Mono<BeerDTO> saveBeer(Mono<BeerDTO> beerDTO) {
        return beerDTO.map(beerMapper::beerDTOtoBeer)
                .flatMap(beerRepository::save)
                .map(beerMapper::beerTobeerDTO);
    }

    @Override
    public Mono<BeerDTO> saveBeer(BeerDTO beerDTO) {
        validate(beerDTO);
        return beerRepository.save(beerMapper.beerDTOtoBeer(beerDTO))
                .map(beerMapper::beerTobeerDTO);
    }

    @Override
    public Mono<BeerDTO> getBeerById(String beerId) {
        return beerRepository.findById(beerId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Beer with ID " + beerId + " not found")))
                .map(beerMapper::beerTobeerDTO);
    }

    @Override
    public Mono<BeerDTO> updateBeer(String beerId, BeerDTO beerDTO) {
        validate(beerDTO);
        return beerRepository.findById(beerId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Beer with ID " + beerId + " not found")))
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
    public Mono<BeerDTO> patchBeerById(String beerId, BeerDTO beerDTO) {
        validate(beerDTO);
        return beerRepository.findById(beerId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Beer with ID " + beerId + " not found")))
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
        return beerRepository.findById(beerId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(foundBeer -> beerRepository.deleteById(foundBeer.getId()));
    }
}
