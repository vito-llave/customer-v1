package com.hitss.customer.adapter.out.persistence.r2dbc.repo;

import com.hitss.customer.adapter.out.persistence.r2dbc.row.CustomerRow;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CustomerRowRepository extends ReactiveCrudRepository<CustomerRow, String> {
}
