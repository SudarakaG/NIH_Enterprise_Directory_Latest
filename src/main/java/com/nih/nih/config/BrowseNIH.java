package com.nih.nih.config;

import com.nih.nih.Service.NIHService;
import com.nih.nih.model.NIHNewModel;
import com.nih.nih.repo.NIHRepository;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

@Controller
public class BrowseNIH implements InitializingBean {

    @Autowired
    private NIHRepository nihRepository;

    private static FirefoxDriver driver = null;
    private static String url[] = {"https://ned.nih.gov/search/?fbclid=IwAR37ECfOdZxDluGqqCQNUhikmzK87IIBlNfdNaZbMw-PE5pd0LK0N6wNdFM"};
    private static String codes[] = {"NIH"};
    private static HashMap<String, String> handlers = new HashMap<>();

//    @Autowired
//    private NIHService nihService;

    public void initialise() throws Exception {

        System.setProperty("webdriver.gecko.driver", "geckodriver.exe");
        System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null");

        DesiredCapabilities dc = new DesiredCapabilities();
        dc.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);

        FirefoxProfile profile = new FirefoxProfile();
        profile.setPreference("dom.max_chrome_script_run_time", 0);
        profile.setPreference("dom.max_script_run_time", 0);

        FirefoxOptions options = new FirefoxOptions();
//        options.setHeadless(true);
        options.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);

        driver = new FirefoxDriver(options);

        for (int i = 0; i < url.length - 1; i++) {
            driver.executeScript("window.open()");
        }

        ArrayList<String> windowsHandles = new ArrayList<>(driver.getWindowHandles());

        for (int i = 0; i < url.length; i++) {
            handlers.put(codes[i], windowsHandles.get(i));
        }

        scrape("https://ned.nih.gov/search/?fbclid=IwAR37ECfOdZxDluGqqCQNUhikmzK87IIBlNfdNaZbMw-PE5pd0LK0N6wNdFM");

//
    }

    public List<NIHNewModel> scrape(String link) throws InterruptedException, IOException, AWTException {
        JavascriptExecutor jse = (JavascriptExecutor) driver;
//        WebDriverWait wait = new WebDriverWait(driver,10);
//        NIHController nihController = new NIHController();

        driver.get(link);
        System.out.println("RUNNING " + link);

        WebElement inputLastName = driver.findElementById("ContentPlaceHolder_txtLastName");
        jse.executeScript("arguments[0].setAttribute('value', 'aa')", inputLastName);
        WebElement searchButton = driver.findElementById("ContentPlaceHolder_btnSearchName");
        jse.executeScript("arguments[0].scrollIntoView();", searchButton);
        searchButton.click();

        Thread.sleep(10000);

        WebElement tableSize = driver.findElementById("ContentPlaceHolder_ddlPageSize");
        tableSize.click();
        tableSize.findElements(By.tagName("option")).get(9).click();

        Thread.sleep(10000);

        List<String> dataUrls = new ArrayList<>();
        int i = 1;
        WebElement pageNumberRow = driver.findElementByXPath("/html/body/div[1]/div[3]/div[1]/table/tbody/tr/td/form/table/tbody/tr[3]/td/table/tbody/tr[1]/td/table/tbody/tr[4]/td/div/table/tbody/tr[3]/td/div/table/tbody/tr[1]/td/table/tbody/tr");
//        for (WebElement td : pageNumberRow.findElements(By.tagName("td"))) {
        List<WebElement> td = pageNumberRow.findElements(By.tagName("td"));
        System.out.println(td.size() + " pages");
        for (int j = 0; j < td.size(); j++) {

            WebElement pageNumbers = driver.findElementByXPath("/html/body/div[1]/div[3]/div[1]/table/tbody/tr/td/form/table/tbody/tr[3]/td/table/tbody/tr[1]/td/table/tbody/tr[4]/td/div/table/tbody/tr[3]/td/div/table/tbody/tr[1]/td/table/tbody/tr");
            if (pageNumbers.findElements(By.tagName("td")).get(j).getAttribute("innerText").equalsIgnoreCase(Integer.toString(j + 1))) {
                pageNumbers.findElements(By.tagName("td")).get(j).click();
                System.out.println("Clicked on : " + (j + 1));
                Thread.sleep(10000);
            }

//             WebElement dataTable = driver.findElementById("ContentPlaceHolder_gvSearchResults");
            WebElement dataTable = driver.findElementByXPath("/html/body/div[1]/div[3]/div[1]/table/tbody/tr/td/form/table/tbody/tr[3]/td/table/tbody/tr[1]/td/table/tbody/tr[4]/td/div/table/tbody/tr[3]/td/div/table/tbody");
            for (WebElement tr : dataTable.findElements(By.tagName("tr"))) {//
                String dataRow = tr.getAttribute("class");
                if (dataRow.equalsIgnoreCase("GVRow") || dataRow.equalsIgnoreCase("GVAltRow")) {
                  try {
                      if (nihRepository.existsByPhone(tr.findElements(By.tagName("td")).get(4).getAttribute("innerText"))) {
                          System.out.println("Duplicate Skipped..");
                          continue;
                      }
                  }catch (Exception n){
                      System.out.println("No Data in DataBase..");
                  }
//                String dataLink = tr.findElement(By.tagName("a")).getAttribute("href");
                    dataUrls.add(tr.findElement(By.tagName("a")).getAttribute("href"));

                }
            }
            System.out.println("**********" + dataUrls.size());
            for (String dataUrl : dataUrls) {
                System.out.println(dataUrl);
            }

        }

        List<NIHNewModel> nihs = gettingDataFromLinks(dataUrls);
        int z = 1;
        for (NIHNewModel nih : nihs) {
            System.out.println("***************************");
            System.out.println(z);
            System.out.println(nih.getLegal_name());
            System.out.println(nih.getPreffered_name());
            System.out.println(nih.getEmail());
            System.out.println(nih.getLocation());
            System.out.println(nih.getMail_stop());
            System.out.println(nih.getPhone());
            System.out.println(nih.getFax());
            System.out.println(nih.getIc());
            System.out.println(nih.getOrganization());
            System.out.println(nih.getClassification());
            System.out.println(nih.getTty());
            System.out.println("***************************");
            z++;

        }


        return nihs;

    }

//    public boolean test() {
////        NIHNewModel nih = new NIHNewModel();
////        nih.setLegal_name("Test");
////        nih.setPreffered_name("Test");
////        nih.setEmail("Test");
////        nih.setLocation("Test");
////        nih.setMail_stop("Test");
////        nih.setPhone("Test");
////        nih.setFax("Test");
////        nih.setIc("Test");
////        nih.setOrganization("Test");
////        nih.setClassification("Test");
////        nih.setTty("Test");
////        System.out.println(nih.getFax() + "_______________");
////        nihService.saveRecord(nih);
////        return true;
//    }

    private List<NIHNewModel> gettingDataFromLinks(List<String> dataUrls) {
        List<NIHNewModel> list = new ArrayList<NIHNewModel>();
        NIHNewModel nih = null;

        for (String dataUrl : dataUrls) {

            String legalName = "";
            String preferredName = "";
            String eMail = "";
            String location = "";
            String mailStop = "";
            String phone = "";
            String fax = "";
            String ic = "";
            String organization = "";
            String classification = "";
            String tty = "";

            nih = new NIHNewModel();

            driver.get(dataUrl);
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                System.out.println("Thread Sleep Not Working..");
            }

            List<WebElement> contentTable = driver.findElementById("ContentPlaceHolder_dvPerson").findElements(By.tagName("tr"));
//            for (WebElement element : contentTable.findElements(By.tagName("tr"))) {
//                element.findElements(By.tagName("td")).get(1).getAttribute("innerText");
//                System.out.println(element.findElements(By.tagName("td")).get(0).getAttribute("innerText") + "  " + element.findElements(By.tagName("td")).get(1).getAttribute("innerText"));
//            }
//            String name = contentTable.get(0).findElements(By.tagName("td")).get(1).getAttribute("innerText");
//            System.out.println("Name : "+name);

            legalName = contentTable.get(0).findElements(By.tagName("td")).get(1).getAttribute("innerText");
            preferredName = contentTable.get(1).findElements(By.tagName("td")).get(1).getAttribute("innerText");
            ;
            eMail = contentTable.get(2).findElements(By.tagName("td")).get(1).getAttribute("innerText");
            location = contentTable.get(3).findElements(By.tagName("td")).get(1).getAttribute("innerText");
            mailStop = contentTable.get(4).findElements(By.tagName("td")).get(1).getAttribute("innerText");
            phone = contentTable.get(5).findElements(By.tagName("td")).get(1).getAttribute("innerText");
            fax = contentTable.get(6).findElements(By.tagName("td")).get(1).getAttribute("innerText");
            ic = contentTable.get(7).findElements(By.tagName("td")).get(1).getAttribute("innerText");
            organization = contentTable.get(8).findElements(By.tagName("td")).get(1).getAttribute("innerText");
            classification = contentTable.get(9).findElements(By.tagName("td")).get(1).getAttribute("innerText");
            tty = contentTable.get(10).findElements(By.tagName("td")).get(1).getAttribute("innerText");

            nih.setLegal_name(legalName);
            nih.setPreffered_name(preferredName);
            nih.setEmail(eMail);
            nih.setLocation(location);
            nih.setMail_stop(mailStop);
            nih.setPhone(phone);
            nih.setFax(fax);
            nih.setIc(ic);
            nih.setOrganization(organization);
            nih.setClassification(classification);
            nih.setTty(tty);

            list.add(nih);

            try {
                nihRepository.save(nih);
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
        return list;

    }

//    private static void createXlsFile(List<SiteRow> siteRowList, String sheetName, Workbook workbook, boolean extra) {
//        CreationHelper createHelper = workbook.getCreationHelper();
//        Sheet sheet = workbook.createSheet(sheetName);
//        Font headerFont = workbook.createFont();
//        headerFont.setBold(true);
//        headerFont.setFontHeightInPoints((short) 14);
//        headerFont.setColor(IndexedColors.RED.getIndex());
//
//        CellStyle headerCellStyle = workbook.createCellStyle();
//        headerCellStyle.setFont(headerFont);
//
//        Row headerRow = sheet.createRow(0);
//
//        ArrayList<String> list = new ArrayList();
//
//        for (int i = 0; i < 6; i++) {
//            list.add("Descrizione" + (i + 1));
//        }
//
//        list.add("Tiposcommessa1");
//        list.add("Tiposcommessa2");
//        list.add("Data");
//        list.add("Giorno");
//        list.add("Orario");
//        list.add("Squadra 1");
//        list.add("Squadra 2");
//        list.add("Uno");
//        list.add("Uno Links");
//        list.add("x");
//        list.add("x Links");
//        list.add("Due");
//        list.add("Due Links");
//        if (extra) {
//            list.add("Under 2.5");
//            list.add("Over 2.5");
//        }
//
//        // Create cells
//        for (int i = 0; i < list.size(); i++) {
//            Cell cell = headerRow.createCell(i);
//            cell.setCellValue(list.get(i));
//            cell.setCellStyle(headerCellStyle);
//        }
//
//
//        // Create Cell Style for formatting Date
//        CellStyle dateCellStyle = workbook.createCellStyle();
//        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy"));
//
//        // Create Other rows and cells with employees data
//        int rowNum = 1;
//        int max = 6;
//        for (SiteRow siteRow : siteRowList) {
//            Row row = sheet.createRow(rowNum++);
//            int i = 0;
//            if (checkEmptyRow(siteRow)) {
//                row.createCell(0).setCellValue(siteRow.getDescrizione_1());
//                row.createCell(1).setCellValue(siteRow.getDescrizione_2());
//                row.createCell(2).setCellValue(siteRow.getDescrizione_3());
//                row.createCell(max).setCellValue(siteRow.getTipo_Scommessa_1());
//                row.createCell(max + 1).setCellValue(siteRow.getTipo_Scommessa_2());
//                row.createCell(max + 1 + 1).setCellValue(siteRow.getDate());
//                row.createCell(max + 1 + 2).setCellValue(siteRow.getGiorno());
//                row.createCell(max + 1 + 3).setCellValue(siteRow.getOrario());
//                row.createCell(max + 1 + 4).setCellValue(siteRow.getSquadra_1());
//                row.createCell(max + 1 + 5).setCellValue(siteRow.getSquadra_2());
//                row.createCell(max + 1 + 6).setCellValue(siteRow.getUnoListAsString());
//                row.createCell(max + 1 + 7).setCellValue(siteRow.getUnoLinkListAsString());
//                row.createCell(max + 1 + 8).setCellValue(siteRow.getXListAsString());
//                row.createCell(max + 1 + 9).setCellValue(siteRow.getXLinkListAsString());
//                row.createCell(max + 1 + 10).setCellValue(siteRow.getDueListAsString());
//                row.createCell(max + 1 + 11).setCellValue(siteRow.getDueLinkListAsString());
//                row.createCell(max + 1 + 12).setCellValue(siteRow.getPercListAsString());
//                if (extra) {
//                    row.createCell(max + 1 + 12).setCellValue(siteRow.getExtra1AsString());
//                    row.createCell(max + 1 + 13).setCellValue(siteRow.getExtra2AsString());
//                }
//            }
//        }
//
//        for (int i = 0; i < list.size(); i++) {
//            sheet.autoSizeColumn(i);
//        }
//
//    }

//    private static boolean checkEmptyRow(SiteRow siteRow) {
//        if (siteRow.getDate() == null && siteRow.getSquadra_1() == null && siteRow.getTipo_Scommessa_1() == null) {
//            return false;
//        } else {
//            return true;
//        }
//    }

    private void firstAttempt() {
//
//////            jse.executeScript("arguments[0].scrollIntoView();", td);
////
//////            wait.until(ExpectedConditions.stalenessOf(td));
////
//////            Wait<WebDriver> stubbornWait = new FluentWait<WebDriver>(driver)
//////                    .withTimeout(30, SECONDS)
//////                    .pollingEvery(5, SECONDS)
//////                    .ignoring(NoSuchElementException.class)
//////                    .ignoring(StaleElementReferenceException.class);
//////
//////            WebElement foo = stubbornWait.until(new Function<WebDriver, WebElement>() {
//////                public WebElement apply(WebDriver driver) {
//////                    return driver.findElement(By.tagName("td"));
//////                }
//////            });
////            boolean elementStale = false;
////            WebDriverWait wait = new WebDriverWait(driver,10);
////            wait.until(new ExpectedCondition<Boolean>() {
////                public Boolean apply(WebDriver driver) {
//////                    WebElement button = driver.findElement(By
//////                            .name("createForm:dateInput_input"));
////
////                    if (td.isDisplayed())
////                        return true;
////                    else
////                        return false;
////
////                }
////            });
////
////            td.click();
////            Thread.sleep(10000);
//
////            WebElement dataTable = driver.findElementById("ContentPlaceHolder_gvSearchResults");
//        WebElement dataTable = driver.findElementByXPath("/html/body/div[1]/div[3]/div[1]/table/tbody/tr/td/form/table/tbody/tr[3]/td/table/tbody/tr[1]/td/table/tbody/tr[4]/td/div/table/tbody/tr[3]/td/div/table/tbody");
//        for (WebElement tr : dataTable.findElements(By.tagName("tr"))) {//
//            String dataRow = tr.getAttribute("class");
//            if (dataRow.equalsIgnoreCase("GVRow") || dataRow.equalsIgnoreCase("GVAltRow")) {
////                    jse.executeScript("arguments[0].scrollIntoView();", tr);
////                    tr.findElement(By.tagName("a")).click();
////                String dataLink = tr.findElement(By.tagName("a")).getAttribute("href");
//                dataUrls.add(tr.findElement(By.tagName("a")).getAttribute("href"));
//
//            }
//        }
//        System.out.println("**********" + dataUrls.size());
//        for (String dataUrl : dataUrls) {
//            System.out.println(dataUrl);
//        }
//
//        Thread.sleep(30000);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Thread.sleep(2000);
        this.initialise();
    }
}
