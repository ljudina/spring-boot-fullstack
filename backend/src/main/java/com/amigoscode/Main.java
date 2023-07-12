package com.amigoscode;

import com.amigoscode.customer.Customer;
import com.amigoscode.customer.CustomerRepository;
import com.amigoscode.customer.Gender;
import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.amigoscode.s3.S3Service;
import com.amigoscode.s3.S3Buckets;

import java.util.Random;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
    @Bean
    CommandLineRunner runner(CustomerRepository customerRepository, PasswordEncoder passwordEncoder/*, S3Service s3Service, S3Buckets s3Buckets*/) {
        return args -> {
            createRandomUser(customerRepository, passwordEncoder);
            //testBucketUploadDownload(s3Service, s3Buckets);
        };
    }

    private static void testBucketUploadDownload(S3Service s3Service, S3Buckets s3Buckets) {
        s3Service.putObject(
                s3Buckets.getCustomer(),
                "foo",
                "Hello World".getBytes()
        );
        byte[] object = s3Service.getObject(
                s3Buckets.getCustomer(),
                "foo"
        );
        System.out.println("Hooray " + new String(object));
    }

    private static void createRandomUser(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
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
    }

}
