# Message Interceptor (Bộ chặn thông điệp) - part 01

Trong **Axon framework**, có hai loại **interceptor** dùng để ngăn chặn thông điệp, **dispatch interceptor** và **handler interceptor**.

**Dispatch Interceptor**:
- Được gọi trước khi một thông điệp được gửi đến **message handler** (bộ xử lí thông điệp).
- Tại thời điểm này, có thể chưa biết có một handler tồn tại cho thông điệp đó hay không.
- Thường dùng để kiểm tra, sửa đổi hoặc thêm dữ liệu vào thông điệp trước khí nó được gửi đi.

**Handler Interceptor**:
- Được gọi ngay trước khi **message handler** được thực thi.
- Dùng để thực hiện các bước xử lí bổ sung, chẳng hạn như kiểm tra bảo mật, xác thực dữ liệu hoặc ghi log trước khi thông điệp được xử lí.

## Command Interceptor (Bộ chặn lệnh)
Một trong những lợi thế của việc sử dụng **command bus** (hệ thống gửi lệnh) là khả năng thực hiện các hành động dựa trên tất cả các command nhận được. Ví dụ, chúng ta có thể sử dụng **interceptor** để thực hiện các hành động như:
- **Logging**: Ghi lại mọi command gửi đến hệ thống.
- **Authentication**: Kiểm tra quyền truy cập hoặc xác thực người dùng trước khi xử lý command.

Nhờ có **Command Interceptors**, có thể triển khai các hành động này cho mọi loại command, bất kể chúng thuộc loại nào. Điều này giúp chúng ta dễ dàng áp dụng các system policy đồng nhất cho tất cả các command mà không cần phải cấu hình riêng lẻ cho từng loại.

### Command Dispatch Interceptor

**Command dispatch interceptor** được gọi khi một command được gửi qua **command bus**. Những interceptor này có khả năng sửa đổi command bằng cách thêm **metadata**. Ngoài ra, chúng có thể chặn command bằng cách ném ra một ngoại lệ. Một điều cần lưu ý là các interceptor này luôn được thực thi trên luồng đang gửi lệnh đi.

Ví dụ: 

**Tạo `MessageDispatchInterceptor`**

Dưới đây là ví dụ về cách tạo một `MessageDispatchInterceptor` để ghi lại log của mỗi lệnh được gửi qua **CommandBus**:

```java
public class MyCommandDispatchInterceptor implements MessageDispatchInterceptor<CommandMessage<?>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyCommandDispatchInterceptor.class);

    @Override
    public BiFunction<Integer, CommandMessage<?>, CommandMessage<?>> handle(List<? extends CommandMessage<?>> messages) {
        return (index, command) -> {
            LOGGER.info("Dispatching a command {}.", command);
            return command;
        };
    }
}
```
Trong đoạn mã trên:
- `MyCommandDispatchInterceptor` implement `MessageDispatchInterceptor` để xử lí các command khi chúng được gửi đi.
- Phương thức `handle` trả về một **BiFunction**, dùng để ghi lại log của mỗi lệnh và trả về chính lệnh đó.
- `LOGGER.info` ghi lại thông tin của lệnh trước khi nó được gửi.

**Đăng ký Interceptor với CommandBus**
Chúng ta có thể đăng ký **dispatch interceptor** với **CommandBus** như sau:

```java
public class CommandBusConfiguration {

    public CommandBus configureCommandBus() {
        CommandBus commandBus = SimpleCommandBus.builder().build();
        commandBus.registerDispatchInterceptor(new MyCommandDispatchInterceptor());
        return commandBus;
    }
}
```
Trong đoạn mã trên:
- Tạo một **CommandBus** mới bằng **SimpleCommandBus.builder()**.
- Đăng ký interceptor `MyCommandDispatchInterceptor` với **CommandBus** bằng phương thức `registerDispatchInterceptor`.

#### Structural validation
Trong hệ thống xử lí lệnh, không có ý nghĩa gì khi xử lí một lệnh nếu lệnh đó không chứa đầy đủ thông tin hoặc thông tin không đúng định dạng. Thực tế, một lệnh thiếu thông tin cần bị chặn càng sớm càng tốt, lý tưởng nhất là trước khi một transaction được bắt đầu. Do đó, cần có một **interceptor** để kiểm tra tính đầy đủ và chính xác của các lệnh trước khi xử lý. Đây được gọi là **kiểm tra cấu trúc** (**structural validation**).

#### Hỗ trợ kiểm tra cấu trúc trong Axon
Axon Framework hỗ trợ kiểm tra cấu trúc dựa trên **JSR 303 Bean Validation**. Điều này cho phép chúng ta đánh dấu các trường của lệnh bằng các annotation như `@NotEmpty` và `@Pattern`. Để sử dụng tính năng này, cần thêm một thư viện JSR 303 (vi dụ như **Hibernate-Validator**) vào **classpath** của project.

Sau đó cần cấu hình `BeanValidationInterceptor` trên **command bus**, và interceptor này sẽ tự động tìm kiếm và cấu hình trình xác thực (**validator**) dựa trên các cài đặt mặc định hợp lí. Tuy nhiên, có thể tùy chỉnh chi tiết theo nhu cầu cụ thể của chúng ta.

> **Gợi ý về thự tự sắp xếp các interceptor**:
> Để tiết kiệm tài nguyên khi xử lí các lệnh không hợp lệ, chúng ta nên đặt **interceptor** kiểm tra cấu trúc ở vị trí đầu tiên trong chuỗi **interceptor**. Trong một số trường hợp, có thể chúng ta cần một `LoggingInterceptor` hoặc `AuditingInterceptor` đứng trước, sau đó là **interceptor** kiểm tra cấu trúc.

Lưu ý, `BeanValidationInterceptor` cũng implement `MessageHandlerInterceptor`, cho phép chúng ta cấu hình nó như một handler interceptor.

**Ví dụ cấu hình `BeanValidationInterceptor`**:

- **Thêm các annotation**: Đánh dấu các trường cần kiểm tra trên lệnh của mình như sau:

    ```java
    public class MyCommand {
        @NotEmpty(message = "Field must not be empty")
        private String importantField;

        @Pattern(regexp = "\\d+", message = "Field must contain only digits")
        private String numericField;
    }
    ```

- **Cấu hình `BeanValidationInterceptor`**:
    ```java
    public class CommandBusConfiguration {
        
        public CommandBus configureCommandBus() {
            CommandBus commandBus = SimpleCommandBus.builder().build();
            commandBus.registerDispatchInterceptor(new BeanValidationInterceptor<>());
            return commandBus;
        }
    }
    ```

### Command Handler Interceptor
Bộ chặn xử lí tin nhắn (**Message handler interceptors**) có thể thực hiện các hành động cả trước và sau khi xử lý lệnh. Bộ chặn thậm chí có thể chặn hoàn toàn việc xử lý lệnh, ví dụ như vì lý do bảo mật.

Bộ chặn phải triển khai interface `MessageHandlerInterceptor`. Interface này khai báo một phương thức `handle`, nhận hai tham số:
- `UnitOfWork`: UnitOfWork hiện tại, cung cấp cho chúng ta: **(1) thông điệp đang được xử lí** và **(2) khả năng liên kết logic trước**, trong hoặc sau khi xử lí tin nhắn lệnh.
- `InterceptorChain`: được sử dụng để tiếp tục quá trình gửi tin nhắn.

Không giống như dispatch interceptors hoạt động ở tầng gửi lệnh, handler interceptors được gọi ngay trong ngữ cảnh của trình xử lý lệnh (command handler). Điều này cho phép chúng truy cập và làm việc trực tiếp với dữ liệu liên quan đến lệnh đang được xử lý. Ví dụ, ta có thể đính kèm thêm thông tin vào unit of work, và thông tin này sẽ được sử dụng trong suốt quá trình xử lý lệnh đó.

Ví dụ, hãy tạo một Message Handler Interceptor mà chỉ cho phép handle các command có field `userId` là `axonUser` trong `MetaData`. Nếu `userId` không tồn tại trong metadata, một ngoại lệ sẽ bị ném ra để ngăn chặn command được thực thi. Bên cạnh đó, nếu giá trị của `userId` không khớp với `axonUser`, chúng ta cũng không xử lí tiếp.

```java
public class MyCommandHandlerInterceptor implements MessageHandlerInterceptor<CommandMessage<?>> {

    @Override
    public Object handle(UnitOfWork<? extends CommandMessage<?>> unitOfWork, InterceptorChain interceptorChain) throws Exception {
        CommandMessage<?> command = unitOfWork.getMessage();
        String userId = Optional.ofNullable(command.getMetaData().get("userId"))
                                .map(uId -> (String) uId)
                                .orElseThrow(IllegalCommandException::new);
        if ("axonUser".equals(userId)) {
            return interceptorChain.proceed();
        }
        return null;
    }
}
```

Chúng ta có thể đăng ký handlerinterceptor cho một `CommandBus` như sau:

```java
public class CommandBusConfiguration {

    public CommandBus configureCommandBus() {
        CommandBus commandBus = SimpleCommandBus.builder().build();
        commandBus.registerHandlerInterceptor(new MyCommandHandlerInterceptor());
        return commandBus;
    }
}
```

### Annotation `@CommandHandlerInterceptor`

Về cơ bản, annotation này cho phép chúng ta thêm một Handler Interceptor trực tiếp vào một phương thức trong Aggregate hoặc Entity. Điểm đặc biệt là, có thể dựa vào trạng thái hiện tại của Aggregate đó để quyết định xem có cho phép lệnh được xử lí hay không.

**Một số điểm quan trọng**:
- Có thể đặt annotation này trên các entity bên trong Aggregate, không chỉ trên Aggregate Root.
- có thể chặn một lệnh ở cấp Aggregate Root, ngay cả khi command handler thực sự nằm trong một child entity.
- Việc thực thi lệnh có thể bị ngăn chặn bằng cách ném ra một exception từ phương phức được đánh dấu `@CommandHandlerInterceptor`
- Có thể định nghĩa tham số `InterceptorChain` trong phương thức này để kiểm soát việc thực thi lệnh một cách linh hoạt hơn.
- Thuộc tính `commandNamePattern` của annotation cho phép chặn tất cả các lệnh khớp với một biểu thức chính quy cụ thể.
- Thậm chí có thể apply một event từ bên trong một phương thức được đánh dấu `@CommandHandlerInterceptor`

Ví dụ:

```java
public class GiftCard {
    //..
    private String state;
    //..
    @CommandHandlerInterceptor
    public void intercept(RedeemCardCommand command, InterceptorChain interceptorChain) {
        if (this.state.equals(command.getState())) {
            interceptorChain.proceed(); // Cho phép lệnh được xử lý tiếp
        }
        // Nếu không khớp trạng thái, lệnh sẽ bị chặn ngầm (không gọi proceed())
    }
}
```

Trong ví dụ trên, phương thức `intercept` sẽ kiểm tra trạng thái của `GiftCard` có khớp với trạng thái trong lệnh `RedeemCardCommand` hay không. Nếu khớp, lệnh sẽ được xử lí; nếu không thì sẽ bị chặn.

**Lưu ý**: `@CommandHandlerInterceptor` về cơ bản là một phiên bản cụ thể hơn của `@MessageHandlerInterceptor`, tập trung vào việc xử lý lệnh trong ngữ cảnh của Aggregate/Entity.

