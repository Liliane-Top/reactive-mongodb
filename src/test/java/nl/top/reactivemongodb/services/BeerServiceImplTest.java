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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        beerDTO = beerMapper.beerToBeerDTO(getTestBeer());
    }

    public static Beer getTestBeer() {
        return Beer.builder()
                .beerName("Space Dust")
                .beerStyle(BeerStyle.IPA)
                .price(BigDecimal.TEN)
                .quantityOnHand(12)
                .upc("123213")
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build();
    }

    public BeerDTO getTestBeerDTO() {
        return beerMapper.beerToBeerDTO(getTestBeer());
    }

    public BeerDTO getSavedBeerDTO() {

        return beerService.saveBeer(Mono.just(getTestBeerDTO())).block();
    }

    @Test
    @DisplayName("Test find the first beer by beerName using subscribe")
    void findFirstByBeerName() {
        BeerDTO beerDTO1 = getSavedBeerDTO();
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        Mono<BeerDTO> foundDTO = beerService.findFirstByBeerName(beerDTO1.getBeerName());

        foundDTO.subscribe(foundBeer -> {
                    assertThat(foundBeer.getBeerStyle()).isEqualTo(BeerStyle.IPA);
                    atomicBoolean.set(true);
                }
        );
        await().atMost(Duration.ofSeconds(5)).untilTrue(atomicBoolean);
    }

    @Test
    @DisplayName("Test find all beers by a certain Beerstyle")
    void findAllByBeerStyle() {
        BeerDTO beerDTO1 = getSavedBeerDTO();
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        beerService.findByBeerStyle(beerDTO1.getBeerStyle())
                .subscribe(dto ->
                {
                    assertThat(dto.getBeerStyle()).isEqualTo(BeerStyle.IPA);
                    atomicBoolean.set(true);
                });

        await().atMost(Duration.ofSeconds(5)).untilTrue(atomicBoolean);
    }

    @Test
    @DisplayName("Test get beer by beerId using subscribe")
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
    @DisplayName("Test save beer using subscribe")
    void saveBeerUseSubscriber() {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        AtomicReference<BeerDTO> atomicReference = new AtomicReference<>();
        Mono<BeerDTO> savedMono = beerService.saveBeer(beerDTO);

        savedMono.subscribe(savedBeer -> {
            assertThat(savedBeer.getBeerName()).isEqualTo("Space Dust");
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
    @DisplayName("Test save beer with subscribe without atomicReference")
    void saveBeer2() {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        Mono<BeerDTO> savedMono = beerService.saveBeer(beerDTO);

        savedMono.subscribe(savedDTO ->
        {
            assertThat(savedDTO.getQuantityOnHand()).isEqualTo(12);
            atomicBoolean.set(true);
        });

        await().atMost(Duration.ofSeconds(5)).untilTrue(atomicBoolean);
    }

    @Test
    @DisplayName("Test save Beer using block")
    void testSaveBeerUsingBlock() {
        BeerDTO savedDTO = beerService.saveBeer(Mono.just(getTestBeerDTO())).block();
        assertThat(savedDTO).isNotNull();
        assertThat(savedDTO.getId()).isNotNull();
    }

    @Test
    @DisplayName("Test update Beer using subscribe")
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
    @DisplayName("Test update Beer Using block")
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
    @DisplayName("Test delete beer by Id using subscribe")
    void deleteBeerByIdStreaming() {
        AtomicBoolean completed = new AtomicBoolean(false);
        BeerDTO testBeer = getSavedBeerDTO();

        beerService.getBeerById(testBeer.getId())
                .flatMap(foundBeer -> beerService.deleteBeerById(foundBeer.getId()))
                .doOnSuccess(voidResult -> assertThatThrownBy(() ->
                        beerService.getBeerById(testBeer.getId()).block())
                        .isInstanceOf(ResponseStatusException.class)
                        .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
                        .hasMessageContaining("Beer with ID " + testBeer.getId() + " not found"))
                .doFinally(signal -> completed.set(true))
                .subscribe();

        await().atMost(Duration.ofSeconds(5)).untilTrue(completed);
    }

    @Test
    @DisplayName("Test delete beer by Id using Block")
    void deleteBeerByIdBlocking() {
        BeerDTO beerToDelete = getSavedBeerDTO();

        assertThatThrownBy(() -> {
            beerService.getBeerById(beerToDelete.getId())
                    .flatMap(foundBeer -> beerService.deleteBeerById(foundBeer.getId()))
                    .then(Mono.defer(() -> beerService.getBeerById(beerToDelete.getId()))).block();
                })
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
                .hasMessageContaining("Beer with ID " + beerToDelete.getId() + " not found");
    }

    @Test
    @DisplayName("Test Patch Beer subscribe")
    void testPatchBeerStreaming() {
        final Integer quantity = 20;
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        BeerDTO savedBeer = getSavedBeerDTO();
        assertThat(savedBeer.getQuantityOnHand()).isEqualTo(12);
        assertFalse(savedBeer.getBeerStyle().equals(BeerStyle.PORTER));
        savedBeer.setQuantityOnHand(quantity);
        savedBeer.setBeerStyle(null);

        Mono<BeerDTO> mono = beerService.patchBeerById(savedBeer.getId(), savedBeer);
        mono.subscribe(patcheddBeer -> {
            beerService.getBeerById(patcheddBeer.getId());
            assertThat(patcheddBeer.getQuantityOnHand()).isEqualTo(20);
            assertThat(patcheddBeer.getBeerName()).isEqualTo("Space Dust");
            atomicBoolean.set(true);
        });

        await().untilTrue(atomicBoolean);
    }

    @Test
    @DisplayName("Test patch beer using block")
    void testPatch() {
        final String newName = "New Beer Name";
        BeerDTO testBeer = getSavedBeerDTO();
        testBeer.setBeerName(newName);

        BeerDTO updatedBeer = beerService.patchBeerById(testBeer.getId(), testBeer).block();

        assertThat(updatedBeer.getBeerName()).isEqualTo(newName);
    }
}