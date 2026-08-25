package com.robert.portfolio.orders.infrastructure.web;

import com.robert.portfolio.orders.application.port.in.CreateOrderUseCase;
import com.robert.portfolio.orders.application.port.in.GetOrderUseCase;
import com.robert.portfolio.orders.domain.model.CustomerId;
import com.robert.portfolio.orders.domain.model.IdempotencyKey;
import com.robert.portfolio.orders.domain.model.Money;
import com.robert.portfolio.orders.domain.model.Order;
import com.robert.portfolio.orders.domain.model.OrderId;
import com.robert.portfolio.orders.domain.model.OrderLine;
import com.robert.portfolio.orders.domain.model.ProductId;
import com.robert.portfolio.orders.domain.model.Quantity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(com.robert.portfolio.orders.infrastructure.configuration.WebCorsConfiguration.class)
class OrderControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateOrderUseCase createOrderUseCase;

    @MockitoBean
    private GetOrderUseCase getOrderUseCase;

    @MockitoBean
    private OrderWebMapper mapper;

    @Test
    void shouldReturnCreatedOrderAndCorrelationId() throws Exception {
        UUID orderId = UUID.randomUUID();
        Order createdOrder = order(orderId);
        when(createOrderUseCase.create(any())).thenReturn(createdOrder);
        when(mapper.toCommand(any(), any())).thenReturn(null);
        when(mapper.toResponse(createdOrder)).thenReturn(new OrderResponse(
                orderId,
                createdOrder.customerId().value(),
                "PENDING",
                List.of(),
                new BigDecimal("10.00"),
                "EUR",
                createdOrder.createdAt(),
                createdOrder.updatedAt()));

        mockMvc.perform(post("/api/v1/orders")
                        .header("Idempotency-Key", "web-test")
                        .header("X-Correlation-Id", "web-correlation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/orders/" + orderId))
                .andExpect(header().string("X-Correlation-Id", "web-correlation"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldReturnBadRequestForInvalidRequestSyntax() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .header("Idempotency-Key", "web-invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": null, "lines": []}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    void shouldAllowCorsRequestsFromLocalFrontend() throws Exception {
        mockMvc.perform(options("/api/v1/orders")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type,idempotency-key"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    private Order order(UUID orderId) {
        Currency eur = Currency.getInstance("EUR");
        return Order.create(
                new OrderId(orderId),
                new CustomerId(UUID.randomUUID()),
                List.of(new OrderLine(
                        new ProductId(UUID.randomUUID()),
                        new Quantity(1),
                        new Money(new BigDecimal("10.00"), eur))),
                new IdempotencyKey("web-test"),
                "fingerprint",
                Instant.parse("2026-01-01T00:00:00Z"));
    }

    private String validRequest() {
        return """
                {
                  "customerId": "ad2f3742-a4a6-4b9c-aed1-45884cc7bd21",
                  "lines": [{
                    "productId": "55db5caf-b0da-49ab-b346-b7581a91ee76",
                    "quantity": 1,
                    "unitPrice": 10.00
                  }]
                }
                """;
    }
}
