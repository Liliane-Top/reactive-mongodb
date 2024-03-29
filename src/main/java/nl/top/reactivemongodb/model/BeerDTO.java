package nl.top.reactivemongodb.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.top.reactivemongodb.domain.BeerStyle;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeerDTO {
    //validation is required in the DTO
    private String id;
    @NotBlank
    @Size(min = 3, max = 255)
    private String beerName;
    private BeerStyle beerStyle;
    @Size(max = 25)
    private String upc;
    private Integer quantityOnHand;
    private BigDecimal price;
    @CreatedDate
    private LocalDateTime createdDate;
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;
}

