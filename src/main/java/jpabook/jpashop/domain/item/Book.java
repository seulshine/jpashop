package jpabook.jpashop.domain.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("B") // DB 에 들어가는 구분 값
@Getter @Setter
public class Book extends Item {

    private String author;
    private String isbn;

}
