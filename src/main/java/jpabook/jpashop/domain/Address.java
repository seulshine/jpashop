package jpabook.jpashop.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

@Embeddable // 내장 타입이기 때문에, 어딘가에 내장될 수 있다!
@Getter
public class Address {

    private String city;
    private String street;
    private String zipcode;

    protected Address() { // 이런저런 곳에서 new 로 생성하는 것을 막기 위해!!
    }

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
