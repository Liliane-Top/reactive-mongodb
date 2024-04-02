package nl.top.reactivemongodb.bootstrap;

import lombok.RequiredArgsConstructor;
import nl.top.reactivemongodb.domain.Beer;
import nl.top.reactivemongodb.domain.BeerStyle;
import nl.top.reactivemongodb.domain.Customer;
import nl.top.reactivemongodb.repositories.BeerRepository;
import nl.top.reactivemongodb.repositories.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class BootstrapData implements CommandLineRunner {
    private final BeerRepository beerRepository;
    private final CustomerRepository customerRepository;

    @Override
    public void run(String... args) {
        beerRepository.deleteAll()//returns an empty mono
                .doOnSuccess(success -> loadBeerData()).subscribe();
        customerRepository.deleteAll()
                .doOnSuccess(success -> loadCustomerData()).subscribe();
        beerRepository.count().subscribe(count -> System.out.println("BeerCount is: " + count));
        customerRepository.count().subscribe(count -> System.out.println("CustomerCount is: " + count));
    }

    private void loadBeerData() {
        beerRepository.count().subscribe(count -> {
            if (count == 0) {
                Beer beer1 = Beer.builder()
                        .beerName("Galaxy Cat")
                        .beerStyle(BeerStyle.PALE_ALE)
                        .upc("12356")
                        .price(new BigDecimal("12.99"))
                        .quantityOnHand(122)
                        .createdDate(LocalDateTime.now())
                        .lastModifiedDate(LocalDateTime.now())
                        .build();

                Beer beer2 = Beer.builder()
                        .beerName("Crank")
                        .beerStyle(BeerStyle.PALE_ALE)
                        .upc("12356222")
                        .price(new BigDecimal("11.99"))
                        .quantityOnHand(392)
                        .createdDate(LocalDateTime.now())
                        .lastModifiedDate(LocalDateTime.now())
                        .build();

                Beer beer3 = Beer.builder()
                        .beerName("Sunshine City")
                        .beerStyle(BeerStyle.IPA)
                        .upc("12356")
                        .price(new BigDecimal("13.99"))
                        .quantityOnHand(144)
                        .createdDate(LocalDateTime.now())
                        .lastModifiedDate(LocalDateTime.now())
                        .build();

                beerRepository.save(beer1).subscribe();//it is required to subscribe to every save as it returns a mono which we have to subscribe to do persist the data
                beerRepository.save(beer2).subscribe();//each save operation returns a mono which needs to be subscribed to persist the data
                beerRepository.save(beer3).subscribe();
            }
        });
    }

    private void loadCustomerData() {
        customerRepository.count().subscribe(count -> {
            if (count == 0) {
                Customer customer1 = Customer.builder()
                        .customerName("Liliane Top")
                        .createdDate(LocalDateTime.now())
                        .lastModifiedDate(LocalDateTime.now())
                        .build();
                Customer customer2 = Customer.builder()
                        .customerName("Ton Kraak")
                        .createdDate(LocalDateTime.now())
                        .lastModifiedDate(LocalDateTime.now())
                        .build();
                Customer customer3 = Customer.builder()
                        .customerName("Robert van Leeuwen")
                        .createdDate(LocalDateTime.now())
                        .lastModifiedDate(LocalDateTime.now())
                        .build();
                customerRepository.save(customer1).subscribe();
                customerRepository.save(customer2).subscribe();
                customerRepository.save(customer3).subscribe();
            }
        });
    }

}
