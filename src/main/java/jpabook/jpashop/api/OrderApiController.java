package jpabook.jpashop.api;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.repository.OrderRepository;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderRepository orderRepository;

    /**
     * 이 방법은 결국 Entity를 직접 노출하기 때문에 쓰면 안된다!!
     * Collection 이 노출되고 있음! => DTO로 바꿔야 함!
     * @return
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());

        for(Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();

            
            // OrderItem List와 그 안의 Item 강제 초기화 (Proxy 초기화??)
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());
        }
        return all;

    }


    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {

        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        /**
         *
         * orders를 OrderDto로 변환!!
         * 쿼리가 엄청 많이 나옴!! 루프가 많아서! Collection 쓰면 쿼리가 많이 나감,, 그래서 최적화에 대해 더 고민해봐야 한다!!
         * 물론 item 이 영속성 컨텍스트에서 Lazy 가 있기 때문에
         * 만약 모든 유저가 JPA1_BOOK 을 샀다면 쿼리는 1번만 나갔을텐데..! 영속성 컨텍스트에 없을 땐 DB로 나가서 쿼리 다 나감!
         */
         List<OrderDto> collect = orders.stream()
                .map(OrderDto::new)
                .collect(Collectors.toList());

        return collect;

    }

    @Getter
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();

            order.getOrderItems().stream().forEach(o -> o.getItem().getName());
            // 그냥 돌리면 orderItems가 안나옴! entity니깐! 바로 위처럼 Proxy 초기화를 해줘야 함!!
            // 그치만 이것보다는 OrderItem 조차도 DTO로 다 만들어줘야 함!!!
            orderItems = order.getOrderItems().stream()
                    .map(OrderItemDto::new)
                    .collect(Collectors.toList());

        }
    }

    @Getter
    static class OrderItemDto {
        
        private String itemName; // 상품명
        private int orderPrice; // 주문 가격
        private int count; // 주문 수량

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
