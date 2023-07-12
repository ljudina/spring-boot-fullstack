package com.amigoscode.s3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Client s3Client;
    private S3Service underTest;

    @BeforeEach
    void setUp(){
        this.underTest = new S3Service(s3Client);
    }
    @Test
    void canPutObject() {
        //Given
        String bucket = "customer";
        String key = "foo";
        byte[] file = "Hello World!".getBytes();
        //When
        underTest.putObject(bucket, key, file);
        //Then
        ArgumentCaptor<PutObjectRequest> putObjectRequestArgumentCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> requestBodyArgumentCaptor = ArgumentCaptor.forClass(RequestBody.class);
        verify(s3Client).putObject(putObjectRequestArgumentCaptor.capture(), requestBodyArgumentCaptor.capture());
        PutObjectRequest putObjectRequestValue = putObjectRequestArgumentCaptor.getValue();
        assertThat(putObjectRequestValue.bucket()).isEqualTo(bucket);
        assertThat(putObjectRequestValue.key()).isEqualTo(key);
        RequestBody requestBodyValue = requestBodyArgumentCaptor.getValue();
        try {
            assertThat(requestBodyValue
                    .contentStreamProvider()
                    .newStream()
                    .readAllBytes()
            ).isEqualTo(
                    RequestBody
                            .fromBytes(file)
                            .contentStreamProvider()
                            .newStream()
                            .readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void canGetObject() throws IOException{
        //Given
        String bucket = "customer";
        String key = "foo";
        byte[] file = "Hello World!".getBytes();
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        ResponseInputStream<GetObjectResponse> res = mock(ResponseInputStream.class);
        when(res.readAllBytes()).thenReturn(file);
        //When
        when(s3Client.getObject(eq(objectRequest))).thenReturn(res);
        byte[] receivedFile = underTest.getObject(bucket, key);
        //Then
        assertThat(receivedFile).isEqualTo(file);
    }

    @Test
    void willThrowOnGetObject() throws IOException{
        //Given
        String bucket = "customer";
        String key = "foo";
        byte[] file = "Hello World!".getBytes();
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        ResponseInputStream<GetObjectResponse> res = mock(ResponseInputStream.class);
        String exceptionMessage = "Can not read file";
        when(res.readAllBytes()).thenThrow(new RuntimeException(exceptionMessage));
        //When
        when(s3Client.getObject(eq(objectRequest))).thenReturn(res);
        assertThatThrownBy(()-> underTest.getObject(bucket, key))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(exceptionMessage);
    }
}