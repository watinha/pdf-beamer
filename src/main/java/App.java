import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
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
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.OutputType;


public class App {
    public static void main (String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("No URL passed you dummy...");
            return ;
        }
        String url = args[0];

        DesiredCapabilities capabilities = DesiredCapabilities.firefox();
        capabilities.setCapability("marionette", true);
        WebDriver driver = new FirefoxDriver(capabilities);
        driver.get(url);
        App app = new App(driver, "images");
        app.generate_beamer();
    }

    private WebDriver driver;
    private String folder;

    public App (WebDriver driver, String folder) {
        this.driver = driver;
        this.folder = folder;
    }

    public void generate_beamer () throws Exception {
        File beamer_file = new File("apresentacao.tex");
        if (beamer_file.exists()) {
            System.out.println("a file already exists as apresentacao.tex...");
            return ;
        }

        Thread.sleep(3000);
        ((JavascriptExecutor) driver).executeScript(
            "document.querySelector('#viewerContainer').style.height = document.querySelector('#viewer').offsetHeight + 'px';" +
            "document.querySelector('#mainContainer').style.height = document.querySelector('#viewer').offsetHeight + 'px';" +
            "document.querySelector('#outerContainer').style.height = document.querySelector('#viewer').offsetHeight + 'px';" +
            "document.querySelector('body').style.height = document.querySelector('#viewer').offsetHeight + 'px';" +
            "document.querySelector('html').style.height = document.querySelector('#viewer').offsetHeight + 'px';");
        Thread.sleep(3000);

        FileWriter writer = new FileWriter(beamer_file);

        writer.write("\\documentclass[aspectratio=43]{beamer}\n");
        writer.write("\\usepackage[utf8]{inputenc}\n");
        writer.write("\\usepackage[T1]{fontenc}\n");
        writer.write("\\usepackage[brazil]{babel}\n");
        writer.write("\\usetheme{default}\n");
        writer.write("\\usecolortheme{default}\n");
        writer.write("\\usefonttheme{default}\n");
        writer.write("\\title[\\sc{Texto no rodap\\'e}]{Modelo do Beamer - Digite o titulo}\n");
        writer.write("\\author[digite seu Nome]{digite seu Nome}\n");
        writer.write("\\institute{nome do instituto}\n");
        writer.write("\\date{\\today}\n");
        writer.write("\\begin{document}\n");
        this.generate_screenshots(writer);
        writer.write("\\end{document}\n");
        writer.close();
    }

    private void generate_screenshots (FileWriter writer) throws Exception {
        File screenshot = ((TakesScreenshot) this.driver).getScreenshotAs(OutputType.FILE);
        List <WebElement> slides = this.driver.findElements(By.cssSelector("div.page .canvasWrapper"));
        File images_folder = new File(this.folder);
        if (!images_folder.exists())
            images_folder.mkdir();
        for (int i = 0; i < slides.size(); i++) {
            this.save_target_screenshot(screenshot, slides.get(i), i);
            writer.write("\\begin{frame}\n");
            writer.write("    \\includegraphics[width=1\\columnwidth]{images/" + i + ".png}\n");
            writer.write("\\end{frame}\n");
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
        File file = new File(this.folder + "/" + slide_index + ".png");
        ImageIO.write(sub_image, "png", file);
    }
}
