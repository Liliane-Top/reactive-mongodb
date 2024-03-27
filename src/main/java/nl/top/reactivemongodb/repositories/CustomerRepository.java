package nl.top.reactivemongodb.repositories;

import nl.top.reactivemongodb.domain.Customer;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CustomerRepository extends ReactiveMongoRepository<Customer, String> {
}
