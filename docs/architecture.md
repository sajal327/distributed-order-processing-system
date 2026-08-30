# Architecture

The system is a modular distributed application. The order service owns order creation and persistence; RabbitMQ carries domain events to inventory, payment and notification consumers. MySQL is the durable relational store. The transactional outbox will bridge the order database transaction and RabbitMQ publication without dual-write inconsistency.
