# gnluckyblock

![Banner](assets/images/banner.png)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Version](https://img.shields.io/badge/Version-1.0.0--SNAPSHOT-blue.svg)](https://github.com/gn027c/gnluckyblock)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg)](https://www.papermc.io)

**gnluckyblock** is a premium Minecraft plugin designed for Paper/Purpur servers, providing a powerful, flexible, and highly customizable Lucky Block system.

---

## 🌟 Key Features

- **Diverse Reward System:** Supports various reward types from items and console commands to special effects.
- **Custom Blocks:** Create multiple types of Lucky Blocks with unique configurations.
- **Modular Architecture:** Built with `api`, `core`, and `paper` modules for easy extension and maintenance.
- **Multi-language Support:** Flexible language system (defaulting to Vietnamese).
- **Optimized Performance:** Utilizes modern technologies (PacketEvents, ProtocolLib) to ensure no server lag.

---

## 🚀 Installation

1. Download the `.jar` file from the [Releases](https://github.com/gn027c/gnluckyblock/releases) page.
2. Stop your Minecraft server.
3. Copy the plugin file into the `plugins/` directory.
4. Restart the server.
5. Configure the plugin in the `plugins/gnluckyblock/` folder.

---

## 🛠 Building

This project uses Gradle. You can build the plugin using the following commands:

```bash
# For Windows
./gradlew.bat shadowJar

# For Linux/macOS
./gradlew shadowJar
```

The built jar file will be located in the `paper/build/libs/` directory.

---

## 🤝 Contributing

Contributions are always welcome! If you find a bug or have a new idea, please:

1. Fork the project.
2. Create a new branch (`git checkout -b feature/NewFeature`).
3. Commit your changes (`git commit -m 'Add some NewFeature'`).
4. Push to the branch (`git push origin feature/NewFeature`).
5. Open a Pull Request.

---

## 📄 License

This project is released under the **MIT** license. See the [LICENSE](LICENSE) file for more details.

---

## 💎 Libraries Used

- [Paper API](https://papermc.io/software/paper)
- [PacketEvents](https://github.com/retrooper/packetevents)
- [ProtocolLib](https://github.com/dmulloy2/ProtocolLib)
- [Shadow](https://github.com/johnrengelman/shadow)

---

*Developed by **gn027c**.*

---

## 🇻🇳 Phiên bản Tiếng Việt (Vietnamese Version)

**gnluckyblock** là một plugin Minecraft cao cấp được thiết kế cho các máy chủ Paper/Purpur, mang đến hệ thống Lucky Block mạnh mẽ và linh hoạt.

### Tính năng:
- Hệ thống phần thưởng phong phú.
- Cấu trúc mô-đun dễ bảo trì.
- Hỗ trợ tốt nhất cho cộng đồng Minecraft Việt Nam.

*(Xem chi tiết hướng dẫn và tính năng bằng Tiếng Anh ở trên).*
