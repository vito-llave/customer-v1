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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateCustomerUseCaseTest {

    @Mock
    private CustomerRepository repo;

    @InjectMocks
    private UpdateCustomerUseCase useCase;

    @Test
    @DisplayName("Emits NOT_FOUND when customer does not exist")
    void emitsNotFoundWhenMissing() {
        var id = "c-404";
        when(repo.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.replace(id, Customer.builder().person(new Person()).build()))
                .expectErrorSatisfies(ex -> {
                    assert ex instanceof AppException;
                    assert ((AppException) ex).getCode() == AppException.Code.NOT_FOUND;
                })
                .verify();

        verify(repo, times(1)).findById(id);
        verifyNoMoreInteractions(repo);
    }

    @Test
    @DisplayName("Emits CONFLICT when new identification belongs to another customer")
    void emitsConflictWhenIdentificationOwnedByAnother() {
        var id = "c-1";
        var existing = Customer.builder()
                .customerId(id)
                .status(true)
                .person(Person.builder().personId("p-1").identificationNumber("0102030405").build())
                .build();

        // Request attempts to change identification to one already used by someone else
        var req = Customer.builder()
                .status(true)
                .person(Person.builder().name("New Name").identificationNumber("9999999999").build())
                .build();

        var otherOwner = Customer.builder()
                .customerId("c-OTHER")
                .person(Person.builder().personId("p-X").identificationNumber("9999999999").build())
                .build();

        when(repo.findById(id)).thenReturn(Mono.just(existing));
        when(repo.findByIdentificationNumber("9999999999")).thenReturn(Mono.just(otherOwner));

        StepVerifier.create(useCase.replace(id, req))
                .expectErrorSatisfies(ex -> {
                    assert ex instanceof AppException;
                    assert ((AppException) ex).getCode() == AppException.Code.CONFLICT;
                })
                .verify();

        verify(repo).findById(id);
        verify(repo).findByIdentificationNumber("9999999999");
        verify(repo, never()).save(any());
        verifyNoMoreInteractions(repo);
    }

    @Test
    @DisplayName("Replaces successfully when identification remains with same owner")
    void replacesWhenIdentificationOwnedBySameCustomer() {
        var id = "c-1";
        var existing = Customer.builder()
                .customerId(id)
                .status(true)
                .person(Person.builder().personId("p-1").identificationNumber("0102030405").build())
                .build();

        var req = Customer.builder()
                .status(false)
                .person(Person.builder().name("Updated Name").identificationNumber("0102030405").build())
                .build();

        when(repo.findById(id)).thenReturn(Mono.just(existing));
        // duplicate check returns the same owner
        when(repo.findByIdentificationNumber("0102030405")).thenReturn(Mono.just(existing));
        when(repo.save(any(Customer.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.replace(id, req))
                .assertNext(saved -> {
                    // Ensure id set from path and personId preserved from existing
                    assert saved.getCustomerId().equals(id);
                    assert saved.getPerson() != null;
                    assert saved.getPerson().getPersonId().equals("p-1");
                    // Status updated according to request
                    assert Boolean.FALSE.equals(saved.getStatus());
                    // Identification unchanged
                    assert saved.getPerson().getIdentificationNumber().equals("0102030405");
                })
                .expectComplete()
                .verify();

        verify(repo).findById(id);
        verify(repo).findByIdentificationNumber("0102030405");
        verify(repo).save(any(Customer.class));
        verifyNoMoreInteractions(repo);
    }

    @Test
    @DisplayName("Replaces successfully when request identification is null (no duplicate check)")
    void replacesWhenIdentificationNullNoDuplicateCheck() {
        var id = "c-2";
        var existing = Customer.builder()
                .customerId(id)
                .status(true)
                .person(Person.builder().personId("p-2").identificationNumber("2222222222").build())
                .build();

        var req = Customer.builder()
                .status(true)
                .person(Person.builder().name("Someone").identificationNumber(null).build())
                .build();

        when(repo.findById(id)).thenReturn(Mono.just(existing));
        when(repo.save(any(Customer.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.replace(id, req))
                .assertNext(saved -> {
                    assert saved.getCustomerId().equals(id);
                    assert saved.getPerson().getPersonId().equals("p-2");
                })
                .expectComplete()
                .verify();

        verify(repo).findById(id);
        // No call to duplicate check when identification is null
        verify(repo, never()).findByIdentificationNumber(any());
        verify(repo).save(any(Customer.class));
        verifyNoMoreInteractions(repo);
    }
}