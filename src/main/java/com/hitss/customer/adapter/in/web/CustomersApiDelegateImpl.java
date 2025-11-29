package com.hitss.customer.adapter.in.web;

import com.hitss.customer.adapter.out.persistence.mapper.CustomerMapper;
import com.hitss.customer.adapters.api.CustomersApiDelegate;
import com.hitss.customer.adapters.api.model.Customer;
import com.hitss.customer.adapters.api.model.CustomerCreate;
import com.hitss.customer.adapters.api.model.CustomerUpdate;
import com.hitss.customer.application.DeleteCustomerUseCase;
import com.hitss.customer.application.GetCustomerUseCase;
import com.hitss.customer.application.RegisterCustomerUseCase;
import com.hitss.customer.application.UpdateCustomerUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomersApiDelegateImpl implements CustomersApiDelegate {

    private final RegisterCustomerUseCase registerCustomerUseCase;
    private final GetCustomerUseCase getCustomerUseCase;
    private final UpdateCustomerUseCase updateCustomerUseCase;
    private final DeleteCustomerUseCase deleteCustomerUseCase;

    @Override
    public Mono<ResponseEntity<Customer>> createCustomer(Mono<CustomerCreate> customerCreate,
                                                         ServerWebExchange exchange) {
        return customerCreate
                .map(CustomerMapper::toDomain)
                .flatMap(registerCustomerUseCase::create)
                .map(CustomerMapper::toApi)
                .map(c -> ResponseEntity.created(URI.create("/customers/" + c.getCustomerId())).body(c));
    }

    @Override
    public Mono<ResponseEntity<Customer>> getCustomer(UUID customerId, ServerWebExchange exchange) {
        return getCustomerUseCase.get(customerId.toString())
                .map(CustomerMapper::toApi)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Customer>> updateCustomer(UUID customerId,
                                                         Mono<CustomerUpdate> customerUpdate,
                                                         ServerWebExchange exchange) {
        return customerUpdate
                .flatMap(req -> updateCustomerUseCase.replace(customerId.toString(),
                                CustomerMapper.toDomain(customerId, req))
                        .map(CustomerMapper::toApi)
                        .map(ResponseEntity::ok));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteCustomer(UUID customerId, ServerWebExchange exchange) {
        return deleteCustomerUseCase.delete(customerId.toString())
                .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }
}
