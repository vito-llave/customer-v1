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
@Table("customers")
public class CustomerRow implements Persistable<String> {
    @Id
    @Column("customer_id")
    private String customerId;

    @Column("status")
    private Boolean status;

    @Transient
    private boolean newRow;

    @Override
    public String getId() {
        return customerId;
    }

    @Override
    public boolean isNew() {
        return newRow;
    }

    public CustomerRow markNew() {
        this.newRow = true;
        return this;
    }
}
