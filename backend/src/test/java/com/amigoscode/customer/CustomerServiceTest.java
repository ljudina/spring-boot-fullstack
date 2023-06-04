package com.amigoscode.customer;

import com.amigoscode.exception.DuplicateResourceException;
import com.amigoscode.exception.RequestValidationException;
import com.amigoscode.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerDAO customerDAO;
    @Mock
    private PasswordEncoder passwordEncoder;
    private CustomerService underTest;
    private final CustomerDTOMapper customerDTOMapper = new CustomerDTOMapper();

    @BeforeEach
    void setUp() {
        underTest = new CustomerService(customerDAO, passwordEncoder, customerDTOMapper);
    }

    @Test
    void getAllCustomers() {
        underTest.getAllCustomers();
        verify(customerDAO)
                .selectAllCustomers();
    }

    @Test
    void canGetCustomer() {
        int id = 1;
        Customer customer = new Customer(
                id,
                "ljudina@gmail.com", "password", "Marko",
                40,
                Gender.MALE
        );
        when(customerDAO.selectCustomerById(id)).thenReturn(Optional.of(customer));
        CustomerDTO expected = customerDTOMapper.apply(customer);
        CustomerDTO actualCustomer = underTest.getCustomer(id);
        assertThat(actualCustomer).isEqualTo(expected);
    }

    @Test
    void willThrowWhenGetCustomerReturnsEmptyOptional() {
        int id = 1;
        when(customerDAO.selectCustomerById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> underTest.getCustomer(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer with id [%s] not found!".formatted(id));
    }

    @Test
    void addCustomer() {
        String email = "ljudina@gmail.com";
        when(customerDAO.existsPersonWithEmail(email)).thenReturn(false);
        CustomerRegistrationRequest request = new CustomerRegistrationRequest("Marko", email, "password", 40, Gender.MALE);
        String passwordHash = "c4324545c343433";
        when(passwordEncoder.encode(request.password())).thenReturn(passwordHash);
        underTest.addCustomer(request);

        ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerDAO).insertCustomer(customerArgumentCaptor.capture());
        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getId()).isNull();
        assertThat(capturedCustomer.getName()).isEqualTo(request.name());
        assertThat(capturedCustomer.getEmail()).isEqualTo(request.email());
        assertThat(capturedCustomer.getPassword()).isEqualTo(passwordHash);
        assertThat(capturedCustomer.getAge()).isEqualTo(request.age());
    }

    @Test
    void addCustomerEmailAlreadyExists() {
        String email = "ljudina@gmail.com";
        when(customerDAO.existsPersonWithEmail(email)).thenReturn(true);
        CustomerRegistrationRequest request = new CustomerRegistrationRequest("Marko", email, "password", 40, Gender.MALE);
        assertThatThrownBy(() -> underTest.addCustomer(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Customer with email [%s] already exists!".formatted(request.email()));
        verify(customerDAO, never()).insertCustomer(any());
    }

    @Test
    void updateCustomerAllFieldsChanged() {
        int id = 1;
        Customer customer = new Customer(id, "ljudina@gmail.com", "password", "Marko", 40, Gender.MALE);
        when(customerDAO.selectCustomerById(id)).thenReturn(Optional.of(customer));

        String newEmail = "ljudina83@gmail.com";
        CustomerUpdateRequest customerUpdateRequest = new CustomerUpdateRequest(
                "Marko Jovanovic", newEmail, 20, Gender.MALE
        );
        Customer updatedCustomer = new Customer(
                id,
                customerUpdateRequest.email(), "password", customerUpdateRequest.name(),
                customerUpdateRequest.age(),
                customerUpdateRequest.gender()
        );
        when(customerDAO.existsPersonWithEmail(newEmail)).thenReturn(false);
        underTest.updateCustomer(id, customerUpdateRequest);
        verify(customerDAO).updateCustomer(updatedCustomer);
    }

    @Test
    void updateCustomerEmailExists() {
        int id = 1;
        Customer customer = new Customer(id, "ljudina@gmail.com", "password", "Marko", 40, Gender.MALE);
        when(customerDAO.selectCustomerById(id)).thenReturn(Optional.of(customer));
        String newEmail = "ljudina83@gmail.com";
        when(customerDAO.existsPersonWithEmail(newEmail)).thenReturn(true);
        CustomerUpdateRequest customerUpdateRequest = new CustomerUpdateRequest(
                "Marko Jovanovic", newEmail, 20, Gender.MALE
        );
        assertThatThrownBy(() -> underTest.updateCustomer(id, customerUpdateRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Customer with email [%s] already exists!".formatted(newEmail));
    }

    @Test
    void updateCustomerNotChanged() {
        int id = 1;
        Customer customer = new Customer(id, "ljudina@gmail.com", "password", "Marko", 40, Gender.MALE);
        when(customerDAO.selectCustomerById(id)).thenReturn(Optional.of(customer));
        CustomerUpdateRequest customerUpdateRequest = new CustomerUpdateRequest(
                customer.getName(), customer.getEmail(), customer.getAge(), customer.getGender()
        );
        assertThatThrownBy(() -> underTest.updateCustomer(id, customerUpdateRequest))
                .isInstanceOf(RequestValidationException.class)
                .hasMessage("Customer information not change");
    }

    @Test
    void deleteCustomer() {
        int id = 1;
        when(customerDAO.existsPersonWithId(id)).thenReturn(true);
        underTest.deleteCustomer(id);
        verify(customerDAO).deleteCustomerById(id);
    }

    @Test
    void deleteCustomerNotFound() {
        int id = 1;
        when(customerDAO.existsPersonWithId(id)).thenReturn(false);
        assertThatThrownBy(() -> underTest.deleteCustomer(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer with id [%s] not found!".formatted(id));
    }
}