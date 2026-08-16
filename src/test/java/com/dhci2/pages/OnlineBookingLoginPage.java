package com.dhci2.pages;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;

/**
 * Page Object for the DH Online Booking SPA (React, hash routing).
 *
 * Target: https://www.clinical.dh.gov.hk/OnlineBookingWeb/#/FHS-CH/login
 *
 * Notes:
 * - The SPA is a React app served from a single index.html; real "pages" are
 *   hash routes (e.g. #/FHS-CH/login). Navigation therefore means: goto the
 *   base URL with the hash, then wait for the React app to render.
 * - The login form posts to /online-booking-user/loginWithSam with a captcha
 *   (captchaKey + captchaAns). The captcha answer is only shown inside the
 *   generated image, so full automated login is not possible without OCR.
 *   Health checks instead verify page rendering + backend API availability.
 */
public class OnlineBookingLoginPage {

    public static final String BASE_URL = "https://www.clinical.dh.gov.hk/OnlineBookingWeb/";
    public static final String URL = BASE_URL + "#/FHS-CH/login";

    /** Backend APIs used by the SPA (relative to /OnlineBookingWeb). */
    public static final String SITE_PARAMS_API = "/online-booking-user/siteParams/map";
    public static final String CAPTCHA_API = "/online-booking-user/generateCaptcha/image";

    private final Page page;

    // Locators (MUI inputs rendered by the React SPA)
    private final Locator usernameInput;
    private final Locator passwordInput;
    private final Locator captchaInput;
    private final Locator loginButton;

    public OnlineBookingLoginPage(Page page) {
        this.page = page;
        // data-testid attributes are stable hooks emitted by the SPA
        this.usernameInput = page.locator("[data-testid=\"login_name\"] input, #login_name input").first();
        this.passwordInput = page.locator("[data-testid=\"login_password\"] input, #login_password input").first();
        this.captchaInput = page.locator("[data-testid=\"captchaInput\"] input, #captchaInput input").first();
        this.loginButton = page.locator("#login_button, [data-testid=\"login_loginBtn\"]").first();
    }

    /** Opens the SPA login route and waits for the React app to render. */
    public void open() {
        page.navigate(URL, new Page.NavigateOptions()
                .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        // Wait for the SPA to finish bootstrapping and render the login form
        try {
            page.waitForLoadState(LoadState.NETWORKIDLE,
                    new Page.WaitForLoadStateOptions().setTimeout(15_000));
        } catch (PlaywrightException ignored) {
            // Some analytics/long-poll requests may keep the network busy;
            // the element waits below are the authoritative check.
        }
        usernameInput.waitFor(new Locator.WaitForOptions().setTimeout(20_000));
    }

    public OnlineBookingLoginPage fillUsername(String username) {
        usernameInput.fill(username);
        return this;
    }

    public OnlineBookingLoginPage fillPassword(String password) {
        passwordInput.fill(password);
        return this;
    }

    public OnlineBookingLoginPage fillCaptcha(String answer) {
        captchaInput.fill(answer);
        return this;
    }

    /** Clicks the Login button (will fail captcha unless the answer is correct). */
    public void submit() {
        loginButton.click();
    }

    // ---------- Assertion helpers ----------

    public String currentPageTitle() {
        return page.title();
    }

    public String currentUrl() {
        return page.url();
    }

    public boolean isLoginFormVisible() {
        return usernameInput.isVisible() && passwordInput.isVisible()
                && loginButton.isVisible();
    }

    public boolean isCaptchaVisible() {
        return captchaInput.isVisible();
    }

    /**
     * Calls the site-params backend API through the browser context and returns
     * the raw JSON. This is what feeds the SPA's site list (FHS-CH etc.), so it
     * is a good backend health signal.
     */
    public APIResponse fetchSiteParams() {
        return page.request().get(BASE_URL + SITE_PARAMS_API.substring(1));
    }

    /**
     * Calls the captcha generation API through the browser context. A healthy
     * response is HTTP 200 with a non-trivial JSON body (contains the base64
     * image and/or captcha key).
     */
    public APIResponse fetchCaptcha() {
        return page.request().get(BASE_URL + CAPTCHA_API.substring(1));
    }

    /** Convenience accessor for request/response debugging in tests. */
    public Page page() {
        return page;
    }
}