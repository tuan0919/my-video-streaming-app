## Implement Saga với Axon
Saga là một loại event listener đặc biệt, có nhiệm vụ quản lí một giao dịch nghiệp vụ (business transaction). Một số giao dịch có thể kéo dài trong vài ngày thậm chí là hàng tuần, trong khi những giao dịch khác chỉ tốn vài mili giây để hoàn thành. Trong Axon, mỗi instance của một Saga chịu trách nhiệm quản lí một giao dịch nghiệp vụ duy nhất. Điều đó có nghĩa là một Saga duy trì trạng thái cần thiết để quản lí giao dịch đó, tiếp tục nó hoặc thực hiện các thao tác bù trừ để hoàn tác bất kì hành động nào đã thực hiện (hoặc commit). Thông thường, và trái ngược với event listener thông thường, một saga có một điểm  bắt đầu và một điểm kết thúc, cả hai đều được kích hoạt bởi các sự kiện. Trong khi điểm bắt đầu của saga thường rất rõ ràng, thì có rất nhiều cách để một saga kết thúc.

Trong Axon, các saga là các lớp định nghĩa một hoặc nhiều phương thức `@SagaEventHandler`. Không giống như các event handler thông thường, nhiều instance của một saga có thể tồn tại tại bất kì thời điểm nào. Saga được quản lí bởi một event processor duy nhất (Tracking hoặc Subscribing), chuey6n xử lí các sự kiện cho loại saga cụ thể đó.

## Vòng đời của một Saga

Một saga instance duy nhất chịu trách nhiệm quản lí một transaction duy nhất. Điều đó có nghĩa là bạn cần có khả năng chỉ ra điểm bắt đầu và điểm kết thúc cho vòng đời của một saga.

Trong một saga, các event handler được chú thích bằng `@SagaEventHandler`. Nếu một sự kiện cụ thể biểu thị sự bắt đầu của một giao dịch, thêm một annotation `@StartSaga` vào phương thức đó. Annotation này sẽ tạo một saga mới và gọi phương thức event handler của nó khi một sự kiện phù hợp được bắn ra.

Theo mặc định, một sag mới chỉ được bắt đầu nếu không tìm thấy saga hiện có phù hợp (cùng loại). Chúng ta có thể bắt buộc thao tác tạo saga mới bằng thuộc tính `forceNew` trên annotation `@StartSaga`.

Kết thúc một saga có thể được thực hiện theo hai cách. Nếu một sự kiện nhất định luôn biểu thị sự kết thúc vòng đời của một saga, đánh dấu phương thức đó bằng `@EndSaga`. Vòng đời của saga sẽ kết thúc khi gọi handler đó. Ngoài ra, có thể gọi hàm `SagaLifeCycle.end()` từ bên trong saga để kết thúc vòng đời của nó, cách tiếp cận này cho phép chúng ta kết thúc một saga có điều kiện.


