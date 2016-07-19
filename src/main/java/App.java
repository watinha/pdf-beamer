import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Point;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.OutputType;


public class App {
    public static void main (String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("No URL passed you dummy...");
            return ;
        }
        String url = args[0];
        WebDriver driver = new FirefoxDriver();
        App app = new App(driver);
        driver.get(url);
        Thread.sleep(3000);
        ((JavascriptExecutor) driver).executeScript(
            "document.querySelector('#viewerContainer').style.height = document.querySelector('#viewer').offsetHeight + 'px';" +
            "document.querySelector('#mainContainer').style.height = document.querySelector('#viewer').offsetHeight + 'px';" +
            "document.querySelector('#outerContainer').style.height = document.querySelector('#viewer').offsetHeight + 'px';" +
            "document.querySelector('body').style.height = document.querySelector('#viewer').offsetHeight + 'px';" +
            "document.querySelector('html').style.height = document.querySelector('#viewer').offsetHeight + 'px';");
        Thread.sleep(3000);

        app.generate_screenshots();
    }

    private WebDriver driver;

    public App (WebDriver driver) {
        this.driver = driver;
    }

    public void generate_screenshots () throws Exception {
        File screenshot = ((TakesScreenshot) this.driver).getScreenshotAs(OutputType.FILE);
        List <WebElement> slides = this.driver.findElements(By.cssSelector("div.page"));
        System.out.println(slides.size());
        for (int i = 0; i < slides.size(); i++) {
            System.out.println(i);
            this.save_target_screenshot(screenshot, slides.get(i), i);
        }
    }

    public void save_target_screenshot (File screenshot, WebElement target, int slide_index) throws Exception {
        BufferedImage full_image = ImageIO.read(screenshot),
                      sub_image = null;
        int left = target.getLocation().getX(),
            top = target.getLocation().getY(),
            height = target.getSize().getHeight(),
            width = target.getSize().getWidth();
        if (top < 0) {
            height = height + top;
            top = 0;
        }
        if (left < 0) {
            width = width + left;
            left = 0;
        }
        if (top >= full_image.getHeight())
            top = full_image.getHeight() - 2;
        if (left >= full_image.getWidth())
            left = full_image.getWidth() - 2;
        if (top + height >= full_image.getHeight())
            height = full_image.getHeight() - top - 1;
        if (left + width >= full_image.getWidth())
            width = full_image.getWidth() - left - 1;
        sub_image = full_image.getSubimage(
                left, top,
                (width <= 0 ? 1 : width),
                (height <= 0 ? 1 : height));
        File file = new File("./" + slide_index + ".png");
        ImageIO.write(sub_image, "png", file);
    }
}
