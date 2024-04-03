package nl.top.reactivemongodb.web.fn.handlers;

import lombok.RequiredArgsConstructor;
import nl.top.reactivemongodb.model.CustomerDTO;
import nl.top.reactivemongodb.services.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import static nl.top.reactivemongodb.web.fn.config.CustomerRouterConfig.CUSTOMER_PATH_ID;

@Component
@RequiredArgsConstructor
public class CustomerHandler implements ResourceHandler<CustomerHandler> {

    private final CustomerService customerService;
    private final Validator validator;

    private void validate(CustomerDTO customerDTO) {
        Errors errors = new BeanPropertyBindingResult(customerDTO, "customerDTO");
        validator.validate(customerDTO, errors);
        if (errors.hasErrors()) {
            throw new ServerWebInputException(errors.toString());
        }
    }
    @Override
    public Mono<ServerResponse> getList(ServerRequest request) {
        return ServerResponse
                .ok()
                .body(customerService.listCustomers(), CustomerDTO.class);
    }

    @Override
    public Mono<ServerResponse> getById(ServerRequest request) {
        String customerId = request.pathVariable("customerId");
        return customerService.getCustomerById(customerId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(customer -> ServerResponse.ok().bodyValue(customer));
    }

    @Override
    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(CustomerDTO.class)
                .flatMap(customerDTO -> {
                    validate(customerDTO);
                    return customerService.saveCustomer(customerDTO)
                            .onErrorResume(error -> Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save customer", error)));
                })
                .flatMap(savedCustomer -> ServerResponse
                        .created(UriComponentsBuilder
                                .fromPath(CUSTOMER_PATH_ID)
                                .build(savedCustomer.getId()))
                        .build())
                .onErrorResume(error -> Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid customer data", error)));
    }
    @Override
    public Mono<ServerResponse> updateById(ServerRequest request) {
        String customerId = request.pathVariable("customerId");
        return customerService.getCustomerById(customerId)
                .flatMap(foundCustomer -> request.bodyToMono(CustomerDTO.class))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(customerDTO -> {
                    validate(customerDTO);
                    return customerService.updateCustomer(customerId, customerDTO);
                })
                .flatMap(updatedCustomer -> ServerResponse.noContent().build());
    }
    @Override
    public Mono<ServerResponse> patchById(ServerRequest request) {
        String customerId = request.pathVariable("customerId");
        return customerService.getCustomerById(customerId)
                .flatMap(foundCustomer -> request.bodyToMono(CustomerDTO.class))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(customerDTO -> {
                    validate(customerDTO);
                    return customerService.patchCustomer(customerId, customerDTO);
                })
                .flatMap(patchedCustomer -> ServerResponse.noContent().build());
    }
    @Override
    public Mono<ServerResponse> deleteById(ServerRequest request) {
        String customerId = request.pathVariable("customerId");
        return customerService.getCustomerById(customerId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .then(customerService.deleteCustomerById(customerId))
                .then(ServerResponse.noContent().build());

    }
}
