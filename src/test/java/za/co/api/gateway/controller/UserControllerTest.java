package za.co.api.gateway.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserControllerTest {
    private RestTemplate restTemplate;
    private UserController userController;

//    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        userController = new UserController();
        userController.restTemplate = restTemplate;
    }

//    @Test
    void loginReturnsOkWhenAuthServiceRespondsSuccessfully() {
        Map<String, String> credentials = Map.of("username", "test", "password", "test");
        Map<String, String> responseBody = Map.of("token", "mock-jwt-token");

        when(restTemplate.postForEntity(eq("http://auth-service/api/user/login"), eq(credentials), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        ResponseEntity<?> response = userController.login(credentials);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseBody, response.getBody());
    }

//    @Test
    void loginReturnsUnauthorizedWhenAuthServiceFails() {
        Map<String, String> credentials = Map.of("username", "test", "password", "wrong");

        when(restTemplate.postForEntity(any(String.class), any(Object.class), eq(Map.class)))
                .thenThrow(new RuntimeException("Unauthorized"));

        ResponseEntity<?> response = userController.login(credentials);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(Map.of("error", "Login failed: Unauthorized"), response.getBody());
    }

//    @Test
    void registerReturnsOkWhenAuthServiceRespondsSuccessfully() {
        Map<String, String> credentials = Map.of("username", "test", "password", "test");
        Map<String, String> responseBody = Map.of("message", "User registered successfully");

        when(restTemplate.postForEntity(eq("http://auth-service/api/user/register"), eq(credentials), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        ResponseEntity<?> response = userController.register(credentials);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseBody, response.getBody());
    }

//    @Test
    void registerReturnsInternalServerErrorWhenAuthServiceFails() {
        Map<String, String> credentials = Map.of("username", "test", "password", "test");

        when(restTemplate.postForEntity(any(String.class), any(Object.class), eq(Map.class)))
                .thenThrow(new RuntimeException("Registration failed"));

        ResponseEntity<?> response = userController.register(credentials);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(Map.of("error", "Registration failed: Registration failed"), response.getBody());
    }

//    @Test
    void resetPasswordReturnsOkWhenAuthServiceRespondsSuccessfully() {
        Map<String, String> credentials = Map.of("username", "test", "password", "new-password");
        Map<String, String> responseBody = Map.of("message", "Password reset successfully");

        when(restTemplate.postForEntity(eq("http://auth-service/api/user/password/reset"), eq(credentials), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        ResponseEntity<?> response = userController.resetPassword(credentials);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseBody, response.getBody());
    }

//    @Test
    void resetPasswordReturnsInternalServerErrorWhenAuthServiceFails() {
        Map<String, String> credentials = Map.of("username", "test", "password", "new-password");

        when(restTemplate.postForEntity(any(String.class), any(Object.class), eq(Map.class)))
                .thenThrow(new RuntimeException("Reset failed"));

        ResponseEntity<?> response = userController.resetPassword(credentials);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(Map.of("error", "Reset failed: Reset failed"), response.getBody());
    }

//    @Test
    void processRequestReturnsErrorResponseWhenHttpStatusCodeExceptionOccurs() {
        Map<String, String> credentials = Map.of("username", "test", "password", "test");
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);

        when(exception.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(exception.getResponseBodyAsString()).thenReturn("Invalid request");
        when(restTemplate.postForEntity(any(String.class), any(Object.class), eq(Map.class)))
                .thenThrow(exception);

        ResponseEntity<?> response = userController.login(credentials);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(Map.of("error", "Invalid request"), response.getBody());
    }
}