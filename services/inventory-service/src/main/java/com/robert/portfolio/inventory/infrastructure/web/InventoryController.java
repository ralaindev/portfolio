package com.robert.portfolio.inventory.infrastructure.web;

import com.robert.portfolio.inventory.application.usecase.ReserveStockService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/stock/reservations")
public class InventoryController {

    private final ReserveStockService reserveStockService;
    private final InventoryWebMapper mapper;

    public InventoryController(ReserveStockService reserveStockService, InventoryWebMapper mapper) {
        this.reserveStockService = reserveStockService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<StockReservationResponse> reserve(
            @Valid @RequestBody ReserveStockRequest request,
            @RequestParam(defaultValue = "false") boolean pessimistic) {
        var reservation = pessimistic
                ? reserveStockService.reservePessimistically(request.orderId(), request.productId(), request.quantity())
                : reserveStockService.reserve(request.orderId(), request.productId(), request.quantity());
        var response = mapper.toResponse(reservation);
        return ResponseEntity.created(URI.create("/api/v1/stock/reservations/" + response.reservationId()))
                .body(response);
    }
}
