package com.amigoscode.customer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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
        Page<Customer> page = mock(Page.class);
        List<Customer> customers = List.of(new Customer());
        when(page.getContent()).thenReturn(customers);
        when(customerRepository.findAll(any(Pageable.class))).thenReturn(page);
        //when
        List<Customer> expected = underTest.selectAllCustomers();
        //Then
        assertThat(expected).isEqualTo(customers);
        ArgumentCaptor<Pageable> pageableArgumentCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(customerRepository).findAll(pageableArgumentCaptor.capture());
        assertThat(pageableArgumentCaptor.getValue()).isEqualTo(Pageable.ofSize(100));
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
                "password", 20,
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
                "password", 20,
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
    @Test
    void canUpdateProfileImageId() {
        String profileImageId = "222";
        Integer customerId = 1;
        underTest.updateCustomerProfileImageId(profileImageId, customerId);
        Mockito.verify(customerRepository).updateProfileImageId(profileImageId, customerId);
    }
}