package nl.top.reactivemongodb.repositories;

import nl.top.reactivemongodb.domain.Beer;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface BeerRepository extends ReactiveMongoRepository<Beer, String> {
}
