package com.orderservice.service.impl;

import com.orderservice.dal.OrderDal;
import com.orderservice.dto.CreateOrderRequestDto;
import com.orderservice.dto.OrderItemRequestDto;
import com.orderservice.dto.OrderItemStockResult;
import com.orderservice.dto.OrderResponseDto;
import com.orderservice.dto.ProductSummaryDto;
import com.orderservice.entity.Order;
import com.orderservice.entity.OrderItem;
import com.orderservice.enums.OrderStatus;
import com.orderservice.exception.InsufficientStockException;
import com.orderservice.integration.StockLookupGateway;
import com.orderservice.mapper.OrderMapper;
import com.orderservice.security.AuthContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Covers {@link OrderServiceImpl#createOrder(CreateOrderRequestDto)}, the method changed by
 * this PR to resolve stock via {@link StockLookupGateway} instead of looping over
 * InventoryServiceClient directly.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private OrderDal orderDal;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private StockLookupGateway stockLookupGateway;

    private OrderServiceImpl orderService;

    private MockedStatic<AuthContext> authContextMock;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(orderDal, orderMapper, stockLookupGateway);
        authContextMock = mockStatic(AuthContext.class);
        authContextMock.when(AuthContext::currentUserId).thenReturn(USER_ID);
    }

    @AfterEach
    void tearDown() {
        authContextMock.close();
    }

    @Test
    void createOrder_withSingleItem_persistsOrderAndReturnsMappedResponse() {
        UUID productId = UUID.randomUUID();
        OrderItemRequestDto itemRequest = OrderItemRequestDto.builder().productId(productId).quantity(2).build();
        CreateOrderRequestDto request = CreateOrderRequestDto.builder().items(List.of(itemRequest)).build();

        ProductSummaryDto product = ProductSummaryDto.builder()
                .productId(productId)
                .title("Widget")
                .price(BigDecimal.valueOf(10))
                .currency("USD")
                .availableQty(5)
                .build();
        when(stockLookupGateway.lookupStock(request.getItems())).thenReturn(
                List.of(OrderItemStockResult.builder().itemRequest(itemRequest).product(product).build()));

        Order savedOrder = Order.builder().id(UUID.randomUUID()).customerId(USER_ID).status(OrderStatus.PENDING).build();
        when(orderDal.saveOrder(any(Order.class))).thenReturn(savedOrder);

        OrderResponseDto expectedResponse = OrderResponseDto.builder().orderId(savedOrder.getId()).build();
        when(orderMapper.toResponseDto(eq(savedOrder), any())).thenReturn(expectedResponse);

        OrderResponseDto response = orderService.createOrder(request);

        assertThat(response).isSameAs(expectedResponse);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderDal).saveOrder(orderCaptor.capture());
        Order capturedOrder = orderCaptor.getValue();
        assertThat(capturedOrder.getCustomerId()).isEqualTo(USER_ID);
        assertThat(capturedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(capturedOrder.getCurrency()).isEqualTo("USD");
        assertThat(capturedOrder.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(20));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<OrderItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderDal).saveOrderItems(itemsCaptor.capture());
        List<OrderItem> savedItems = itemsCaptor.getValue();
        assertThat(savedItems).hasSize(1);
        OrderItem savedItem = savedItems.get(0);
        assertThat(savedItem.getProductId()).isEqualTo(productId);
        assertThat(savedItem.getProductTitleSnapshot()).isEqualTo("Widget");
        assertThat(savedItem.getQuantity()).isEqualTo(2);
        assertThat(savedItem.getUnitPrice()).isEqualByComparingTo(BigDecimal.valueOf(10));
        assertThat(savedItem.getOrder()).isSameAs(savedOrder);
    }

    @Test
    void createOrder_withMultipleItems_sumsTotalAndUsesFirstResolvedCurrency() {
        UUID productId1 = UUID.randomUUID();
        UUID productId2 = UUID.randomUUID();
        OrderItemRequestDto itemRequest1 = OrderItemRequestDto.builder().productId(productId1).quantity(1).build();
        OrderItemRequestDto itemRequest2 = OrderItemRequestDto.builder().productId(productId2).quantity(3).build();
        CreateOrderRequestDto request = CreateOrderRequestDto.builder().items(List.of(itemRequest1, itemRequest2)).build();

        ProductSummaryDto product1 = ProductSummaryDto.builder()
                .productId(productId1).title("A").price(BigDecimal.valueOf(5)).currency("USD").availableQty(10).build();
        ProductSummaryDto product2 = ProductSummaryDto.builder()
                .productId(productId2).title("B").price(BigDecimal.valueOf(3)).currency("EUR").availableQty(10).build();

        when(stockLookupGateway.lookupStock(request.getItems())).thenReturn(List.of(
                OrderItemStockResult.builder().itemRequest(itemRequest1).product(product1).build(),
                OrderItemStockResult.builder().itemRequest(itemRequest2).product(product2).build()));

        Order savedOrder = Order.builder().id(UUID.randomUUID()).build();
        when(orderDal.saveOrder(any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.toResponseDto(eq(savedOrder), any())).thenReturn(OrderResponseDto.builder().build());

        orderService.createOrder(request);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderDal).saveOrder(orderCaptor.capture());
        Order capturedOrder = orderCaptor.getValue();

        // total = 1*5 + 3*3 = 14
        assertThat(capturedOrder.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(14));
        // currency is taken from the first stock result only, matching current behavior
        assertThat(capturedOrder.getCurrency()).isEqualTo("USD");
    }

    @Test
    void createOrder_whenAvailableQtyLessThanRequestedQuantity_throwsInsufficientStockException() {
        UUID productId = UUID.randomUUID();
        OrderItemRequestDto itemRequest = OrderItemRequestDto.builder().productId(productId).quantity(5).build();
        CreateOrderRequestDto request = CreateOrderRequestDto.builder().items(List.of(itemRequest)).build();

        ProductSummaryDto product = ProductSummaryDto.builder()
                .productId(productId).availableQty(2).price(BigDecimal.ONE).currency("USD").build();
        when(stockLookupGateway.lookupStock(request.getItems())).thenReturn(
                List.of(OrderItemStockResult.builder().itemRequest(itemRequest).product(product).build()));

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("requested=5")
                .hasMessageContaining("available=2");

        verify(orderDal, never()).saveOrder(any());
        verify(orderDal, never()).saveOrderItems(any());
    }

    @Test
    void createOrder_whenAvailableQtyIsNull_throwsInsufficientStockExceptionWithZeroAvailable() {
        UUID productId = UUID.randomUUID();
        OrderItemRequestDto itemRequest = OrderItemRequestDto.builder().productId(productId).quantity(1).build();
        CreateOrderRequestDto request = CreateOrderRequestDto.builder().items(List.of(itemRequest)).build();

        ProductSummaryDto product = ProductSummaryDto.builder()
                .productId(productId).availableQty(null).price(BigDecimal.ONE).currency("USD").build();
        when(stockLookupGateway.lookupStock(request.getItems())).thenReturn(
                List.of(OrderItemStockResult.builder().itemRequest(itemRequest).product(product).build()));

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("available=0");

        verify(orderDal, never()).saveOrder(any());
    }

    @Test
    void createOrder_withNullItems_throwsIllegalArgumentExceptionBeforeCallingGateway() {
        CreateOrderRequestDto request = CreateOrderRequestDto.builder().items(null).build();

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one item");

        verifyNoInteractions(stockLookupGateway, orderDal, orderMapper);
    }

    @Test
    void createOrder_withEmptyItems_throwsIllegalArgumentExceptionBeforeCallingGateway() {
        CreateOrderRequestDto request = CreateOrderRequestDto.builder().items(List.of()).build();

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one item");

        verifyNoInteractions(stockLookupGateway, orderDal, orderMapper);
    }

    @Test
    void createOrder_withMissingProductId_throwsIllegalArgumentExceptionBeforeCallingGateway() {
        OrderItemRequestDto itemRequest = OrderItemRequestDto.builder().productId(null).quantity(1).build();
        CreateOrderRequestDto request = CreateOrderRequestDto.builder().items(List.of(itemRequest)).build();

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("productId");

        verifyNoInteractions(stockLookupGateway);
    }

    @Test
    void createOrder_withInvalidQuantity_throwsIllegalArgumentExceptionBeforeCallingGateway() {
        OrderItemRequestDto itemRequest = OrderItemRequestDto.builder().productId(UUID.randomUUID()).quantity(0).build();
        CreateOrderRequestDto request = CreateOrderRequestDto.builder().items(List.of(itemRequest)).build();

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quantity >= 1");

        verifyNoInteractions(stockLookupGateway);
    }

    @Test
    void createOrder_delegatesStockResolutionToGatewayWithOriginalRequestItems() {
        UUID productId = UUID.randomUUID();
        OrderItemRequestDto itemRequest = OrderItemRequestDto.builder().productId(productId).quantity(1).build();
        CreateOrderRequestDto request = CreateOrderRequestDto.builder().items(List.of(itemRequest)).build();

        ProductSummaryDto product = ProductSummaryDto.builder()
                .productId(productId).availableQty(1).price(BigDecimal.ONE).currency("USD").build();
        when(stockLookupGateway.lookupStock(any())).thenReturn(
                List.of(OrderItemStockResult.builder().itemRequest(itemRequest).product(product).build()));
        when(orderDal.saveOrder(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toResponseDto(any(), any())).thenReturn(OrderResponseDto.builder().build());

        orderService.createOrder(request);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<OrderItemRequestDto>> captor = ArgumentCaptor.forClass(List.class);
        verify(stockLookupGateway, times(1)).lookupStock(captor.capture());
        assertThat(captor.getValue()).containsExactly(itemRequest);
    }

    @Test
    void createOrder_whenGatewayThrows_propagatesExceptionAndDoesNotPersistOrder() {
        UUID productId = UUID.randomUUID();
        OrderItemRequestDto itemRequest = OrderItemRequestDto.builder().productId(productId).quantity(1).build();
        CreateOrderRequestDto request = CreateOrderRequestDto.builder().items(List.of(itemRequest)).build();

        RuntimeException gatewayFailure = new RuntimeException("inventory-service unavailable");
        when(stockLookupGateway.lookupStock(request.getItems())).thenThrow(gatewayFailure);

        assertThatThrownBy(() -> orderService.createOrder(request)).isSameAs(gatewayFailure);

        verify(orderDal, never()).saveOrder(any());
        verify(orderDal, never()).saveOrderItems(any());
    }

    @Test
    void createOrder_usesAuthenticatedUserIdAsCustomerId() {
        UUID productId = UUID.randomUUID();
        OrderItemRequestDto itemRequest = OrderItemRequestDto.builder().productId(productId).quantity(1).build();
        CreateOrderRequestDto request = CreateOrderRequestDto.builder().items(List.of(itemRequest)).build();

        ProductSummaryDto product = ProductSummaryDto.builder()
                .productId(productId).availableQty(1).price(BigDecimal.ONE).currency("USD").build();
        when(stockLookupGateway.lookupStock(request.getItems())).thenReturn(
                List.of(OrderItemStockResult.builder().itemRequest(itemRequest).product(product).build()));
        when(orderDal.saveOrder(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toResponseDto(any(), any())).thenReturn(OrderResponseDto.builder().build());

        orderService.createOrder(request);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderDal).saveOrder(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getCustomerId()).isEqualTo(USER_ID);
    }
}