Prototype storm bolt (MessageBusProducerBolt) which inserts inserts data onto the message bus

MessageBusProducerBolt
------------

Docker
------------
To run the demo storm cluster using docker do:
```
docker build -t ana-storm . && docker run ana-storm
```

You should see a document being written to the message bus
