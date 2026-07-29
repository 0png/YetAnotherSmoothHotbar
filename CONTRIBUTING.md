# Contributing to YASH

感謝你協助改善 YASH。提交變更前，請先閱讀 [`README.md`](README.md)，並確認是否已有相關 Issue 或 Pull Request。

## Branch 模型

- `dev`：預設整合分支，也是一般 Pull Request 的目標。
- `main`：穩定發布分支，只接受 `dev → main` 的 release Pull Request。
- 從最新 `dev` 建立短期 branch，例如 `feature/spring-animation`、`fix/resource-reload` 或 `docs/build-guide`。
- 禁止直接推送、force push 或刪除 `dev`、`main`。

## 開發環境

- JDK 25
- 使用 repository 內的 Gradle Wrapper，不使用系統 Gradle。

```powershell
git switch dev
git pull --ff-only
git switch -c feature/short-description
.\gradlew.bat clean build
```

## 提交與 Pull Request

- Commit 建議使用 Conventional Commits：`feat:`、`fix:`、`docs:`、`build:`、`ci:`、`refactor:`、`test:`、`chore:`。
- 一個 Pull Request 聚焦一個目的，並說明行為改變、測試方式與玩家影響。
- 合併採 squash merge；repository 會在合併後刪除來源 branch。
- 所有 required checks 必須通過，所有 review conversation 必須解決。

## 程式與資源邊界

- `shared` 不得引用 Minecraft 或 loader API。
- `versions/<version>/minecraft` 不得引用 Fabric、NeoForge 等 loader API。
- 玩家可見文字不得硬編碼在 Java；同一變更必須同步更新 `en_us.json`、`zh_tw.json`。
- 翻譯 key 使用穩定英文識別碼，設定檔不得保存翻譯後文字。
- 原創美術素材受 [`ASSETS_LICENSE.md`](ASSETS_LICENSE.md)約束；提交素材即表示你有權提供給本專案使用。

## 發布

1. 將 `gradle.properties` 的 `mod_version` 改成不含 `-SNAPSHOT` 的正式版本。
2. 更新 `CHANGELOG.md`，由 `dev` 建立 Pull Request 到 `main`。
3. 合併且 CI／CodeQL 通過後，在 `main` 的 release commit 建立 `v<mod_version>` tag。
4. Release workflow 驗證 branch、版本與 build 後，自動上傳 JAR 和 SHA-256 checksum。
