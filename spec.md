
# YASH 完整第一版實作計畫

## 1. 專案摘要

建立一個名為 **YASH — Yet Another Smooth Hotbar** 的 Minecraft Java Edition Client-side Mod。

YASH 的核心不是只讓快捷欄選取框平滑移動，而是提供：

* 高品質快捷欄選取框動畫
* 多種動畫預設
* 多種 Minecraft 風格選取框主題
* 遊戲內即時預覽與設定
* 選取框外觀自訂
* 資源包相容模式
* 快速切換時仍然連續、穩定的動畫

玩家只需安裝 YASH Mod，**不需要伺服器安裝、不需要額外 Resource Pack**。

---

# 2. 專案基本資料

## 正式名稱

```text
YASH
```

完整名稱：

```text
Yet Another Smooth Hotbar
```

建議副標：

```text
Smooth animations. Expressive themes. Your hotbar, your style.
```

簡介：

> A lightweight and customizable animated hotbar selector mod for Minecraft.

## 第一版支援環境

```text
Minecraft 26.2
Fabric
Client-side only
```

第一版暫時不支援：

* NeoForge
* Forge
* Quilt
* 舊版 Minecraft
* 多版本共用 JAR
* 伺服器端功能

玩家可以安裝 YASH 後進入：

* 原版伺服器
* Fabric 伺服器
* Paper 伺服器
* Spigot 伺服器

伺服器不需要安裝 YASH。

---

# 3. 核心產品定位

YASH 應定位為：

> **快捷欄選取框的動畫與主題系統。**

它不是完整 HUD 重製 Mod，也不是整套 GUI 動畫 Mod。

第一版只專注：

* 快捷欄選取框
* 選取框移動動畫
* 選取框外觀
* 選取框動態效果
* 選取框主題

不修改：

* 實際選中的物品
* 玩家背包資料
* 伺服器封包
* 遊戲操作速度
* 物品切換機制
* 攻擊或使用物品行為

YASH 只改變客戶端看到的視覺效果。

---

# 4. 核心動畫功能

## 4.1 平滑移動

原版 Minecraft 的快捷欄選取框會直接從一格跳到另一格。

YASH 應讓選取框從目前顯示位置平滑移動到最新選中的格子。

例如：

```text
第 1 格 → 第 5 格
```

原版：

```text
1 → 瞬間出現在 5
```

YASH：

```text
1 → 2 → 3 → 4 → 5
```

實際動畫應為連續座標移動，而不是逐格跳動。

---

## 4.2 連續重新導向

玩家快速切換：

```text
1 → 5 → 8 → 3
```

選取框不得：

* 瞬移回上一格
* 停止後重新開始
* 抖動
* 跳回整數位置
* 排隊播放過期動畫

當目標改變時，動畫必須從**目前畫面上的實際位置**直接轉向最新目標。

舊目標立即失效。

---

## 4.3 幀率獨立

動畫速度不得依賴 FPS。

在以下情況，動畫時間應大致一致：

* 30 FPS
* 60 FPS
* 144 FPS
* 240 FPS

不能因 FPS 高而動畫變快，也不能因 FPS 低而動畫變得異常緩慢。

---

## 4.4 所有選取方式都應觸發動畫

不要只偵測滑鼠滾輪。

以下方式改變快捷欄格子時，都應正常播放動畫：

* 滑鼠滾輪
* 數字鍵 `1–9`
* Pick Block
* 控制器輸入
* 自訂按鍵綁定
* 其他 Mod 改變 selected slot
* 伺服器或遊戲狀態造成的格子改變

YASH 應根據「目前選中的 Slot 發生改變」啟動動畫，而不是依賴特定輸入按鍵。

---

# 5. 動畫預設

動畫風格和選取框主題必須分開。

例如玩家可以使用：

```text
動畫：Snappy
主題：Creeper
```

或：

```text
動畫：Spring
主題：Wolf
```

## 5.1 Bedrock Inspired

預設動畫。

風格：

* 快速開始
* 平滑減速
* 不明顯回彈
* 反應快速
* 接近 Minecraft Bedrock Edition 的切換手感

建議初始時間：

```text
約 100–130 ms
```

這只是 Bedrock-inspired，不需要宣稱完全 1:1 複製。

---

## 5.2 Smooth

風格：

* 移動柔和
* 動畫稍慢
* 適合生存與建築
* 沒有回彈

建議初始時間：

```text
約 160–190 ms
```

---

## 5.3 Snappy

風格：

* 非常快速
* 仍能明顯看見滑動
* 不會感覺選取框追不上操作
* 適合 PvP 或快速操作

建議初始時間：

```text
約 60–90 ms
```

---

## 5.4 Spring

風格：

* 選取框抵達目標後輕微超越
* 再回彈到正確位置
* 動感較明顯
* 不應彈跳太久

Spring 必須可以調整：

* 彈性強度
* 阻尼
* Overshoot 程度

---

## 5.5 Minimal

風格：

* 非常短的平滑過渡
* 接近原版反應速度
* 適合只想稍微改善原版的人
* 沒有 Glow、Stretch 或回彈

建議初始時間：

```text
約 40–60 ms
```

---

## 5.6 Custom

允許玩家自行調整所有進階參數。

---

## 5.7 Disabled

完全關閉 YASH 動畫。

關閉後應恢復原版選取框行為，不需要退出遊戲。

---

# 6. 快捷欄循環行為

玩家由第 9 格切換到第 1 格，或第 1 格切到第 9 格時，提供兩種模式。

## Across

選取框跨過整條快捷欄。

```text
9 → 8 → 7 → 6 → … → 1
```

優點：

* 視覺直觀
* 符合快捷欄是直線的畫面結構

建議作為預設。

## Wrap

選取框從右側離開，再從左側進入。

```text
9 → 右側消失 → 左側出現 → 1
```

反方向同理。

Wrap 動畫必須連續，不能突然在另一邊閃現。

---

# 7. 選取框主題系統

## 7.1 核心原則

主題系統只修改選取框，不必完整替換整條快捷欄。

每個主題可以包含：

* 主選取框貼圖
* 額外裝飾層
* 預設顏色
* Glow 顏色
* 建議 Pulse 強度
* 建議 Stretch 強度
* 自訂尺寸
* X／Y 偏移
* 主題預覽圖

主題和動畫預設必須能自由搭配。

---

## 7.2 第一版內建主題

### Vanilla / Resource Pack

使用玩家目前啟用資源包所提供的原版選取框。

YASH 只負責平滑移動。

這個模式必須：

* 尊重原版材質
* 尊重玩家資源包
* 不強制替換外觀
* 不加入額外顏色，除非玩家手動啟用 Tint

這應是最安全的相容模式。

---

### Clean

簡潔乾淨的白色／淺灰選取框。

特徵：

* 邊框較細
* 不阻擋物品圖示
* 適合極簡風格
* 可搭配任何動畫預設

---

### Creeper

受 Creeper 啟發的綠色像素主題。

特徵：

* 綠色像素邊框
* 四角或邊緣帶有 Creeper 臉部元素
* 深綠色陰影
* 輕微綠色 Glow
* 不要把完整 Creeper 臉蓋在物品上

視覺應一眼能聯想到 Creeper，但仍保持選取框清楚。

---

### Enderman

受 Enderman 啟發的黑紫色主題。

特徵：

* 深色選取框
* 紫色眼睛元素
* 紫色 Glow
* 抵達格子時可有輕微紫色 Pulse
* 整體神秘但不能過暗

第一版不必加入真正粒子系統，可用貼圖和 Glow 模擬。

---

### Wolf

受 Minecraft Wolf 啟發的可愛灰白主題。

特徵：

* 灰白毛色
* 選取框上方可有兩個小耳朵
* 紅色項圈作為少量點綴
* 抵達格子時可以輕微彈一下
* 不遮擋物品數量與耐久度

---

### Bee

受 Minecraft Bee 啟發的黃黑色主題。

特徵：

* 黃黑條紋
* 左右可加入小翅膀裝飾
* 適合搭配 Spring 動畫
* Glow 使用暖黃色
* 外觀可愛、明亮

---

# 8. 外觀效果

效果和主題分開，可以單獨開關。

## 8.1 Glow

在選取框外圍加入柔和發光。

可設定：

* 開啟／關閉
* Glow 強度
* Glow 顏色
* Glow 範圍

部分主題可以提供預設 Glow 顏色，但玩家仍可覆蓋。

---

## 8.2 Stretch

選取框快速移動時，沿水平方向輕微拉伸。

要求：

* 移動越快，Stretch 越明顯
* 停止後恢復正常比例
* 不得過度拉長
* 不得讓選取框模糊或遮擋大量快捷欄

可設定：

* 開啟／關閉
* Stretch 強度

---

## 8.3 Arrival Pulse

選取框抵達目標格時：

1. 輕微放大
2. 快速恢復正常尺寸

可設定：

* 開啟／關閉
* Pulse 強度

快速連續切換時，不應每次都疊加成巨大框。

---

## 8.4 Tint

允許替選取框套用顏色。

可設定：

* 開啟／關閉
* RGB／Hex 顏色
* 顏色強度

對彩色主題使用 Tint 時，要避免完全破壞原本的美術配色。

---

## 8.5 Opacity

允許調整選取框透明度。

範圍：

```text
20%–100%
```

不能調到完全不可見，避免玩家誤以為 Mod 壞掉。

---

## 8.6 Scale

允許調整選取框大小。

建議範圍：

```text
0.75×–1.50×
```

主題本身可以有預設尺寸。

---

# 9. 第一版暫時不加入的效果

為避免第一版膨脹，以下功能延後：

* 長距離拖影
* 真正 HUD 粒子
* 選取框音效
* Shader 特效
* 動態 RGB 彩虹效果
* 3D 選取框
* 物品本身縮放動畫
* 物品名稱 Tooltip 動畫
* 經驗條動畫
* 整條快捷欄移動
* 攻擊冷卻條動畫

YASH 1.0 必須先把 selector 本身做好。

---

# 10. 設定介面

## 10.1 開啟方式

提供兩種方式：

* 安裝 Mod Menu 時，從 Mod Menu 開啟 YASH 設定
* 提供一個可自訂按鍵，用來直接開啟 YASH 設定

Mod Menu 應是選裝，不應成為強制依賴。

---

## 10.2 設定頁結構

設定介面分成三個主要頁面。

### Presets

包含：

* 啟用 YASH
* 動畫預設
* 動畫速度
* 循環模式
* 恢復預設設定

### Themes

包含：

* 主題選擇
* 主題預覽
* Tint
* Opacity
* Scale
* Glow
* Stretch
* Pulse

### Advanced

包含：

* 動畫 Duration
* Easing
* Spring Strength
* Damping
* Overshoot
* Glow 強度
* Stretch 強度
* Pulse 強度
* 降低動畫模式

---

# 11. 即時預覽

設定畫面必須提供即時預覽。

預覽區顯示模擬快捷欄：

```text
[1][2][3][4][5][6][7][8][9]
```

玩家可以：

* 點擊任意格子測試
* 自動循環播放
* 測試第 1 格與第 9 格切換
* 測試快速連續切換
* 即時查看主題
* 即時查看 Glow、Stretch、Pulse

修改設定後，預覽應立即更新，不需要按下儲存才能看到。

設定離開時自動儲存。

---

# 12. 自訂動畫設定

## 基本模式

普通玩家只看到：

* Preset
* Speed
* Theme
* Effects

## Advanced Mode

進階玩家可以修改：

* Duration
* Easing Curve
* Spring Strength
* Damping
* Overshoot
* Stretch Strength
* Glow Strength
* Pulse Strength

修改任何預設參數後，動畫模式自動變成：

```text
Custom
```

玩家重新選擇官方 Preset 時，恢復該 Preset 的完整預設值。

---

# 13. Reduced Motion

提供「降低動畫」選項。

啟用後：

* 使用非常短的動畫
* 關閉 Spring
* 關閉 Stretch
* 關閉 Pulse
* 關閉明顯 Glow 動畫
* 保留主題外觀

玩家仍可以使用 Creeper、Wolf 等主題，但視覺移動更少。

---

# 14. 資源包相容

## Vanilla / Resource Pack 模式

此模式應使用玩家目前資源包中的原版快捷欄選取框材質。

資源包重新載入後：

* YASH 自動重新載入選取框
* 不需要重新啟動遊戲
* 不應顯示遺失貼圖
* 動畫狀態安全重設

## YASH Built-in Theme 模式

選擇 YASH 內建主題時：

* 使用 Mod JAR 內置素材
* 不依賴外部 Resource Pack
* 玩家不需額外下載任何東西

---

# 15. 顯示與介面相容

YASH 必須正確支援：

* 所有原版 GUI Scale
* 視窗模式
* 全螢幕模式
* 不同長寬比
* Ultrawide 螢幕
* Minecraft HUD 隱藏
* F1 模式
* 玩家死亡與重生
* 切換世界
* 進入及離開伺服器
* 資源包重新載入
* 修改視窗大小
* 修改 GUI Scale

發生上述狀態變更時，選取框不得：

* 卡在畫面中央
* 留下殘影
* 出現兩個框
* 瞬間飛越整個畫面
* 使用錯誤座標

必要時直接將動畫位置重設到目前選中格。

---

# 16. 原版選取框處理

啟用 YASH 時：

* 原版選取框不可同時顯示
* 畫面只能存在一個選取框
* YASH 在動畫座標繪製自己的框

停用 YASH 時：

* 恢復原版選取框
* 不需要重新啟動遊戲
* 不留下 YASH 的額外效果

---

# 17. 主題美術規則

所有內建主題素材必須：

* 使用原創像素美術
* 不直接複製其他 Resource Pack
* 不直接搬用其他 Mod 素材
* 保持 Minecraft 像素風
* 在不同 GUI Scale 下保持清楚
* 不遮住物品數量
* 不遮住耐久度條
* 不阻礙辨認物品
* 保持選中格位置清楚

可以受到 Minecraft 生物啟發，但所有素材由 YASH 專案自行製作。

---

# 18. 語言

第一版提供：

```text
English
繁體中文（zh-TW）
```

所有設定名稱、說明文字與按鈕都需使用語言檔。

遊戲會依 Minecraft 語言自動切換。

沒有對應翻譯時回退英文。

---

# 19. 設定保存

需要保存：

* 是否啟用
* 動畫 Preset
* Theme
* Duration
* Easing
* Wrap 模式
* Tint
* Opacity
* Scale
* Glow
* Stretch
* Pulse
* Reduced Motion
* 進階動畫參數

重新啟動遊戲後，設定仍然保留。

設定損壞時：

* 不讓遊戲崩潰
* 回退安全預設值
* 記錄警告
* 保留或備份損壞檔案

---

# 20. 預設設定

首次安裝建議使用：

```text
Enabled: true
Animation Preset: Bedrock Inspired
Theme: Vanilla / Resource Pack
Wrap Mode: Across
Speed: Normal
Glow: false
Stretch: false
Arrival Pulse: false
Tint: disabled
Opacity: 100%
Scale: 1.0×
Reduced Motion: false
```

這樣玩家首次安裝時，只會得到高品質平滑動畫，不會突然看到大量花俏效果。

主題與效果由玩家自行開啟。

---

# 21. 第一版明確排除項目

YASH 1.0 不製作：

* 外部 Theme Pack API
* 自訂 PNG 匯入
* 遊戲內主題編輯器
* 主題商店
* 線上下載主題
* Preset 分享碼
* Fabric 以外 Loader
* 26.2 以外版本
* 完整快捷欄背景替換
* 背包 GUI 動畫
* Chat 動畫
* Tooltip 動畫
* 經驗條動畫
* 物品拿取動畫
* 伺服器同步設定
* 帳號同步
* Telemetry
* 廣告或付費主題

第一版不要製作任何連線到外部伺服器的功能。
---

# 25. 最終產品描述

> **YASH — Yet Another Smooth Hotbar** is a lightweight client-side Fabric mod that transforms Minecraft’s instant hotbar selector into a smooth, responsive and fully themed experience. Choose polished animation presets, customize visual effects, or switch between adorable built-in themes such as Creeper, Enderman, Wolf and Bee—all without requiring a resource pack or server installation.
