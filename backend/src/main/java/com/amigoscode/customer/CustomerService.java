package com.amigoscode.customer;

import com.amigoscode.exception.DuplicateResourceException;
import com.amigoscode.exception.RequestValidationException;
import com.amigoscode.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerDAO customerDAO;

    public CustomerService(@Qualifier("jdbc") CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    public List<Customer> getAllCustomers(){
        return customerDAO.selectAllCustomers();
    }
    public Customer getCustomer(Integer id){
        return customerDAO.selectCustomerById(id).orElseThrow(
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
                customerRegistrationRequest.age(),
                customerRegistrationRequest.gender()
        );
        customerDAO.insertCustomer(customer);
    }

    public void updateCustomer(Integer id, CustomerUpdateRequest customerUpdateRequest){
        Customer customer = getCustomer(id);

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
