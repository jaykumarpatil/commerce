package com.projects.api.core.shipping;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddress {
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
