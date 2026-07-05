package com.poc.orderservice.repository.impl;

import com.poc.orderservice.entity.Order;
import com.poc.orderservice.entity.OrderItem;
import com.poc.orderservice.repository.OrderRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepositoryImpl implements OrderRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Order createOrder(Order order) {
        entityManager.persist(order);
        return order;
    }

    @Override
    public List<OrderItem> addOrderItems(Order order, List<OrderItem> items) {
        for (OrderItem item : items) {
            item.setOrder(order);
            entityManager.persist(item);
        }
        return items;
    }

    @Override
    public Optional<Order> findByIdWithItems(Long orderId) {
        TypedQuery<Order> query = entityManager.createQuery(
                "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :orderId", Order.class);
        query.setParameter("orderId", orderId);
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}