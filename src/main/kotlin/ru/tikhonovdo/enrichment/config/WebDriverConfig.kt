package ru.tikhonovdo.enrichment.config

import com.codeborne.selenide.Configuration
import com.codeborne.selenide.SelenideConfig
import org.openqa.selenium.chrome.ChromeOptions
import org.springframework.context.annotation.Import

@org.springframework.context.annotation.Configuration
@Import(SelenideConfig::class)
class WebDriverConfig {

    companion object {
        init {
            Configuration.browserCapabilities = ChromeOptions().apply {
                browserVersion = "117.0"
                addArguments("--disable-blink-features=AutomationControlled")
            }
        }
    }

}