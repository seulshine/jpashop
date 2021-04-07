package jpabook.jpashop.api;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderSearch;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.*;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
/**
 *
 * xToOne(ManyToOne, OneToOne) 관계 최적화
 * Order
 * Order -> Member
 * Order -> Delivery
 *
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    /**
     * V1. 엔티티 직접 노출
     * - Hibernate5Module 모듈 등록, LAZY=null 처리
     * - 양방향 관계 문제 발생 -> @JsonIgnore
     *
     * 오류
     * No serializer found for class org.hibernate.proxy.pojo.bytebuddy.ByteBuddyInterceptor and no properties discovered to create BeanSerializer
     *
     * Order의 Member는 fetch = LAZY (지연 로딩)으로 설정되어 있다. 지연 로딩은 진짜 new 해서 DB에서 Member 객체를 안가져옴, 그렇다고 null 을 넣어 둘 수 없으니 hibernate 에서 Proxy 라이브러리를 이용하여 Member를 상속받아 Proxy Member 객체를 만들어 놓음 걔가 봐로 ByteBuddy 라는 얘!!
     * 아무튼 proxy를 임시로 넣어 놓고 어디선가 Member 값을 꺼내가려 할 때 그 때 DB에 SQL을 날려서 데이터를 넣어 줌 = proxy 를 초기화!
     *
     * 지연로딩인 경우 json library야 아무것도 하지마! 라고 hibernate module이 말해줘야 함! => API 스펙에도 안맞을 수 있고 성능상 매우매우매우 안좋아서 쓰면 안됨!!
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); //Lazy 강제 초기화
            order.getDelivery().getAddress(); //Lazy 강제 초기환
        }
        return all;
    }

    /**
     * V2. 엔티티를 조회해서 DTO로 변환(fetch join 사용X) API 스펙이 쫙 맞춰서!!
     * - 단점: 지연로딩으로 쿼리 N번 호출
     */
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        /**
         * 1. order -> sql 1번 -> 결과 주문 수 2개
         *
         * 첫번쨰 루프가 돌 때
         * 2, 4.member를 가지고 오고
         * 3, 5delivery를 가져옴!!
         *
         * 5번을 DB에서 가져옴!!
         *
         * N + 1 -> 1 + 회원 N + 배송 N  (지연로딩은 영속성 컨텍스트에서 조회하므로, 이미 조회된 경우 쿼리를 생략한다. => 같은 멤버면 1명만 조회)
         *
         * 그렇다고 EAGER 하면 안됨!! 쿼리는 쿼리 대로 나가고 성능은 안나옴! EAGER로 하면 문제가 Order를 가지고 오고 까봤더니 eager가 있으니 큰일났네 하나씩 가져와야 하네!!
         */
        List<Order> orders = orderRepository.findAllByString(new OrderSearch()); // 1번!! order -> sql 1번 -> 결과 주문 수 2개
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(toList());
        return result;
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate; //주문시간
        private OrderStatus orderStatus;
        private Address address;
        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); // LAZY 초기화 됨!! 영속성 컨텍스트가 memberId를 가지고 영속성 컨텍스트를 찾아봄! 없으니까 db에 쿼리를 날려서 찾아옴!!
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
        }
    }


    /**
     * V3. 엔티티를 조회해서 DTO로 변환(fetch join 사용O)
     * - fetch join으로 쿼리 1번 호출
     * 참고: fetch join에 대한 자세한 내용은 JPA 기본편 참고(정말 중요함)
     * FETCH JOIN 적극적으로 활용하기!!!!
     */
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(toList());
        return result;
    }


    /**
     * V4. JPA에서 DTO로 바로 조회
     * - 쿼리 1번 호출
     * - select 절에서 원하는 데이터만 선택해서 조회
     */
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        return orderSimpleQueryRepository.findOrderDtos();
    }

    /**
     * 정리
     * 엔티티를 DTO로 변환하거나, DTO로 바로 조회하는 두가지 방법은 각각 장단점이 있다. 둘중 상황에 따라
     * 서 더 나은 방법을 선택하면 된다. 엔티티로 조회하면 리포지토리 재사용성도 좋고, 개발도 단순해진다. 따라
     * 서 권장하는 방법은 다음과 같다.
     *
     * 쿼리 방식 선택 권장 순서
     * 1. 우선 엔티티를 DTO로 변환하는 방법을 선택한다.
     * 2. 필요하면 페치 조인으로 성능을 최적화 한다. 대부분의 성능 이슈가 해결된다.
     * 3. 그래도 안되면 DTO로 직접 조회하는 방법을 사용한다.
     * 4. 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 스프링 JDBC Template을 사용해서 SQL을 직접 사
     * 용한다.
     */
}