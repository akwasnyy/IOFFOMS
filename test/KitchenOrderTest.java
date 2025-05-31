package test;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KitchenOrderTest {
//    Wejście: Klient składa nowe zamówienie.
//    Przebieg: Zamówienie pojawia się w panelu kuchni.
//    Oczekiwany wynik: Nowe zamówienie znajduje się na liście, zgodnie z kolejnością zgłoszeń.

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeAll
    void setup() {
        System.setProperty("webdriver.chrome.driver", TestConfig.WEBDRIVER_URL);
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @Test
    void testNewOrderAppearsInKitchenPanel() {
        driver.get(TestConfig.INDEX_URL);

        // --- Krok 1: Przełącz na iframe klienta i złóż zamówienie ---

        WebElement clientIframe = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("iframe[src='client.html']")));
        driver.switchTo().frame(clientIframe);

        // Poczekaj na produkty i dodaj pierwszy produkt do koszyka
        List<WebElement> addButtons = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector("#menu-list button.btn-primary"), 0));
        WebElement firstAddBtn = addButtons.get(0);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstAddBtn);

        // Kliknij przycisk złożenia zamówienia
        WebElement submitOrderBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn.btn-success")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitOrderBtn);

        // Poczekaj i potwierdź alert z potwierdzeniem zamówienia
        wait.until(ExpectedConditions.alertIsPresent());
        Alert alert = driver.switchTo().alert();
        String alertText = alert.getText();
        Assertions.assertTrue(alertText.contains("Zamówienie złożone!"), "Alert powinien potwierdzać złożenie zamówienia.");
        alert.accept();

        // Wyjdź z iframe klienta
        driver.switchTo().defaultContent();

        // --- Krok 2: Przełącz na iframe kuchni ---

        WebElement kitchenTabButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-bs-target='#tab-kitchen']")));
        kitchenTabButton.click();
        WebElement kitchenIframe = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#tab-kitchen iframe[src='kitchen.html']")));
        driver.switchTo().frame(kitchenIframe);


        // Poczekaj aż lista zamówień się załaduje
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("orders-list")));

        // Pobierz listę zamówień na kuchni
        List<WebElement> orders = driver.findElements(By.cssSelector("#orders-list > div"));

        // Sprawdź, że jest co najmniej jedno zamówienie (np. to które właśnie złożono)
        Assertions.assertFalse(orders.isEmpty(), "Lista zamówień powinna zawierać co najmniej jedno zamówienie.");

        // Sprawdź, czy pierwsze zamówienie ma status "Oczekujące" (badge z tekstem "Oczekujące")
        WebElement firstOrder = orders.get(0);
        WebElement statusBadge = firstOrder.findElement(By.cssSelector(".bg-info"));

        Assertions.assertEquals("Oczekujące", statusBadge.getText(), "Nowe zamówienie powinno mieć status 'Oczekujące'.");

        // Wyjdź z iframe kuchni
        driver.switchTo().defaultContent();
    }

    @AfterAll
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
