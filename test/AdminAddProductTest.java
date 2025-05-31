package test;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AdminAddProductTest {
//    Wejście: Administrator wpisuje nazwę i cenę nowego produktu i klika „Zapisz”.
//    Przebieg: System zapisuje nowy produkt w bazie danych.
//    Oczekiwany wynik: Produkt pojawia się na liście w interfejsie klienta.

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeAll
    void setup() {
        System.setProperty("webdriver.chrome.driver", TestConfig.WEBDRIVER_URL);
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @Test
    void testAdminAddsProductAndAppearsInClient() {
        driver.get(TestConfig.INDEX_URL);

        // --- Krok 1: Przełącz na zakładkę admin ---

        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-bs-target='#tab-admin']"))).click();
        driver.switchTo().frame(wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#tab-admin iframe[src='admin.html']"))));

        // --- Krok 3: Wypełnij formularz nowego produktu ---
        String testProductName = "Testowy Produkt";
        String testProductPrice = "9.99";

        WebElement nameInput = driver.findElement(By.id("productName"));
        WebElement priceInput = driver.findElement(By.id("productPrice"));
        WebElement availableSelect = driver.findElement(By.id("productAvailable"));
        WebElement saveButton = driver.findElement(By.cssSelector("button[type='submit']"));

        nameInput.clear();
        nameInput.sendKeys(testProductName);
        priceInput.clear();
        priceInput.sendKeys(testProductPrice);
        availableSelect.sendKeys("Tak");  // wybierz "Tak"

        // Kliknij Zapisz
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveButton);

        // Poczekaj aż tabela zostanie zaktualizowana, np. pojawi się nowy wiersz z nazwą testProductName
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//tbody[@id='product-table-body']/tr[td[contains(text(), '" + testProductName + "')]]")));

        // Wyjdź z iframe admin
        driver.switchTo().defaultContent();

        // --- Krok 4: Przełącz na zakładkę klienta ---
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-bs-target='#tab-client']"))).click();
        driver.switchTo().frame(wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#tab-client iframe[src='client.html']"))));

        // --- Krok 6: Sprawdź, czy na liście produktów pojawił się nowy produkt ---
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        List<WebElement> productNames = wait.until(
                ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector("#menu-list .card .card-body .card-title"), 0)
        );
        boolean found = productNames.stream().anyMatch(el -> el.getText().equals(testProductName));
        Assertions.assertTrue(found, "Nowy produkt powinien być widoczny na liście klienta.");




        // Wyjdź z iframe klienta
        driver.switchTo().defaultContent();
    }

    @AfterAll
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
