# Axon Refererences Guide (4.9)

## Axon Framework
<details>
<summary>
<b>Messaging Concepts</b>
</summary>

### Messaging Concepts

#### Ý tưởng cốt lõi:
Một trong những khái niệm quan trọng nhất trong Axon là messaging (nhắn tin). Mọi giao tiếp giữa các thành phần trong ứng dụng Axon đều được thực hiện qua các đối tượng message. Điều này mang lại cho các thành phần tính **location transparency** (tính trong suốt về vị trí), nghĩa là chúng không cần biết về các thành phần khác nằm ở đâu, giúp dễ dàng mở rộng và phân tán hệ thống khi cần thiết.

#### Các loại message:
Mặc dù tất cả các message đều triển khai interface `Message`, nhưng chúng có sự phân biệt rõ ràng giữa các loại khác nhau và cách chúng được xử lí.

> **Lưu ý**:
> - Tất cả các message đều là bất biến (immutable). Điều này có nghĩa là một khi một message được tạo ra, nó không thể bị thay đổi.
> - Nếu bạn cần "lưu trữ" thêm dữ liệu vào một message, thực chất bạn đang tạo ra một message mới dựa trên message cũ, với thông tin bổ sung được thêm vào.
> - Tính bất biến này đảm bảo rằng các message an toàn để sử dụng trong môi trường đa luồng và phân tán, vì bạn không cần lo lắng về việc một luồng nào đó vô tình thay đổi nội dung của một message mà các luồng khác đang sử dụng.

#### Cấu trúc của message:
Mỗi message bao gồm 3 phần chính:
1. **Payload (tải trọng)**: Đây là phần dữ liệu chức năng của message, mô tả ý nghĩa của message đó. Ví dụ, trong một Command (lệnh), payload có thể chứa thông tin về hành động cần thực hiện.
2. **Metadata (siêu dữ liệu)**: Metadata cung cấp ngữ cảnh cho message, ví dụ như thông tin về nguồn gốc của message, thông tin theo dõi (tracing) hoặc thông tin bảo mật.
3. **Unique identifier (định danh duy nhất)**: Mỗi message có một định danh duy nhất để phân biệt nó với các message khác trong hệ thống.

### Commands

Commands thể hiện một ý định thay đổi trạng thái của ứng dụng. Ví dụ: chúng ta có thể có một `PlaceOrderCommand` để tạo một đơn hàng mới, hoặc một `UpdateProductCommand` để cập nhật thông tin sản phẩm.

Command thường được triển khai dưới dạng các Plain Old Java Objects (POJOs), lý tưởng nhất là các POJO read-only. Khi được gửi đi, chúng sẽ được gói bên trong một `CommandMessage` để cung cấp thêm thông tin ngữ cảnh.

Mỗi command chỉ luôn có **một và chỉ một đích đến** cụ thể. Mặc dù người gửi không quan tâm thành phần nào sẽ xử lý lệnh hoặc thành phần đó nằm ở đâu, nhưng họ có thể muốn biết được kết quả của việc xử lí lệnh đó.

Chính vì vậy, các command được gửi qua **Command Bus** cho phép trả về một kết quả. Điều này giúp người gửi có thể biết được lệnh đã được xử lý thành công hay không, hoặc có thể nhận được dữ liệu mới từ quá trình xử lý lệnh.

### Events
Events là các đối tượng mô tả một sự kiện đã xảy ra trong ứng dụng. Nguồn điển hình của event thường là từ các Aggregate. Khi một điều quan trọng xảy ra bên trong Aggregate, nó sẽ phát ra một event.

Trong Axon Framework, events có thể là bất kì đối tượng nào, tuy nhiên chúng ta nên đảm bảo rằng tất cả các events đều có thể tuần tự hóa (serializable) để dễ dàng lưu trữ và truyền tải.

#### Event Message
Khi events được gửi đi, Axon sẽ đóng gói chúng trong một `EventMessage`. Loại `Message` cụ thể được sử dụng phụ thuộc vảo nguồn gốc của event:
- Nếu event được phát ra bởi một Aggregate, nó sẽ được đóng gói trong một `DomainEventMessage` (là một lớp con của EventMessage).
- Các event khác sẽ được đóng gói trong một `EventMessage` thông thường.

Ngoài các thuộc tính chung của `Message` như `Identifier` (định danh duy nhất), `EventMessage` còn chứa một `timestamp` (thời gian xảy ra sự kiện).

`DomainEventMessage` còn chứa thêm thông tin về `type` và `identifier` của Aggregate đã phát ra event, cùng với `sequence number` của event trong dòng sự kiện của Aggregate, cho phép tái tạo lại thứ tự các sự kiện đã xảy ra.

> **Note**:
> - Mặc dù `DomainEventMessage` chứa tham chiếu đến `Aggregate Identifier`, chúng ta vẫn nên đưa `identifier` này vào trong bản thân đối tượng Event
> - `Identifier` trong `DomainEventMessage` được `EventStore` sử dụng để lưu trữ các sự kiện và có thể không phải lúc nào cũng cung cấp một giá trị đáng tin cậy cho các mục đích khác.

#### Payload và MetaData
Đối tượng event gốc được lưu trữ dưới dạng `payload` của một `EventMessage`. Bên cạnh `payload`, ta có thể lưu trữ thêm thông tin trong `metadata` của `EventMessage`. `Metadata` thường được sử dụng để lưu trữ thông tin bổ sung về một event, không phải là thông tin liên quan đến nghiệp vụ chính, ví dụ như thông tin kiểm toán (auditing), cho phép chúng ta biết được hoàn cảnh mà một Event được phát ra như tài khoản người dùng đã kích hoạt quá trình xử lý hạo8c tên cũa máy đã xử lý event.

> **Note**
> - Nói chung, không nên dựa vào thông tin bên trong **metadata** để đưa ra các quyết định liên quan đến nghiệp vụ (business decisions). Nếu cần làm như vậy, có thể thông tin đó nên được đưa vào trong chính bản thân event.

#### Tính bất biến và thiết kế Event
Mặc dù không bắt buộc, nhưng nên thiết kế các `domain event` là bất biến (immutable), tốt nhất là bằng cách khai báo tất cả các trường là final và khởi tạo event trong constructor. Có thể cân nhắc sử dụng pattern Builder nếu việc xây dựng Event trở nên quá phức tạp.

> **Note**
> - Mặc dù **domain event** về mặt kỹ thuật biểu thị cho một sự thay đổi trạng thái, ta cũng nên cố gắng nắm bắt được ý định của sự thay đổi trạng thái đó trong event.
> - Một cách hay là sử dụng một lớp trừu tượng để biểu diễn sự kiện thay đổi trạng thái chung, và sau đó tạo các lớp con cụ thể để thể hiện ý định của sự thay đổi. Ví dụ: có thể có một lớp trừu tượng **AddressChangedEvent**, và hai lớp con **ContactMovedEvent** và **AddressCorrectedEvent** để nắm bắt ý định của sự thay đổi địa chỉ. 
> - Một số trình lắng nghe (listener) không quan tâm đến ý định (ví dụ: các trình lắng nghe cập nhật cơ sở dữ liệu), chúng sẽ lắng nghe loại trừu tượng. Trong khi đó, các trình lắng nghe khác quan tâm đến ý định và sẽ lắng nghe các loại con cụ thể (ví dụ: để gửi email xác nhận thay đổi địa chỉ cho khách hàng).

#### Gửi Event
Khi gửi một event trên `Event Bus`, cần đóng gói nó trong một `EventMessage`. `GenericEventMessage` là một triển khai cho phép đóng gói Event của mình trong một Message. Chúng ta có thể sử dụng constructor hoặc phương thức static `asEventMessage()`. Phương thức này sẽ kiểm tra xem tham số đã triển khai interface `Message` hay chưa. Nếu có, nó sẽ được trả về trực tiếp (nếu nó triển khai `EventMessage`) hoặc nó sẽ trả về một `GenericEventMessage` mới sử dụng `payload` và `metadata` của `Message` đã cho. Nếu một Event được áp dụng (xuất bản - publish) bởi một Aggregate, Axon sẽ tự động đóng gói Event trong một `DomainEventMessage` chứa `Identifier`, `Type` và `Sequence Number` của Aggregate.

### Các section liên quan
1. **[Anatomy of Message](/my-notes/anatomy-of-message.md)**

2. **[Message Correlation](/my-notes/message-correlation.md)**

3. **[Message Intercepting - part 01](/my-notes/message-intercepting-01.md)**

4. **[Unit of Work](/my-notes/unit-of-work.md)**
</details>

<details>
<summary>
<b>Command Message</b>
</summary>

Loại message đầu tiên mà một ứng dụng Axon thường liên quan đến là Command Message (hay gọi tắt là Command).

### Các section liên quan
1. **[Aggregate](/my-notes/aggregate.md)**

2. **[State Stored Aggregates](/my-notes/state-stored-aggregates.md)**
</details>
