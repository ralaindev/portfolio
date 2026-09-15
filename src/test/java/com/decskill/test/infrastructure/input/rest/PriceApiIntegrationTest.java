package com.decskill.test.infrastructure.input.rest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PriceApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest(name = "{0} -> tarifa {1}")
    @CsvSource({
            "2020-06-14T10:00:00+02:00, 1, 35.50, 2020-06-14T00:00:00+02:00, 2020-12-31T23:59:59+02:00",
            "2020-06-14T16:00:00+02:00, 2, 25.45, 2020-06-14T15:00:00+02:00, 2020-06-14T18:30:00+02:00",
            "2020-06-14T21:00:00+02:00, 1, 35.50, 2020-06-14T00:00:00+02:00, 2020-12-31T23:59:59+02:00",
            "2020-06-15T10:00:00+02:00, 3, 30.50, 2020-06-15T00:00:00+02:00, 2020-06-15T11:00:00+02:00",
            "2020-06-16T21:00:00+02:00, 4, 38.95, 2020-06-15T16:00:00+02:00, 2020-12-31T23:59:59+02:00"
    })
    void shouldMeetAcceptanceCases(String queryDate, int priceList, double price,
                                   String startDate, String endDate) throws Exception {
        mockMvc.perform(get("/api/v1/prices")
                        .param("queryDate", queryDate)
                        .param("productId", "35455")
                        .param("brandId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(35455))
                .andExpect(jsonPath("$.brandId").value(1))
                .andExpect(jsonPath("$.priceList").value(priceList))
                .andExpect(jsonPath("$.startDate").value(startDate))
                .andExpect(jsonPath("$.endDate").value(endDate))
                .andExpect(jsonPath("$.price").value(price))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    @Test
    void shouldReturnNotFoundWhenNoApplicablePriceExists() throws Exception {
        mockMvc.perform(get("/api/v1/prices")
                        .param("queryDate", "2019-06-14T10:00:00+02:00")
                        .param("productId", "35455")
                        .param("brandId", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("No se ha encontrado un precio aplicable."));
    }

    @ParameterizedTest
    @CsvSource({
            "invalid-date,35455,1",
            "2020-06-14T16:00:00+02:00,abc,1",
            "2020-06-14T16:00:00+02:00,0,1",
            "2020-06-14T16:00:00+02:00,35455,-1"
    })
    void shouldReturnBadRequestForInvalidParameters(String date, String product, String brand) throws Exception {
        mockMvc.perform(get("/api/v1/prices").param("queryDate", date)
                        .param("productId", product).param("brandId", brand))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Parámetros de entrada no válidos."));
    }

    @ParameterizedTest
    @ValueSource(strings = {"queryDate", "productId", "brandId"})
    void shouldReturnBadRequestWhenRequiredParameterIsMissing(String missing) throws Exception {
        var request = get("/api/v1/prices");
        if (!"queryDate".equals(missing)) {
            request.param("queryDate", "2020-06-14T16:00:00+02:00");
        }
        if (!"productId".equals(missing)) {
            request.param("productId", "35455");
        }
        if (!"brandId".equals(missing)) {
            request.param("brandId", "1");
        }

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Parámetros de entrada no válidos."));
    }
}
