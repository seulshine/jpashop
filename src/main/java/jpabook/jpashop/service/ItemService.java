package jpabook.jpashop.service;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    /**
     * 영속성 컨텍스트가 자동 변경
     */
    @Transactional
    public void updateItem(Long id, String name, int price) {
        Item item = itemRepository.findOne(id);
        //findItem.change(price, name, stockQuantity); // 밑에 setter들이 아닌 의미 있는 메소드를 사용해야 한다!
        item.setName(name);
        item.setPrice(price);

        // Transactional 이 끝나면 트랜잭션이 commit됨! commit이 되면 flush를 날림!(=영속성 컨텍스트에 있는 entity 중에 변경이 있는 걸 update 해줌!)
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }

}