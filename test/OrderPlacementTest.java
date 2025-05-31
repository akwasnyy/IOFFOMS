package test;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OrderPlacementTest {
//    Wejście: Użytkownik wybiera kilka produktów i przechodzi do koszyka.
//    Przebieg: Produkty są dodane do koszyka, pokazuje się podsumowanie, użytkownik zatwierdza zamówienie.
//    Oczekiwany wynik: Zamówienie zostaje przesłane i pojawia się komunikat potwierdzający.

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeAll
    void setup() {
        System.setProperty("webdriver.chrome.driver",  TestConfig.WEBDRIVER_URL);
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @Test
    void testAddProductsAndSubmitOrder() {
        driver.get( TestConfig.INDEX_URL);

        // Przełącz na iframe, jeśli jest
        WebElement iframe = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("iframe[src='client.html']")));
        driver.switchTo().frame(iframe);

        // Poczekaj na produkty i dodaj pierwsze 2 do koszyka
        List<WebElement> addButtons = wait.until(
                ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector("#menu-list button.btn-primary"), 1)
        );

        WebElement button1 = addButtons.get(0);
        ((JavascriptExecutor)driver).executeScript("arguments[0].click();", button1);

        WebElement button2 = addButtons.get(1);
        ((JavascriptExecutor)driver).executeScript("arguments[0].click();", button2);


        // Sprawdź czy koszyk pokazuje 2 produkty
        List<WebElement> cartItems = wait.until(
                ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector(".list-group li"), 1)
        );
        Assertions.assertEquals(2, cartItems.size(), "Koszyk powinien zawierać 2 produkty.");

        // Sprawdź czy jest widoczny przycisk zatwierdzający zamówienie (np. id="submit-order")
        WebElement submitButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn.btn-success"))
        );

        // Kliknij zatwierdzenie zamówienia
        ((JavascriptExecutor)driver).executeScript("arguments[0].click();", submitButton);

        // Poczekaj na alert z potwierdzeniem
        wait.until(ExpectedConditions.alertIsPresent());
        Alert alert = driver.switchTo().alert();

        // Sprawdź, czy tekst alertu zawiera informację o złożonym zamówieniu
        String alertText = alert.getText();
        Assertions.assertTrue(alertText.contains("Zamówienie złożone!"), "Alert powinien potwierdzać złożenie zamówienia.");

        alert.accept();

        // Po zatwierdzeniu zamówienia koszyk powinien być pusty
        List<WebElement> emptyCart = driver.findElements(By.cssSelector(".list-group li"));
        Assertions.assertTrue(emptyCart.isEmpty(), "Koszyk powinien być pusty po złożeniu zamówienia.");
    }

    @AfterAll
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
