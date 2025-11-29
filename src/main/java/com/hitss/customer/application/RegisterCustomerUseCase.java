package com.hitss.customer.application;

import com.hitss.customer.domain.model.Customer;
import com.hitss.customer.domain.port.CustomerRepository;
import com.hitss.customer.shared.error.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterCustomerUseCase {
    private final CustomerRepository repo;

    public Mono<Customer> create(Customer customer) {

        String idn = customer.getPerson().getIdentificationNumber();

        return repo.findByIdentificationNumber(idn)
                .flatMap(existing -> Mono.<Customer>error(AppException.of(AppException.Code.CONFLICT,
                        "Customer with identification already exists")))
                .switchIfEmpty(Mono.defer(() -> {
                    if (customer.getCustomerId() == null) customer.setCustomerId(UUID.randomUUID().toString());
                    if (customer.getPerson().getPersonId() == null)
                        customer.getPerson().setPersonId(UUID.randomUUID().toString());
                    return repo.save(customer);
                }));
    }
}
