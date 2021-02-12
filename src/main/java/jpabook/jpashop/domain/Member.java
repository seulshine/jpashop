package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")  // 그냥 두면 id 됨
    private Long id;

    private String name;

    @Embedded // 내장타입이다.
    private Address address;

    @OneToMany(mappedBy = "member") // 매핑 되는 거울일 뿐야~~ 읽기 전용이 됨!
    private List<Order> orders = new ArrayList<>();

}
