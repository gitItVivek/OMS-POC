package com.poc.orderservice.repository.impl;

import com.poc.orderservice.dto.OrderSearchRequestDto;
import com.poc.orderservice.entity.Order;
import com.poc.orderservice.entity.OrderItem;
import com.poc.orderservice.entity.OrderStatusHistory;
import com.poc.orderservice.repository.OrderRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Override
    public OrderStatusHistory addOrderStatusHistory(Order order, OrderStatusHistory statusHistory) {
        statusHistory.setOrder(order);
        entityManager.persist(statusHistory);
        return statusHistory;
    }

    private static final Map<String, String> SORTABLE_FIELDS = Map.of(
            "customerId", "customerId",
            "status", "orderStatus",
            "orderDate", "orderDate",
            "totalAmount", "totalAmount",
            "createdAt", "createdAt"
    );

    @Override
    public Page<Order> searchOrders(OrderSearchRequestDto orderSearchRequestDto) {
        int page = orderSearchRequestDto.page() != null ? orderSearchRequestDto.page() : 0;
        int size = orderSearchRequestDto.size() != null ? orderSearchRequestDto.size() : 20;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> idQuery = cb.createQuery(Long.class);
        Root<Order> idRoot = idQuery.from(Order.class);
        idQuery.select(idRoot.get("id"))
                .where(buildPredicates(cb, idRoot, orderSearchRequestDto).toArray(new Predicate[0]));

        String sortField = SORTABLE_FIELDS.getOrDefault(orderSearchRequestDto.sort(), "id");
        idQuery.orderBy(cb.asc(idRoot.get(sortField)));

        TypedQuery<Long> idTypedQuery = entityManager.createQuery(idQuery);
        idTypedQuery.setFirstResult(page * size);
        idTypedQuery.setMaxResults(size);
        List<Long> orderIds = idTypedQuery.getResultList();

        List<Order> results = orderIds.isEmpty() ? List.of() : fetchWithItemsPreservingOrder(orderIds);

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Order> countRoot = countQuery.from(Order.class);
        countQuery.select(cb.count(countRoot))
                .where(buildPredicates(cb, countRoot, orderSearchRequestDto).toArray(new Predicate[0]));
        long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(results, PageRequest.of(page, size), total);
    }

    private List<Order> fetchWithItemsPreservingOrder(List<Long> orderIds) {
        TypedQuery<Order> fetchQuery = entityManager.createQuery(
                "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id IN :orderIds", Order.class);
        fetchQuery.setParameter("orderIds", orderIds);
        Map<Long, Order> byId = fetchQuery.getResultList().stream()
                .collect(Collectors.toMap(Order::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
        return orderIds.stream()
                .map(byId::get)
                .collect(Collectors.toList());
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Order> root, OrderSearchRequestDto dto) {
        List<Predicate> predicates = new ArrayList<>();
        if (dto.customerId() != null) {
            predicates.add(cb.equal(root.get("customerId"), dto.customerId()));
        }
        if (dto.status() != null && !dto.status().isBlank()) {
            predicates.add(cb.equal(root.get("orderStatus"), dto.status()));
        }
        if (dto.fromDate() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("orderDate"), dto.fromDate()));
        }
        if (dto.toDate() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("orderDate"), dto.toDate()));
        }
        return predicates;
    }
}