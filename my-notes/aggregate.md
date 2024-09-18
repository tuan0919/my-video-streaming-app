## Cấu trúc cơ bản của một Aggregate
Một aggregate trong Axon Framework là một đối tượng thông thường, chứa trạng thái và các phương thức của trạng thái đó. Khi bạn tạo một đối tượng Aggregate, về cơ bản bạn đang tạo ra 'Aggregate Root', thường mang tên của toàn bộ Aggregate.

**Ví dụ về Aggregate GiftCard**
```java
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

public class GiftCard {

    @AggregateIdentifier // 1
    private String id;

    @CommandHandler // 2
    public GiftCard(IssueCardCommand cmd) {
        // 3
       apply(new CardIssuedEvent(cmd.getCardId(), cmd.getAmount()));
    }

    @EventSourcingHandler // 4
    public void on(CardIssuedEvent evt) {
        id = evt.getCardId();
    }

    // 5
    protected GiftCard() {
    }
    // omitted command handlers and event sourcing handlers
}
```
**Giải thích các khái niệm**:
1. `@AggregateIdentifier`: Đây là điểm tham chiếu bên ngoài để trỏ vào Aggregate `GiftCard`. Truờng này là bắt buộc., vì nếu không có nó, Axon sẽ không biết `Command` nào được nhắm vào Aggregate nào. Annotation này có thể được đặt trên một trường hoặc một phương thức.
2. Constructor được đánh dấu `@CommandHandler`: Annotation này cho framework biết rằng constructor này có khả năng xử lý `IssueCardCommand`. Các hàm được đánh dấu `@CommandHandler` là nơi bạn đặt logic nghiệp vụ/quyết định của mình.
3. `AggregateLifecycle.apply(Object...)`: Được sử dụng khi một `Event Message` cần được phát hành. Khi gọi hàm này, các đối tượng được cung cấp sẽ được phát hành dưới dạng `EventMessage` trong phạm vi của Aggregate mà chúng được áp dụng.
4. `@EventSourcingHandler`: cho framework biết rằng hàm được đánh dấu này nên được gọi khi Aggregate được 'tạo nguồn từ các sự kiện của nó'. Vì tất cả các **Event Sourcing Handler** khi  kết hợp với nhau sẽ tạo thành Aggregate, đây là nơi tất cả các thay đổi trạng thái xảy ra. Lưu ý rằng `Aggregate Identifier` phải được đặt trong `@EventSourcingHandler` của Event đầu tiên được phát hành bởi aggregate (thường là sự kiện tạo).
5. Constructor không tham số: Bắt buộc bởi Axon. Axon sử dụng constuctor này để tạo ra một phiên bản aggregate trống trước khi khởi tạo nó bằng các Event trong quá khứ. Nếu không cung cấp constructor này sẽ dẫn đến một exception khi tải Aggregate.

## Các thao tác trong vòng đời của một Aggregate
Trong quá trình tồn tại của một Aggregate, có một số thao tác quan trọng mà bạn có thể muốn thực hiện. Để hỗ trợ điều này, lớp `AggregateLifeCycle` trong Axon cung cấp một số hàm static hữu ích:
- `apply(Object)` và `apply(Object, MetaData)`:
  - Hai hàm này dùng để **phát hành một Event message** lên `EventBus`.
  - Bạn có thể cung cấp chỉ đối tượng Event hoặc cả Event và một số `MetaData` cụ thể.
  - Framework sẽ đảm bảo rằng message này được đánh dấu là xuất phát từ Aggregate đang thực hiện thao tác.
- `createNew(Class, Callable)`:
  - Hàm này dùng để **tạo một Aggregate mới** như là kết quả của việc xử lý một command.
  - Bạn cần cung cấp lớp của Aggregate mới và một `Callable` để thực hiện logic khi khởi tạo Aggregate.
- `isLive()`:
  - Hàm này dùng để **kiểm tra xem Aggregate có đang ở trạng thái 'live'** hay không.
  - Một Aggregate được coi là "live" nếu nó đã hoàn thành việc replay các sự kiện lịch sử để tạo lại trạng thái của nó.
  - Nếu Aggregate đang trong quá trình được `event sourced`, một lệnh gọi `AggregateLifecycle.isLive()` sẽ trả về `false`.
  - Có thể sử dụng phương thức `isLive()` để thực hiện các hoạt động chỉ nên được thực hiện khi xử lí các sự kiện mới được tạo ra.
- `markDeleted()`:
  - Dùng để **đánh dấu aggregate instance** gọi hàm là đã bị "xóa".
  - Hữu ích nếu ứng dụng quy định rằng một Aggregate nhất định có thể bị xóa/lọai bỏ/đóng, sau đó nó không nên được phép xử lí bất kì Command nào nữa.
  - Hàm này nên được gọi từ một hàm được đánh dấu `@EventSourcingHandler` để đảm bảo rằng việc đánh dấu xóa là một phần của trạng thái của Aggregate đó.