# dhci2_auto_check

以 **Java + Playwright + JUnit 5** 建立的網站自動化測試專案（依據 [Playwright for Java 官方文件](https://playwright.dev/java/docs/intro)）。

## 測試目標

| 項目 | 內容 |
| --- | --- |
| 登入頁網址 | https://www1.dhsisp.gov.hk/SchoolPortalWeb/login.htm |
| 帳號 | `ehr-test` |
| 密碼 | `Sira!!2117` |

## 專案結構

```
dhci2_auto_check/
├── pom.xml                                     # Maven 設定（Playwright、JUnit 5）
└── src/test/java/com/dhci2/
    ├── pages/
    │   └── LoginPage.java                      # 登入頁 Page Object
    └── tests/
        ├── TestBase.java                       # Playwright 生命週期管理（Base 類別）
        └── LoginTest.java                      # 登入相關測試案例
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

| 測試 | 說明 |
| --- | --- |
| `loginPageLoadsSuccessfully` | 登入頁可正常載入，表單元素（帳號/密碼/Submit）皆顯示 |
| `loginWithValidCredentials` | 使用正確帳密登入，應導向 `SchoolPortalTrust/post_login_Action.action`（Login Success 頁面） |
| `loginWithInvalidCredentials` | 使用錯誤密碼登入，應停留在登入頁或顯示錯誤訊息 |
| `clearButtonEmptiesFields` | 點擊 Clear 按鈕應清空帳號與密碼欄位 |

## 注意事項

- 登入頁為舊式政府網站，測試已設定 `ignoreHTTPSErrors` 以避免 HTTPS 憑證問題。
- 登入成功後會導向 `SchoolPortalTrust/post_login_Action.action`（標題為 `Department of Health - Login Success`），測試以離開 `SchoolPortalWeb/login` 頁面作為成功判斷。
- 若同一帳號已在其他裝置登入，網站會彈出「There is another active session for the same User Name」對話框，測試會自動點擊「I understood and wanted to proceed with logging in here」繼續登入。
- 帳密目前寫在 `LoginTest.java` 中；若要上 CI，建議改用環境變數（`TEST_USERNAME` / `TEST_PASSWORD`）。
