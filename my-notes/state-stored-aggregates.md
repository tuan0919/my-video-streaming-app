## State Stored Aggregates

Trong phần **[Aggregate](/my-notes/aggregate.md)**, chúng ta đã thấy phương pháp tạo một Aggregate với sự hỗ trợ của Event Sourcing. Nói cách khác, cách tiếp cận này này là replay lại các sự kiện đã tạo nên thay đổi trên một Aggregate.

Tuy nhiên, một Aggregate cũng có thể được lưu trữ trực tiếp trạng thái của nó. Khi làm như vậy, Repository được sử dụng để lưu và tải Aggregate là `GenericJpaRepository`. Cấu trúc của một State Stored Aggregate hơi khác so với Event Sourced Aggregate.

Ví dụ:

```java
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateMember;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;

@Entity // 1.
public class GiftCard {

    @Id // 2.
    @AggregateIdentifier
    private String id;

    // 3.
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "giftCardId")
    @AggregateMember
    private List<GiftCardTransaction> transactions = new ArrayList<>();

    private int remainingValue;

    @CommandHandler  // 4.
    public GiftCard(IssueCardCommand cmd) {
        if (cmd.getAmount() <= 0) {
            throw new IllegalArgumentException("amount <= 0");
        }
        id = cmd.getCardId();
        remainingValue = cmd.getAmount();

         // 5.
        apply(new CardIssuedEvent(cmd.getCardId(), cmd.getAmount()));
    }

    @CommandHandler
    public void handle(RedeemCardCommand cmd) {
         // 6.
        if (cmd.getAmount() <= 0) {
            throw new IllegalArgumentException("amount <= 0");
        }
        if (cmd.getAmount() > remainingValue) {
            throw new IllegalStateException("amount > remaining value");
        }
        if (transactions.stream().map(GiftCardTransaction::getTransactionId).anyMatch(cmd.getTransactionId()::equals)) {
            throw new IllegalStateException("TransactionId must be unique");
        }

         // 7.
        remainingValue -= cmd.getAmount();
        transactions.add(new GiftCardTransaction(id, cmd.getTransactionId(), cmd.getAmount()));

        apply(new CardRedeemedEvent(id, cmd.getTransactionId(), cmd.getAmount()));
    }

    @EventHandler  // 8.
    protected void on(CardReimbursedEvent event) {
        this.remainingValue += event.getAmount();
    }

    protected GiftCard() { }  // 9.
}
```

1. `@Entity`: Vì Aggregate được lưu trữ trong một JPA Repository, cần phải đánh dấu lớp bằng `@Entity`.
2. `@Id` và `@AggregateIdentifier`: Một Aggregate Root phải khai báo một trường chứa `Aggregate Identifier`. Trường này được khởi tạo muộn nhất là khi sự kiện đầu tiên được phát hành. Trường này phải được đánh dấu bằng annotation `@AggregateIdentifier`. Khi sử dụng JPA để lưu trữ Aggregate, Axon sẽ sử dụng annotation `@Id` được cung cấp bởi JPA. Vì Aggregate là một entity, annotation `@Id` là bắt buộc.
3. `@AggregateMember`: Aggregate này có một số 'Aggregate Members'. Vì Aggregate được lưu trữ như nó vốn có, cần phải xem xét kỹ việc ánh xạ chính xác entity.
4. Constructor được đánh dấu `@CommandHandler`: Annotation này cho framework biết rằng constructor này có khả năng xử lý `issueCardCommand`.
5. `AggregateLifecycle.apply(Object...)`: Có thể được sử dụng để phát hành một `Event Mesage`. Khi gọi hàm này, các đối tượng được cung cấp sẽ được phát hành dưới dạng `EventMessages` trong phạm vi của Aggregate mà chúng được áp dụng.
6. Phương thức xử lý Command: Phương thức này sẽ quyết định xem Command đến có hợp lệ để xử lí tại thời điểm này hay không. Sau khi logic nghiệp vụ đã được xác nhận, trạng thái của Aggregate có thể được điều chỉnh.
7. Điều chỉnh trạng thái và phát hành sự kiện: Trạng thái của Aggregate được cập nhật trực tiếp trong phương thức xử lý Command (`remainingValue` và `transactions`). Sau đó, một sự kiện `CardRedeemedEvent` được phát hành để thông báo về sự thay đổi này.
8. `@EventHandler`: Các entity bên trong một Aggregate có thể lắng nghe các sự kiện mà Aggregate phát hành, bằng cách định nghĩa một phương thức được đánh dấu `@EventHandler`. Các phương thức này sẽ được gọi khi một `Event Messaage` được phát hành trước khi được xử lí bởi bất kì trình xử lí bên ngoài nào.
9. Constructor không tham số: Bắt buộc bởi JPA. Nếu không cung cấp constructor này sẽ dẫn đến một exception khi tải Aggregate.

## Điều chỉnh trạng thái trong Command Handlers
Khác với **[Event Sourced Aggregates](/my-notes/anatomy-of-message.md)**, State-Stored Aggregates có thể kết hợp logic quyết định và thay đổi trạng thái trong một Command Handler. Không có hậu quả nào đối với State-Stored Aggregates khi làm theo mô hình này vì không có Event Sourcing Handlers nào điều khiển trạng thái của nó.