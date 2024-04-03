package nl.top.reactivemongodb.web.fn;

import nl.top.reactivemongodb.model.CustomerDTO;
import nl.top.reactivemongodb.services.CustomerService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static nl.top.reactivemongodb.web.fn.config.CustomerRouterConfig.CUSTOMER_PATH;
import static nl.top.reactivemongodb.web.fn.config.CustomerRouterConfig.CUSTOMER_PATH_ID;
import static org.hamcrest.Matchers.greaterThan;
@SpringBootTest
@AutoConfigureWebTestClient
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CustomerHandlerTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    CustomerService customerService;

    public CustomerDTO getSavedCustomerDTO() {
        return customerService.saveCustomer(Mono.just(getTestCustomerDTO())).block();
    }
    private static CustomerDTO getTestCustomerDTO(){
        return CustomerDTO.builder()
                .customerName("Els Stam")
                .build();
    }
    @Test
    @Order(1)
    @DisplayName("Test get list of customers")
    void listCustomers() {
        webTestClient.get().uri(CUSTOMER_PATH)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "application/json")
                .expectBody().jsonPath("$.size()").value(greaterThan(1));
    }
    @Test
    @DisplayName("Test get customer by valid ID")
    void getCustomerById() {
        CustomerDTO testCustomer = getSavedCustomerDTO();

        webTestClient.get().uri(CUSTOMER_PATH_ID, testCustomer.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "application/json")
                .expectBody(CustomerDTO.class);
    }
    @Test
    @DisplayName("Test get customer by non-existing ID throwing exception")
    void getCustomerByNonExistingId() {
        webTestClient.get().uri(CUSTOMER_PATH_ID, "1b")
                .exchange()
                .expectStatus().isNotFound();
    }
    @Test
    @DisplayName("Test create new customer")
    void createNewCustomer() {
         webTestClient.post().uri(CUSTOMER_PATH)
                        .body(Mono.just(getTestCustomerDTO()), CustomerDTO.class)
                        .header("Content-Type", "application/json")
                        .exchange()
                        .expectStatus().isCreated()
                        .expectHeader().exists("location");
    }
    @Test
    @DisplayName("Test create new customer with bad data")
    void createNewCustomerWithBadData() {
        CustomerDTO customerEmpty = CustomerDTO.builder().build();
        webTestClient.post().uri(CUSTOMER_PATH)
                .body(Mono.just(customerEmpty), CustomerDTO.class)
                .header("Content-Type", "application/json")
                .exchange()
                .expectStatus().isBadRequest();
    }
    @Test
    @DisplayName("Test update customer with valid data")
    void updateCustomerById() {
        CustomerDTO toBeUpdatedCustomer = getSavedCustomerDTO();
        toBeUpdatedCustomer.setCustomerName("Monique Abels");

        webTestClient.put().uri(CUSTOMER_PATH_ID, toBeUpdatedCustomer.getId())
                .body(Mono.just(toBeUpdatedCustomer), CustomerDTO.class)
                .header("Content-Type", "application/json")
                .exchange()
                .expectStatus().isNoContent();
    }
    @Test
    @DisplayName("Test update customer with bad data")
    void updateCustomerByIdWithBadData() {
        CustomerDTO toBeUpdatedCustomer = getSavedCustomerDTO();
        toBeUpdatedCustomer.setCustomerName("Mo");

        webTestClient.put().uri(CUSTOMER_PATH_ID, toBeUpdatedCustomer.getId())
                .body(Mono.just(toBeUpdatedCustomer), CustomerDTO.class)
                .header("Content-Type", "application/json")
                .exchange()
                .expectStatus().isBadRequest();
    }
    @Test
    @DisplayName("Test update customer that is non-existing")
    void updateCustomerByIdNotFound() {
        CustomerDTO toBeUpdatedCustomer = getSavedCustomerDTO();

        webTestClient.put().uri(CUSTOMER_PATH_ID, 1)
                .body(Mono.just(toBeUpdatedCustomer), CustomerDTO.class)
                .header("Content-Type", "application/json")
                .exchange()
                .expectStatus().isNotFound();
    }
    @Test
    @DisplayName("Test patch customer with existing id and valid data")
    void patchCustomerById() {
        CustomerDTO testCustomer = getSavedCustomerDTO();
        testCustomer.setCustomerName("Heleen Top");

        webTestClient.patch()
                .uri(CUSTOMER_PATH_ID, testCustomer.getId())
                .body(Mono.just(testCustomer), CustomerDTO.class)
                .header("Content-Type", "application/json")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("Test patch customer with existing id and bad data")
    void patchCustomerByIdWithBadData() {
        CustomerDTO testCustomer = getSavedCustomerDTO();
        testCustomer.setCustomerName("Ho");

        webTestClient.patch()
                .uri(CUSTOMER_PATH_ID, testCustomer.getId())
                .body(Mono.just(testCustomer), CustomerDTO.class)
                .header("Content-Type", "application/json")
                .exchange()
                .expectStatus().isBadRequest();
    }
    @Test
    @DisplayName("Test patch customer with non-existing id")
    void patchCustomerByIdWithNonExistingId() {
        CustomerDTO testCustomer = getSavedCustomerDTO();
        testCustomer.setCustomerName("Harma Swarma");

        webTestClient.patch()
                .uri(CUSTOMER_PATH_ID, 1)
                .body(Mono.just(testCustomer), CustomerDTO.class)
                .header("Content-Type", "application/json")
                .exchange()
                .expectStatus().isNotFound();
    }
    @Test
    @DisplayName("Test delete customer with existing Id")
    void deleteCustomerById() {
        CustomerDTO testCustomer = getSavedCustomerDTO();
        webTestClient.delete()
                .uri(CUSTOMER_PATH_ID, testCustomer.getId())
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("Test delete customer with non-existing Id")
    void deleteCustomerByNonExistingId() {
        webTestClient.delete()
                .uri(CUSTOMER_PATH_ID, 1)
                .exchange()
                .expectStatus().isNotFound();
    }
}