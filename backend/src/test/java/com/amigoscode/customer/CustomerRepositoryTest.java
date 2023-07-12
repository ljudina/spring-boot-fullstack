package com.amigoscode.customer;

import com.amigoscode.AbstractTestContainers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestConfig.class})
class CustomerRepositoryTest extends AbstractTestContainers {

    @Autowired
    private CustomerRepository underTest;

    @Test
    void existsPersonWithEmail() {
        var email = FAKER.internet().emailAddress() + "-" + UUID.randomUUID();
        Customer customer = new Customer(
                FAKER.name().fullName(),
                email,
                "password", 20,
                Gender.MALE
        );
        underTest.save(customer);
        assertThat(underTest.existsCustomerByEmail(email)).isTrue();
    }

    @Test
    void existsPersonWithEmailNotPresent() {
        var email = FAKER.internet().emailAddress() + "-" + UUID.randomUUID();
        assertThat(underTest.existsCustomerByEmail(email)).isFalse();
    }

    @Test
    void existsPersonWithId() {
        var email = FAKER.internet().emailAddress() + "-" + UUID.randomUUID();
        Customer customer = new Customer(
                FAKER.name().fullName(),
                email,
                "password", 20,
                Gender.MALE
        );
        underTest.save(customer);
        int id = underTest.findAll()
                .stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();
        assertThat(underTest.existsCustomerById(id)).isTrue();
    }

    @Test
    void existsPersonWithIdNotPresent(){
        int id = -1;
        assertThat(underTest.existsCustomerById(id)).isFalse();
    }

    @Test
    void canUpdateProfileImageId() {
        var email = FAKER.internet().emailAddress() + "-" + UUID.randomUUID();
        Customer customer = new Customer(
                FAKER.name().fullName(),
                email,
                "password", 20,
                Gender.MALE
        );
        underTest.save(customer);
        int id = underTest.findAll()
                .stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();

        String profileImageId = "222";
        underTest.updateProfileImageId(profileImageId, id);
        Optional<Customer> customerOptional = underTest.findById(id);
        assertThat(customerOptional).isPresent().hasValueSatisfying(c -> {
          assertThat(c.getProfileImageId()).isEqualTo(profileImageId);
        });
    }
}