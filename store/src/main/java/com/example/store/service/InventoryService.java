package com.example.store.service;

import com.example.store.dto.request.AvailabilityRequest;
import com.example.store.dto.request.ReleaseRequest;
import com.example.store.dto.request.ReserveRequest;
import com.example.store.dto.response.AvailabilityResponse;
import com.example.store.dto.response.ReserveResponse;

public interface InventoryService {
    AvailabilityResponse plan(AvailabilityRequest req);
    ReserveResponse reserve(ReserveRequest req);
    void confirm(Integer orderId);
    void release(ReleaseRequest req);
}
