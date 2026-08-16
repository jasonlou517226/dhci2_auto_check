# dhci2_auto_check

以 **Java + Playwright + JUnit 5** 建立的網站自動化測試專案（依據 [Playwright for Java 官方文件](https://playwright.dev/java/docs/intro)）。

## 測試目標

### 1. School Portal（傳統 JSP 網站）

| 項目 | 內容 |
| --- | --- |
| 登入頁網址 | https://www1.dhsisp.gov.hk/SchoolPortalWeb/login.htm |
| 帳號 | `ehr-test` |
| 密碼 | `Sira!!2117` |

### 2. DH Online Booking SPA（React 單頁應用）

| 項目 | 內容 |
| --- | --- |
| 登入頁網址 | https://www.clinical.dh.gov.hk/OnlineBookingWeb/#/FHS-CH/login |
| 測試重點 | SPA 可載入渲染、後端 API（`siteParams/map`、`generateCaptcha/image`）健康檢查、錯誤 captcha 被正常拒絕 |

> Online Booking 登入需要圖形驗證碼（CAPTCHA），答案只存在於產生的圖片內，無法全自動登入，因此測試以「服務可用性健康檢查」為主。

## 專案結構

```
dhci2_auto_check/
├── pom.xml                                     # Maven 設定（Playwright、JUnit 5）
└── src/test/java/com/dhci2/
    ├── pages/
    │   ├── LoginPage.java                      # School Portal 登入頁 Page Object
    │   └── OnlineBookingLoginPage.java         # Online Booking SPA 登入頁 Page Object
    └── tests/
        ├── TestBase.java                       # Playwright 生命週期管理（Base 類別）
        ├── LoginTest.java                      # School Portal 登入測試
        └── OnlineBookingLoginTest.java         # Online Booking SPA 健康檢查測試
```

## 環境需求

- JDK 17 以上（本專案以 Java 24 驗證）
- Maven 3.6+（若無 Maven，IntelliJ IDEA 內建 Maven 亦可）

## 快速開始

### 1. 安裝 Playwright 瀏覽器（首次執行前需做一次）

```bash
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"
```

> 若使用 IntelliJ 內建 Maven，可執行：
> ```bash
> "/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" \
>   exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"
> ```

### 2. 執行全部測試

```bash
mvn test
```

### 3. 常用選項

| 選項 | 說明 |
| --- | --- |
| `-Dheaded=true` | 以有頭模式（看得到瀏覽器視窗）執行 |
| `-Dslowmo=true` | 每個操作放慢 500ms，方便肉眼觀察 |
| `-Dtest=LoginTest#loginWithValidCredentials` | 只執行單一測試方法 |

例如以有頭 + 慢速模式執行登入成功測試：

```bash
mvn test -Dheaded=true -Dslowmo=true -Dtest=LoginTest#loginWithValidCredentials
```

## 測試案例說明

### LoginTest（School Portal）

| 測試 | 說明 |
| --- | --- |
| `loginPageLoadsSuccessfully` | 登入頁可正常載入，表單元素（帳號/密碼/Submit）皆顯示 |
| `loginWithValidCredentials` | 使用正確帳密登入，應導向 `SchoolPortalTrust/post_login_Action.action`（Login Success 頁面） |
| `loginWithInvalidCredentials` | 使用錯誤密碼登入，應停留在登入頁或顯示錯誤訊息 |
| `clearButtonEmptiesFields` | 點擊 Clear 按鈕應清空帳號與密碼欄位 |

### OnlineBookingLoginTest（Online Booking SPA）

| 測試 | 說明 |
| --- | --- |
| `spaLoginPageLoadsSuccessfully` | SPA 登入路由可載入並渲染表單（帳號/密碼/captcha/登入按鈕） |
| `siteParamsApiIsHealthy` | 後端 `siteParams/map` API 回傳 HTTP 200 與站點參數清單 |
| `captchaApiIsHealthy` | 後端 `generateCaptcha/image` API 回傳 HTTP 200 與 base64 圖片 |
| `wrongCaptchaIsRejectedGracefully` | 輸入錯誤 captcha 應顯示「Verification code is incorrect」且停留在登入頁 |
| `loginFormReflectsInput` | 表單欄位可正常輸入並反映輸入值 |

## 注意事項

- 登入頁為舊式政府網站，測試已設定 `ignoreHTTPSErrors` 以避免 HTTPS 憑證問題。
- 登入成功後會導向 `SchoolPortalTrust/post_login_Action.action`（標題為 `Department of Health - Login Success`），測試以離開 `SchoolPortalWeb/login` 頁面作為成功判斷。
- 若同一帳號已在其他裝置登入，網站會彈出「There is another active session for the same User Name」對話框，測試會自動點擊「I understood and wanted to proceed with logging in here」繼續登入。
- 帳密目前寫在 `LoginTest.java` 中；若要上 CI，建議改用環境變數（`TEST_USERNAME` / `TEST_PASSWORD`）。
- Online Booking SPA 為 React 應用（hash routing），測試需等待 React 渲染完成後才進行元素斷言；後端 API 檢查透過 `page.request()` 以瀏覽器 context 呼叫，藉此沿用網站的連線與憑證設定。
