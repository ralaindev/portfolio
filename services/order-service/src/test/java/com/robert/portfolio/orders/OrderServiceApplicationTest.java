package com.robert.portfolio.orders;

import com.robert.portfolio.orders.infrastructure.persistence.OrderQueryLabService;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.hibernate.SessionFactory;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureMockMvc
@Testcontainers
class OrderServiceApplicationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private OrderQueryLabService orderQueryLabService;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @BeforeEach
    void cleanOrders() {
        jdbcTemplate.update("DELETE FROM outbox_events");
        jdbcTemplate.update("DELETE FROM order_lines");
        jdbcTemplate.update("DELETE FROM orders");
    }

    @Test
    void shouldStartApplicationContext() {
    }

    @Test
    void shouldServeTheVersionedOpenApiContract() throws Exception {
        mockMvc.perform(get("/openapi/order-service-openapi.yaml"))
                .andExpect(status().isOk())
                .andExpect(result -> org.assertj.core.api.Assertions.assertThat(result.getResponse().getContentAsString())
                        .contains("openapi: 3.1.0"));
    }

    @Test
    void shouldShowNPlusOneAndTheIndependentFetchAlternatives() throws Exception {
        create("query-lab-1", validRequest(1, "10.00"));
        create("query-lab-2", validRequest(1, "11.00"));
        create("query-lab-3", validRequest(1, "12.00"));
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);

        sessionFactory.getStatistics().clear();
        orderQueryLabService.loadWithNPlusOne();
        long nPlusOneQueries = sessionFactory.getStatistics().getPrepareStatementCount();

        sessionFactory.getStatistics().clear();
        orderQueryLabService.loadWithFetchJoin();
        long fetchJoinQueries = sessionFactory.getStatistics().getPrepareStatementCount();

        sessionFactory.getStatistics().clear();
        orderQueryLabService.loadWithEntityGraph();
        long entityGraphQueries = sessionFactory.getStatistics().getPrepareStatementCount();

        sessionFactory.getStatistics().clear();
        orderQueryLabService.loadWithDtoProjection();
        long projectionQueries = sessionFactory.getStatistics().getPrepareStatementCount();

        org.assertj.core.api.Assertions.assertThat(nPlusOneQueries).isGreaterThan(fetchJoinQueries);
        org.assertj.core.api.Assertions.assertThat(fetchJoinQueries).isEqualTo(entityGraphQueries);
        org.assertj.core.api.Assertions.assertThat(projectionQueries).isEqualTo(1);
    }

    @Test
    void shouldCreateAndRetrieveOrderThroughHttp() throws Exception {
        String request = """
                {
                  "customerId": "ad2f3742-a4a6-4b9c-aed1-45884cc7bd21",
                  "lines": [
                    {
                      "productId": "55db5caf-b0da-49ab-b346-b7581a91ee76",
                      "quantity": 2,
                      "unitPrice": 19.99
                    }
                  ]
                }
                """;

        String response = mockMvc.perform(post("/api/v1/orders")
                        .header("Idempotency-Key", "order-creation-1")
                        .header("X-Correlation-Id", "integration-test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.total").value(39.98))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String orderId = com.jayway.jsonpath.JsonPath.read(response, "$.orderId");
        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId));

        Integer orderCount = jdbcTemplate.queryForObject("SELECT count(*) FROM orders", Integer.class);
        Integer lineCount = jdbcTemplate.queryForObject("SELECT count(*) FROM order_lines", Integer.class);
        Integer eventCount = jdbcTemplate.queryForObject("SELECT count(*) FROM outbox_events", Integer.class);
        org.assertj.core.api.Assertions.assertThat(orderCount).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(lineCount).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(eventCount).isEqualTo(1);
    }

    @Test
    void shouldReturnPreviousOrderWhenIdempotencyKeyAndContentAreRepeated() throws Exception {
        String request = validRequest(1, "10.00");

        String firstResponse = create("same-key", request);
        String secondResponse = create("same-key", request);

        String firstOrderId = com.jayway.jsonpath.JsonPath.read(firstResponse, "$.orderId");
        String secondOrderId = com.jayway.jsonpath.JsonPath.read(secondResponse, "$.orderId");
        org.assertj.core.api.Assertions.assertThat(secondOrderId).isEqualTo(firstOrderId);
        Integer orderCount = jdbcTemplate.queryForObject("SELECT count(*) FROM orders", Integer.class);
        Integer eventCount = jdbcTemplate.queryForObject("SELECT count(*) FROM outbox_events", Integer.class);
        org.assertj.core.api.Assertions.assertThat(orderCount).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(eventCount).isEqualTo(1);
    }

    @Test
    void shouldRejectDifferentContentForAnExistingIdempotencyKey() throws Exception {
        create("conflicting-key", validRequest(1, "10.00"));

        mockMvc.perform(post("/api/v1/orders")
                        .header("Idempotency-Key", "conflicting-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest(2, "10.00")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Idempotency conflict"))
                .andExpect(jsonPath("$.correlationId").exists());
    }

    @Test
    void shouldReturnValidationProblemForAnInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .header("Idempotency-Key", "invalid-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": null, "lines": []}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.correlationId").exists());
    }

    private String create(String idempotencyKey, String request) throws Exception {
        return mockMvc.perform(post("/api/v1/orders")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private String validRequest(int quantity, String unitPrice) {
        return """
                {
                  "customerId": "ad2f3742-a4a6-4b9c-aed1-45884cc7bd21",
                  "lines": [{
                    "productId": "55db5caf-b0da-49ab-b346-b7581a91ee76",
                    "quantity": %d,
                    "unitPrice": %s
                  }]
                }
                """.formatted(quantity, unitPrice);
    }
}
