package sample.cafekiosk.spring.domain.order;

import static org.assertj.core.api.Assertions.assertThat;
import static sample.cafekiosk.spring.domain.product.ProductSellingStatus.SELLING;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sample.cafekiosk.spring.IntegrationTestSupport;
import sample.cafekiosk.spring.domain.orderproduct.OrderProductRepository;
import sample.cafekiosk.spring.domain.product.Product;
import sample.cafekiosk.spring.domain.product.ProductRepository;
import sample.cafekiosk.spring.domain.product.ProductType;

@Transactional
class OrderRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @AfterEach
    void tearDown() {
        orderProductRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("주어진 기간과 상태에 해당하는 주문 목록을 조회한다.")
    void findOrdersBy() {
        //given
        LocalDateTime startDateTime = LocalDateTime.now();
        LocalDateTime endDateTime = startDateTime.plusDays(1);
        OrderStatus orderStatus = OrderStatus.PAYMENT_COMPLETED;

        Product product1 = createProduct("001", ProductType.HANDMADE, 4500);
        Product product2 = createProduct("002", ProductType.HANDMADE, 5000);
        Product product3 = createProduct("003", ProductType.HANDMADE, 3000);
        Product product4 = createProduct("004", ProductType.HANDMADE, 5000);
        productRepository.saveAll(List.of(product1, product2, product3, product4));

        Order order1 = createOrder(product1, product2, orderStatus, startDateTime);
        Order order2 = createOrder(product3, product4, orderStatus, endDateTime.plusDays(2));
        orderRepository.saveAll(List.of(order1, order2));

        //when
        List<Order> ordersBy = orderRepository.findOrdersBy(startDateTime, endDateTime, orderStatus);

        //then
        assertThat(ordersBy).hasSize(1)
                .extracting("orderStatus", "totalPrice")
                .contains(
                        Tuple.tuple(orderStatus, 9500)
                );

        assertThat(ordersBy.get(0).getOrderProducts()).hasSize(2)
                .extracting(op -> op.getProduct().getProductNumber())
                .containsExactlyInAnyOrder("001", "002");
    }

    private static Order createOrder(Product product1, Product product2, OrderStatus orderStatus,
                                     LocalDateTime startDateTime) {
        return Order.builder()
                .products(List.of(product1, product2))
                .orderStatus(orderStatus)
                .registeredDateTime(startDateTime)
                .build();
    }

    private Product createProduct(String productNumber, ProductType type, int price) {
        return Product.builder()
                .productNumber(productNumber)
                .type(type)
                .price(price)
                .sellingStatus(SELLING)
                .name("메뉴 이름")
                .build();
    }
}