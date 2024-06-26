package nl.top.reactivemongodb.web.fn;

import nl.top.reactivemongodb.domain.BeerStyle;
import nl.top.reactivemongodb.model.BeerDTO;
import nl.top.reactivemongodb.services.BeerService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static nl.top.reactivemongodb.web.fn.config.BeerRouterConfig.BEER_PATH;
import static nl.top.reactivemongodb.web.fn.config.BeerRouterConfig.BEER_PATH_ID;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOAuth2Login;

@SpringBootTest
@AutoConfigureWebTestClient
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BeerHandlerTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    BeerService beerService;

    public BeerDTO getSavedBeerDTO() {
        return beerService.saveBeer(Mono.just(getTestBeer())).block();
    }

    private static BeerDTO getTestBeer() {
        return BeerDTO.builder()
                .beerName("Space Dust")
                .beerStyle(BeerStyle.IPA)
                .price(new BigDecimal(10))
                .quantityOnHand(256)
                .upc("ipa")
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("Test get list of beers")
    void listBeer() {
        webTestClient
                .mutateWith(mockOAuth2Login())
                .get().uri(BEER_PATH)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-type", "application/json")
                .expectBody().jsonPath("$.size()").value(greaterThan(2));
    }

    @Test
    void findAllByBeerStyle() {
        final BeerStyle beerStyle = BeerStyle.PILSNER;
        BeerDTO testBeer = getSavedBeerDTO();
        testBeer.setBeerStyle(beerStyle);

        webTestClient.mutateWith(mockOAuth2Login())
                .post().uri(BEER_PATH).body(Mono.just(testBeer), BeerDTO.class)
                .header("Content-Type", "application/json")
                .exchange();

        webTestClient.mutateWith(mockOAuth2Login())
                .get().uri(UriComponentsBuilder
                        .fromPath(BEER_PATH)
                        .queryParam("beerStyle", BeerStyle.PILSNER).build().toUri())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "application/json")
                .expectBody().jsonPath("$.size()").value(equalTo(1));
    }

    @Test
    void findFirstByBeerName() {

        webTestClient.mutateWith(mockOAuth2Login())
                .get().uri(UriComponentsBuilder
                        .fromPath(BEER_PATH)
                        .queryParam("beerName", "Crank").build().toUri())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "application/json")
                .expectBody().jsonPath("$.size()").value(equalTo(1));

    }

    @Test
    @Disabled
    @Order(99)
    void emptyListBeer() {
        webTestClient.mutateWith(mockOAuth2Login())
                .delete()
                .uri(BEER_PATH_ID, 1)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isNoContent();
        webTestClient.mutateWith(mockOAuth2Login())
                .delete()
                .uri(BEER_PATH_ID, 2/**/)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isNoContent();
        webTestClient.mutateWith(mockOAuth2Login())
                .delete()
                .uri(BEER_PATH_ID, 4)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isNoContent();
        webTestClient.mutateWith(mockOAuth2Login())
                .get().uri(BEER_PATH)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(2)
    void getBeerById() {
        BeerDTO testBeer = getSavedBeerDTO();
        webTestClient.mutateWith(mockOAuth2Login())
                .get().uri(BEER_PATH_ID, testBeer.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-type", "application/json")
                .expectBody(BeerDTO.class);
    }

    @Test
    @Order(3)
    void getBeerByNonExistingId() {
        webTestClient.mutateWith(mockOAuth2Login())
                .get().uri(BEER_PATH_ID, 10)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(4)
    void createNewBeer() {
        webTestClient.mutateWith(mockOAuth2Login())
                .post().uri(BEER_PATH)
                .body(Mono.just(getTestBeer()), BeerDTO.class)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("location");
    }

    @Test
    @Order(5)
    void createNewBeerWithBadData() {
        BeerDTO testBeer = getTestBeer();
        testBeer.setBeerName("");

        webTestClient.mutateWith(mockOAuth2Login())
                .post().uri(BEER_PATH)
                .body(Mono.just(testBeer), BeerDTO.class)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @Order(6)
    void updateBeer() {
        BeerDTO testBeer = getSavedBeerDTO();
        testBeer.setBeerName("Heineken");

        webTestClient.mutateWith(mockOAuth2Login())
                .put()
                .uri(BEER_PATH_ID, testBeer.getId())
                .body(Mono.just(testBeer), BeerDTO.class)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isNoContent();

    }

    @Test
    @Order(7)
    void updateBeerWithBeerNotFound() {
        webTestClient.mutateWith(mockOAuth2Login())
                .put()
                .uri(BEER_PATH_ID, 99)
                .body(Mono.just(getTestBeer()), BeerDTO.class)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(8)
    void updateBeerWithBadData() {
        BeerDTO testBeer = getSavedBeerDTO();
        testBeer.setBeerName("");

        webTestClient.mutateWith(mockOAuth2Login())
                .put()
                .uri(BEER_PATH_ID, testBeer.getId())
                .body(Mono.just(testBeer), BeerDTO.class)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @Order(9)
    void patchBeer() {
        BeerDTO testBeer = getSavedBeerDTO();
        testBeer.setBeerName("Heineken");

        webTestClient.mutateWith(mockOAuth2Login())
                .patch()
                .uri(BEER_PATH_ID, testBeer.getId())
                .body(Mono.just(getTestBeer()), BeerDTO.class)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @Order(10)
    void patchBeerWithBadData() {
        BeerDTO testBeer = getTestBeer();
        testBeer.setBeerName("");

        webTestClient.mutateWith(mockOAuth2Login())
                .patch()
                .uri(BEER_PATH_ID, 1)
                .body(Mono.just(testBeer), BeerDTO.class)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @Order(11)
    void patchBeerWithIdNotFound() {
        webTestClient.mutateWith(mockOAuth2Login())
                .patch()
                .uri(BEER_PATH_ID, 99)
                .body(Mono.just(getTestBeer()), BeerDTO.class)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(12)
    void deleteBeer() {
        BeerDTO testBeer = getSavedBeerDTO();
        webTestClient.mutateWith(mockOAuth2Login())
                .delete()
                .uri(BEER_PATH_ID, testBeer.getId())
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.mutateWith(mockOAuth2Login())
                .get().uri(BEER_PATH_ID, testBeer.getId())
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().valueEquals("Content-type", "application/json")
                .expectBody(BeerDTO.class);
    }

    @Test
    @Order(13)
    void deleteBeerWithNonExistingId() {
        webTestClient.mutateWith(mockOAuth2Login())
                .delete()
                .uri(BEER_PATH_ID, 99)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isNotFound();
    }
}