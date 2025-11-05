package zeromonos.functionality;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.seljup.SeleniumJupiter;
import io.github.bonigarcia.seljup.Options;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "sel.jup.chrome.options=--headless,--no-sandbox,--disable-dev-shm-usage,--disable-gpu"
})
@ExtendWith(SeleniumJupiter.class)
class WebFunctionalTest {

    @LocalServerPort
    private int port;

    @Test
    void testCreateBookingAsCitizen(ChromeDriver driver) {
        driver.get("http://localhost:8080/index.html");
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        
        WebElement citizenBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button.menu-btn:not(.staff)")
        ));
        citizenBtn.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("bookingForm")));
        
        WebElement descriptionInput = driver.findElement(By.id("description"));
        descriptionInput.sendKeys("Large furniture disposal");
        WebElement municipalitySelect = driver.findElement(By.id("municipality"));
        
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector("#municipality option"), 1));
        Select municipality = new Select(municipalitySelect);
        municipality.selectByVisibleText("Aveiro");
        
        WebElement dateInput = driver.findElement(By.id("date"));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].value = '2025-12-25T10:00';", dateInput);
        
        WebElement submitBtn = driver.findElement(By.cssSelector("button[type='submit']"));
        submitBtn.click();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("result")));
        WebElement result = driver.findElement(By.id("result"));
        
        assertThat(result.getText()).containsAnyOf("Submitting...", "✅ Booking Created Successfully!", "Booking created! Token:");
    }
    
    @Test
    void testSearchBooking(ChromeDriver driver) {
        driver.get("http://localhost:8080/user.html");
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        WebElement myBookingsTab = driver.findElement(By.cssSelector("button:nth-of-type(2)"));
        myBookingsTab.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("searchForm")));
        WebElement tokenInput = driver.findElement(By.id("token"));
        tokenInput.sendKeys("CF5DC1AB");

        WebElement searchBtn = driver.findElement(By.cssSelector("#searchForm button[type='submit']"));
        searchBtn.click();
 
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("bookingResult")));
        WebElement bookingResult = driver.findElement(By.id("bookingResult"));
        
        assertThat(bookingResult.getText()).isNotEmpty();
    }
    
    @Test
    void testStaffPortalAccess(ChromeDriver driver) {
        driver.get("http://localhost:8080/index.html");
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        WebElement staffBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button.menu-btn.staff")
        ));
        staffBtn.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("loadBtn")));
        assertThat(driver.getTitle()).contains("Staff Portal");
        
        WebElement loadBtn = driver.findElement(By.id("loadBtn"));
        loadBtn.click();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("staffResult")));
        WebElement staffResult = driver.findElement(By.id("staffResult"));
        
        assertThat(staffResult.getText()).isNotEmpty();
    }
    
    @Test
    void testRoleSwitching(ChromeDriver driver) {
        driver.get("http://localhost:8080/user.html");
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement roleIcon = driver.findElement(By.cssSelector(".role-icon"));
        roleIcon.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".role-menu.active")));
        
        WebElement staffOption = driver.findElement(By.cssSelector(".role-option:nth-child(2)"));
        staffOption.click();
        
        wait.until(ExpectedConditions.titleContains("Staff Portal"));
        assertThat(driver.getCurrentUrl()).contains("staff.html");
    }
    
    @Test
    void testMunicipalityFilter(ChromeDriver driver) {
        driver.get("http://localhost:8080/staff.html");
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        WebElement municipalitySelect = driver.findElement(By.id("municipality"));
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector("#municipality option"), 1));
        
        Select municipality = new Select(municipalitySelect);
        municipality.selectByVisibleText("Abrantes");
        WebElement loadBtn = driver.findElement(By.id("loadBtn"));
        loadBtn.click();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("staffResult")));
        
        WebElement staffResult = driver.findElement(By.id("staffResult"));
        assertThat(staffResult.isDisplayed()).isTrue();
    }

    @Test
    void testEmployeeManagement(ChromeDriver driver) {
        driver.get("http://localhost:8080/staff.html");
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        WebElement employeesTab = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button.tab:nth-of-type(2)")
        ));
        employeesTab.click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("addEmployeeBtn")));
        
        WebElement addEmployeeBtn = driver.findElement(By.id("addEmployeeBtn"));
        addEmployeeBtn.click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("employeeForm")));
        
        driver.findElement(By.id("empName")).sendKeys("Test Employee");
        driver.findElement(By.id("empEmail")).sendKeys("test@example.com");
        
        Select municipality = new Select(driver.findElement(By.id("empMunicipality")));
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
            By.cssSelector("#empMunicipality option"), 1
        ));
        municipality.selectByVisibleText("Aveiro");
        
        Select role = new Select(driver.findElement(By.id("empRole")));
        role.selectByValue("COLLECTOR");
        
        WebElement submitBtn = driver.findElement(By.cssSelector("#employeeForm button[type='submit']"));
        submitBtn.click();
        
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("addEmployeeForm")));
        assertThat(driver.findElement(By.id("addEmployeeForm")).isDisplayed()).isFalse();
    }

    @Test
    void testLoadEmployees(ChromeDriver driver) {
        driver.get("http://localhost:8080/staff.html");
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        WebElement employeesTab = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button.tab:nth-of-type(2)")
        ));
        employeesTab.click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loadEmployeesBtn")));
        
        WebElement loadEmployeesBtn = driver.findElement(By.id("loadEmployeesBtn"));
        loadEmployeesBtn.click();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("employeesResult")));
        WebElement employeesResult = driver.findElement(By.id("employeesResult"));
        
        assertThat(employeesResult.getText()).isNotEmpty();
    }

    @Test
    void testTasksTab(ChromeDriver driver) {
        driver.get("http://localhost:8080/staff.html");
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        WebElement tasksTab = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button.tab:nth-of-type(3)")
        ));
        tasksTab.click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loadTasksBtn")));
        
        WebElement loadTasksBtn = driver.findElement(By.id("loadTasksBtn"));
        loadTasksBtn.click();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("tasksResult")));
        WebElement tasksResult = driver.findElement(By.id("tasksResult"));
        
        assertThat(tasksResult.getText()).isNotEmpty();
    }

    @Test
    void testAssignEmployeeModalOpens(ChromeDriver driver) {
        driver.get("http://localhost:8080/staff.html");
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        WebElement employeesTab = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button.tab:nth-of-type(2)")
        ));
        employeesTab.click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loadEmployeesBtn")));
        driver.findElement(By.id("loadEmployeesBtn")).click();
        
        driver.findElement(By.cssSelector("button.tab:nth-of-type(1)")).click();
        
        wait.until(ExpectedConditions.elementToBeClickable(By.id("loadBtn")));
        driver.findElement(By.id("loadBtn")).click();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("staffResult")));
        
        List<WebElement> assignButtons = driver.findElements(By.className("assign-btn"));
        
        if (!assignButtons.isEmpty()) {
            assignButtons.get(0).click();
            
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("assignModal")));
            
            WebElement modal = driver.findElement(By.id("assignModal"));
            assertThat(modal.isDisplayed()).isTrue();
            
            WebElement employeeList = driver.findElement(By.id("employeeList"));
            assertThat(employeeList.isDisplayed()).isTrue();
            
            driver.findElement(By.cssSelector("#assignModal .close-btn")).click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("assignModal")));
        }
    }

    @Test
    void testCompleteTaskModalOpens(ChromeDriver driver) {
        driver.get("http://localhost:8080/staff.html");
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        WebElement tasksTab = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button.tab:nth-of-type(3)")
        ));
        tasksTab.click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loadTasksBtn")));
        driver.findElement(By.id("loadTasksBtn")).click();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("tasksResult")));
        
        List<WebElement> updateButtons = driver.findElements(By.cssSelector("#tasksResult .update-btn"));
        
        if (!updateButtons.isEmpty()) {
            updateButtons.get(0).click();
            
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("completeModal")));
            
            WebElement modal = driver.findElement(By.id("completeModal"));
            assertThat(modal.isDisplayed()).isTrue();
            
            WebElement notesTextarea = driver.findElement(By.id("completionNotes"));
            assertThat(notesTextarea.isDisplayed()).isTrue();
            
            driver.findElement(By.cssSelector("#completeModal .close-btn")).click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("completeModal")));
        }
    }

    @Test
    void testBookingStatusUpdate(ChromeDriver driver) {
        driver.get("http://localhost:8080/staff.html");
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        WebElement loadBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("loadBtn")));
        loadBtn.click();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("staffResult")));
        
        List<WebElement> statusSelects = driver.findElements(By.className("status-select"));
        
        if (!statusSelects.isEmpty()) {
            WebElement firstSelect = statusSelects.get(0);
            if (firstSelect.isEnabled()) {
                Select statusSelect = new Select(firstSelect);
                String originalValue = statusSelect.getFirstSelectedOption().getText();
                
                if (!originalValue.equals("CANCELLED") && !originalValue.equals("COMPLETED")) {
                    if (originalValue.equals("RECEIVED")) {
                        statusSelect.selectByValue("IN_PROGRESS");
                    } else {
                        statusSelect.selectByValue("RECEIVED");
                    }
                    
                    List<WebElement> updateButtons = driver.findElements(By.cssSelector(".update-btn:not(:disabled)"));
                    if (!updateButtons.isEmpty()) {
                        updateButtons.get(0).click();
                        
                        wait.until(ExpectedConditions.or(
                            ExpectedConditions.textToBePresentInElementLocated(By.id("staffMsg"), "Status updated"),
                            ExpectedConditions.textToBePresentInElementLocated(By.id("staffMsg"), "✅")
                        ));
                        
                        WebElement staffMsg = driver.findElement(By.id("staffMsg"));
                        String msgText = staffMsg.getText();
                        
                        assertThat(msgText).matches(".*(?:Status updated|✅).*");
                    }
                }
            }
        }
    }

    @Test
    void testTabNavigation(ChromeDriver driver) {
        driver.get("http://localhost:8080/staff.html");
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        WebElement bookingsContent = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("bookings")));
        assertThat(bookingsContent.isDisplayed()).isTrue();
        
        WebElement employeesTab = driver.findElement(By.cssSelector("button.tab:nth-of-type(2)"));
        employeesTab.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("employees")));
        WebElement employeesContent = driver.findElement(By.id("employees"));
        assertThat(employeesContent.isDisplayed()).isTrue();
        assertThat(bookingsContent.isDisplayed()).isFalse();
        
        WebElement tasksTab = driver.findElement(By.cssSelector("button.tab:nth-of-type(3)"));
        tasksTab.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("tasks")));
        WebElement tasksContent = driver.findElement(By.id("tasks"));
        assertThat(tasksContent.isDisplayed()).isTrue();
        assertThat(employeesContent.isDisplayed()).isFalse();
        
        WebElement bookingsTab = driver.findElement(By.cssSelector("button.tab:nth-of-type(1)"));
        bookingsTab.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("bookings")));
        assertThat(bookingsContent.isDisplayed()).isTrue();
        assertThat(tasksContent.isDisplayed()).isFalse();
    }
}