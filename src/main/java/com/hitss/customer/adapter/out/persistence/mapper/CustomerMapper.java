package com.hitss.customer.adapter.out.persistence.mapper;

import com.hitss.customer.adapters.api.model.CustomerCreate;
import com.hitss.customer.adapters.api.model.Customer;
import com.hitss.customer.adapters.api.model.CustomerUpdate;
import com.hitss.customer.domain.model.Person;
import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class CustomerMapper {

    public static com.hitss.customer.domain.model.Customer toDomain(CustomerCreate req) {
        var p = Person.builder()
                .name(req.getPerson().getName())
                .gender(req.getPerson().getGender().getValue())
                .age(req.getPerson().getAge())
                .identificationNumber(req.getPerson().getIdentificationNumber())
                .address(req.getPerson().getAddress())
                .phone(req.getPerson().getPhone())
                .build();
        return com.hitss.customer.domain.model.Customer.builder()
                .status(req.getStatus())
                .person(p)
                .build();
    }

    public static com.hitss.customer.domain.model.Customer toDomain(UUID id, CustomerUpdate req) {
        var p = Person.builder()
                .name(req.getPerson().getName())
                .gender(req.getPerson().getGender().getValue())
                .age(req.getPerson().getAge())
                .identificationNumber(req.getPerson().getIdentificationNumber())
                .address(req.getPerson().getAddress())
                .phone(req.getPerson().getPhone())
                .build();
        return com.hitss.customer.domain.model.Customer.builder()
                .customerId(id.toString())
                .status(req.getStatus())
                .person(p)
                .build();
    }

    public static com.hitss.customer.adapters.api.model.Person toApi(Person p) {
        if (p == null) return null;
        return new com.hitss.customer.adapters.api.model.Person()
                .personId(p.getPersonId() != null ? UUID.fromString(p.getPersonId()) : null)
                .name(p.getName())
                .gender(com.hitss.customer.adapters.api.model.Person.GenderEnum.fromValue(p.getGender()))
                .age(p.getAge())
                .identificationNumber(p.getIdentificationNumber())
                .address(p.getAddress())
                .phone(p.getPhone());
    }

    public static Customer toApi(com.hitss.customer.domain.model.Customer c) {
        return new Customer()
                .customerId(c.getCustomerId() != null ? UUID.fromString(c.getCustomerId()) : null)
                .status(c.getStatus())
                .person(toApi(c.getPerson()));
    }

}
