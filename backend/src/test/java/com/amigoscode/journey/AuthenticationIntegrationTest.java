package com.amigoscode.journey;

import com.amigoscode.auth.AuthenticationRequest;
import com.amigoscode.auth.AuthenticationResponse;
import com.amigoscode.customer.CustomerDTO;
import com.amigoscode.customer.CustomerRegistrationRequest;
import com.amigoscode.customer.Gender;
import com.amigoscode.jwt.JWTUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.github.javafaker.Faker;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthenticationIntegrationTest {
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private JWTUtil jwtUtil;

    private static final Faker FAKER = new Faker();
    private static final Random RANDOM = new Random();
    private static String AUTHENTICATION_PATH = "/api/v1/auth";
    private static String CUSTOMER_PATH = "/api/v1/customers";

    @Test
    void canLogin() {
        //create registration request
        String name = FAKER.name().fullName();
        String email = name + "-" + UUID.randomUUID() + "@testemail.com";
        int age = RANDOM.nextInt(1, 100);
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(name, email, "password", age, Gender.MALE);
        AuthenticationRequest authRequest = new AuthenticationRequest(request.email(), request.password());
        webTestClient.post()
                .uri(AUTHENTICATION_PATH + "/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(authRequest), AuthenticationRequest.class)
                .exchange()
                .expectStatus()
                .isUnauthorized();

        //Register customer
        webTestClient.post()
                .uri(CUSTOMER_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), CustomerRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isOk();

        EntityExchangeResult<AuthenticationResponse> result = webTestClient.post()
                .uri(AUTHENTICATION_PATH + "/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(authRequest), AuthenticationRequest.class)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<AuthenticationResponse>() {
                })
                .returnResult();

        String jwtToken = result.getResponseHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
        CustomerDTO customerDTO = result.getResponseBody().customerDTO();
        assertThat(jwtUtil.isTokenValid(jwtToken, customerDTO.username())).isTrue();
        assertThat(customerDTO.email()).isEqualTo(email);
        assertThat(customerDTO.age()).isEqualTo(age);
        assertThat(customerDTO.name()).isEqualTo(name);
        assertThat(customerDTO.username()).isEqualTo(email);
        assertThat(customerDTO.gender()).isEqualTo(Gender.MALE);
        assertThat(customerDTO.roles()).isEqualTo(List.of("ROLE_USER"));
    }
}
