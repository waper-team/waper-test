package com.waper;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RegisterE2ETest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static ExtentReports reporte;
    private ExtentTest testLog;
    private static final String REGISTER_URL = "http://localhost:5173/register";

    @BeforeAll
    public static void configurarReporteYEntorno() {
        ExtentSparkReporter spark = new ExtentSparkReporter("reportes/ResultadoRegistro.html");
        reporte = new ExtentReports();
        reporte.attachReporter(spark);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(8));

        driver.get(REGISTER_URL);
    }

    @Test
    @Order(1)
    public void validarElementosVisibles() {
        testLog = reporte.createTest("1. Verificar UI", "Comprueba que los campos existan en el DOM");
        testLog.info("Buscando elementos por data-testid.");

        String[] elementosEsperados = {
                "register-form", "register-username-input", "register-email-input",
                "register-password-input", "register-password-toggle",
                "register-confirm-password-input", "register-submit-button"
        };

        for (String testId : elementosEsperados) {
            WebElement elemento = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("[data-testid='" + testId + "']")
            ));
            assertTrue(elemento.isDisplayed(), "El elemento esperado no se mostró: " + testId);
        }
        testLog.pass("Todos los elementos renderizaron correctamente.");
    }

    @Test
    @Order(2)
    public void validarVisibilidadContrasena() {
        testLog = reporte.createTest("2. Toggle de Contraseña", "Prueba botón mostrar/ocultar");

        ingresarTexto("register-username-input", "juan_perez");
        ingresarTexto("register-email-input", "juan@ejemplo.com");
        ingresarTexto("register-password-input", "MiClave123!");

        WebElement inputPassword = driver.findElement(By.cssSelector("[data-testid='register-password-input']"));

        clickBtn("register-password-toggle");
        assertEquals("text", inputPassword.getAttribute("type"), "El input no cambió a texto plano.");
        testLog.info("Contraseña visible en texto plano.");

        clickBtn("register-password-toggle");
        assertEquals("password", inputPassword.getAttribute("type"), "El input no volvió a ocultarse.");
        testLog.pass("Toggle de contraseña verificado exitosamente.");
    }

    @Test
    @Order(3)
    public void validarContrasenasNoCoinciden() {
        testLog = reporte.createTest("3. Error de Validación", "Prueba contraseñas no coincidentes");

        ingresarTexto("register-confirm-password-input", "ClaveDiferente999");
        clickBtn("register-submit-button");

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='register-error-message']")
        ));

        String textoObtenido = errorMessage.getText();
        assertEquals("Las contraseñas no coinciden", textoObtenido, "El mensaje de error renderizado es incorrecto.");

        testLog.pass("Alerta de error capturada correctamente.");
    }

    @Test
    @Order(4)
    public void validarEnvioFormularioExitoso() {
        testLog = reporte.createTest("4. Envío Válido", "Prueba registro con contraseñas iguales");

        WebElement confirmInput = driver.findElement(By.cssSelector("[data-testid='register-confirm-password-input']"));
        confirmInput.clear();

        ingresarTexto("register-confirm-password-input", "MiClave123!");
        clickBtn("register-submit-button");

        pausa(1000);
        testLog.pass("Formulario enviado con éxito.");
    }

    @Test
    @Order(5)
    public void validarRedireccionAlLogin() {
        testLog = reporte.createTest("5. Redirección Login", "Verifica enlace a /login");

        clickBtn("login-redirect-button");
        wait.until(ExpectedConditions.urlContains("/login"));

        String urlFinal = driver.getCurrentUrl();
        assertEquals("http://localhost:5173/login", urlFinal, "La redirección no fue a la página de login.");

        testLog.pass("Redirección completada.");
    }

    @AfterAll
    public static void finalizarSujeto() {
        if (driver != null) {
            pausa(1500);
            driver.quit();
        }
        reporte.flush();
    }

    private void ingresarTexto(String testId, String texto) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='" + testId + "']")
        ));
        input.clear();
        input.sendKeys(texto);
        pausa(300);
    }

    private void clickBtn(String testId) {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='" + testId + "']")
        ));
        btn.click();
        pausa(500);
    }

    private static void pausa(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}