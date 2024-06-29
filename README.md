Github：[PhilippLackner-DraggableMusicKnob](https://github.com/RainBowT0506/PhilippLackner-DraggableMusicKnob)
![image](https://hackmd.io/_uploads/r1dGToa8A.png)

Demo

![image](https://hackmd.io/_uploads/rk7mpoTIA.png)

RainbowRGB

![image](https://hackmd.io/_uploads/SJKXTi6LA.png)

# 音樂旋鈕的可定制性
- 音樂旋鈕的條數可以輕鬆調整，例如從 20 條變為 30 條，仍能適應相同寬度。
- 使用者可以調整條數以適應應用需求。

# 理解旋轉的數學原理
- 旋轉需要計算角度，利用圓心和游標位置的坐標。
- 當旋轉超出限制角度時，旋轉會停止。

# 開始編寫 Music Knob Composable
## 創建音樂旋鈕 Composable
- 建立一個名為 `MusicKnob` 的 Composable，接受參數：`modifier`、`limitingAngle`、`onValueChange`。
- `rotation` 狀態保存當前旋轉角度。
- `touchX` 和 `touchY` 保存觸控位置。

## 配置旋鈕圖像
- 使用 `Image` 組件並設置 `painterResource` 來顯示音樂旋鈕圖像。
- 利用 `onGloballyPositioned` 獲取圖像的中心點位置。

## 處理觸控事件
- 使用 `pointerInput` 偵測觸控位置，更新 `touchX` 和 `touchY`。
- 計算旋轉角度，使用 `atan2` 函數來計算角度。
- 將弧度轉換為角度，並校正方向。

## 計算固定角度和百分比
- 校正角度範圍，確保角度在 0 到 360 度之間。
- 計算旋轉百分比並呼叫 `onValueChange`。

## 旋轉圖像
- 使用 `rotate` 函數來應用旋轉角度。
- 確保旋轉應用在其他修改器之後。 




# 開始編寫 Volume Bar Composable
## 設定 Volume Bar 參數
- 創建一個名為 `VolumeBar` 的 Composable 函數
- 設置三個參數：
  - `modifier`，預設為 `Modifier`
  - `activeBars`，初始值為 0，表示當前活躍的柱狀條數量
  - `barCount`，預設值為 10，表示總共的柱狀條數量

## 計算單個柱狀條的寬度
- 使用 Box 來設置內容的對齊方式和 `modifier`
- 計算單個柱狀條的寬度：
  - 使用 `constraints.maxWidth` 獲取 Box 的最大寬度
  - 計算公式為 `constraints.maxWidth / (2f * barCount)`
  - 確保每個柱狀條之間有適當的間距

## 繪製柱狀條
- 使用 Canvas 來繪製柱狀條
- 設置 Canvas 的 `modifier`
- 對每個柱狀條進行繪製：
  - 使用 `drawRoundRect` 方法
  - 根據 `activeBars` 的值設置柱狀條的顏色：
    - 活躍的柱狀條使用 `Color.Green`
    - 非活躍的柱狀條使用 `Color.DarkGray`
  - 設置柱狀條的位置：
    - x 位置為 `i * barWidth * 2f + barWidth / 2f`
    - y 位置為 0
  - 設置柱狀條的尺寸：
    - 寬度為 `barWidth`
    - 高度為 `constraints.maxHeight.toFloat()`
  - 可選擇設置圓角半徑


# 開始編寫 主畫面設置 Composables
## 設定主畫面 Box
- 使用 Box 作為主要容器，設置內容對齊方式為 `Alignment.Center`
- 設置 Box 的 `modifier` 為 `Modifier.fillMaxSize()`，填滿整個螢幕
- 設定背景顏色為 `Color(0xFF101010)`

## 設置 Row 來排列 Composables
- 在 Box 內使用 Row 來排列 Composables
- 設置 Row 的內容對齊方式為水平和垂直居中
- 設置 Row 的 `modifier` 以添加綠色邊框、圓角形狀和內邊距：
  - 邊框寬度為 1.dp，顏色為綠色
  - 圓角半徑為 10.dp
  - 內邊距為 30.dp

## 設置狀態和變數
- 創建音量狀態 `volume`，初始值為 0f
- 創建柱狀條數量變數 `barCount`，設置為 20f

## 添加 Music Knob 和 Volume Bar
- 設置 Music Knob 的 `modifier`，大小為 100.dp x 100.dp
- 音量旋鈕旋轉的百分比值設定為音量狀態 `volume`
- 添加 Spacer 以在兩個 Composables 之間創建空間，寬度為 20.dp
- 設置 Volume Bar 的 `modifier`，寬度填滿，並設置高度為 30.dp
- 設置活躍柱狀條的數量為總柱狀條數量乘以音量百分比並四捨五入為整數
- 設置總柱狀條數量為 `barCount`，即 20

## 更新 Gradle 插件
- 打開 Gradle 腳本文件夾，更新 build.gradle 項目文件
- 將插件版本更新為 1.5.10
- 同步更改並啟動應用程式

## 測試應用程式
- 打開模擬器，啟動應用程式
- 測試音量旋鈕和柱狀條的互動
