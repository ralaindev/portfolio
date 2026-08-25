package com.robert.portfolio.inventory;

import com.robert.portfolio.inventory.application.usecase.ReserveStockService;
import com.robert.portfolio.inventory.domain.exception.StockReservationConflictException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class InventoryServiceApplicationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    private static final UUID PRODUCT_ID = UUID.fromString("55db5caf-b0da-49ab-b346-b7581a91ee76");

    @Autowired
    private ReserveStockService reserveStockService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @BeforeEach
    void resetStock() {
        // Each test starts with exactly one available unit so the final database state is deterministic.
        jdbcTemplate.update("DELETE FROM stock_reservations");
        jdbcTemplate.update("DELETE FROM product_stock");
        jdbcTemplate.update(
                "INSERT INTO product_stock (product_id, available_quantity, reserved_quantity, version) VALUES (?, 1, 0, 0)",
                PRODUCT_ID);
    }

    @Test
    void shouldReserveStockAndUpdateAvailableQuantity() {
        UUID orderId = UUID.randomUUID();

        var reservation = reserveStockService.reserve(orderId, PRODUCT_ID, 1);

        assertThat(reservation.orderId()).isEqualTo(orderId);
        Integer available = jdbcTemplate.queryForObject(
                "SELECT available_quantity FROM product_stock WHERE product_id = ?", Integer.class, PRODUCT_ID);
        Integer reserved = jdbcTemplate.queryForObject(
                "SELECT reserved_quantity FROM product_stock WHERE product_id = ?", Integer.class, PRODUCT_ID);
        assertThat(available).isZero();
        assertThat(reserved).isEqualTo(1);
    }

    @Test
    void shouldServeTheVersionedOpenApiContract() throws Exception {
        mockMvc.perform(get("/openapi/inventory-service-openapi.yaml"))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getContentAsString())
                        .contains("openapi: 3.1.0"));
    }

    @Test
    void shouldApplySameReservationOnlyOnce() {
        UUID orderId = UUID.randomUUID();

        var first = reserveStockService.reserve(orderId, PRODUCT_ID, 1);
        var second = reserveStockService.reserve(orderId, PRODUCT_ID, 1);

        assertThat(second.reservationId()).isEqualTo(first.reservationId());
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM stock_reservations", Integer.class)).isOne();
    }

    @Test
    void shouldAllowOnlyOneConcurrentReservationForTheLastUnit() throws Exception {
        // Both tasks start together; the test observes the database conflict rather than mocking it.
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> attempts = new ArrayList<>();

        try {
            for (int index = 0; index < 2; index++) {
                UUID orderId = UUID.randomUUID();
                attempts.add(executor.submit(() -> {
                    start.await();
                    try {
                        reserveStockService.reserve(orderId, PRODUCT_ID, 1);
                        return true;
                    } catch (StockReservationConflictException exception) {
                        return false;
                    }
                }));
            }
            start.countDown();

            long successfulReservations = 0;
            for (Future<Boolean> attempt : attempts) {
                try {
                    if (attempt.get()) {
                        successfulReservations++;
                    }
                } catch (ExecutionException exception) {
                    throw new AssertionError("Concurrent reservation failed unexpectedly", exception.getCause());
                }
            }

            assertThat(successfulReservations).isOne();
            assertThat(jdbcTemplate.queryForObject(
                    "SELECT available_quantity FROM product_stock WHERE product_id = ?", Integer.class, PRODUCT_ID))
                    .isZero();
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM stock_reservations", Integer.class)).isOne();
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void shouldReserveUsingPessimisticLockingWhenExplicitlySelected() {
        var reservation = reserveStockService.reservePessimistically(UUID.randomUUID(), PRODUCT_ID, 1);

        Assertions.assertThat(reservation.quantity().value()).isOne();
    }
}
