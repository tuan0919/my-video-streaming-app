# Ghi chú cho bản thân:

<details>
    <summary><span style="font-size: 25px; font-weight: 600">Java Reactive Programming</span></summary>
    
-   <details>
    <summary>
        <b>Một số hiểu sai</b>
    </summary>

    - Reactive Programming không nhất thiết phải có **Asynchronous**, chúng ta có thể code **Synchronous**.

    </details>

- <details>
    <summary>
        <b>Use case</b>
    </summary>

    - **User events**: Đặc biệt là khi làm việc với các tác vụ bên phía UI, Front End. Khi user thực hiện một sự kiện nào đó thì cần thực hiện một **hành động** tương ứng cho sự kiện đấy.

    - **IO resposne**: Khi user thực hiện một input gì đấy chẳng hạn đọc file, sẽ có một luồng input diễn ra và sau khi đọc xong, cần thực hiện một **hành động** nào đó.
  </details>

- <details>
    <summary>
        <b>Tại sao lại cần quan tâm?</b>
    </summary>

    - <details>
      <summary><b>Câu hỏi</b></summary>

       Tại sao chúng ta lại phải quan tâm các vấn đề ở phần **Use case** khi mà đó là các việc xảy ra ở UI trong khi Java là ngôn ngữ được thực hiện đa số ở server-side? 
       Quá trình hoạt động chủ yếu của server side là:

       - Nhận request đến.
       - Server thực hiện một số tác vụ.
       - Response dữ liệu.

       Trông có vẻ là **synchronous**? Chúng ta không bỏ ngang và làm một tác vụ gì khác, thế tại sao ta - developer back-end phải quan tâm đến reactive programming? Về cơ bản request phải **chờ request thực hiện xontg** thì mới trả về client, đó là đặc trưng cơ bản của **HTTP**.
      </details>
    - <details>
      <summary><b>Yêu cầu của ứng dụng hiện đại</b></summary>
        Các ứng dụng hiện đại yêu cầu đến các vấn đề sau:

        - **High data scale** - dữ liệu truyền tải lớn.
        - **High usage scale** - số lượng người dùng lớn.
        - **Cloud based costs** - với sự bùng nổ của các giải pháp đám mây, hiện nay chúng ta thường thuê một dịch vụ lưu trữ bên thứ ba nên sẽ quan tâm đến vấn đề truyền tải hơn để tiết kiệm chi phí.
      </details>
    - <details>
       <summary>
        <b>Xem xét ví dụ</b>
       </summary>

       **Ví dụ 1:** Vấn đề gì với đoạn code dưới đây?
       ```java
       @GetMapping("/users/{userId}")
        public User getUserDetails(@PathVariable String userId) {
            User user = userService.getUser(userId);
            UserPreferences prefs = userPreferencesService.getPreferences (userId);
            user.setPreferences (prefs);
            return user;
        }
       ```
       Đoạn code trên thực hiện hai thao tác:
       - a. Lấy user từ **User Service**.
       - b. Lấy user preferences từ **User Preferences Service**.

       Ta thấy hai thao tác này đang block lẫn nhau, thao tác `a.` cần phải diễn ra trước sau đó đến thao tác `b.` trong khi trên thực tế, hai thao tác này không hề phụ thuộc lẫn nhau => **Unnecessarily sequential**
       
       **Ví dụ 2:** Sơ đồ dưới đây thể hiện hoạt động của web server một cách khái quát nhất:
       ![example](images/Screenshot%202024-08-07%20192144.png)
       
       Về cơ bản thì: 
       - Khi web server nhận được request, nó thêm một thread mới để handle request đó.
       - Sau đó một thread mới đến trong khi thread trước đó vẫn đang xử lí, web server sẽ spawn thêm một thread mới.
       - Nghĩa là, **một thread xử lí càng lâu** sẽ khiến cho **server có nhiều thread cùng tồn tại**.

      Ta thấy được đến một lúc nào đó, số lượng thread sẽ đạt giới hạn và server sẽ không thể spawn thêm thread mới => **Idling threads**
      </details>
    - <details>
      <summary>
      <b>Cách giải quyết</b>
      </summary>

       **Ý tưởng đầu tiên**: Ta sử dụng các **Concurrency APIs** để giải quyết. Cụ thể là ta sẽ dùng hai class **Future** và **CompletableFuture** đã có từ Java 8:
       
       ```java
        CompletableFuture<User> userAsync = CompletetableFuture
            .supplyAsync(() => userService.getUser(userId));
       ```

        Vấn đề là khi chúng ta sử dụng nó trong SpringBoot sẽ khiến code của chúng ta trông rất lộn xộn như sau:
       
        ```java
        @GetMapping("/users/{userId}")
        public User getUserDetails(@PathVariable String userId) {
            CompletetableFuture<User> userAsync = CompletetableFuture.supplyAsync(() => userService.getUser(userId))
            CompletetableFuture<UserPreferences> userPreferencesAsync = CompletetableFuture.supplyAsync(() => userPreferencesService.getPreferences(userId))
            CompletetableFuture<Void> bothFutures = CompletetableFuture.allOf(userAsync, userPreferencesAsync)
            bothFutures.join()
            User user = userAsync.join();
            UserPreferences prefs = userPreferencesAsync.join();
            user.setPreferences(prefs);
            return user;
        }
        ```

        Ngoài ra, chúng ta cần phải thực hiện **tất cả các bước trên** chỉ để **hai tác vụ** được chạy **đồng thời**. 
        
        Việc gọi hàm `userAsync.join()` vẫn sẽ khiến thread bị block, thread này vẫn cần phải **chờ cả hai tác vụ hoàn thành** thì sau đó mới return, bởi vì endpoint này return về **Object User**, thế nên thread phải đợi cả hai tác vụ trên hoàn thành để lấy được đầy đủ thông tin của User.
        
        Về cơ bản, cách tiếp cận này **cải thiện** được việc hai tác vụ bây giờ sẽ **chạy song song** chứ không còn **chạy tuần tự**. Thế nhưng **thread vẫn bị block**.
      </details>
</details>

</details>
