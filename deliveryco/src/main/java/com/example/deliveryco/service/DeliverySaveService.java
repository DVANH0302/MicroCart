package com.example.deliveryco.service;


import com.example.deliveryco.dto.request.DeliveryRequest;
import com.example.deliveryco.entity.Delivery;
import com.example.deliveryco.entity.DeliveryStatus;
import com.example.deliveryco.repository.DeliveryRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class DeliverySaveService {

    private final DeliveryRepository deliveryRepository;

    public DeliverySaveService(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }

    @Transactional
    public Delivery save(DeliveryRequest deliveryRequest) {
        Delivery delivery = Delivery.Builder.newBuilder()
                .orderId(deliveryRequest.getOrderId())
                .customerName(deliveryRequest.getUserFullName())
                .customerEmail(deliveryRequest.getUserEmail())
                .address(deliveryRequest.getAddress())
                .warehouseIds(deliveryRequest.getWarehouseIds())
                .status(DeliveryStatus.RECIEVED.name())
                .build();

        Delivery savedDelivery =  deliveryRepository.save(delivery);
        return savedDelivery;
    }
}
