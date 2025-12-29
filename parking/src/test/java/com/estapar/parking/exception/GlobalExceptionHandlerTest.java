package com.estapar.parking.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Deve tratar IllegalStateException com 409 Conflict")
    void shouldHandleBusinessException() {
        IllegalStateException ex = new IllegalStateException("Regra quebrada");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleBusinessException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Regra quebrada", response.getBody().message());
    }

    @Test
    @DisplayName("Deve tratar Erro Genérico com 500 Internal Server Error")
    void shouldHandleGenericException() {
        RuntimeException ex = new RuntimeException("Erro banco de dados");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Ocorreu um erro interno no servidor.", response.getBody().message());
    }

    @Test
    @DisplayName("Deve tratar JSON Inválido com 400 Bad Request")
    void shouldHandleJsonError() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getMessage()).thenReturn("Erro de parse");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleJsonError(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody().message());
    }

    @Test
    @DisplayName("Deve tratar Recurso Não Encontrado com 404 Not Found")
    void shouldHandleNotFound() {
        NoResourceFoundException ex = new NoResourceFoundException(org.springframework.http.HttpMethod.GET, "swagger-ui");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}