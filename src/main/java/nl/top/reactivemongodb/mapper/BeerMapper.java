package nl.top.reactivemongodb.mapper;

import nl.top.reactivemongodb.domain.Beer;
import nl.top.reactivemongodb.model.BeerDTO;
import org.mapstruct.Mapper;

@Mapper
public interface BeerMapper {

    BeerDTO beerTobeerDTO(Beer beer);

    Beer beerDTOtoBeer(BeerDTO beerDTO);
}
