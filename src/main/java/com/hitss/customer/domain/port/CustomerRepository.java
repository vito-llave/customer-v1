package com.hitss.customer.domain.port;

import com.hitss.customer.domain.model.Customer;
import reactor.core.publisher.Mono;

public interface CustomerRepository {
    Mono<Customer> save(Customer c);

    Mono<Customer> findById(String id);

    Mono<Void> deleteById(String id);

    Mono<Customer> findByIdentificationNumber(String idNumber);
}
