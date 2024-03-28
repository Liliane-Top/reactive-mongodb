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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
        Mono<BeerDTO> savedMono = beerService.saveBeer(beerDTO);

        savedMono.subscribe(savedBeer -> {
            System.out.println(savedBeer.getId());
            System.out.println(savedBeer.getBeerName());
            // assertThat(savedBeer.getBeerName().equals("Space")); //FIXME this doesn't assert anything as the combination of asserThat and equals() do not work together
            assertThat(savedBeer.getBeerName()).isEqualTo("Space Dust");//but this works fine
            assertThat(savedBeer.getId()).isNotNull();

            atomicReference.set(savedBeer);
            atomicBoolean.set(true);
        });

        await().untilTrue(atomicBoolean);

        BeerDTO persistedDTO = atomicReference.get();
        assertThat(persistedDTO).isNotNull();
        assertThat(persistedDTO.getId()).isNotNull();
        assertFalse(persistedDTO.getBeerName().equals("Lili"));
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
        BeerDTO beerToBeUpdated = getTestBeerDTO();
        BeerDTO savedBeer = beerService.saveBeer(beerToBeUpdated).block();

        beerToBeUpdated.setBeerName(newName);
        BeerDTO updatedBeer = beerService.updateBeer(savedBeer.getId(), beerToBeUpdated).block();
        assertThat(updatedBeer.getBeerName()).isEqualTo(newName);//LETOP!! equals() doesn't work with assertThat
        assertEquals(newName, updatedBeer.getBeerName());//this works fine
    }

    @Test
    @DisplayName("Test Update Beer Reactive Stream")
    void testUpdateBeerStreaming() {
        final String newName = "Heineken";
        BeerDTO beerToBeUpdated = getTestBeerDTO();
        AtomicReference<BeerDTO> atomicReference = new AtomicReference<>();
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        Mono<BeerDTO> savedMono = beerService.saveBeer(beerToBeUpdated);

        savedMono.subscribe(savedBeer -> {
            assertThat(savedBeer.getBeerName()).isEqualTo("Space Dust");
            atomicReference.set(savedBeer);
            atomicBoolean.set(true);
        });

        await().untilTrue(atomicBoolean);
        assertThat(atomicReference.get().getBeerName()).isEqualTo(beerToBeUpdated.getBeerName());

        beerToBeUpdated.setBeerName(newName);
        Mono<BeerDTO> updatedMono = beerService.updateBeer(atomicReference.get().getId(), beerToBeUpdated);
        updatedMono.subscribe(updatedBeer -> {
            assertThat(updatedBeer.getBeerName()).isEqualTo("Heineken");
            atomicReference.set(updatedBeer);
        });

        await().until(() -> atomicReference.get() != null);
        assertThat(atomicReference.get().getBeerName()).isEqualTo(newName);
    }

    @Test
    @DisplayName("Test Patch Beer Reactive Stream")
    void testPatchBeerStreaming() {
        final Integer quantity = 20;
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        BeerDTO savedBeer = getSavedBeerDTO();
        assertThat(savedBeer.getQuantityOnHand()).isEqualTo(12);
        assertFalse(savedBeer.getBeerStyle().equals(BeerStyle.PORTER));
        savedBeer.setQuantityOnHand(quantity);
        savedBeer.setBeerStyle(null);
        savedBeer.setBeerName(null);

        Mono<BeerDTO> mono = beerService.patchBeer(savedBeer.getId(), savedBeer);
        mono.subscribe(patcheddBeer -> {
            beerService.getBeerById(patcheddBeer.getId());
            assertThat(patcheddBeer.getQuantityOnHand()).isEqualTo(20);
            assertThat(patcheddBeer.getBeerName()).isEqualTo("Space Dust");
            atomicBoolean.set(true);
        });

        await().untilTrue(atomicBoolean);
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
                .subscribe(beerDto -> {
                    System.out.println(beerDto.getBeerName());
                });
    }
}