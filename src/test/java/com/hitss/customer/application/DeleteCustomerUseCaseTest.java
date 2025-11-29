package com.hitss.customer.application;

import com.hitss.customer.domain.model.Customer;
import com.hitss.customer.domain.model.Person;
import com.hitss.customer.domain.port.CustomerRepository;
import com.hitss.customer.shared.error.AppException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteCustomerUseCaseTest {

    @Mock
    private CustomerRepository repo;

    @InjectMocks
    private DeleteCustomerUseCase useCase;

    @Test
    @DisplayName("Deletes customer when it exists")
    void deletesWhenExists() {
        var id = "c-123";
        var existing = Customer.builder()
                .customerId(id)
                .status(true)
                .person(Person.builder().personId("p-1").identificationNumber("0102030405").build())
                .build();

        when(repo.findById(id)).thenReturn(Mono.just(existing));
        when(repo.deleteById(id)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.delete(id))
                .expectComplete()
                .verify();

        verify(repo, times(1)).findById(id);
        verify(repo, times(1)).deleteById(id);
        verifyNoMoreInteractions(repo);
    }

    @Test
    @DisplayName("Emits NOT_FOUND when trying to delete a non-existent customer")
    void emitsNotFoundWhenMissing() {
        var id = "missing";

        when(repo.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.delete(id))
                .expectErrorSatisfies(ex -> {
                    assert ex instanceof AppException;
                    assert ((AppException) ex).getCode() == AppException.Code.NOT_FOUND;
                })
                .verify();

        verify(repo, times(1)).findById(id);
        verify(repo, never()).deleteById(anyString());
        verifyNoMoreInteractions(repo);
    }
}