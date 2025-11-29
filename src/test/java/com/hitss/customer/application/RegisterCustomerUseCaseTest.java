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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterCustomerUseCaseTest {

    @Mock
    private CustomerRepository repo;

    @InjectMocks
    private RegisterCustomerUseCase useCase;

    @Test
    @DisplayName("Emits CONFLICT when the identification already exists")
    void conflictWhenIdentificationExists() {
        var person = Person.builder()
                .name("Jose Lema").gender("M").age(32)
                .identificationNumber("0102030405")
                .address("Otavalo sn y principal").phone("0982548785")
                .build();
        var input = Customer.builder().status(true).person(person).build();

        var existing = Customer.builder().customerId("c1").status(true)
                .person(Person.builder().personId("p1").identificationNumber("0102030405").build())
                .build();

        when(repo.findByIdentificationNumber("0102030405")).thenReturn(Mono.just(existing));

        var result = useCase.create(input);

        StepVerifier.create(result)
                .expectErrorSatisfies(ex -> {
                    assert ex instanceof AppException;
                    assert ((AppException) ex).getCode() == AppException.Code.CONFLICT;
                })
                .verify();
    }

    @Test
    @DisplayName("Creates customer when identification does not exist")
    void createsCustomerWhenIdentificationNotExists() {
        var person = Person.builder()
                .name("Jose Lema").gender("M").age(32)
                .identificationNumber("0102030406")
                .address("Otavalo sn y principal").phone("0982548785")
                .build();
        var input = Customer.builder().status(true).person(person).build();

        when(repo.findByIdentificationNumber("0102030406")).thenReturn(Mono.empty());
        when(repo.save(any(Customer.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        var result = useCase.create(input);

        StepVerifier.create(result)
                .assertNext(created -> {
                    assert created.getCustomerId() != null && !created.getCustomerId().isBlank();
                    assert created.getPerson() != null;
                    assert created.getPerson().getPersonId() != null && !created.getPerson().getPersonId().isBlank();
                    assert Boolean.TRUE.equals(created.getStatus());
                    assert created.getPerson().getIdentificationNumber().equals("0102030406");
                })
                .expectComplete()
                .verify();
    }
}