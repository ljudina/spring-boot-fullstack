package com.amigoscode.customer;

import com.amigoscode.exception.DuplicateResourceException;
import com.amigoscode.exception.RequestValidationException;
import com.amigoscode.exception.ResourceNotFoundException;
import com.amigoscode.s3.S3Buckets;
import com.amigoscode.s3.S3Service;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerDAO customerDAO;
    private final PasswordEncoder passwordEncoder;
    private final CustomerDTOMapper customerDTOMapper;
    private final S3Service s3Service;
    private final S3Buckets buckets;

    public CustomerService(@Qualifier("jpa") CustomerDAO customerDAO, PasswordEncoder passwordEncoder, CustomerDTOMapper customerDTOMapper, S3Service s3Service, S3Buckets buckets) {
        this.customerDAO = customerDAO;
        this.passwordEncoder = passwordEncoder;
        this.customerDTOMapper = customerDTOMapper;
        this.s3Service = s3Service;
        this.buckets = buckets;
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

    public void uploadCustomerProfileImage(Integer customerId, MultipartFile file) {
        if(!customerDAO.existsPersonWithId(customerId)){
            throw new ResourceNotFoundException("Customer with id [%s] not found!".formatted(customerId));
        }
        String profileImageId = UUID.randomUUID().toString();
        try {
            s3Service.putObject(buckets.getCustomer(), "profile-images/%s/%s".formatted(customerId, profileImageId), file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("failed to upload customer profile image", e);
        }
        customerDAO.updateCustomerProfileImageId(profileImageId, customerId);
    }

    public byte[] getCustomerProfileImage(Integer customerId) {
        Customer customer = customerDAO.selectCustomerById(customerId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Customer with id [%s] not found!".formatted(customerId))
                );
        String profileImageId = customer.getProfileImageId();
        if(profileImageId == null || profileImageId.isBlank()){
            throw new ResourceNotFoundException("Customer with id [%s] profile image not found!".formatted(customerId));
        }
        return s3Service.getObject(buckets.getCustomer(), "profile-images/%s/%s".formatted(customerId, customer.getProfileImageId()));
    }
}
