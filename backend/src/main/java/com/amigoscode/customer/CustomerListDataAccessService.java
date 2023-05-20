package com.amigoscode.customer;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository("list")
public class CustomerListDataAccessService implements CustomerDAO{

    private static List<Customer> customers;
    static {
        customers = new ArrayList<>();
        Customer alex = new Customer(
                1,
                "Alex",
                "alex@gmail.com",
                21,
                Gender.MALE
        );
        customers.add(alex);
        Customer jamila = new Customer(
                2,
                "Jamila",
                "jamila@gmail.com",
                19,
                Gender.FEMALE
        );
        customers.add(jamila);
    }

    @Override
    public List<Customer> selectAllCustomers() {
        return customers;
    }

    @Override
    public Optional<Customer> selectCustomerById(Integer id) {
        return customers.stream()
                .filter(customer -> customer.getId().equals(id))
                .findFirst();
    }

    @Override
    public void insertCustomer(Customer customer) {
        Long id = customers.stream().count() + 1;
        customer.setId(id.intValue());
        customers.add(customer);
    }

    @Override
    public void updateCustomer(Customer customer) {
        int index = 0;
        for(int i = 0; i < customers.stream().count(); i++){
            Customer currentCustomer = customers.get(i);
            if(currentCustomer.getId().equals(customer.getId())){
                index = i;
                break;
            }
        }
        customers.set(index, customer);
    }

    @Override
    public boolean existsPersonWithEmail(String email) {
        return customers.stream()
                .anyMatch(customer -> customer.getEmail().equals(email));
    }

    @Override
    public boolean existsPersonWithId(Integer id) {
        return customers.stream()
                .anyMatch(customer -> customer.getId().equals(id));
    }

    @Override
    public void deleteCustomerById(Integer id) {
        customers.removeIf(customer -> customer.getId().equals(id));
    }
}
