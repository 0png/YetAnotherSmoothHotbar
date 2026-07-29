# YASH — Yet Another Smooth Hotbar

[![CI](https://github.com/0png/YetAnotherSmoothHotbar/actions/workflows/ci.yml/badge.svg?branch=dev)](https://github.com/0png/YetAnotherSmoothHotbar/actions/workflows/ci.yml)
[![CodeQL](https://github.com/0png/YetAnotherSmoothHotbar/actions/workflows/codeql.yml/badge.svg?branch=main)](https://github.com/0png/YetAnotherSmoothHotbar/actions/workflows/codeql.yml)
[![License: MIT](https://img.shields.io/badge/code-MIT-blue.svg)](LICENSE)
[![Minecraft 26.2](https://img.shields.io/badge/Minecraft-26.2-62B47A)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Loader-Fabric-DBD0B4)](https://fabricmc.net/)

YASH 是一個 Minecraft Java Edition 的純客戶端 Mod，目標是提供流暢、可自訂且支援主題的快捷欄選取框。

目前首版開發目標為：

- Minecraft 26.2
- Fabric
- Java 25
- Client-side only
- English 與繁體中文

完整產品規格請參閱 [`spec.md`](spec.md)，分階段工作與驗收條件請參閱 [`task.md`](task.md)。

## 專案模組

- `shared`：不依賴 Minecraft 或 loader 的共用程式碼、資源與翻譯。
- `versions/26_2/minecraft`：Minecraft 26.2 專用但不引用 Fabric API 的整合層。
- `versions/26_2/fabric`：Fabric 啟動與最終發行 JAR。

未來會在 `versions/<minecraft_version>/<loader>` 增加其他版本與 loader，不製作多版本共用 JAR。

## 開發流程

- `dev` 是預設整合分支；功能與修正從 `dev` 建立短期 branch，透過 Pull Request 合併回 `dev`。
- `main` 只保存可發布狀態，僅接受由 `dev` 發出的 release Pull Request。
- `v<version>` tag 必須指向 `main`，且版本必須與 `gradle.properties` 的 `mod_version` 完全相同；CI 才會建立 GitHub Release。

詳細規範請參閱 [`CONTRIBUTING.md`](CONTRIBUTING.md)與 [`SECURITY.md`](SECURITY.md)。

## 建置

```powershell
.\gradlew.bat clean build
```

成品位於：

```text
versions/26_2/fabric/build/libs/yash-26.2-fabric-<version>.jar
```

啟動 Fabric 開發客戶端：

```powershell
.\gradlew.bat :versions:26_2:fabric:runClient
```

## 授權

- 程式碼：MIT，詳見 [`LICENSE`](LICENSE)。
- 原創圖像與其他美術素材：All Rights Reserved，詳見 [`ASSETS_LICENSE.md`](ASSETS_LICENSE.md)。

## 專案連結

- 原始碼與版本發布：https://github.com/0png/YetAnotherSmoothHotbar
- 問題回報：https://github.com/0png/YetAnotherSmoothHotbar/issues
