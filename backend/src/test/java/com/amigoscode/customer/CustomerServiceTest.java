package com.amigoscode.customer;

import com.amigoscode.exception.DuplicateResourceException;
import com.amigoscode.exception.RequestValidationException;
import com.amigoscode.exception.ResourceNotFoundException;
import com.amigoscode.s3.S3Buckets;
import com.amigoscode.s3.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    @Mock
    private S3Service s3Service;
    @Mock
    private S3Buckets buckets;
    private CustomerService underTest;
    private final CustomerDTOMapper customerDTOMapper = new CustomerDTOMapper();

    @BeforeEach
    void setUp() {
        underTest = new CustomerService(customerDAO, passwordEncoder, customerDTOMapper, s3Service, buckets);
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

    @Test
    void canUploadProfileImage() {
        //Given
        int id = 1;
        when(customerDAO.existsPersonWithId(id)).thenReturn(true);
        String fileName = "file";
        byte[] fileContent = "Hello World".getBytes();
        MultipartFile uploadedFile = new MockMultipartFile(fileName, fileContent);
        String bucketName = "customer";
        when(buckets.getCustomer()).thenReturn(bucketName);
        underTest.uploadCustomerProfileImage(id, uploadedFile);

        ArgumentCaptor<String> profileImageIdArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(customerDAO).updateCustomerProfileImageId(profileImageIdArgumentCaptor.capture(), eq(id));
        verify(s3Service).putObject(bucketName, "profile-images/%s/%s".formatted(id, profileImageIdArgumentCaptor.getValue()), fileContent);
    }

    @Test
    void canNotUploadProfileImageWhenCustomerDoesNotExists() {
        //Given
        int id = 1;
        when(customerDAO.existsPersonWithId(id)).thenReturn(false);

        //When
        assertThatThrownBy(() -> underTest.uploadCustomerProfileImage(id, mock(MultipartFile.class)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer with id [%s] not found!".formatted(id));

        //Then
        verify(customerDAO).existsPersonWithId(id);
        verifyNoMoreInteractions(customerDAO);
        verifyNoInteractions(buckets);
        verifyNoInteractions(s3Service);
    }

    @Test
    void canNotUploadProfileImageExceptionIsThrown() throws IOException {
        //Given
        int id = 1;
        when(customerDAO.existsPersonWithId(id)).thenReturn(true);
        String fileName = "file";
        byte[] fileContent = "Hello World".getBytes();
        MultipartFile uploadedFile = mock(MultipartFile.class);
        when(uploadedFile.getBytes()).thenThrow(IOException.class);
        String bucketName = "customer";
        when(buckets.getCustomer()).thenReturn(bucketName);

        assertThatThrownBy(() -> underTest.uploadCustomerProfileImage(id, uploadedFile))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("failed to upload customer profile image")
                .hasRootCauseInstanceOf(IOException.class);
        verify(customerDAO, never()).updateCustomerProfileImageId(any(), any());
    }

    @Test
    void canDownloadProfileImage() {
        //Given
        int id = 1;
        Customer customer = new Customer(id, "ljudina@gmail.com", "password", "Marko", 40, Gender.MALE, "2222");
        byte[] profileImage = "User Image Test".getBytes();
        when(customerDAO.selectCustomerById(id)).thenReturn(Optional.of(customer));
        when(s3Service.getObject(buckets.getCustomer(), "profile-images/%s/%s".formatted(id, customer.getProfileImageId()))).thenReturn(profileImage);
        byte[] actualImage = underTest.getCustomerProfileImage(id);
        assertThat(actualImage).isEqualTo(profileImage);
    }

    @Test
    void canNotDownloadCustomerProfileImageIfNotSet() {
        //Given
        int id = 1;
        Customer customer = new Customer(id, "ljudina@gmail.com", "password", "Marko", 40, Gender.MALE);
        when(customerDAO.selectCustomerById(id)).thenReturn(Optional.of(customer));
        //When
        assertThatThrownBy(() -> underTest.getCustomerProfileImage(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer with id [%s] profile image not found!".formatted(id));
        //Then
        verifyNoInteractions(buckets);
        verifyNoInteractions(s3Service);
    }

    @Test
    void canNotDownloadCustomerProfileImageIfCustomerDoesNotExists() {
        int id = 1;
        when(customerDAO.selectCustomerById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> underTest.getCustomerProfileImage(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer with id [%s] not found!".formatted(id));
        verifyNoInteractions(buckets);
        verifyNoInteractions(s3Service);
    }
}