package com.amigoscode.customer;

import com.amigoscode.exception.DuplicateResourceException;
import com.amigoscode.exception.RequestValidationException;
import com.amigoscode.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerDAO customerDAO;
    private final PasswordEncoder passwordEncoder;
    private final CustomerDTOMapper customerDTOMapper;

    public CustomerService(@Qualifier("jpa") CustomerDAO customerDAO, PasswordEncoder passwordEncoder, CustomerDTOMapper customerDTOMapper) {
        this.customerDAO = customerDAO;
        this.passwordEncoder = passwordEncoder;
        this.customerDTOMapper = customerDTOMapper;
    }

    public List<CustomerDTO> getAllCustomers(){
        return customerDAO
                .selectAllCustomers()
                .stream()
                .map(customerDTOMapper)
                .collect(Collectors.toList());
    }
    public CustomerDTO getCustomer(Integer id){
        return customerDAO.selectCustomerById(id)
                .map(customerDTOMapper)
                .orElseThrow(
                () -> new ResourceNotFoundException("Customer with id [%s] not found!".formatted(id))
        );
    }
    public void addCustomer(CustomerRegistrationRequest customerRegistrationRequest){
        String email = customerRegistrationRequest.email();
        if(customerDAO.existsPersonWithEmail(email)){
            throw new DuplicateResourceException("Customer with email [%s] already exists!".formatted(email));
        }
        Customer customer = new Customer(
                customerRegistrationRequest.name(),
                customerRegistrationRequest.email(),
                passwordEncoder.encode(customerRegistrationRequest.password()),
                customerRegistrationRequest.age(),
                customerRegistrationRequest.gender()
        );
        customerDAO.insertCustomer(customer);
    }

    public void updateCustomer(Integer id, CustomerUpdateRequest customerUpdateRequest){
        Customer customer = customerDAO.selectCustomerById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Customer with id [%s] not found!".formatted(id))
                );

        boolean changes = false;

        if (customerUpdateRequest.name() != null && !customerUpdateRequest.name().equals(customer.getName())) {
            customer.setName(customerUpdateRequest.name());
            changes = true;
        }

        if (customerUpdateRequest.email() != null && !customerUpdateRequest.email().equals(customer.getEmail())) {
            String email = customerUpdateRequest.email();
            if(customerDAO.existsPersonWithEmail(email)){
                throw new DuplicateResourceException("Customer with email [%s] already exists!".formatted(email));
            }
            customer.setEmail(customerUpdateRequest.email());
            changes = true;
        }

        if (customerUpdateRequest.age() != null && !customerUpdateRequest.age().equals(customer.getAge())) {
            customer.setAge(customerUpdateRequest.age());
            changes = true;
        }
        if(!changes){
            throw new RequestValidationException("Customer information not change");
        }
        customerDAO.updateCustomer(customer);
    }

    public void deleteCustomer(Integer id){
        if(!customerDAO.existsPersonWithId(id)){
            throw new ResourceNotFoundException("Customer with id [%s] not found!".formatted(id));
        }
        customerDAO.deleteCustomerById(id);
    }
}
