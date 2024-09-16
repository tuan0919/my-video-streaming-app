## Anatomy của Message
Trong Axon, **tất cả** giao tiếp giữa các thành phần được thực hiện thông qua các message một cách rõ ràng, được thể hiện bằng interface `Message`. Một `Message` bao gồm phần:
- **Payload**: là một đối tượng cụ thể của ứng dụng sẽ đại diện cho chức năng thực sự của thông điệp.
- **Meta Data**: một cặp key - value mô tả context của thông điệp.

Mỗi interface con của `Message` đại diện cho một loại thông điệp cụ thể, và định thêm thông tin bổ sung giúp mô tả thông điệp đó. Khác với **Meta Data**, thông tin bổ sung này định nghĩa các thông tin cần thiết cho việc xử lí đúng loại thông điệp đó.

Các thông điệp là bất biến (immutable). Điều này có nghĩa là, khi muốn thêm một phần tử **Meta Data**, chúng ta thực chất tạo ra một phiên bản mới của `Message` với phần tử **Meta Data** bổ sung (hoặc thay đổi). Để đảm bảo rằng hai đối tượng `Message` trong Java có thể được coi là đại diện cho cùng một thông điệp khái niệm, mỗi `Message` sẽ có một **identifier** (định danh). Việc thay đổi **Meta Data** sẽ không làm thay đổi **identifier** này.

## Meta Data
**Meta Data** của thông điệp thường mô tả ngữ cảnh mà một thông điệp được tạo ra. Ví dụ, **Meta Data** có thể chứa thông tin về thông điệp ban đầu đã dẫn đến việc tạo ra thông điệp hiện tại (như việc **Command handler** sinh ra **Event** dựa trên một **Command** nhận vào).

Trong Axon, **Meta Data** được biểu diễn như một **Map** với cặp key-value (String - Object). Mặc dù chúng ta có thể thêm bất kì loại dữ liệu nào vào **Meta Data**, Axon khuyến nghị nên chỉ sử dụng kiểu dữ liệu nguyên thủy và String (hoặc các đối tượng đủ đơn giản). **Meta Data** không linh hoạt như **PayLoad** trong việc thay đổi cấu trúc.

Không giống như một `Map<string, Object` thông thường, **Meta Data** trong Axon là immutable. Khi chúng ta thực hiện các thao tác thay đổi, chúng ta sẽ tạo và trả về một phiên bản mới của **Meta Data** thay vì sửa đổi trên phiên bản hiện tại.

Ví dụ:

```java
MetaData metaData = MetaData.with("myKey", 42) // Tạo một MetaData với cặp key-value đầu tiên
                            .and("otherKey", "some Value"); // Thêm cặp key-value khác
```
- **MetaData.with()**: Tạo một đối tượng `MetaData` với cặp key-value được chỉ định.
- **MetaData.and()**: Thêm cặp key-value mới vào Meta Data và trả về phiên bản mới của đối tượng `MetaData`.

## Meta Data trong thông điệp
Trong các thông điệp (message), cách làm việc với **Meta Data** cũng tương tự:

```java
EventMessage eventMessage = 
        GenericEventMessage.asEventMessage("myPayload") // Tạo EventMessage với payload là "myPayload"
                           .withMetaData(singletonMap("myKey", 42)) // Thay thế MetaData hiện tại bằng Map mới
                           .andMetaData(singletonMap("otherKey", "some value")); // Thêm MetaData khác
```
- **withMetaData()**: Thay thế bất kì **Meta Data** hiện tại nào trong thông điệp bằng **Map** đã chỉ định.
- **andMetaData()**: Thêm các mục từ **Map** vào **Meta Data** của thông điệp. Các mục có khóa trùng sẽ bị ghi đè.

Trong ví dụ trên, phương thức `java.util.Collections.singletonMap()` được sử dụng để định nghĩa một mục duy nhất trong **Map**.