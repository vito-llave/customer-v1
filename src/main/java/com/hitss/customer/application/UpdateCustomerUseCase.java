package com.hitss.customer.application;

import com.hitss.customer.domain.model.Customer;
import com.hitss.customer.domain.model.Person;
import com.hitss.customer.domain.port.CustomerRepository;
import com.hitss.customer.shared.error.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateCustomerUseCase {

    private final CustomerRepository repo;

    public Mono<Customer> replace(String id, Customer customer) {
        return repo.findById(id)
                .switchIfEmpty(Mono.error(AppException.of(AppException.Code.NOT_FOUND, "Customer not found")))
                .flatMap(existing -> {
                    String newIdn = customer.getPerson().getIdentificationNumber();
                    if (newIdn != null) {
                        return repo.findByIdentificationNumber(newIdn)
                                .flatMap(owner -> owner.getCustomerId().equals(id) ? Mono.empty()
                                        : Mono.error(AppException.of(AppException.Code.CONFLICT,
                                        "Customer with identification already exists")))
                                .then(Mono.defer(() -> saveReplace(id, existing, customer)));
                    }
                    return saveReplace(id, existing, customer);
                });
    }

    private Mono<Customer> saveReplace(String id, Customer existing, Customer req) {
        Person p = req.getPerson();

        if (p.getPersonId() == null)
            p.setPersonId(existing.getPerson() != null ? existing.getPerson().getPersonId() : UUID.randomUUID().toString());

        req.setCustomerId(id);
        return repo.save(req);
    }
}
