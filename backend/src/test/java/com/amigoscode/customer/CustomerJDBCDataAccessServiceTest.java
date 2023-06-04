package com.amigoscode.customer;

import com.amigoscode.AbstractTestContainers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerJDBCDataAccessServiceTest extends AbstractTestContainers {

    private CustomerJDBCDataAccessService underTest;
    private final CustomerRowMapper customerRowMapper = new CustomerRowMapper();

    @BeforeEach
    void setUp() {
        underTest = new CustomerJDBCDataAccessService(getJdbcTemplate(), customerRowMapper);
    }

    @Test
    void selectAllCustomers() {
        Customer customer = new Customer(
                FAKER.name().fullName(),
                FAKER.internet().emailAddress() + "-" + UUID.randomUUID(),
                "password", 20,
                Gender.MALE
        );
        underTest.insertCustomer(customer);
        List<Customer> actualCustomers = underTest.selectAllCustomers();
        assertThat(actualCustomers).isNotEmpty();
    }

    @Test
    void selectCustomerById() {
        var email = FAKER.internet().emailAddress() + "-" + UUID.randomUUID();
        Customer customer = new Customer(
                FAKER.name().fullName(),
                email,
                "password", 20,
                Gender.MALE
        );
        underTest.insertCustomer(customer);
        int id = underTest.selectAllCustomers()
                .stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();

        Optional<Customer> actualCustomer = underTest.selectCustomerById(id);
        assertThat(actualCustomer).isPresent().hasValueSatisfying(c -> {
            assertThat(c.getId()).isEqualTo(id);
            assertThat(c.getName()).isEqualTo(customer.getName());
            assertThat(c.getEmail()).isEqualTo(customer.getEmail());
            assertThat(c.getAge()).isEqualTo(customer.getAge());
            assertThat(c.getGender()).isEqualTo(customer.getGender());
        });
    }

    @Test
    void willReturnEmptyWhenSelectCustomerById(){
        int id = -1;
        Optional<Customer> actualCustomer = underTest.selectCustomerById(id);
        assertThat(actualCustomer).isEmpty();
    }

    @Test
    void insertCustomer() {
        var email = FAKER.internet().emailAddress() + "-" + UUID.randomUUID();
        Customer customer = new Customer(
                FAKER.name().fullName(),
                email,
                "password", 20,
                Gender.MALE
        );
        underTest.insertCustomer(customer);
        int id = underTest.selectAllCustomers()
                .stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();
        Optional<Customer> actualCustomer = underTest.selectCustomerById(id);
        assertThat(actualCustomer).isPresent().hasValueSatisfying(c -> {
            assertThat(c.getId()).isEqualTo(id);
            assertThat(c.getName()).isEqualTo(customer.getName());
            assertThat(c.getEmail()).isEqualTo(customer.getEmail());
            assertThat(c.getAge()).isEqualTo(customer.getAge());
            assertThat(c.getGender()).isEqualTo(customer.getGender());
        });
    }

    @Test
    void updateCustomer() {
        var email = FAKER.internet().emailAddress() + "-" + UUID.randomUUID();
        Customer customer = new Customer(
                FAKER.name().fullName(),
                email,
                "password", 20,
                Gender.MALE
        );
        underTest.insertCustomer(customer);
        int id = underTest.selectAllCustomers()
                .stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();
        var newEmail = FAKER.internet().emailAddress() + "-" + UUID.randomUUID();
        customer.setId(id);
        customer.setEmail(newEmail);
        underTest.updateCustomer(customer);
        Optional<Customer> actualCustomer = underTest.selectCustomerById(id);
        assertThat(actualCustomer).isPresent().hasValueSatisfying(c -> {
            assertThat(c.getId()).isEqualTo(id);
            assertThat(c.getName()).isEqualTo(customer.getName());
            assertThat(c.getEmail()).isEqualTo(customer.getEmail());
            assertThat(c.getAge()).isEqualTo(customer.getAge());
            assertThat(c.getGender()).isEqualTo(customer.getGender());
        });
    }

    @Test
    void existsPersonWithEmail() {
        var email = FAKER.internet().emailAddress() + "-" + UUID.randomUUID();
        Customer customer = new Customer(
                FAKER.name().fullName(),
                email,
                "password", 20,
                Gender.MALE
        );
        underTest.insertCustomer(customer);
        assertThat(underTest.existsPersonWithEmail(email)).isTrue();
    }

    @Test
    void existsPersonWithId() {
        var email = FAKER.internet().emailAddress() + "-" + UUID.randomUUID();
        Customer customer = new Customer(
                FAKER.name().fullName(),
                email,
                "password", 20,
                Gender.MALE
        );
        underTest.insertCustomer(customer);
        int id = underTest.selectAllCustomers()
                .stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();
        assertThat(underTest.existsPersonWithId(id)).isTrue();
    }

    @Test
    void deleteCustomerById() {
        var email = FAKER.internet().emailAddress() + "-" + UUID.randomUUID();
        Customer customer = new Customer(
                FAKER.name().fullName(),
                email,
                "password", 20,
                Gender.MALE
        );
        underTest.insertCustomer(customer);
        int id = underTest.selectAllCustomers()
                .stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();
        underTest.deleteCustomerById(id);
        Optional<Customer> actual = underTest.selectCustomerById(id);
        assertThat(actual).isNotPresent();
    }
}