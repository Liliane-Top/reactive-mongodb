package nl.top.reactivemongodb.services;

import nl.top.reactivemongodb.domain.Beer;
import nl.top.reactivemongodb.domain.BeerStyle;
import nl.top.reactivemongodb.mapper.BeerMapper;
import nl.top.reactivemongodb.model.BeerDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
class BeerServiceImplTest {

    @Autowired
    BeerService beerService;

    @Autowired
    BeerMapper beerMapper;
    BeerDTO beerDTO;

    @BeforeEach
    void setUp() {
        beerDTO = beerMapper.beerTobeerDTO(getTestBeer());
    }

    private Beer getTestBeer() {
        return Beer.builder()
                .beerName("Space Dust")
                .beerStyle(BeerStyle.IPA)
                .price(BigDecimal.TEN)
                .quantityOnHand(12)
                .upc("123213")
                .build();
    }

    private BeerDTO getTestBeerDTO() {
        return beerMapper.beerTobeerDTO(getTestBeer());
    }

    private BeerDTO getSavedBeerDTO() {
        return beerService.saveBeer(Mono.just(getTestBeerDTO())).block();
    }

    @Test
    @DisplayName("Test Save Beer Using Subscriber")
    void saveBeerUseSubscriber() {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        AtomicReference<BeerDTO> atomicReference = new AtomicReference<>();
        Mono<BeerDTO> savedMono = beerService.saveBeer(Mono.just(beerDTO));

        savedMono.subscribe(savedDTO -> {
            System.out.println(savedDTO.getId());
            atomicBoolean.set(true);
            atomicReference.set(savedDTO);
        });

        await().untilTrue(atomicBoolean);

        BeerDTO persistedDTO = atomicReference.get();
        assertThat(persistedDTO).isNotNull();
        assertThat(persistedDTO.getId()).isNotNull();
    }

    @Test
    @DisplayName("Test Save Beer using Block")
    void testSaveBeerUsingBlock() {
        BeerDTO savedDTO = beerService.saveBeer(Mono.just(getTestBeerDTO())).block();
        assertThat(savedDTO).isNotNull();
        assertThat(savedDTO.getId()).isNotNull();
    }

    @Test
    @DisplayName("Test Update Beer Using Block")
    void testUpdateBeerBlocking() {
        final String newName = "Heineken";
        BeerDTO savedBeerDTO = getSavedBeerDTO();
        savedBeerDTO.setBeerName(newName);

        BeerDTO updatedDTO = beerService.saveBeer(Mono.just(savedBeerDTO)).block();

        //verify it exists in db
        BeerDTO fetchedDTO = beerService.getBeerById(updatedDTO.getId()).block();
        assertThat(fetchedDTO.getBeerName()).isEqualTo(newName);
    }

    @Test
    @DisplayName("Test Update Beer Reactive Stream")
    void testUpdateBeerStreaming() {
        final String newName = "Heineken";
        AtomicReference<BeerDTO> atomicReference = new AtomicReference<>();

        beerService.saveBeer(Mono.just(getTestBeerDTO()))
                .map(savedBeerDTO -> {
                    savedBeerDTO.setBeerName(newName);
                    return savedBeerDTO;
                })
                .flatMap(beerService::saveBeer) //save updated beer
                .flatMap(savedUpdatedBeerDTO -> beerService.getBeerById(savedUpdatedBeerDTO.getId())) // from db
                .subscribe(dtoFromDB -> {
                    atomicReference.set(dtoFromDB);
                });

        await().until(() -> atomicReference.get() != null);
        assertThat(atomicReference.get().getBeerName()).isEqualTo(newName);
    }

    @Test
    @DisplayName("Test Patch Beer Reactive Stream")
    void testPatchBeerStreaming() {
        final Integer quantity = 20;
        AtomicReference<BeerDTO> atomicReference = new AtomicReference<>();

       BeerDTO savedBeer = getSavedBeerDTO();
       savedBeer.setQuantityOnHand(quantity);

       beerService.patchBeer(savedBeer.getId(), savedBeer);

        await().until(() -> atomicReference.get() != null);

//        Mono<BeerDTO> result = beerService.patchBeer(atomicReference.get().getId(), atomicReference.get());

        assertThat(atomicReference.get().getBeerName()).isEqualTo("Space Dust");
        assertThat(atomicReference.get().getQuantityOnHand()).isEqualTo(quantity);
    }

    @Test
    void saveBeer() {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        AtomicReference<BeerDTO> atomicReference = new AtomicReference<>();
        Mono<BeerDTO> savedMono = beerService.saveBeer(Mono.just(beerDTO));
        savedMono.subscribe(savedDTO -> {
            atomicReference.set(savedDTO);
            atomicBoolean.set(true);
        });

        await().untilTrue(atomicBoolean);
        assertThat(atomicReference.get().getBeerName()).isEqualTo(beerDTO.getBeerName());
    }

    @Test
    void saveBeer2() {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        Mono<BeerDTO> savedMono = beerService.saveBeer(beerDTO);
        savedMono.subscribe(savedDTO ->
        {
            System.out.println(savedDTO.getId());
            atomicBoolean.set(true);
        });

        await().untilTrue(atomicBoolean);
    }

    @Test
    void getBeerById() {
        AtomicReference<BeerDTO> atomicReference = new AtomicReference<>();
        beerService.saveBeer(Mono.just(getTestBeerDTO()))
                .flatMap(beerService::saveBeer)
                .flatMap(saveddBeerDTO -> beerService.getBeerById(saveddBeerDTO.getId())) // from db
                .subscribe(dtoFromDB -> {
                    atomicReference.set(dtoFromDB);
                });
        await().until(() -> atomicReference.get() != null);
        assertThat(atomicReference.get().getId()).isNotNull();
    }

    @Test
    @DisplayName("Test delete beer by beerId Streaming")
    void deleteBeerByIdStreaming() {
        AtomicReference<BeerDTO> atomicReference = new AtomicReference<>();
        beerService.saveBeer(Mono.just(getTestBeerDTO()))
                .flatMap(beerService::saveBeer)
                .flatMap(saveddBeerDTO -> beerService.getBeerById(saveddBeerDTO.getId())) // from db
                .subscribe(dtoFromDB -> {
                    atomicReference.set(dtoFromDB);
                });
        await().until(() -> atomicReference.get() != null);
        assertThat(atomicReference.get().getId()).isNotNull();
    }

    @Test
    @DisplayName("Test delete beer by Id Blocking")
    void deleteBeerByIdBlocking() {
        BeerDTO beerToDelete = getSavedBeerDTO();
        beerService.deleteBeerById(beerToDelete.getId()).block();
        BeerDTO expectedEmptyBeer = beerService.getBeerById(beerToDelete.getId()).block();
        assertThat(expectedEmptyBeer).isNull();
    }

    @Test//This test is rubish it does not test anything
    @DisplayName("Test Patch Using Reactive Streams")
    void testPatch() {
        final String newName = "New Beer Name";  // use final so cannot mutate

        AtomicReference<BeerDTO> atomicDto = new AtomicReference<>();

        beerService.saveBeer(Mono.just(getTestBeerDTO()))
                .subscribe(savedDTO -> atomicDto.set(savedDTO));

        await().until(() -> atomicDto.get() != null);

        beerService.patchBeer(atomicDto.get().getId(), atomicDto.get())
                .subscribe( beerDto -> {
                    System.out.println(beerDto.getBeerName());
                });
    }
}