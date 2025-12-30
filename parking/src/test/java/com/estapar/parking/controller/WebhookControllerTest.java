package com.estapar.parking.controller;

import com.estapar.parking.dto.WebhookEventDTO;
import com.estapar.parking.service.ParkingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebhookController.class)
class WebhookControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean
    private ParkingService parkingService;

    @Test
    @DisplayName("Deve retornar 400 Bad Request se o JSON estiver inválido (Ex: data errada)")
    void shouldReturn400ForInvalidJson() throws Exception {
        String brokenJson = "{ \"license_plate\": \"ABC-123\", \"event_type\": \"ENTRY\", \"entry_time\": \"DATA_INVALIDA\" }";

        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(brokenJson))
                .andExpect(status().isBadRequest()); // Espera erro 400, não 500
    }

    @Test
    @DisplayName("Deve retornar 409 Conflict se o Service lançar IllegalStateException (Regra de Negócio)")
    void shouldReturn409WhenBusinessRuleFails() throws Exception {
        WebhookEventDTO dto = new WebhookEventDTO();

        doThrow(new IllegalStateException("Carro já existe"))
                .when(parkingService).processEvent(org.mockito.ArgumentMatchers.any());

        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /webhook: Deve retornar 200 OK quando o evento é processado com sucesso")
    void shouldReturn200OnSuccess() throws Exception {
        String validJson = """
            {
                "license_plate": "ABC-1234",
                "event_type": "ENTRY",
                "entry_time": "2025-12-29T10:00:00",
                "lat": -23.5,
                "lng": -46.6
            }
        """;


        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk());
    }
}