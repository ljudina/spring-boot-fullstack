package com.amigoscode.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerRowMapperTest {

    private CustomerRowMapper underTest;

    @Mock
    private ResultSet rs;

    @BeforeEach
    void setUp() {
        underTest = new CustomerRowMapper();
    }

    @Test
    void mapRow() throws Exception {
        Customer customer = new Customer(1, "Marko", "ljudina@gmail.com", 40);
        when(rs.getInt("id")).thenReturn(customer.getId());
        when(rs.getString("name")).thenReturn(customer.getName());
        when(rs.getString("email")).thenReturn(customer.getEmail());
        when(rs.getInt("age")).thenReturn(customer.getAge());

        Customer actualCustomer = underTest.mapRow(rs, 1);
        assertThat(actualCustomer).isEqualTo(customer);
    }
}