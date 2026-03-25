package se.magnus.microservices.core.shipping.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("shipping_addresses")
public class ShippingAddressEntity {
    @Id
    private Long id;
    private String addressId;
    private String userId;
    private String fullName;
    private String phoneNumber;
    private String streetAddress;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private Boolean isDefault;
    private String createdAt;
}
