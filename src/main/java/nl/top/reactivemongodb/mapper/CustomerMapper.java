package nl.top.reactivemongodb.mapper;

import nl.top.reactivemongodb.domain.Customer;
import nl.top.reactivemongodb.model.CustomerDTO;
import org.mapstruct.Mapper;

@Mapper
public interface CustomerMapper {

    Customer customerDTOtoCustomer(CustomerDTO customerDTO);

    CustomerDTO customerToCustomerDTO(Customer customer);
}
