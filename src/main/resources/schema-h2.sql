-- =========================================================
-- Customer Service API - H2 Schema (DDL)
-- Tablas:
--   PERSON  : datos de la persona
--   CUSTOMER: estado del cliente y credenciales (hash)
-- Notas:
--  - UUIDs guardados como CHAR(36) para simplicidad en H2.
--  - identification_number es único.
--  - Una PERSON solo puede pertenecer a un CUSTOMER (1:1).
-- =========================================================

-- Schema for Customer Service API (H2)

DROP TABLE IF EXISTS persons;
DROP TABLE IF EXISTS customers;

CREATE TABLE customers (
    customer_id VARCHAR(36) PRIMARY KEY,
    status BOOLEAN NOT NULL
);

CREATE TABLE persons (
    person_id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    gender CHAR(1) NOT NULL,
    age INT NOT NULL,
    identification_number VARCHAR(255) NOT NULL UNIQUE,
    address VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    customer_id VARCHAR(36) NOT NULL UNIQUE,
    CONSTRAINT fk_person_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

-- =========================================================
-- (Opcional) Semillas de ejemplo:
--  Mueve estas INSERT a data-h2.sql si prefieres separación DDL/DML.
-- =========================================================
-- INSERT INTO PERSON (person_id, name, gender, age, identification_number, address, phone)
-- VALUES
--   ('00000000-0000-0000-0000-000000000001','Jose Lema','M',32,'0102030405','Otavalo sn y principal','0982548785'),
--   ('00000000-0000-0000-0000-000000000002','Marianela Montalvo','F',28,'0102030406','Amazonas y NNUU','097548965'),
--   ('00000000-0000-0000-0000-000000000003','Juan Osorio','M',30,'0102030407','13 junio y Equinoccial','098874587');
--
-- INSERT INTO CUSTOMER (customer_id, person_id, status, password_hash)
-- VALUES
--   ('10000000-0000-0000-0000-000000000001','00000000-0000-0000-0000-000000000001', TRUE, '$2a$10$hashDeBCrypt1234'),
--   ('10000000-0000-0000-0000-000000000002','00000000-0000-0000-0000-000000000002', TRUE, '$2a$10$hashDeBCrypt5678'),
--   ('10000000-0000-0000-0000-000000000003','00000000-0000-0000-0000-000000000003', TRUE, '$2a$10$hashDeBCrypt1245');

-- Sugerencias de configuración Spring (application.yml):
-- spring.jpa.hibernate.ddl-auto=none
-- spring.sql.init.mode=always
-- spring.h2.console.enabled=true
