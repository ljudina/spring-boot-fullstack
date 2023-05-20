package com.amigoscode.customer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class CustomerJPADataAccessServiceTest {

    private CustomerJPADataAccessService underTest;
    private AutoCloseable autoCloseable;
    @Mock
    private CustomerRepository customerRepository;
    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        underTest = new CustomerJPADataAccessService(customerRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    void selectAllCustomers() {
        underTest.selectAllCustomers();
        Mockito.verify(customerRepository)
                .findAll();
    }

    @Test
    void selectCustomerById() {
        int id = 1;
        underTest.selectCustomerById(id);
        Mockito.verify(customerRepository).findById(id);
    }

    @Test
    void insertCustomer() {
        Customer customer = new Customer(
                "Marko",
                "ljudina@gmail.com",
                20,
                Gender.MALE
        );
        underTest.insertCustomer(customer);
        Mockito.verify(customerRepository).save(customer);
    }

    @Test
    void updateCustomer() {
        Customer customer = new Customer(
                "Marko",
                "ljudina@gmail.com",
                20,
                Gender.MALE
        );
        underTest.insertCustomer(customer);
        Mockito.verify(customerRepository).save(customer);
    }

    @Test
    void existsPersonWithEmail() {
        var email = "ljudina@gmail.com";
        underTest.existsPersonWithEmail(email);
        Mockito.verify(customerRepository).existsCustomerByEmail(email);
    }

    @Test
    void existsPersonWithId() {
        int id = 1;
        underTest.existsPersonWithId(id);
        Mockito.verify(customerRepository).existsCustomerById(id);
    }

    @Test
    void deleteCustomerById() {
        int id = 1;
        underTest.deleteCustomerById(id);
        Mockito.verify(customerRepository).deleteById(id);
    }
}