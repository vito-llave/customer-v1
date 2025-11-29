package com.hitss.customer.adapter.out.persistence.r2dbc;

import com.hitss.customer.domain.model.Customer;
import com.hitss.customer.domain.port.CustomerRepository;
import com.hitss.customer.domain.model.Person;
import com.hitss.customer.shared.error.AppException;
import com.hitss.customer.adapter.out.persistence.r2dbc.repo.CustomerRowRepository;
import com.hitss.customer.adapter.out.persistence.r2dbc.repo.PersonRowRepository;
import com.hitss.customer.adapter.out.persistence.r2dbc.row.CustomerRow;
import com.hitss.customer.adapter.out.persistence.r2dbc.row.PersonRow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
@RequiredArgsConstructor
public class R2dbcCrudCustomerRepository implements CustomerRepository {

    private final CustomerRowRepository customers;
    private final PersonRowRepository persons;

    @Transactional
    @Override
    public Mono<Customer> save(Customer customer) {
        CustomerRow cr = new CustomerRow(customer.getCustomerId(), Boolean.TRUE.equals(customer.getStatus()), false);
        Mono<Void> upsertCustomer = customers.existsById(cr.getCustomerId())
                .flatMap(exists -> exists ? customers.save(cr) : customers.save(cr.markNew()))
                .doOnError(th -> log.error("[DB] Error saving CustomerRow. customerId={}", customer.getCustomerId(), th))
                .then();

        Mono<Void> upsertPerson = Mono.defer(() -> {
            Person p = customer.getPerson();
            if (p == null) return Mono.empty();
            var pr = new PersonRow(
                    p.getPersonId(), p.getName(), p.getGender(), p.getAge(),
                    p.getIdentificationNumber(), p.getAddress(), p.getPhone(), customer.getCustomerId(), false
            );
            return persons.findByCustomerId(customer.getCustomerId())
                    .doOnError(th ->
                            log.error("[DB] Error finding PersonRow by customerId before upsert. customerId={}",
                                    customer.getCustomerId(), th))
                    .flatMap(persons::delete)
                    .doOnError(th ->
                            log.error("[DB] Error deleting existing PersonRow. customerId={}",
                                    customer.getCustomerId(), th))
                    .then(persons.save(pr.markNew())
                            .doOnError(th ->
                                    log.error("[DB] Error saving PersonRow. customerId={}, personId={}, idNumber={}",
                                            customer.getCustomerId(), p.getPersonId(), p.getIdentificationNumber(), th)))
                    .then();
        });

        return upsertCustomer.then(upsertPerson)
                .then(findById(customer.getCustomerId()))
                .doOnError(th ->
                        log.error("[DB] Error during save pipeline (customer + person). customerId={}",
                                customer.getCustomerId(), th))
                .onErrorMap(th -> AppException.of(AppException.Code.DB_ERROR,
                        "Database error while saving customer", th));
    }

    @Override
    public Mono<Customer> findById(String id) {
        return Mono.zip(customers.findById(id)
                                .doOnError(th ->
                                        log.error("[DB] Error finding CustomerRow by id. customerId={}", id, th)),
                        persons.findByCustomerId(id)
                                .doOnError(th ->
                                        log.error("[DB] Error finding PersonRow by customerId. customerId={}", id, th)))
                .map(t -> toDomain(t.getT1(), t.getT2()))
                .switchIfEmpty(customers.findById(id)
                        .doOnError(th ->
                                log.error("[DB] Error finding CustomerRow (switchIfEmpty path). customerId={}", id, th))
                        .map(c -> toDomain(c, null)))
                .doOnError(th ->
                        log.error("[DB] Error during findById pipeline. customerId={}", id, th))
                .onErrorMap(th -> AppException.of(AppException.Code.DB_ERROR,
                        "Database error while fetching customer", th));
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return persons.findByCustomerId(id)
                .doOnError(th ->
                        log.error("[DB] Error finding PersonRow before deleteById. customerId={}", id, th))
                .flatMap(persons::delete)
                .doOnError(th ->
                        log.error("[DB] Error deleting PersonRow in deleteById. customerId={}", id, th))
                .then(customers.deleteById(id)
                        .doOnError(th ->
                                log.error("[DB] Error deleting CustomerRow by id. customerId={}", id, th)))
                .doOnError(th ->
                        log.error("[DB] Error during deleteById pipeline. customerId={}", id, th))
                .onErrorMap(th -> AppException.of(AppException.Code.DB_ERROR,
                        "Database error while deleting customer", th));
    }

    @Override
    public Mono<Customer> findByIdentificationNumber(String idNumber) {
        return persons.findByIdentificationNumber(idNumber)
                .doOnError(th ->
                        log.error("[DB] Error finding PersonRow by identificationNumber. identificationNumber={}",
                                idNumber, th))
                .flatMap(pr -> customers.findById(pr.getCustomerId())
                        .doOnError(e ->
                                log.error("[DB] Error finding CustomerRow by id (from idNumber). customerId={}, identificationNumber={}",
                                        pr.getCustomerId(), idNumber, e))
                        .map(cr -> toDomain(cr, pr)))
                .doOnError(th ->
                        log.error("[DB] Error during findByIdentificationNumber pipeline. identificationNumber={}",
                                idNumber, th))
                .onErrorMap(th -> AppException.of(AppException.Code.DB_ERROR,
                        "Database error while finding by identification", th));
    }

    private Customer toDomain(CustomerRow cr, PersonRow pr) {
        Person p = null;
        if (pr != null) {
            p = Person.builder()
                    .personId(pr.getPersonId())
                    .name(pr.getName())
                    .gender(pr.getGender())
                    .age(pr.getAge())
                    .identificationNumber(pr.getIdentificationNumber())
                    .address(pr.getAddress())
                    .phone(pr.getPhone())
                    .build();
        }
        return Customer.builder()
                .customerId(cr.getCustomerId())
                .status(Boolean.TRUE.equals(cr.getStatus()))
                .person(p)
                .build();
    }
}
