package com.decskill.test.infrastructure.input.rest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PriceCrudIntegrationTest {
    private static final String BODY = """
            {"brandId":1,"productId":99999,"priceList":90001,"priority":2,
             "startDate":"2020-06-14T15:00:00+02:00","endDate":"2020-06-14T18:30:00+02:00",
             "price":25.45,"currency":"EUR"}
            """;

    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;

    @AfterEach
    void cleanCreatedPrices() {
        jdbc.update("DELETE FROM prices WHERE price_list = 90001");
    }

    @Test
    void shouldCreateReadUpdateAndDeletePriceAndReflectChangesInApplicablePrice() throws Exception {
        String location = create(BODY);
        mvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.brandId").value(1))
                .andExpect(jsonPath("$.productId").value(99999))
                .andExpect(jsonPath("$.priceList").value(90001))
                .andExpect(jsonPath("$.priority").value(2))
                .andExpect(jsonPath("$.startDate").value("2020-06-14T13:00:00Z"))
                .andExpect(jsonPath("$.endDate").value("2020-06-14T16:30:00Z"))
                .andExpect(jsonPath("$.price").value(25.45))
                .andExpect(jsonPath("$.currency").value("EUR"));

        mvc.perform(put(location).contentType(MediaType.APPLICATION_JSON)
                        .content(BODY.replace("25.45", "19.90").replace("EUR", "USD")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.price").value(19.90));
        mvc.perform(get(location)).andExpect(jsonPath("$.currency").value("USD"));
        mvc.perform(get("/api/v1/prices").param("queryDate", "2020-06-14T14:00:00Z")
                        .param("productId", "99999").param("brandId", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.price").value(19.90));

        mvc.perform(delete(location)).andExpect(status().isNoContent()).andExpect(content().string(""));
        mvc.perform(get(location)).andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
        mvc.perform(get("/api/v1/prices").param("queryDate", "2020-06-14T14:00:00Z")
                        .param("productId", "99999").param("brandId", "1"))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {"get", "put", "delete"})
    void shouldReturnNotFoundForUnknownId(String method) throws Exception {
        var request = switch (method) {
            case "put" -> put("/api/v1/prices/999999").contentType(MediaType.APPLICATION_JSON).content(BODY);
            case "delete" -> delete("/api/v1/prices/999999");
            default -> get("/api/v1/prices/999999");
        };
        mvc.perform(request).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").isNotEmpty());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM prices WHERE price_list = 90001", Integer.class));
    }

    @ParameterizedTest
    @CsvSource({
            "25.45,-1", "25.45,1.234", "25.45,100000000", "25.45,null",
            "EUR,ZZZ", "EUR,eur", "EUR,EURO", "99999,0", "99999,null",
            "18:30:00,14:00:00", "15:00:00,invalid"
    })
    void shouldRejectInvalidCreateAndUpdateWithoutChangingStoredPrice(String from, String to) throws Exception {
        String invalidBody = BODY.replace(from, to);
        mvc.perform(post("/api/v1/prices").contentType(MediaType.APPLICATION_JSON).content(invalidBody))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM prices WHERE price_list = 90001", Integer.class));
        String location = create(BODY);
        mvc.perform(put(location).contentType(MediaType.APPLICATION_JSON).content(invalidBody))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        mvc.perform(get(location)).andExpect(status().isOk()).andExpect(jsonPath("$.price").value(25.45))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "{", "{}", "null"})
    void shouldRejectMissingOrMalformedBody(String body) throws Exception {
        mvc.perform(post("/api/v1/prices").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "abc"})
    void shouldRejectInvalidPathId(String id) throws Exception {
        mvc.perform(get("/api/v1/prices/" + id)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldAcceptZeroAmountAndEqualInstantsWithDifferentOffsets() throws Exception {
        create(BODY.replace("25.45", "0")
                .replace("2020-06-14T18:30:00+02:00", "2020-06-14T13:00:00Z"));
    }

    private String create(String body) throws Exception {
        return mvc.perform(post("/api/v1/prices").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/v1/prices/")))
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn().getResponse().getHeader("Location");
    }
}
