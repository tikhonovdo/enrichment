package ru.tikhonovdo.enrichment.config

import org.openqa.selenium.chrome.ChromeOptions
import org.springframework.context.annotation.Configuration

@Configuration
class WebDriverConfig {

    companion object {
        init {
            com.codeborne.selenide.Configuration.remote = "http://localhost:4444/wd/hub"
            com.codeborne.selenide.Configuration.holdBrowserOpen = true
            com.codeborne.selenide.Configuration.browserCapabilities = ChromeOptions().apply {
                browserVersion = "117.0"

                addArguments("--disable-blink-features=AutomationControlled")
                addArguments("--window-size=1920,1080")
            }
        }
    }

}