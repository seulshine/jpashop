package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders") // 테이블 명대로 order가 될 수 있기 때문에 orders 명시
@Getter @Setter
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 기본이 EAGER!
    @JoinColumn(name = "member_id") // FK가 member_id 가 됨!
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL) // 기본이 LAZY! EAGER X!!, persist(order)만 해주면 item 들 먼저 저장해주고 해줌 (연쇄적)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY) // 주로 Order 를 더 많이 access 하기 때문에!
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;


    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // 주문 상태 (ORDER, CANCEL)

    //==연관관계 편의 메서드== 위치는 핵심적으로 control 하는 쪽이 들고 있는 게 좋음!!//
    public void setMember(Member member){
        this.member = member;
        member.getOrders().add(this);
    }
    //==연관관계 메서드==//
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }
    //==연관관계 메서드==//
    public void setDelivery(Delivery delivery){
        this.delivery = delivery;
        delivery.setOrder(this);
    }
}
