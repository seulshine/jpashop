package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager em;

    @Test
    //@Rollback(false) // @Transactional 에 기본은 Rollback 이라서 커밋 하려면 false 설정해줘야 함! (insert 쿼리 보고 싶으면 이거 해줘야 함!)
    public void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setName("choi");

        // when
        Long saveId = memberService.join(member);

        //then
        em.flush(); // DB에 쿼리가 강제로 나감! 그치만 트랜잭션이 Rollback을 하기에 DB에 남지 않음!
        assertEquals(member, memberRepository.findOne(saveId)); // 같은 트랜잭션안에 있으면 같은 id 값, 같은 영속성으로 관리 됨

    }
    
    @Test(expected = IllegalStateException.class)
    public void 중복_회원_에러() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("choi1");
        Member member2 = new Member();
        member2.setName("choi1");

        // when
        memberService.join(member1);
        memberService.join(member2);
//        try {
//            memberService.join(member2);
//        } catch (IllegalStateException e){
//            return;
//        }
        //then
        fail("예외가 발생해야 한다.");
    }
}