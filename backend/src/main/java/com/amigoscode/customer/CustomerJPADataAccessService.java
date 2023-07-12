package com.amigoscode.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("jpa")
public class CustomerJPADataAccessService implements CustomerDAO{

    private final CustomerRepository customerRepository;

    public CustomerJPADataAccessService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public List<Customer> selectAllCustomers() {
        Page<Customer> page = this.customerRepository.findAll(Pageable.ofSize(100));
        return page.getContent();
    }

    @Override
    public Optional<Customer> selectCustomerById(Integer id) {
        return this.customerRepository.findById(id);
    }

    @Override
    public void insertCustomer(Customer customer) {
        this.customerRepository.save(customer);
    }

    @Override
    public void updateCustomer(Customer customer) {
        this.customerRepository.save(customer);
    }

    @Override
    public boolean existsPersonWithEmail(String email) {
        return this.customerRepository.existsCustomerByEmail(email);
    }

    @Override
    public boolean existsPersonWithId(Integer id) {
        return this.customerRepository.existsCustomerById(id);
    }

    @Override
    public void deleteCustomerById(Integer id) {
        this.customerRepository.deleteById(id);
    }

    @Override
    public Optional<Customer> selectUserByEmail(String email) { return this.customerRepository.findCustomerByEmail(email); }

    @Override
    public void updateCustomerProfileImageId(String profileImageId, Integer customerId) {
        this.customerRepository.updateProfileImageId(profileImageId, customerId);
    }
}
