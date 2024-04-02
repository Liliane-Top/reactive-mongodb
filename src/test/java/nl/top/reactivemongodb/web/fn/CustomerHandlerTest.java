package nl.top.reactivemongodb.web.fn;

import nl.top.reactivemongodb.model.CustomerDTO;
import nl.top.reactivemongodb.services.CustomerService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static nl.top.reactivemongodb.web.fn.CustomerRouterConfig.CUSTOMER_PATH;
import static nl.top.reactivemongodb.web.fn.CustomerRouterConfig.CUSTOMER_PATH_ID;
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
                .id(null)
                .customerName("Els Stam")
                .lastModifiedDate(null)
                .createdDate(null)
                .build();
    }
    @Test
    @Order(1)
    void listCustomers() {
        webTestClient.get().uri(CUSTOMER_PATH)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "application/json")
                .expectBody().jsonPath("$.size()").value(greaterThan(1));
    }
    @Test
    void getCustomerById() {
        CustomerDTO testCustomer = getSavedCustomerDTO();

        webTestClient.get().uri(CUSTOMER_PATH_ID, testCustomer.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "application/json")
                .expectBody(CustomerDTO.class);
    }
    @Test
    void createNewCustomer() {
         webTestClient.post().uri(CUSTOMER_PATH)
                        .body(Mono.just(getTestCustomerDTO()), CustomerDTO.class)
                        .header("Content-Type", "application/json")
                        .exchange()
                        .expectStatus().isCreated()
                        .expectHeader().exists("location");
    }
    @Test
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
    void deleteCustomerById() {
        CustomerDTO testCustomer = getSavedCustomerDTO();
        webTestClient.delete()
                .uri(CUSTOMER_PATH_ID, testCustomer.getId())
                .exchange()
                .expectStatus().isNoContent();
    }
}