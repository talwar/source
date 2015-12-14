package swip.ch13framework;

import com.google.common.base.Function;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import swip.framework.Config;
import swip.framework.WebDriverRunner;

import javax.inject.Inject;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.openqa.selenium.By.linkText;

@RunWith(WebDriverRunner.class)
@Config(exclude = "browserName=htmlunit")
public class ErrorProneLocatingLogicIT {

    private final StopWatch stopWatch = new StopWatch();

    @Before
    public void startStopWatch() {
        stopWatch.start();
    }

    @After
    public void print() {
        System.out.println("Time taken " + stopWatch);
    }

    @Inject
    private WebDriver driver;

    @Test
    //  @Ignore("When the location is choose, the menu fades in over a few seconds. This test cannot deal with that.")
    public void errorProneLocatingLogic() {
        driver.get("http://localhost:8080/location-chooser.html");
        driver.findElement(linkText("change location")).click();
        WebElement tabMenu = driver.findElement(By.id("location"));
        tabMenu.findElement(linkText("CANADA")).click();
        tabMenu.findElement(linkText("Ontario")).click();
        assertEquals(0, tabMenu.findElements(linkText("Ontario")).size());
        assertEquals("Ontario", driver
            .findElement(By.cssSelector(".tools-location strong")) // <1>
            .getText());
    }

    @Test
    public void usingImplicitWait() {
        driver.manage().timeouts().implicitlyWait(5, SECONDS); // <1>
        driver.get("http://localhost:8080/location-chooser.html");
        driver.findElement(linkText("change location")).click();
        WebElement tabMenu = driver.findElement(By.id("location"));
        tabMenu.findElement(linkText("CANADA")).click();
        tabMenu.findElement(linkText("Ontario")).click();
        assertEquals(0, tabMenu.findElements(linkText("Ontario")).size());
        assertEquals("Ontario", driver
            .findElement(By.cssSelector(".tools-location strong"))
            .getText());
    }

    @Test(expected = NoSuchElementException.class)
    public void usingImplicitWaitButNotFound() {
        driver.manage().timeouts().implicitlyWait(5, SECONDS); // <1>
        driver.get("http://localhost:8080/location-chooser.html");
        driver.findElement(linkText("change location")).click();
        WebElement tabMenu = driver.findElement(By.id("location"));
        tabMenu.findElement(linkText("CANADA")).click();
        tabMenu.findElement(linkText("Ontario")).click();
        assertEquals(0, tabMenu.findElements(linkText("Ontario")).size());
        assertEquals("Ontario", driver
            .findElement(By.cssSelector(".tools-locati"))
            .getText());
    }


    @Test
    public void usingExplicitWait() {
        driver.get("http://localhost:8080/location-chooser.html");
        driver.findElement(linkText("change location")).click();

        WebDriverWait webDriverWait = new WebDriverWait(driver, 5);  //<1>

        WebElement location = webDriverWait.until(
            new Function<WebDriver, WebElement>() {
                @Override
                public WebElement apply(WebDriver driver) {
                    return driver.findElement(By.id("location"));
                }
            }
        );

        FluentWait<WebElement> webElementWait              //<2>
            = new FluentWait<WebElement>(location)
            .withTimeout(30, SECONDS)
            .ignoring(NoSuchElementException.class);
        WebElement canada = webElementWait.until(
            new Function<WebElement, WebElement>() {
                @Override
                public WebElement apply(WebElement element) {
                    return location.findElement(linkText("CANADA"));
                }
            }
        );
        canada.click();
        WebElement allCanada = webElementWait.until(
            new Function<WebElement, WebElement>() {
                @Override
                public WebElement apply(WebElement element) {
                    return location.findElement(linkText("Ontario"));
                }
            }
        );
        allCanada.click();
        assertEquals(0, driver.findElements(linkText("Ontario")).size());
        assertEquals("Ontario", driver
            .findElement(By.cssSelector(".tools-location strong"))
            .getText());
    }

    @Test
    public void usingExplicitWaitLambda() {
        driver.get("http://localhost:8080/location-chooser.html");
        driver.findElement(linkText("change location")).click();
        WebDriverWait webDriverWait = new WebDriverWait(driver, 5); // <1>

        WebElement tabMenu = webDriverWait
            .until((WebDriver d) -> driver.findElement(By.id("location")));

        FluentWait<WebElement> webElementWait = new FluentWait<>(tabMenu) // <2>
            .withTimeout(5, SECONDS)
            .pollingEvery(100, MILLISECONDS)
            .ignoring(Exception.class);

        webElementWait.until(
            (WebElement element) -> tabMenu.findElement(linkText("CANADA")))
            .click();
        webElementWait
            .until((WebElement element) -> tabMenu.findElement(linkText("Ontario")))
            .click();
        assertEquals(0, tabMenu.findElements(linkText("Ontario")).size());
        assertEquals("Ontario", driver
            .findElement(By.cssSelector(".tools-location strong"))
            .getText());
    }
}
