package com.example.store.controller;

import com.example.store.dto.request.AvailabilityRequest;
import com.example.store.dto.request.ReleaseRequest;
import com.example.store.dto.request.ReserveRequest;
import com.example.store.dto.response.AvailabilityResponse;
import com.example.store.dto.response.ReserveResponse;
import com.example.store.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/internal/inventory") // test-only endpoint
@RequiredArgsConstructor
public class InventoryInternalController {

    private final InventoryService inventory;

    @PostMapping("/availability")
    public AvailabilityResponse availability(@RequestBody AvailabilityRequest req) {
        return inventory.plan(req);
    }

    @PostMapping("/reserve")
    public ReserveResponse reserve(@RequestBody ReserveRequest req) {
        return inventory.reserve(req);
    }

    @PostMapping("/confirm/{orderId}")
    public ResponseEntity<Void> confirm(@PathVariable Integer orderId) {
        inventory.confirm(orderId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/release")
    public ResponseEntity<Void> release(@RequestBody ReleaseRequest req) {
        inventory.release(req);
        return ResponseEntity.noContent().build();
    }
}
