package com.example.store.service;

import org.springframework.stereotype.Service;

import com.example.store.dto.request.AvailabilityRequest;
import com.example.store.dto.request.ReleaseRequest;
import com.example.store.dto.request.ReserveRequest;
import com.example.store.dto.response.AvailabilityResponse;
import com.example.store.dto.response.ReserveResponse;
import com.example.store.entity.WarehouseStock;
import com.example.store.repository.WarehouseStockRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService{
    private final WarehouseStockRepository repo;

    @Override
    public AvailabilityResponse plan(AvailabilityRequest req) {
        // no lock
        List<WarehouseStock> rows = repo.findAllForProduct(req.getProductId());;

        int needed = req.getQuantity();
        List<AvailabilityResponse.WarehouseAllocation> plan = new ArrayList<>();

        for (var ws : rows) {
            int avail = Math.max(0, ws.getQuantity());
            if (avail <= 0) continue;
            int take = Math.min(avail, needed);
            if (take > 0) plan.add(new AvailabilityResponse.WarehouseAllocation(ws.getWarehouseId(), take));
            needed -= take;
            if (needed == 0) break;
        }

        var res = new AvailabilityResponse();
        res.setCanFulfill(needed == 0);
        res.setAllocations(plan);
        return res;
    }

    @Override
    @Transactional
    public ReserveResponse reserve(ReserveRequest req) {
        // Plan under lock and immediately apply
        var plan = plan(new AvailabilityRequest(){{
        setProductId(req.getProductId()); setQuantity(req.getQuantity());
        }});
        if (!plan.isCanFulfill()) throw new IllegalStateException("Insufficient stock for product=" + req.getProductId());

        List<ReleaseRequest.Alloc> applied = new ArrayList<>();

        for (var a : plan.getAllocations()) {
            var row = repo.findByWarehouseIdAndProductId(a.getWarehouseId(), req.getProductId())
                .orElseThrow();
            if (row.getQuantity() < a.getAvailable())
                throw new IllegalStateException("Concurrent update detected");
            row.setQuantity(row.getQuantity() - a.getAvailable());
            repo.save(row);

            var alloc = new ReleaseRequest.Alloc();
                alloc.setWarehouseId(a.getWarehouseId());
                alloc.setQty(a.getAvailable());
                applied.add(alloc);
        }

        log.info("RESERVE order={} product={} allocations={}", req.getOrderId(), req.getProductId(), applied);
        var resp = new ReserveResponse();
        resp.setOrderId(req.getOrderId());
        resp.setProductId(req.getProductId());
        resp.setAllocations(applied);
        return resp;
    }

    @Override
    public void confirm(Integer orderId) {
        log.info("CONFIRM order={}", orderId);
    }

    @Override
    @Transactional
    public void release(ReleaseRequest req) {
        for (var a : req.getAllocations()) {
        var row = repo.findByWarehouseIdAndProductId(a.getWarehouseId(), req.getProductId())
            .orElseThrow();
        row.setQuantity(row.getQuantity() + a.getQty());
        repo.save(row);
        }
        log.info("RELEASE order={} product={} restored={}", req.getOrderId(), req.getProductId(), req.getAllocations());
    }
}
