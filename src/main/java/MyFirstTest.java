import static java.lang.System.setProperty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

/*******************************************************************
 * Vic's Selenium Study ver 4.2 September 1, 2018 Lucas Baizer
 * BIOS & PHOTOS 7-19-18.pdf
 *******************************************************************/
public class MyFirstTest {
	private static PrintWriter dataWriter;

	public static void main(String[] args) {
		System.out.println("1.  Hello World");
		setProperty("webdriver.chrome.driver", new File("chromedriver").getAbsolutePath());
		ChromeDriver webDriver = new ChromeDriver();
		webDriver.get("https://sandiego.score.org/");
		HashMap profileMap = new HashMap();
		System.out.println("2.  Website Called");
		try {
			dataWriter = new PrintWriter(new File("info.txt"));
			webDriver.findElement(By.xpath("//a[text()=\"Mentor Hub \"]")).click();
			System.out.println("3.  Mentor Hub clicked");
			webDriver
					.findElement(By.xpath(
							"//img[@ src='http://s3.amazonaws.com/mentoring.redesign/s3fs-public/Documents.png']"))
					.click();
			System.out.println("4.  Chapter Documents clicked");
			List<String> volunteerURLlist = new ArrayList<String>();
			WebElement element = webDriver.findElement(By.xpath("//*['Chapter Documents']"));
			ArrayList<String> tabs = new ArrayList<String>(webDriver.getWindowHandles());
			webDriver.switchTo().window(tabs.get(1));
			webDriver.findElement(By.name("password")).sendKeys("wo0140sd");
			System.out.println("5. Password entered");
			webDriver.findElement(By.tagName("button")).click();
			System.out.println("6.  OK clicked");
			Thread.sleep(1000);
			WebElement rosterElement = webDriver.findElement(By.linkText("ROSTERS"));
			rosterElement.click();
			System.out.println("7.  ROSTERS clicked");

			Thread.sleep(1000);
			String fileName = "Roster - ACTIVE MEMBERS - BIOS & PHOTOS";
			WebElement pdfLink = webDriver.findElement(By.xpath("//a[contains(text(), '" + fileName + "')]"));
			pdfLink.click();

			Thread.sleep(1000);

			File newFile = new File("/Users/" + System.getProperty("user.name") + "/Downloads/" + fileName);
			newFile.delete();

			File zip = new File("/Users/" + System.getProperty("user.name") + "/Downloads/ROSTERS.zip");
			zip.delete();

			WebElement downloadButton = webDriver.findElement(By.xpath("//button[@ data-resin-target=\"download\"]"));
			webDriver.executeScript("arguments[0].click();", downloadButton);

			Thread.sleep(10000);

			ZipInputStream input = new ZipInputStream(new FileInputStream(zip));
			byte[] buffer = new byte[1024];
			ZipEntry entry;
			while ((entry = input.getNextEntry()) != null) {
				System.out.println("Entry: " + entry.getName());
				if (entry.getName().equals("ROSTERS/" + fileName)) {
					System.out.println("Writing entry!");
					FileOutputStream fos = new FileOutputStream(newFile);
					int len;
					while ((len = input.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}
					fos.close();
					break;
				}
			}
			input.closeEntry();
			input.close();

			System.out.println(newFile.getAbsolutePath());
			PDDocument document = PDDocument.load(newFile);
			document.getClass();
			if (!document.isEncrypted()) {
				PDFTextStripperByArea stripper = new PDFTextStripperByArea();
				stripper.setSortByPosition(true);
				PDFTextStripper tStripper = new PDFTextStripper();
				String pdfFileInText = tStripper.getText(document);
				String lines[] = pdfFileInText.split("\\r?\\n");// split by whitespace
				for (String line : lines) {
					System.out.println(line);
				}
			}
			int i = 0;
			for (String volunteerInfoLink : volunteerURLlist) {
				webDriver.get(volunteerInfoLink);
				List<WebElement> labels = webDriver.findElementsByClassName("label-field");// Volunteer item description
				List<WebElement> values = webDriver.findElementsByClassName("value-field");// Volunter item info
				Iterator<WebElement> labelIterator = labels.iterator();
				Iterator<WebElement> valueIterator = values.iterator();
				while (labelIterator.hasNext() && valueIterator.hasNext()) {
					WebElement label = labelIterator.next();
					WebElement value = valueIterator.next();
					profileMap.put(label.getText(), value.getText());
				}
				for (Object labelName : profileMap.keySet()) {
					if (labelName.equals("First Name:")) {
						System.out.print(profileMap.get(labelName));
						dataWriter.print(labelName + " " + profileMap.get(labelName) + " ");
					}
					if (labelName.equals("Last Name:")) {
						System.out.println(" " + profileMap.get(labelName));
						dataWriter.println(labelName + " " + profileMap.get(labelName));
					}
				}

				dataWriter
						.println("\n.................................... Next Volunteer .............................");
				System.out.println("\n.................................... Volunteer (" + i++ + ")");
			}
		} catch (Exception e) {
			System.out.println("...................................Exception => " + e);
			e.printStackTrace(System.out);
		}
		dataWriter.close();
		System.out.println("Proper Finish...hooray!");
	}
}
