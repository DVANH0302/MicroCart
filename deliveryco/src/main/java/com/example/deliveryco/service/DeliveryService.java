package com.example.deliveryco.service;

import com.example.deliveryco.config.RabbitMQConfig;
import com.example.deliveryco.dto.request.DeliveryRequest;
import com.example.deliveryco.entity.Delivery;
import com.example.deliveryco.entity.DeliveryStatus;
import com.example.deliveryco.repository.DeliveryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@Slf4j
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final Random random = new Random();
    private final DeliveryUpdateService deliveryUpdateService;
    private final DeliverySaveService deliverySaveService;

    public DeliveryService( DeliveryRepository deliveryRepository, DeliveryUpdateService deliveryUpdateService,  DeliverySaveService deliverySaveService) {
        this.deliveryRepository = deliveryRepository;
        this.deliveryUpdateService = deliveryUpdateService;
        this.deliverySaveService = deliverySaveService;
    }

    public void processDelivery(DeliveryRequest deliveryRequest) {
        log.info("Successfully recieved deliveryRequest: {}", deliveryRequest);


        if (deliveryRepository.findByOrderId(deliveryRequest.getOrderId()).isPresent()) {
//            String message = String.format("Order ID %d already exists", deliveryRequest.getOrderId());

            log.error("Order ID {} already exists. No need to do anything!", deliveryRequest.getOrderId());
            return;
        }


        // saving the delivery in db
        Delivery delivery  = deliverySaveService.save(deliveryRequest);


        log.info("Successfully saved deliveryRequest: {}", deliveryRequest);
        log.info("Delivery after being saved: {}", delivery);

         new Thread(() -> {
             int orderId = deliveryRequest.getOrderId();
             try {
                 // PICKED_UP
                 log.info("orderId {} is PICKED UP", orderId);
                 Thread.sleep(5000);
                 saveAndSendUpdate(orderId, DeliveryStatus.PICKED_UP);

                 // ON_DELIVERY
                 log.info("orderId {} is  ON_DELIVERY", orderId);
                 Thread.sleep(5000);
                 saveAndSendUpdate(orderId, DeliveryStatus.ON_DELIVERY);

                 // DELIVERED OR LOST (5%)
                 Thread.sleep(5000);
                 if (random.nextInt(100) < 5) {
                     log.info("orderId {} is  LOST", orderId);
                     saveAndSendUpdate(orderId, DeliveryStatus.LOST);
                 } else{
                     log.info("orderId {} is  DELIVERED", orderId);
                     saveAndSendUpdate(orderId, DeliveryStatus.DELIVERED);
                 }

             } catch (Exception e) {
                 log.info("orderId delivery failed {}", orderId);
                 Thread.currentThread().interrupt();
             }
         }).start();
    }



    public void saveAndSendUpdate(int orderId, DeliveryStatus deliveryStatus) {
        deliveryUpdateService.saveStatus(orderId, deliveryStatus);
        deliveryUpdateService.sendUpdate(
                orderId,
                deliveryStatus,
                RabbitMQConfig.getStatusMessage(deliveryStatus));
    }
}
