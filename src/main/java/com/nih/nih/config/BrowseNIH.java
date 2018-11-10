package com.nih.nih.config;

import com.nih.nih.Service.NIHService;
import com.nih.nih.model.NIHNewModel;
import com.nih.nih.repo.NIHRepository;
import com.opencsv.CSVWriter;
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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
        createCSVFile();

//
    }

    public void scrape(String link) throws InterruptedException, IOException, AWTException {

        JavascriptExecutor jse = (JavascriptExecutor) driver;
//        WebDriverWait wait = new WebDriverWait(driver,10);
//        NIHController nihController = new NIHController();

        String[] alphabet = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};

        for (int g = 0; g < (alphabet.length); g++) {
            for (int k = 0; k < (alphabet.length); k++) {

                String searchText = alphabet[g] + alphabet[k];
                System.out.println("Searching : " + searchText);
//        String searchText = "aa";

                try {
                    driver.get(link);
                    System.out.println("RUNNING " + link);

                    WebElement inputLastName = driver.findElementById("ContentPlaceHolder_txtLastName");
                    jse.executeScript("arguments[0].setAttribute('value', '" + searchText + "')", inputLastName);
                    WebElement searchButton = driver.findElementById("ContentPlaceHolder_btnSearchName");
                    jse.executeScript("arguments[0].scrollIntoView();", searchButton);
                    searchButton.click();
                }catch (Exception e){
                    System.out.println("Page Not Loaded..");
                    driver.navigate().refresh();
                    Thread.sleep(10000);
                    k = k-1;
                    continue;
                }

                Thread.sleep(20000);

                try {
                    WebElement tableSize = driver.findElementById("ContentPlaceHolder_ddlPageSize");
                    tableSize.click();
                    tableSize.findElements(By.tagName("option")).get(9).click();

                    Thread.sleep(10000);

//                    List<String> dataUrls = new ArrayList<>();
                    int i = 1;
//                    try {
                        WebElement pageNumberRow = driver.findElementByXPath("/html/body/div[1]/div[3]/div[1]/table/tbody/tr/td/form/table/tbody/tr[3]/td/table/tbody/tr[1]/td/table/tbody/tr[4]/td/div/table/tbody/tr[3]/td/div/table/tbody/tr[1]/td/table/tbody/tr");
//        for (WebElement td : pageNumberRow.findElements(By.tagName("td"))) {
                        List<WebElement> td = pageNumberRow.findElements(By.tagName("td"));
                        System.out.println(td.size() + " pages");


                        int paginationSize = td.size();
                        for (int j = 0; j < paginationSize; j++) {

                            WebElement pageNumbersTab = driver.findElementByXPath("/html/body/div[1]/div[3]/div[1]/table/tbody/tr/td/form/table/tbody/tr[3]/td/table/tbody/tr[1]/td/table/tbody/tr[4]/td/div/table/tbody/tr[3]/td/div/table/tbody/tr[1]/td/table/tbody/tr");

                            paginationSize = pageNumbersTab.findElements(By.tagName("td")).size();

                            System.out.println("***************"+pageNumbersTab.findElements(By.tagName("td")).get(0).findElement(By.xpath("./*")).getTagName());

                            if (pageNumbersTab.findElements(By.tagName("td")).get(0).findElement(By.xpath("./*")).getTagName().equalsIgnoreCase("span") || !pageNumbersTab.findElements(By.tagName("td")).get(0).findElement(By.tagName("a")).getAttribute("innerText").equalsIgnoreCase("First")){
//                                gettingDataUrls();
                                System.out.println("Clicked on : "+pageNumbersTab.findElements(By.tagName("td")).get(j).findElement(By.xpath("./*")).getAttribute("innerText"));
                                pageNumbersTab.findElements(By.tagName("td")).get(j).click();
                            }else {
//                                gettingDataUrls();
                                try {

                                    System.out.println("Clicked on : " + pageNumbersTab.findElements(By.tagName("td")).get(j + 2).findElement(By.tagName("a")).getAttribute("innerText"));
                                    pageNumbersTab.findElements(By.tagName("td")).get(j + 2).click();

                                }catch (Exception a){
                                    System.out.println("Got Data from All Pages..");
                                    break;
                                }
                            }

                            if (j == 10){
                                j = 0;
                            }

//                            if (!pageNumbersTab.findElements(By.tagName("td")).get(0).findElement(By.tagName("a")).getAttribute("innerText").equalsIgnoreCase("First")){
//
//                                System.out.println("Clicked on : "+pageNumbersTab.findElements(By.tagName("td")).get(j).findElement(By.tagName("a")).getAttribute("innerText"));
//                                pageNumbersTab.findElements(By.tagName("td")).get(j).click();
//
//                            }else{
//
//                                System.out.println("Clicked on : "+pageNumbersTab.findElements(By.tagName("td")).get(j+2).findElement(By.tagName("a")).getAttribute("innerText"));
//                                pageNumbersTab.findElements(By.tagName("td")).get(j+2).click();
//
//                            }
                            Thread.sleep(10000);

//                            if (pageNumbersTab.findElements(By.tagName("td")).size() == 12 && j == 10){
//                                System.out.println("***** All Pages Success..");
//                                break;
//                            }

//                            WebElement pageNumbers = driver.findElementByXPath("/html/body/div[1]/div[3]/div[1]/table/tbody/tr/td/form/table/tbody/tr[3]/td/table/tbody/tr[1]/td/table/tbody/tr[4]/td/div/table/tbody/tr[3]/td/div/table/tbody/tr[1]/td/table/tbody/tr");
//                            if (pageNumbers.findElements(By.tagName("td")).get(j).getAttribute("innerText").equalsIgnoreCase(Integer.toString(j + 1))) {
//                                pageNumbers.findElements(By.tagName("td")).get(j).click();
//                                System.out.println("Clicked on : " + (j + 1));
//                                Thread.sleep(10000);
//                            }else if(pageNumbers.findElements(By.tagName("td")).get(j).getAttribute("innerText").equalsIgnoreCase("...")){
//                                pageNumbers.findElements(By.tagName("td")).get(j).click();
//                                System.out.println("clicked on ...");
//                                Thread.sleep(10000);
//                                td = driver.findElementByXPath("/html/body/div[1]/div[3]/div[1]/table/tbody/tr/td/form/table/tbody/tr[3]/td/table/tbody/tr[1]/td/table/tbody/tr[4]/td/div/table/tbody/tr[3]/td/div/table/tbody/tr[1]/td/table/tbody/tr").findElements(By.tagName("td"));
//                                continue;
//                            }else{
//                                pageNumbers.findElements(By.tagName("td")).get(j+2).click();
//                            }



//             WebElement dataTable = driver.findElementById("ContentPlaceHolder_gvSearchResults");

                            gettingDataUrls();

                        }
                    } catch (Exception d) {
                        System.out.println("***** No Page Number Row..");
                        gettingDataUrls();
                    }

//                    if (!dataUrls.isEmpty()) {
//                        gettingDataFromLinks(dataUrls);
//                        System.out.println("** One Set Sent to Database Completely..");
//                    }
//        int z = 1;
//        for (NIHNewModel nih : nihs) {
//            System.out.println("***************************");
//            System.out.println(z);
//            System.out.println(nih.getLegal_name());
//            System.out.println(nih.getPreffered_name());
//            System.out.println(nih.getEmail());
//            System.out.println(nih.getLocation());
//            System.out.println(nih.getMail_stop());
//            System.out.println(nih.getPhone());
//            System.out.println(nih.getFax());
//            System.out.println(nih.getIc());
//            System.out.println(nih.getOrganization());
//            System.out.println(nih.getClassification());
//            System.out.println(nih.getTty());
//            System.out.println("***************************");
//            z++;
//
//        }

//                } catch (Exception c) {
//                    System.out.println("***** No Data to Read..");
////                    continue;
//                }

            }

        }


    }

    private void gettingDataUrls() {

//        List<String> dataUrls = new ArrayList<>();
        try {
            WebElement dataTable = driver.findElementByXPath("/html/body/div[1]/div[3]/div[1]/table/tbody/tr/td/form/table/tbody/tr[3]/td/table/tbody/tr[1]/td/table/tbody/tr[4]/td/div/table/tbody/tr[3]/td/div/table/tbody");
            for (WebElement tr : dataTable.findElements(By.tagName("tr"))) {//
                String dataRow = tr.getAttribute("class");
                if (dataRow.equalsIgnoreCase("GVRow") || dataRow.equalsIgnoreCase("GVAltRow")) {

                        String tele = tr.findElements(By.tagName("td")).get(4).getAttribute("innerText");
                    try {
                        if (!tele.equalsIgnoreCase("") && nihRepository.existsByPhone(tele)) {
                            System.out.println("***** Duplicate Skipped..");
                            continue;
                        }
                    } catch (Exception n) {
                        System.out.println("***** No Data in DataBase..");
                    }
                        String prefeerdName = tr.findElements(By.tagName("td")).get(0).getAttribute("innerText");
                        String email = tr.findElements(By.tagName("td")).get(3).getAttribute("innerText");
                        String ic = tr.findElements(By.tagName("td")).get(1).getAttribute("innerText");
                        String classification = tr.findElements(By.tagName("td")).get(2).getAttribute("innerText");

                        NIHNewModel nihNewModel = new NIHNewModel();
                        nihNewModel.setPreffered_name(prefeerdName);
                        nihNewModel.setEmail(email);
                        nihNewModel.setIc(ic);
                        nihNewModel.setClassification(classification);
                        nihNewModel.setPhone(tele);

                        try {
                            nihRepository.save(nihNewModel);
                        }catch (Exception e){
                            System.out.println("Error Occured while Sending Data..");
                        }

//                String dataLink = tr.findElement(By.tagName("a")).getAttribute("href");
//                    dataUrls.add(tr.findElement(By.tagName("a")).getAttribute("href"));

                }
            }
//            System.out.println("**********" + dataUrls.size());
//            for (String dataUrl : dataUrls) {
//                System.out.println(dataUrl);
//            }
        }catch (Exception n){
            System.out.println("No Data to Write..");
        }

//        return dataUrls;

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
//        List<NIHNewModel> list = new ArrayList<NIHNewModel>();
//        NIHNewModel nih = null;
//
//        for (String dataUrl : dataUrls) {
//
//            String legalName = "";
//            String preferredName = "";
//            String eMail = "";
//            String location = "";
//            String mailStop = "";
//            String phone = "";
//            String fax = "";
//            String ic = "";
//            String organization = "";
//            String classification = "";
//            String tty = "";
//
//            nih = new NIHNewModel();
//
//            driver.get(dataUrl);
//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//                System.out.println("Thread Sleep Not Working..");
//            }
//
//            List<WebElement> contentTable = driver.findElementById("ContentPlaceHolder_dvPerson").findElements(By.tagName("tr"));
////            for (WebElement element : contentTable.findElements(By.tagName("tr"))) {
////                element.findElements(By.tagName("td")).get(1).getAttribute("innerText");
////                System.out.println(element.findElements(By.tagName("td")).get(0).getAttribute("innerText") + "  " + element.findElements(By.tagName("td")).get(1).getAttribute("innerText"));
////            }
////            String name = contentTable.get(0).findElements(By.tagName("td")).get(1).getAttribute("innerText");
////            System.out.println("Name : "+name);
//
//            legalName = contentTable.get(0).findElements(By.tagName("td")).get(1).getAttribute("innerText");
//            preferredName = contentTable.get(1).findElements(By.tagName("td")).get(1).getAttribute("innerText");
//            ;
//            eMail = contentTable.get(2).findElements(By.tagName("td")).get(1).getAttribute("innerText");
//            location = contentTable.get(3).findElements(By.tagName("td")).get(1).getAttribute("innerText");
//            mailStop = contentTable.get(4).findElements(By.tagName("td")).get(1).getAttribute("innerText");
//            phone = contentTable.get(5).findElements(By.tagName("td")).get(1).getAttribute("innerText");
//            fax = contentTable.get(6).findElements(By.tagName("td")).get(1).getAttribute("innerText");
//            ic = contentTable.get(7).findElements(By.tagName("td")).get(1).getAttribute("innerText");
//            organization = contentTable.get(8).findElements(By.tagName("td")).get(1).getAttribute("innerText");
//            classification = contentTable.get(9).findElements(By.tagName("td")).get(1).getAttribute("innerText");
//            tty = contentTable.get(10).findElements(By.tagName("td")).get(1).getAttribute("innerText");
//
//            nih.setLegal_name(legalName);
//            nih.setPreffered_name(preferredName);
//            nih.setEmail(eMail);
//            nih.setLocation(location);
//            nih.setMail_stop(mailStop);
//            nih.setPhone(phone);
//            nih.setFax(fax);
//            nih.setIc(ic);
//            nih.setOrganization(organization);
//            nih.setClassification(classification);
//            nih.setTty(tty);
//
//            list.add(nih);
//
//            try {
//                nihRepository.save(nih);
//            } catch (Exception e) {
//                System.out.println("Problem in sending data to database..");
//                e.printStackTrace();
//            }
//
//
//        }
        return null;

    }

//

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

    public void createCSVFile() throws IOException {
        List<NIHNewModel> allData = nihRepository.findAll();
//        String filepath="./tmp/NIHDirectory.csv";
        new File("C:\\Users\\gksde\\OneDrive\\Desktop\\nih\\tmp").mkdir();
        String filepath="C:\\Users\\gksde\\OneDrive\\Desktop\\nih\\tmp/\\NIHDirectory.csv";
        File file = new File(filepath);
        FileWriter outputFile = new FileWriter(file);
//        PrintWriter pw = new PrintWriter(filepath);

//        StringBuilder sb = new StringBuilder();

        CSVWriter writer = new CSVWriter(outputFile);

//        String ColumnNamesList = "Preferred Name,E-Mail,IC,Classification";
// No need give the headers Like: id, Name on builder.append
        String[] header = {"Preferred Name","E-Mail","IC","Classification"};
        writer.writeNext(header);

//        sb.append(ColumnNamesList +"\n");

        for (NIHNewModel allDatum : allData) {
//            sb.append(allDatum.getPreffered_name()+",");
//            sb.append(allDatum.getEmail()+",");
//            sb.append(allDatum.getIc()+",");
//            sb.append(allDatum.getClassification()+"\n");

            String[] data = {allDatum.getPreffered_name(),allDatum.getEmail(),allDatum.getIc(),allDatum.getClassification()};
            writer.writeNext(data);

        }


//        pw.write(sb.toString());
        writer.close();
        System.out.println("Done creating csv file..!");
    }

}
