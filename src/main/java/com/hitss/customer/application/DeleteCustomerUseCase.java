package com.hitss.customer.application;

import com.hitss.customer.domain.port.CustomerRepository;
import com.hitss.customer.shared.error.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DeleteCustomerUseCase {

    private final CustomerRepository repo;

    public Mono<Void> delete(String id) {
        return repo.findById(id)
                .switchIfEmpty(Mono.error(AppException.of(AppException.Code.NOT_FOUND, "Customer not found")))
                .then(Mono.defer(() -> repo.deleteById(id)));
    }
}
