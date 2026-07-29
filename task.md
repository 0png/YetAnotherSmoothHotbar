# YASH 開發任務

本文件將 [`spec.md`](spec.md) 拆成可依序交付的大任務。每個任務完成時，必須同時滿足交付物與驗收條件；若新增玩家可見文字，也必須在同一任務更新 `en_us.json`、`zh_tw.json` 與語言檔測試。

## 狀態說明

- `[ ]` 尚未開始
- `[-]` 進行中
- `[x]` 已完成並通過驗收

## 架構原則

- 共用方向固定為 `shared → Minecraft 版本層 → loader adapter`。
- `shared` 不得引用 Minecraft、Fabric、NeoForge 或其他 loader 類別。
- Minecraft 版本層可以引用該版本的原版 client API，但不得引用 loader API。
- loader adapter 只負責啟動、生命週期、Mixin 註冊及 loader 專屬的選裝整合。
- 所有玩家可見文字使用 i18n key；設定檔只保存穩定識別碼，不保存翻譯結果。
- 每個 Minecraft 版本與 loader 組合輸出獨立 JAR，不製作多版本共用 JAR。

---

## [x] T0 — 專案骨架與架構邊界

**依賴：** 無

**目標：** 建立可持續擴充、可建置的 Minecraft 26.2 Fabric client-only 專案。

**交付物：**

- Gradle multi-project：`shared`、`versions/26_2/minecraft`、`versions/26_2/fabric`。
- Java 25、Gradle Wrapper、Fabric 26.2 依賴與 client-only metadata。
- Fabric client entrypoint、空的 client Mixin 設定與最終 JAR 命名規則。
- README、程式碼 MIT 授權、素材 All Rights Reserved 授權。
- `en_us.json`、`zh_tw.json` 與翻譯 key 完整性測試。

**驗收條件：**

- `gradlew clean build` 成功。
- 所有自動測試通過。
- 最終 JAR 名稱為 `yash-26.2-fabric-0.1.0-SNAPSHOT.jar`。
- JAR 含 Fabric metadata、Mixin 設定、共用類別及兩份語言檔。
- Fabric metadata 沒有 server 或 main entrypoint。

---

## [ ] T1 — 幀率獨立動畫核心

**依賴：** T0

**目標：** 在 `shared` 實作完全不依賴 Minecraft 的 selector 動畫狀態機。

**交付物：**

- `AnimationEngine`、`AnimationFrame`、`AnimationConfig`、`AnimationPreset`、`EasingType`、`WrapMode` 等內部契約。
- 以 elapsed time 計算的幀率獨立更新流程。
- Bedrock Inspired、Smooth、Snappy、Spring、Minimal、Custom、Disabled 的動畫參數。
- Across／Wrap 路徑及從目前畫面位置重新導向最新目標的行為。
- Spring overshoot、damping、收斂與 arrival pulse 事件邊界。

**驗收條件：**

- 30、60、144、240 FPS 模擬下，動畫完成時間與指定 duration 的誤差在一個模擬 frame 內。
- `1 → 5 → 8 → 3` 快速切換不排隊、不重設到整數格，也不播放舊目標。
- Across 與雙向 Wrap 均連續，Spring 在合理時間內收斂。
- Pulse 不因連續切換而無限疊加。

---

## [ ] T2 — 設定模型、預設與安全保存

**依賴：** T0、T1

**目標：** 建立穩定、可遷移且損壞時不會讓遊戲崩潰的設定系統。

**交付物：**

- Enabled、Preset、Theme、Duration、Easing、Wrap、Tint、Opacity、Scale、Glow、Stretch、Pulse、Reduced Motion 與進階 Spring 參數。
- JSON schema version 與向後遷移入口；enum 以穩定識別碼序列化。
- 官方 preset 完整套用、修改 preset 參數後自動轉為 Custom。
- 原子式保存、損壞檔案備份、警告紀錄與安全預設回退。
- Reduced Motion 的集中式有效設定計算，不覆寫玩家原始偏好。

**驗收條件：**

- 首次啟動取得 `spec.md` 指定的安全預設值，重啟後所有欄位保留。
- 損壞或缺欄位設定不造成崩潰，原檔有備份且可回退。
- Disabled 可即時恢復原版行為。
- Reduced Motion 關閉 Spring、Stretch、Pulse 與明顯 Glow 動畫，但保留主題外觀。

---

## [ ] T3 — Minecraft 26.2 HUD 整合

**依賴：** T1、T2

**目標：** 將動畫核心接到 Minecraft 26.2 快捷欄，且只改變客戶端視覺。

**交付物：**

- 直接觀察 selected slot，而非監聽特定輸入按鍵。
- 原版 selector 抑制與 YASH selector 單一繪製 hook。
- 正確的 HUD／GUI scale 座標換算與 partial-frame 時間來源。
- F1、死亡重生、換世界、連線狀態、視窗尺寸、GUI Scale 及資源重載時的安全重設。

**驗收條件：**

- 滾輪、數字鍵、Pick Block、控制器、自訂綁定及其他 Mod 改 slot 均觸發相同行為。
- 啟用時畫面只有一個 selector；停用後不需重啟且不殘留效果。
- 狀態變更後不會卡在中央、留下殘影、飛越畫面或使用舊座標。
- 不讀寫背包資料、不送封包、不改變實際切換或操作行為。

---

## [ ] T4 — 主題渲染、原創素材與視覺效果

**依賴：** T2、T3

**目標：** 完成主題、資源包相容與可獨立組合的 selector 效果。

**交付物：**

- Vanilla／Resource Pack、Clean、Creeper、Enderman、Wolf、Bee 六種主題。
- 原創 Minecraft 像素風素材、主題 metadata、尺寸、偏移與建議效果參數。
- Tint、Opacity、Scale、Glow、Stretch、Arrival Pulse。
- Vanilla 模式從目前資源包取得 selector；內建主題只讀取 Mod JAR 素材。

**驗收條件：**

- 資源包重載後不需重啟、不出現遺失貼圖，動畫狀態安全重設。
- 主題、動畫 preset 與效果可任意組合，主題預設不會覆蓋玩家明確選擇。
- 所有 GUI Scale 下都能辨認選中格，且不遮擋物品數量與耐久度。
- Render loop 不進行每幀檔案讀取或貼圖載入。

---

## [ ] T5 — 設定畫面與即時預覽

**依賴：** T2、T4

**目標：** 提供不離開遊戲即可理解、調整和測試所有功能的設定介面。

**交付物：**

- Presets、Themes、Advanced 三頁，以及基本／進階模式。
- 九格模擬快捷欄、點擊、循環、首尾切換與快速連續切換測試。
- 可自訂的設定畫面按鍵。
- Mod Menu 20.0.1 選裝整合；未安裝 Mod Menu 時仍能正常載入。
- 即時預覽、恢復預設與離開畫面自動保存。

**驗收條件：**

- 所有控制項修改後立即影響預覽，不需先保存。
- 重新選擇官方 preset 會完整恢復該 preset；手動改進階參數會切到 Custom。
- 安裝與未安裝 Mod Menu 的環境都能啟動，且只有安裝時才出現 Mod Menu 設定入口。
- 預覽與實際 HUD 使用同一動畫與主題渲染核心。

---

## [ ] T6 — i18n、無障礙與生命週期相容

**依賴：** T3、T4、T5

**目標：** 確保所有 UI 可翻譯、降低動態模式一致，並完成遊戲生命週期相容。

**交付物：**

- `en_us.json` 作為 canonical key 集合，完整的 `zh_tw.json`。
- `option`、`description`、`preset`、`theme`、`action`、`keybind` 等 key 命名空間。
- 語言檔 JSON、空字串及 key parity 自動測試。
- 新增語言文件流程；新增語言不得要求修改 Java 或設定格式。
- HUD 隱藏、切換世界／伺服器、死亡重生與資源重載相容檢查。

**驗收條件：**

- Java 原始碼沒有玩家可見的硬編碼字串。
- 英文與繁中 key 集合完全相同；缺少其他語言或 key 時回退英文。
- Reduced Motion 在 HUD、設定畫面與預覽中使用同一有效設定。
- 所有生命週期場景都不產生雙 selector、殘影或錯誤動畫位置。

---

## [ ] T7 — 效能、相容矩陣與 1.0 發布

**依賴：** T1–T6

**目標：** 完成正式發布所需的效能、品質、相容性與自動建置。

**交付物：**

- FPS、GUI Scale、解析度、長寬比、ultrawide、視窗／全螢幕及資源包測試矩陣。
- 原版、Fabric、Paper、Spigot 伺服器連線 smoke test；伺服器不安裝 YASH。
- Render loop 配置與資源載入檢查、效能基準及回歸門檻。
- JDK 25 CI、發布 JAR、變更紀錄、安裝說明與人工 smoke-test 清單。

**驗收條件：**

- CI 從乾淨 checkout 完成 build 與所有測試。
- 低 FPS 與高 FPS 下無可感知的 duration 漂移，render path 無非必要每幀配置。
- 發布 JAR 僅包含 client-side 功能且不連線外部服務。
- 完成 `spec.md` 所列顯示、資源包及生命週期情境。

---

## [ ] T8 — 1.0 後多版本、多 loader 擴充

**依賴：** T7；不屬於 YASH 1.0 範圍

**目標：** 驗證既有分層能低風險加入 NeoForge 與後續 Minecraft 版本。

**交付物：**

- `versions/26_2/neoforge` adapter，重用 `shared` 與 26.2 Minecraft 層。
- 後續版本使用 `versions/<version>/minecraft` 與各自 loader adapter。
- CI 擴充為 Minecraft 版本 × loader 矩陣，各組合獨立輸出與測試。
- 版本差異紀錄與 porting checklist。

**驗收條件：**

- 新 loader 不需在 `shared` 加入 loader 判斷或 loader API。
- 新 Minecraft 版本不以條件碼污染舊版本實作。
- 每個產物只宣告並包含單一 Minecraft 版本與單一 loader。
- 共同動畫、設定與 i18n 測試在所有矩陣項目重用並通過。
