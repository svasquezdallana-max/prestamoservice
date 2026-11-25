package com.santacruz.prestamoservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PrestamoserviceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void flujoCompletoConTokenYCarteraAislada() throws Exception {
        // 1) Sin token debe responder 401.
        mockMvc.perform(get("/prestamos")).andExpect(status().isUnauthorized());

        // 2) Registro público devuelve token.
        String tokenUno = registrar("ana@correo.com", "Ana");
        String tokenDos = registrar("beto@correo.com", "Beto");
        assertNotEquals(tokenUno, tokenDos);

        // 3) Login también devuelve token (puede cambiar por la fecha de emisión).
        String tokenLogin = login("ana@correo.com");
        mockMvc.perform(get("/prestamos")
                        .header("Authorization", "Bearer " + tokenLogin))
                .andExpect(status().isOk());

        // 4) Crear préstamo con token y verificar el total calculado.
        String cuerpoPrestamo = """
                {"cliente":"Juan","dni":"12345678","monto":1000.00,
                 "interes":10.00,"fechaVencimiento":"2026-12-31","pagado":false}
                """;

        MvcResult creado = mockMvc.perform(post("/prestamos")
                        .header("Authorization", "Bearer " + tokenUno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoPrestamo))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(creado.getResponse().getContentAsString());
        long id = json.get("id").asLong();
        assertEquals(0, new BigDecimal("1100.00")
                .compareTo(json.get("totalACobrar").decimalValue()));

        // 5) El dueño lo ve; otro prestamista NO lo ve (404).
        mockMvc.perform(get("/prestamos/" + id)
                        .header("Authorization", "Bearer " + tokenUno))
                .andExpect(status().isOk());
        mockMvc.perform(get("/prestamos/" + id)
                        .header("Authorization", "Bearer " + tokenDos))
                .andExpect(status().isNotFound());

        // 6) Marcar como pagado y borrar.
        String editado = """
                {"cliente":"Juan editado","dni":"12345678","monto":1000.00,
                 "interes":10.00,"fechaVencimiento":"2026-12-31","pagado":true}
                """;
        mockMvc.perform(put("/prestamos/" + id)
                        .header("Authorization", "Bearer " + tokenUno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(editado))
                .andExpect(status().isOk());

        mockMvc.perform(get("/prestamos/resumen")
                        .header("Authorization", "Bearer " + tokenUno))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/prestamos/" + id)
                        .header("Authorization", "Bearer " + tokenUno))
                .andExpect(status().isNoContent());
    }

    private String registrar(String email, String nombre) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(
                                "{\"nombre\":\"%s\",\"email\":\"%s\",\"password\":\"clave123\"}",
                                nombre, email)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(resultado.getResponse().getContentAsString())
                .get("token").asText();
    }

    private String login(String email) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(
                                "{\"email\":\"%s\",\"password\":\"clave123\"}", email)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(resultado.getResponse().getContentAsString())
                .get("token").asText();
    }
}
