package com.amigoscode;

import com.amigoscode.customer.Customer;
import com.amigoscode.customer.CustomerRepository;
import com.amigoscode.customer.Gender;
import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Random;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
    @Bean
    CommandLineRunner runner(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            var faker = new Faker();
            var firstName = faker.name().firstName();
            var lastName = faker.name().lastName();
            Random random = new Random();
            Customer customer = new Customer(
                    firstName + " " + lastName,
                    firstName.toLowerCase()+"."+lastName.toLowerCase()+"@example.com",
                    passwordEncoder.encode("password"), random.nextInt(16, 99),
                    Gender.MALE
            );
            customerRepository.save(customer);
        };
    }

}
