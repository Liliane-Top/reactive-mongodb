package nl.top.reactivemongodb.services;

import nl.top.reactivemongodb.domain.Customer;
import nl.top.reactivemongodb.mapper.CustomerMapper;
import nl.top.reactivemongodb.model.CustomerDTO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CustomerServiceImplTest {

    @Autowired
    CustomerService customerService;
    @Autowired
    CustomerMapper customerMapper;
    CustomerDTO customerDTO;

    @BeforeEach
    void setUp() {
        customerDTO = customerMapper.customerToCustomerDTO(getTestCustomer());
    }

    private static Customer getTestCustomer() {
        return Customer.builder()
                .customerName("Olivia Newton John")
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build();
    }

    private CustomerDTO getTestCustomerDTO() {
        return customerMapper.customerToCustomerDTO(getTestCustomer());
    }

    private CustomerDTO getSavedCustomerDTO() {
        return customerService.saveCustomer(Mono.just(getTestCustomerDTO())).block();
    }

    @Test
    @DisplayName("Test find all customers")
    @Order(1)
    void listCustomers() {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        Flux<CustomerDTO> flux = customerService.listCustomers();
        flux.count().subscribe(count -> assertThat(count).isEqualTo(3L));
        atomicBoolean.set(true);
        await().untilTrue(atomicBoolean);
    }

    @Test
    @DisplayName("Test find first customer by name using subscribe")
    @Order(2)
    void findFirstByCustomerName() {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        customerService.findFirstByCustomerName("Robert van Leeuwen").subscribe(
                customer -> {
                    assertThat(customer.getCustomerName()).isEqualTo("Robert van Leeuwen");
                    atomicBoolean.set(true);
                }
        );
        await().untilTrue(atomicBoolean);
    }
    @Test
    @DisplayName("Test find first customer by name using block")
    @Order(3)
    void findFirstByCustomerNameBlocking() {
        CustomerDTO customer = customerService.findFirstByCustomerName("Ton Kraak").block();
        assertThat(customer).isNotNull();
        assertThat(customer.getCustomerName()).isEqualTo("Ton Kraak");
    }

    @Test
    @DisplayName("Test save customer using subscribe")
    void saveCustomer() {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        AtomicReference<CustomerDTO> atomicReference = new AtomicReference<>();
        Mono<CustomerDTO> savedMono = customerService.saveCustomer(customerDTO);

        savedMono.subscribe(savedCustomer -> {
            assertThat(savedCustomer.getCustomerName()).isEqualTo("Olivia Newton John");
            assertThat(savedCustomer.getId()).isNotNull();

            atomicReference.set(savedCustomer);
            atomicBoolean.set(true);
        });

        await().untilTrue(atomicBoolean);

        CustomerDTO persistedDTO = atomicReference.get();
        assertThat(persistedDTO).isNotNull();
        assertThat(persistedDTO.getId()).isNotNull();
//        assertThat(persistedDTO.getCustomerName()).isEqualTo("Lili");
        assertThat(persistedDTO.getCustomerName()).isNotEqualTo("Lili");
    }

    @Test
    @DisplayName("Test save customer using block")
    void saveCustomerUsingBlock() {
        CustomerDTO savedDTO = customerService.saveCustomer(Mono.just(getTestCustomerDTO())).block();
        assertThat(savedDTO).isNotNull();
        assertThat(savedDTO.getId()).isNotNull();
    }

    @Test
    @DisplayName("Test get customer by Id using subscribe")
    void getCustomerById() {
        CustomerDTO savedCustomer = getSavedCustomerDTO();
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        customerService.getCustomerById(savedCustomer.getId()).subscribe(
                foundCustomer -> {
                    assertThat(foundCustomer.getId()).isEqualTo(savedCustomer.getId());
                    assertThat(foundCustomer.getCustomerName()).isEqualTo("Olivia Newton John");
                    atomicBoolean.set(true);
                }
        );
        await().untilTrue(atomicBoolean);
    }

    @Test
    @DisplayName("Test get customer by Id using block")
    void getCustomerByIdBlock() {
        CustomerDTO customer = customerService.getCustomerById(getSavedCustomerDTO().getId()).block();
        assertThat(customer).isNotNull();
        assertThat(customer.getCustomerName()).isEqualTo("Olivia Newton John");
    }

    @Test
    @DisplayName("Test update customer using subscribe")
    void updateCustomer() {
        final String newName = "Cora van Mora";
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        CustomerDTO customerToBeUpdated = getSavedCustomerDTO();
        customerToBeUpdated.setCustomerName(newName);
        customerService.updateCustomer(customerToBeUpdated.getId(), customerToBeUpdated).subscribe(
                updatedCustomer -> {
                    assertThat(updatedCustomer.getCustomerName()).isEqualTo(newName);
                    atomicBoolean.set(true);
                }
        );
        await().untilTrue(atomicBoolean);
    }

    @Test
    @DisplayName("Test update customer using block")
    void updateCustomerBlocking() {
        final String newName = "Annemarie van Ginkel";
        CustomerDTO customerToBeUpdated = getSavedCustomerDTO();
        customerToBeUpdated.setCustomerName(newName);
        CustomerDTO updatedCustomer = customerService.
                updateCustomer(customerToBeUpdated.getId(), customerToBeUpdated).block();
        assertThat(updatedCustomer).isNotNull();
        assertThat(updatedCustomer.getCustomerName()).isEqualTo(newName);
    }

    @Test
    @DisplayName("Test patch customer using subscribe")
    void patchCustomer() {
        final String newName = "Cora van Mora";
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        CustomerDTO customerToBeUpdated = getSavedCustomerDTO();
        customerToBeUpdated.setCustomerName(newName);
        customerService.patchCustomer(customerToBeUpdated.getId(), customerToBeUpdated).subscribe(
                updatedCustomer -> {
                    assertThat(updatedCustomer.getCustomerName()).isEqualTo(newName);
                    atomicBoolean.set(true);
                }
        );
        await().untilTrue(atomicBoolean);
    }

    @Test
    @DisplayName("Test patch customer using block")
    void patchCustomerBlocking() {
        final String newName = "Annemarie van Ginkel";
        CustomerDTO customerToBeUpdated = getSavedCustomerDTO();
        customerToBeUpdated.setCustomerName(newName);
        CustomerDTO updatedCustomer = customerService.
                patchCustomer(customerToBeUpdated.getId(), customerToBeUpdated).block();
        assertThat(updatedCustomer).isNotNull();
        assertThat(updatedCustomer.getCustomerName()).isEqualTo(newName);
    }

    @Test
    @DisplayName("Test delete customer by Id using subscribe")
    void deleteCustomerByIdSubscribe() {
        CustomerDTO testCustomer = getSavedCustomerDTO();
        AtomicBoolean completed = new AtomicBoolean(false);

        customerService.getCustomerById(testCustomer.getId())
                .flatMap(foundCustomer -> customerService.deleteCustomerById(foundCustomer.getId()))
                .doOnSuccess(voidResult -> assertThatThrownBy(() ->
                        customerService.getCustomerById(testCustomer.getId()).block())
                        .isInstanceOf(ResponseStatusException.class)
                        .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
                        .hasMessageContaining("Customer with ID " + testCustomer.getId() + "not found"))
                .doFinally(signal -> completed.set(true))
                .subscribe();

        await().atMost(Duration.ofSeconds(5)).untilTrue(completed);
    }

    @Test
    @DisplayName("Test delete customer Id with StepVerifier")
    void deleteCustomerByIdWithStepVerifier() {
        CustomerDTO testCustomer = getSavedCustomerDTO();

        StepVerifier.create(customerService.getCustomerById(testCustomer.getId()))
                .expectNextCount(1)
                .expectComplete()
                .verify();

        StepVerifier.create(customerService.deleteCustomerById(testCustomer.getId()))
                .expectComplete()
                .verify();

        StepVerifier.create(customerService.getCustomerById(testCustomer.getId()))
                .expectErrorMatches(throwable -> throwable instanceof ResponseStatusException &&
                        ((ResponseStatusException) throwable).getStatusCode().equals(HttpStatus.NOT_FOUND) &&
                        throwable.getMessage().contains("Customer with ID " + testCustomer.getId() + " not found"))
                .verify();
    }

    @Test
    @DisplayName("Test delete customer by Id using block")
    void deleteCustomerByIdBlocking() {
        CustomerDTO testCustomer = getSavedCustomerDTO();

        assertThatThrownBy(() -> customerService.getCustomerById(testCustomer.getId())
                .flatMap(foundCustomer -> customerService.deleteCustomerById(foundCustomer.getId()))
                .then(Mono.defer(() -> customerService.getCustomerById(testCustomer.getId()))).block())
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
                .hasMessageContaining("Customer with ID " + testCustomer.getId() + " not found");
    }
}