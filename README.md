# GnLuckyBlock

![Banner](assets/images/banner.png)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Version](https://img.shields.io/badge/Version-1.0.0--SNAPSHOT-blue.svg)](https://github.com/dangvu/GnLuckyBlock)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg)](https://www.papermc.io)

**GnLuckyBlock** là một plugin Minecraft cao cấp được thiết kế cho các máy chủ Paper/Purpur, mang đến hệ thống Lucky Block (Khối May Mắn) mạnh mẽ, linh hoạt và có khả năng tùy biến cực cao.

---

## 🌟 Tính năng nổi bật

- **Hệ thống Phần thưởng Đa dạng:** Hỗ trợ nhiều loại phần thưởng từ vật phẩm, lệnh console đến các hiệu ứng đặc biệt.
- **Khối Tùy chỉnh:** Cho phép tạo nhiều loại Lucky Block khác nhau với cấu hình riêng biệt.
- **Kiến trúc Mô-đun:** Plugin được xây dựng với cấu trúc `api`, `core`, và `paper` giúp dễ dàng mở rộng và bảo trì.
- **Đa ngôn ngữ:** Hệ thống ngôn ngữ linh hoạt (mặc định hỗ trợ Tiếng Việt).
- **Hiệu suất Tối ưu:** Sử dụng các công nghệ hiện đại (PacketEvents, ProtocolLib) để đảm bảo không gây giật lag cho server.

---

## 🚀 Cài đặt

1. Tải file `.jar` từ trang [Releases](https://github.com/dangvu/GnLuckyBlock/releases).
2. Tắt server Minecraft của bạn.
3. Chép file plugin vào thư mục `plugins/`.
4. Khởi động lại server.
5. Cấu hình plugin trong thư mục `plugins/GnLuckyBlock/`.

---

## 🛠 Biên dịch (Build)

Dự án này sử dụng Gradle. Bạn có thể tự build plugin bằng lệnh sau:

```bash
# Đối với Windows
./gradlew.bat shadowJar

# Đối với Linux/macOS
./gradlew shadowJar
```

File jar sau khi build sẽ nằm trong thư mục `paper/build/libs/`.

---

## 🤝 Đóng góp

Chúng tôi luôn hoan nghênh các đóng góp từ cộng đồng! Nếu bạn tìm thấy lỗi hoặc có ý tưởng mới, vui lòng:

1. Fork dự án này.
2. Tạo nhánh mới (`git checkout -b feature/NewFeature`).
3. Commit thay đổi của bạn (`git commit -m 'Add some NewFeature'`).
4. Push lên nhánh (`git push origin feature/NewFeature`).
5. Tạo một Pull Request.

---

## 📄 Giấy phép

Dự án này được phát hành dưới giấy phép **MIT**. Xem file [LICENSE](LICENSE) để biết thêm chi tiết.

---

## 💎 Thư viện sử dụng

- [Paper API](https://papermc.io/software/paper)
- [PacketEvents](https://github.com/retrooper/packetevents)
- [ProtocolLib](https://github.com/dmulloy2/ProtocolLib)
- [Shadow](https://github.com/johnrengelman/shadow)

---

*Phát triển bởi **Dang Vu**.*
