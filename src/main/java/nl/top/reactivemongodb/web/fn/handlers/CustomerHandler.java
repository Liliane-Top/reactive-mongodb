package nl.top.reactivemongodb.web.fn.handlers;

import lombok.RequiredArgsConstructor;
import nl.top.reactivemongodb.model.CustomerDTO;
import nl.top.reactivemongodb.services.CustomerService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import static nl.top.reactivemongodb.web.fn.config.CustomerRouterConfig.CUSTOMER_PATH_ID;

@Component
@RequiredArgsConstructor
public class CustomerHandler implements ResourceHandler<CustomerHandler> {

    private final CustomerService customerService;

    @Override
    public Mono<ServerResponse> getList(ServerRequest request) {
        return ServerResponse
                .ok()
                .body(customerService.listCustomers(), CustomerDTO.class);
    }

    @Override
    public Mono<ServerResponse> getById(ServerRequest request) {
        return ServerResponse
                .ok()
                .body(customerService.getCustomerById(request.pathVariable("customerId")), CustomerDTO.class);
    }

    @Override
    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(CustomerDTO.class)
                .flatMap(customerService::saveCustomer)
                .flatMap(savedCustomer -> ServerResponse.created(UriComponentsBuilder
                        .fromPath(CUSTOMER_PATH_ID)
                        .build(savedCustomer.getId())).build());
    }

    @Override
    public Mono<ServerResponse> updateById(ServerRequest request) {
        return request.bodyToMono(CustomerDTO.class)
                .flatMap(customerDTO -> customerService.updateCustomer(request.pathVariable("customerId"), customerDTO))
                .flatMap(savedDTO -> ServerResponse.noContent().build());
    }

    @Override
    public Mono<ServerResponse> patchById(ServerRequest request) {
        return request.bodyToMono(CustomerDTO.class)
                .flatMap(customerDTO -> customerService.patchCustomer(request.pathVariable("customerId"), customerDTO))
                .flatMap(savedDTO -> ServerResponse.noContent().build());
    }

    @Override
    public Mono<ServerResponse> deleteById(ServerRequest request) {
        return customerService.deleteCustomerById(request.pathVariable("customerId"))
                .then(ServerResponse.noContent().build());
    }
}
