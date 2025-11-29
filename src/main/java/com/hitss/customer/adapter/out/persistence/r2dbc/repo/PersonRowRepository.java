package com.hitss.customer.adapter.out.persistence.r2dbc.repo;

import com.hitss.customer.adapter.out.persistence.r2dbc.row.PersonRow;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface PersonRowRepository extends ReactiveCrudRepository<PersonRow, String> {
    Mono<PersonRow> findByCustomerId(String customerId);

    Mono<PersonRow> findByIdentificationNumber(String identificationNumber);
}
