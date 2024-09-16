## Liên kết Thông Điệp (Message Correlation)
Trong các hệ thống dựa vào thông điệp (messaging system), việc nhóm hoặc liên kết các thông điệp với nhau là rất phổ biến. Trong **Axon Framework**, một **Command** có thể dẫn đến một hoặc nhiều **Event**, và một **Query** có thể đẫn đến một hoặc nhiều **QueryResponse**. Thông thường, sự liên kết này được thực hiện bằng cách sử dụng một thuộc tính cụ thể của thông điệp, được gọi là **correlation identifier** (định danh liên kết).

Để có thể điền `MetaData` cho thông điệp mới được phát ra bên trong một **[Unit Of Work](#)**,`CorrelationDataProvider` có thể được sử dụng. Chính **Unit of Work** là nơi chịu trách nhiệm điền `MetaData` cho thông điệp mới dựa trên `CorrelationDataProvider`. **Axon Framework** hiện tại cung cấp một số implementatation cho functional interface này.

### *MessageOriginProvider*

Mặc định, `MessageOriginProvider` được đăng ký làm **correlation data provider** để sử dụng. Nó chịu trách nhiệm cung cấp hai giá trị quan trọng từ một `Message` này sang một `Message` khác, đó là: **correlationId** và **traceId**.

- **CorrelationId**: Đây là định danh liên kết của thông điệp, luôn tham chiếu đến **identifier** của thông điệp mà nó xuất phát, tức là thông điệp cha.
- **TraceId**: Tham chiếu đến **identifier** của thông điệp đầu tiên tron chuỗi thông điệp, tức là thông điệp root khởi tạo ra chuỗi liên kết hiện tại.

Nếu không có **correlationId** hoặc **traceId** trong `MetaData` của một thông điệp cha khi một thông điệp mới được tạo ra, `MessageOriginProvider` sẽ sử dụng **identifier** của thông điệp đó cho cả hai giá trị này.

### *SimpleCorrelationDataProvider*
`SimpleCorrelationDataProvider` trong **Axon Framework** được cấu hình để sao chép một cách vô điều kiện các giá trị của các khóa được chỉ định từ một `Message` (thông điệp) này sang `MetaData` của thông điệp khác. Điều này có nghĩa, nếu chúng ta muốn sao chép một hoặc nhiều giá trị từ thông điệp ban đầu vào **MetaData** của thông điệp mới, chúng ta có thể sử dụng `SimpleCorrelationDataProvider`

Để cấu hình, chúng ta phải gọi constructor của `SimpleCorrelationDataProvider` với danh sách các khóa (keys) mà chúng ta muốn sao chép. Ví dụ dưới đây minh họa cấch cấu hình `SimpleCorrelationDataProvider` để sao chép các giá trị có khóa `myId` và `myId2`.

```java
public class Configuration {
    
    public CorrelationDataProvider customCorrelationDataProvider() {
        return new SimpleCorrelationDataProvider("myId", "myId2");
    }
}
```

Trong đoạn mã trên:
- **SimpleCorrelationDataProvider** được cấu hình để sao chép giá trị của hai khóa `myId` và `myId2` từ **MetaData** của một thông điệp này sang một thông điệp khác.
  
Nhờ **SimpleCorrelationDataProvider**, có thể dễ dàng truyền các giá trị cụ thể giữa các thông điệp, đảm bảo rằng dữ liệu liên quan được giữ nguyên trong suốt quá trình xử lý mà không cần thay đổi quá nhiều logic.

### *MultiCorrelationDataProvider*

Một `MultiCorrelationDataProvider` có khả năng kết hợp chức năng của nhiều correlation data providers lại với nhau. Để làm thế, cần gọi `MultiCorrelationDataProvider` và truyền vào một list các provider, như ví dụ dưới đây:

```java
public class Configuration {
    
    public CorrelationDataProvider customCorrelationDataProviders() {
        return new MultiCorrelationDataProvider<CommandMessage<?>>(
            Arrays.asList(
                new SimpleCorrelationDataProvider("someKey"),
                new MessageOriginProvider()
            )
        );
    }
}
```

