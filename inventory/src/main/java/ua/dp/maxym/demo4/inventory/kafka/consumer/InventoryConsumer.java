package ua.dp.maxym.demo4.inventory.kafka.consumer;

import org.springframework.stereotype.Component;

@Component
public class InventoryConsumer {

    /*
    @Autowired
    private InventoryProducer inventoryProducer;

    @Autowired
    private InventoryRepository inventoryRepository;

    @KafkaListener(topics = "commands.init")
    public void listenInit(ConsumerRecord<String, Initialize> initializePaymentConsumerRecord) {
        // don't need var initializeCheckout = initializeCheckoutConsumerRecord.value();
        inventoryRepository.deleteAll();
        inventoryRepository.insert(new Inventory("item1", 10, 100.0));
        inventoryRepository.insert(new Inventory("item2", 5, 10.0));
    }

    @KafkaListener(topics = "inventory.commands.get")
    public void listenGet(ConsumerRecord<String, InventoryAsset> collectGoodsConsumerRecord) {
        var getRequest = collectGoodsConsumerRecord.value();
        var inventory = inventoryRepository.findByGoods(getRequest.goods());
        if (inventory == null) {
            inventoryProducer.failed(new CollectingGoodsFailed(getRequest.requestId(), String.format("There's no such
             goods as %s", getRequest.goods())));
        } else if (inventory.quantity() < getRequest.quantity()) {
            inventoryProducer.failed(new CollectingGoodsFailed(getRequest.requestId()
                    , String.format("Not enough goods %s in inventory (requested %s but inventory has %s)"
                        , getRequest.goods(), getRequest.quantity(), inventory.quantity())));
        } else {
            var newInventory = new Inventory(inventory.goods(), inventory.quantity() - getRequest.quantity());
            inventoryRepository.save(newInventory);
            inventoryProducer.succeeded(new CollectingGoodsSucceeded(getRequest.requestId()));
        }
    }


    @KafkaListener(topics = "inventory.events.getting.succeeded")
    public void listenSucceeded(ConsumerRecord<String, GettingGoodsSucceeded> gettingGoodsSucceededConsumerRecord) {
        // do nothing
    }

    @KafkaListener(topics = "inventory.events.getting.failed")
    public void listenFailed(ConsumerRecord<String, GettingGoodsFailed> gettingGoodsFailedConsumerRecord) {
        // do nothing
    }
    */
}
