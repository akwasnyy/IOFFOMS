package test;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MenuDisplayTest {
//    Wejście: Użytkownik otwiera aplikację i przechodzi do sekcji menu.
//    Przebieg: Lista produktów jest pobierana z serwera i wyświetlana.
//    Oczekiwany wynik: Lista zawiera nazwę i cenę każdego produktu.

    private WebDriver driver;

    @BeforeAll
    void setup() {
        // Ustaw ścieżkę do chromedrivera
        System.setProperty("webdriver.chrome.driver",  TestConfig.WEBDRIVER_URL);
        driver = new ChromeDriver();
    }

    @Test
    void testMenuListDisplaysProducts() {
        driver.get( TestConfig.INDEX_URL);

        WebElement iframe = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("iframe[src='client.html']")));
        driver.switchTo().frame(iframe);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        List<WebElement> productCards = wait.until(
                ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector("#menu-list .card"), 0)
        );

        Assertions.assertFalse(productCards.isEmpty(), "Lista produktów nie została załadowana.");

        for (WebElement card : productCards) {
            WebElement nameElement = card.findElement(By.cssSelector(".card-title"));
            WebElement priceElement = card.findElement(By.cssSelector(".card-text"));

            String name = nameElement.getText();
            String price = priceElement.getText();

            Assertions.assertFalse(name.isBlank(), "Produkt nie ma nazwy.");
            Assertions.assertTrue(price.matches(".*\\d+.*zł"), "Niepoprawny format ceny: " + price);
        }
    }

    @AfterAll
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
