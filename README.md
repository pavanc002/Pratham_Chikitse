# 🩺 Pratham Chikitse — Offline Emergency First Aid Guide

> **ಪ್ರಥಮ ಚಿಕಿತ್ಸೆ** *(Pratham Chikitse)* — Kannada for *"First Aid"*

A lightweight, fully offline Android application providing instant first aid guidance during emergencies — no internet required. Built for rural communities, disaster zones, and anyone who needs reliable medical guidance when connectivity is unavailable.

---

## 📱 About the App

**Pratham Chikitse** is a native Android app designed to be a pocket-sized emergency medical companion. It delivers clear, step-by-step first aid instructions for a wide range of medical emergencies — all without needing an internet connection. The "Light" edition is optimized for low-end devices with minimal storage and memory requirements.

---

## ✨ Features

- 🔴 **Fully Offline** — All content is bundled with the app; no internet needed
- ⚡ **Instant Access** — Find emergency procedures in seconds
- 📋 **Step-by-Step Guides** — Clear, actionable instructions for common emergencies
- 🪶 **Lightweight** — Optimized for low-end Android devices and minimal storage
- 🌐 **Multilingual Ready** — Designed with regional language support in mind
- 🆘 **Emergency Categories** — Covers bleeding, burns, fractures, CPR, choking, poisoning, and more

---

## 🛠️ Tech Stack

| Technology | Details |
|---|---|
| Language | Kotlin |
| Platform | Android |
| Min SDK | Android 5.0+ (API 21) |
| Build System | Gradle (Kotlin DSL) |
| Architecture | MVVM |
| UI | Jetpack / XML Layouts |
| AndroidX | Fully migrated |

---

## 🚀 Getting Started

### Prerequisites

- Android Studio (latest stable recommended)
- JDK 11 or higher
- Android SDK with API level 21+

### Clone & Run

```bash
# Clone the repository
git clone https://github.com/YOUR_USERNAME/pratham-chikitse.git

# Open in Android Studio
# File → Open → select the project folder

# Build & run
./gradlew assembleDebug
```

Or simply open the project in **Android Studio** and click **Run ▶️**.

---

## 📁 Project Structure

```
PrathamChikitseLight/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/          # Kotlin source files
│   │   │   ├── res/           # Layouts, drawables, strings
│   │   │   └── AndroidManifest.xml
│   │   └── test/              # Unit tests
│   └── build.gradle.kts
├── gradle/
│   └── wrapper/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🤝 Contributing

Contributions are welcome! If you'd like to add more first aid guides, improve translations, or fix bugs:

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/add-snake-bite-guide`)
3. Commit your changes (`git commit -m 'Add snake bite first aid guide'`)
4. Push to the branch (`git push origin feature/add-snake-bite-guide`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).

---

## 🙏 Acknowledgements

- Inspired by the need for accessible healthcare information in underserved communities
- "Pratham Chikitse" (ಪ್ರಥಮ ಚಿಕಿತ್ಸೆ) means *First Aid* in Kannada
- Built with ❤️ for Bharat

---

> ⚠️ **Disclaimer:** This app provides general first aid guidance for informational purposes only. Always seek professional medical help in emergencies. Call your local emergency number (112 in India) immediately in life-threatening situations.
