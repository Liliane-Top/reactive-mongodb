package nl.top.reactivemongodb.web.fn;

import lombok.RequiredArgsConstructor;
import nl.top.reactivemongodb.model.CustomerDTO;
import nl.top.reactivemongodb.services.CustomerService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import static nl.top.reactivemongodb.web.fn.CustomerRouterConfig.CUSTOMER_PATH_ID;

@Component
@RequiredArgsConstructor
public class CustomerHandler {

    private final CustomerService customerService;

    public Mono<ServerResponse> listCustomers(ServerRequest request) {
        return ServerResponse
                .ok()
                .body(customerService.listCustomers(), CustomerDTO.class);
    }

    public Mono<ServerResponse> getCustomerById(ServerRequest request) {
        return ServerResponse
                .ok()
                .body(customerService.getCustomerById(request.pathVariable("customerId")),
                        CustomerDTO.class);
    }

    public Mono<ServerResponse> createNewCustomer(ServerRequest request) {
        return request.bodyToMono(CustomerDTO.class)
                .flatMap(customerService::saveCustomer)
                .flatMap(savedCustomer -> ServerResponse
                        .created(UriComponentsBuilder
                                .fromPath(CUSTOMER_PATH_ID)
                                .build(savedCustomer.getId()))
                        .build());
    }
    public Mono<ServerResponse> updateCustomerById(ServerRequest request) {
        return request.bodyToMono(CustomerDTO.class)
                .flatMap(customerDTO -> customerService.updateCustomer(
                        request.pathVariable("customerId"), customerDTO))
                .flatMap(updatedCustomer -> ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> patchCustomerById(ServerRequest request) {
        return request.bodyToMono(CustomerDTO.class)
                .flatMap(customerDTO -> customerService.patchCustomer(
                        request.pathVariable("customerId"), customerDTO))
                .flatMap(patchedCustomer -> ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> deleteCustomerById(ServerRequest request) {
        return customerService.deleteCustomerById(request.pathVariable("customerId"))
                .then(ServerResponse.noContent().build());
    }
}
