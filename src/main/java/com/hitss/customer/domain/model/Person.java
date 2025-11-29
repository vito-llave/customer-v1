package com.hitss.customer.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Person {
    private String personId; // UUID string
    private String name;
    private String gender; // M, F, O
    private Integer age;
    private String identificationNumber;
    private String address;
    private String phone;
}
