package sample.cafekiosk.spring.api.service.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static sample.cafekiosk.spring.domain.product.ProductSellingStatus.SELLING;
import static sample.cafekiosk.spring.domain.product.ProductType.HANDMADE;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sample.cafekiosk.spring.IntegrationTestSupport;
import sample.cafekiosk.spring.domain.history.mail.MailSendHistory;
import sample.cafekiosk.spring.domain.history.mail.MailSendHistoryRepository;
import sample.cafekiosk.spring.domain.order.Order;
import sample.cafekiosk.spring.domain.order.OrderRepository;
import sample.cafekiosk.spring.domain.order.OrderStatus;
import sample.cafekiosk.spring.domain.orderproduct.OrderProductRepository;
import sample.cafekiosk.spring.domain.product.Product;
import sample.cafekiosk.spring.domain.product.ProductRepository;
import sample.cafekiosk.spring.domain.product.ProductType;

class OrderStatisticsServiceTest extends IntegrationTestSupport {

    @Autowired
    private OrderStatisticsService orderStatisticsService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MailSendHistoryRepository mailSendHistoryRepository;

    @AfterEach
    void tearDown() {
        orderProductRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        mailSendHistoryRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("결제 완료 주문들을 조회하여 매출 통계 메일을 전송한다.")
    void sendOrderStatisticsMail() {
        //given
        LocalDateTime now = LocalDateTime.of(2025, 5, 10, 0, 0);

        Product product1 = createProduct("001", HANDMADE, 1000);
        Product product2 = createProduct("002", HANDMADE, 2000);
        Product product3 = createProduct("003", HANDMADE, 3000);
        List<Product> products = List.of(product1, product2, product3);
        productRepository.saveAll(products);

        createPaymentCompletedOrder(LocalDateTime.of(2025, 5, 9, 23, 59, 59), products);
        createPaymentCompletedOrder(now, products);
        createPaymentCompletedOrder(LocalDateTime.of(2025, 5, 10, 23, 59, 59), products);
        createPaymentCompletedOrder(LocalDateTime.of(2025, 5, 11, 0, 0), products);

        //stubbing -> mock에 원하는 행위 지정
        when(mailSendClient.sendEmail(any(String.class), any(String.class), any(String.class), any(String.class)))
                .thenReturn(true);

        //when
        boolean result = orderStatisticsService.sendOrderStatisticsMail(LocalDate.of(2025, 5, 10), "test@test.com");
        List<MailSendHistory> histories = mailSendHistoryRepository.findAll();

        //then
        assertThat(result).isTrue();

        assertThat(histories).hasSize(1)
                .extracting("content")
                .contains("총 매출 합계는 12000원 입니다.");
    }

    private void createPaymentCompletedOrder(LocalDateTime now, List<Product> products) {
        Order order = Order.builder()
                .products(products)
                .orderStatus(OrderStatus.PAYMENT_COMPLETED)
                .registeredDateTime(now)
                .build();
        orderRepository.save(order);
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