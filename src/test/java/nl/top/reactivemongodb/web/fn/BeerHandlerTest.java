package nl.top.reactivemongodb.web.fn;

import nl.top.reactivemongodb.domain.BeerStyle;
import nl.top.reactivemongodb.model.BeerDTO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static nl.top.reactivemongodb.web.fn.BeerRouterConfig.*;

@SpringBootTest
@AutoConfigureWebTestClient
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BeerHandlerTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    @Order(1)
    void listBeer() {
        webTestClient.get().uri(BEER_PATH)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-type", "application/json")
                .expectBody().jsonPath("$.size()").isEqualTo(3);
    }
    @Test
    @Disabled
    @Order(99)
    void emptyListBeer(){
        webTestClient.delete()
                .uri(BEER_PATH_ID, 1)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isNoContent();
        webTestClient.delete()
                .uri(BEER_PATH_ID, 2/**/)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isNoContent();
        webTestClient.delete()
                .uri(BEER_PATH_ID, 4)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get().uri(BEER_PATH)
                .exchange()
                .expectStatus().isNotFound();


    }

    @Test
    @Order(2)
    void getBeerById() {
        webTestClient.get().uri(BEER_PATH_ID, 1)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-type", "application/json")
                .expectBody(BeerDTO.class);
    }

    @Test
    @Order(3)
    void getBeerByNonExistingId() {
        webTestClient.get().uri(BEER_PATH_ID, 10)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(4)
    void createNewBeer() {
        webTestClient.post().uri(BEER_PATH)
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

        webTestClient.post().uri(BEER_PATH)
                .body(Mono.just(testBeer), BeerDTO.class)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isBadRequest();
    }

    private static BeerDTO getTestBeer(){
        return BeerDTO.builder()
                .beerName("Space Dust")
                .beerStyle(BeerStyle.IPA)
                .price(new BigDecimal(10))
                .quantityOnHand(256)
                .upc("ipa")
                .build();
    }


    @Test
    @Order(6)
    void updateBeer(){
        BeerDTO testBeer = getTestBeer();
        testBeer.setBeerName("");

        webTestClient.put()
                .uri(BEER_PATH_ID, 1)
                .body(Mono.just(testBeer), BeerDTO.class)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isBadRequest();

    }

    @Test
    @Order(7)
    void updateBeerWithBeerNotFound(){
        webTestClient.put()
                .uri(BEER_PATH_ID, 99)
                .body(Mono.just(getTestBeer()), BeerDTO.class)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isNotFound();

    }
    @Test
    @Order(8)
    void updateBeerWithBadData(){
        BeerDTO testBeer = getTestBeer();
        testBeer.setBeerName("");

        webTestClient.put()
                .uri(BEER_PATH_ID, 1)
                .body(Mono.just(testBeer), BeerDTO.class)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isBadRequest();

    }

    @Test
    @Order(9)
    void patchBeer(){
        webTestClient.patch()
                .uri(BEER_PATH_ID, 1)
                .body(Mono.just(getTestBeer()), BeerDTO.class)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isOk();

    }

    @Test
    @Order(10)
    void patchBeerWithBadData(){
        BeerDTO testBeer = getTestBeer();
        testBeer.setBeerName("");

        webTestClient.patch()
                .uri(BEER_PATH_ID, 1)
                .body(Mono.just(testBeer), BeerDTO.class)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @Order(11)
    void patchBeerWithIdNotFound(){
        webTestClient.patch()
                .uri(BEER_PATH_ID, 99)
                .body(Mono.just(getTestBeer()), BeerDTO.class)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isNotFound();
    }



    @Test
    @Order(12)
    void deleteBeer() {

        webTestClient.delete()
                .uri(BEER_PATH_ID, 3)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @Order(13)
    void deleteBeerWithNonExistingId() {
        webTestClient.delete()
                .uri(BEER_PATH_ID, 99)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isNotFound();
    }
}