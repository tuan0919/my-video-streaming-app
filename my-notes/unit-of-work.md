## Unit of Work 

Unit of Work (UoW) đóng vai trò như một đơn vị công việc trong Axon, quản lý và phối hợp các hành động được thực hiện trong quá trình xử lý một message (có thể là command, event hoặc query).

Mặc dù UoW là một khái niệm cốt lõi, nhưng trong hầu hết trường hợp, chúng ta không cần tương tác trực tiếp với nó. Axon Framework tự động quản lí UoW để đảm bảo t1inh nhất quán và toàn vẹn dữ liệu.

### Vai trò của Unit of Work
- **Phối hợp các hành động**: UoW cho phép các thành phần khác nhau trong ứng dụng đăng ký các hành động cần thực hiện tại các giai đoạn khác nhau của quá trình xử lí message, ví dụ như `onPrepareCommit` (chuẩn bị commit), `onRollback` (rollback khi có lỗi) hoặc `onCleanup` (dọn dẹp sau khi hoàn thành).

- **Quản lý tài nguyên**: Có thể sử dụng UoW để đính kèm các tài nguyên cần được tái sử dụng nhiều lần trong quá trình xử lí message, hoặc để đảm bảo các tài nguyên được tạo ra sẽ được dọn dẹp đúng cách khi UoW kết thúc.

### Truy cập Unit of Work
- **Thông qua tham số** Trong các `handler`, có thể nhận UoW thông qua một tham số trong phương thức `handle`. Nếu sử dụng annotation, có thể thêm một tham số kiểu `UnitOfWork` vào phương thức được đánh dấu annotation.
- **Sử dụng `CurrentUnitOfWork`**: Ở các vị trí khác nhau trong mã, có thể lấy UoW liên quan với luồng hiện tại bằng cách gọi CurrentUnitOfWork.get(). Lưu ý rằng, phương thức này sẽ ném ra một exception nếu không có UoW nào được liên kết với luồng hiện tại. Có thể sử dụng `CurrentUnitOfWork.isStarted()` để kiểm tra xem có UoW nào đang hoạt động hay không.

### Unit of Work và Transaction
- **UoW không thay thế transaction**: UoW chỉ là một bộ đệm (buffer) các thay đổi, không phải là một sự thay thế cho các transaction trong cơ sở dữ liệu. Mặc dù tất cả các thay đổi được staged (đưa vào UoW) chỉ được commit khi UoW được commit, nhưng quá trình commit của UoW không phải là nguyên tử (atomic). Điều này có nghĩa là nếu commit thất bại, một số thay đổi có thể đã được lưu trữ vĩnh viễn, trong khi một số khác thì không.
- **Sử dụng transaction khi cần**: Nếu UoW của chúng ta chứa nhiều hành động, nên cân nhắc đính kèm một transaction vào quá trình commit của UoW. Sử dụng `unitOfWork.onCommit(...)` để đăng ký các hành động cần thực hiện khi UoW được commit, đảm bảo tính nguyên tử của các thay đổi.

### Xử lý Exception và Rollback
- **Exception trong Handler**: Trong quá trình xử lý message, các handler của chúng ta có thể ném ra các `Exception`. Theo mặc định, các `unchecked exceptions` (như `RunTimeException` và các lớp con của nó) sẽ khiến cho `UnitOfWork` rollback tầt cả các thay đổi đã được staged. Điều này đồng nghĩa với việc các tác dụng phụ (side effects) đã được lên lịch cũng sẽ được hủy bỏ.
- **Các chiến lược rollback**: Axon Framework cung cấp một số chiến lược rollback sẵn có:
  - `RollbackConfigurationType.NEVER`: Luôn luôn commit `UnitOfWork`, bất kể có exception nào xảy ra hay không.
  - `RollbackConfigurationType.ANY_THROWABLE`: Luôn luôn rollback khi có bất kì exception nào xảy ra.
  - `RollbackConfigurationType.UNCHECKED_EXCEPTIONS`: Rollback khi có `Error` hoặc `RuntimeException`.
  - `RollbackConfigurationType.RUNTIME_EXCEPTION`: Chỉ rollback khi có `RuntimeException`, không rollback khi có `Error`.
#### Quản lí vòng đời của Unit Of Work
- **Tự động**: Khi sử dụng các thành phần có sẵn của Axon Framework để xử lý message, vòng đời của `UnitOfWork` sẽ được tự động quản lí.
- **Thủ công**: Nếu chúng ta tự triển khai việc xử lí message, thì cần phải tự khởi tạo và commit (hoặc rollback) `UnitOfWork` một cách thủ công.
#### Sử dụng `DefaultUnitOfWork`
- **Phù hợp cho hầu hết trường hợp**: Trong phần lớn trường hợp, `DefaultUnitOfWork` sẽ đáp ứng đủ nhu cầu của chúng ta. Nó được thiết kế để xử lí message trên một luồng duy nhất.
- **Thực thi tác vụ trong ngữ cảnh của UoW**: Đển thực thi tác vụ trong ngữ cảnh của một `UnitOfWork`, chúng ta chỉ cần gọi `UnitOfWork.execute(Runnable)` hoặc `UnitOfWork.executeWithResult(Callable)` trên một `DefaultUnitOfWork` mới. UoW sẽ tự đ6ọng start và commit khi các tác vụ hoàn thành, hoặc rollback nếu tác vụ gặp lỗi.
- **Kiểm soát thủ công**: Chúng ta cũng có thể chọn cách start, commit hoặc rollback UoW một cách thủ công nếu cần kiểm soát chi tiết hơn.

Ví dụ:

```java
UnitOfWork uow = DefaultUnitOfWork.startAndGet(message);

// Cách 1: Tự động commit
ResultMessage<?> result = uow.executeWithResult(() -> {
    // Logic xử lý message ở đây
    return "Kết quả xử lý"; 
});

// Cách 2: Thủ công commit hoặc rollback
try {
    // Logic xử lý message ở đây
    uow.commit();
} catch (Exception e) {
    uow.rollback(e);
    // Có thể rethrow exception nếu cần
}
```

> **Note**
> - `UnitOfWork` luôn xoay quanh việc xử lí một message. Nó luôn được bắt đầu với một message cần xử lý.
> - Kết quả thực thi của UoW(`executeWithResult(...)`) sẽ là một `ResultMessage`. Nếu trong quá trình xử lí message gặp vấn đề, ta sẽ nhận được một `Resultmessage` mang tính chất exception - `isExceptional()` sẽ trả về `true` và `exceptionResult()` sẽ cung cấp `Throwable` cụ thể để biểu thị lỗi đã xảy ra.

### Các giai đoạn của Unit Of Work
Mỗi khi `UnitOfWork` chuyển sang một giai đạon mới, các listener đã đăng ký sẽ được thông báo.

#### 1. Giai đoạn Active (Hoạt động)
- Là giai đoạn khởi đầu của `UnitOfWork`.
- `UnitOfWork` thường được đăng ký với luồng hiện tại trong giai đoạn này (thông qua `CurrentUnitOfWork.set(UnitOfWork)`).
- Sau đó, message thường được xử lí bởi một message handler trong giai đoạn này.

#### 2. Giai đoạn Commit (Cam kết)
- Giai đoạn này diễn ra sau khi message đã được xử lí nhưng trước khi `UnitOfWork` được commit.
- Các listener `onPrepareCommit` sẽ được gọi.
- Nếu `UnitOfWork` được liên kết với một transaction, các listener `onCommit` sẽ được gọi để commit các transaction hỗ trợ.
- Khi commit thành công, các listener `afterCommit` sẽ được gọi.
- Nếu commit hoặc bất kì bước nào trước đó thất bại, các listener `onRollback` sẽ được gọi.
- Kết quả của message handler được chứa trong `ExcetutionHandler` của `UnitOfWork`, nếu có.

#### 3. Giai đoạn Cleanup (Dọn dẹp)
- Đây là giai đoạn mà bất kỳ tài nguyên nào được giữ bởi `UnitOfWork` này (chẳng hạn như khóa - lock) sẽ được giải phóng.
- Nếu có nhiều `UnitOfWork` lồng nhau, giai đoạn cleanup sẽ bị trì hoãn cho đến khi `UnitOfWork` ngoài cùng sẵn sàng để dọn dẹp.

### UnitOfWork và tính nguyên tử
Quá trình xử lí message có thể được coi là một thủ tục nguyên tử; nó nên được xử lí hoàn toàn hoặc không xử lí gì cả. Axon Framework sử dụng `UnitOfWork` để theo dõi các hành động được thực hiện bởi các message handler. Sau khi handler hoàn thành, Axon sẽ cố gắng commit các hành động đả được đăng ký với `UnitOfWork`.

### UnitOfWork và Transaction
Có thể liên kết một transaction với một `UnitOfWork`. Nhiều thành phần, chẳng hạn như các triển khai của `CommandBus` và `QueryBus` và tất cả các `EventProcessor` xử lý không đồng bộ, cho phép cấu hình một `TransactionManager`. `TransactionManager` này sau đó sẽ được sử dụng để tạo ra các transaction để liên kết với `UnitOfWork` được sử dụng để quản lí quá trình xử lí của một mesasge.

### Quản lý tài nguyên với UnitOfWork
Khi các thành phần ứng dụng cần tài nguyên ở các giai đoạn khác nhau của quá trình xử lý message, chẳng hạn như kết nối cơ sở dữ liệu hoặc một `EntityManager`, các tài nguyên này có thể được đính kèm vào `UnitOfWork`. Phương thức `unitOfWork.getResources()` cho phép ta truy cập vào các tài nguyên được đính kèm vào `UnitOfWork` hiện tại. Một số phương thức hỗ trợ trực tiếp trên `UnitOfWork` để giúp làm việc với các tài nguyên dễ dàng hơn.

Khi các `UnitOfWork` lồng nhau cần có thể truy cập vào một tài nguyên, ta nên đăng ký trên `UnitOfWork` gốc, có thể được truy cập bằng cách sử dụng `unitOfWork.root()`. Nếu một `UnitOfWork` là gốc, nó sẽ đơn giản trả về chính nó.