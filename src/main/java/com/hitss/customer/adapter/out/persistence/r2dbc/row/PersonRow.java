package com.hitss.customer.adapter.out.persistence.r2dbc.row;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("persons")
public class PersonRow implements Persistable<String> {
    @Id
    @Column("person_id")
    private String personId;

    @Column("name")
    private String name;

    @Column("gender")
    private String gender;

    @Column("age")
    private Integer age;

    @Column("identification_number")
    private String identificationNumber;

    @Column("address")
    private String address;

    @Column("phone")
    private String phone;

    @Column("customer_id")
    private String customerId;

    @Transient
    private boolean newRow;

    @Override
    public String getId() {
        return personId;
    }

    @Override
    public boolean isNew() {
        return newRow;
    }

    public PersonRow markNew() {
        this.newRow = true;
        return this;
    }
}
