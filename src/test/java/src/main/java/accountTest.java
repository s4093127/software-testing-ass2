package src.main.java;

import com.inflectra.spiratest.addons.junitextension.SpiraTestCase;
import com.inflectra.spiratest.addons.junitextension.SpiraTestConfiguration;
import org.junit.jupiter.api.*;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

@SpiraTestConfiguration (
        url = "https://rmit.spiraservice.net",
        login = "s4093127",
        rssToken = "{03BBFE50-3D3D-4AEE-8451-D913FC2D0A00}",
        projectId = 1198
)

public class accountTest {
    @Test
    @DisplayName("initializes all fields to their defaults account")
    @SpiraTestCase(testCaseId = 48287)
    public void defaultConstructor_setsDefaultValues() {
        Account acc = new Account();

        assertAll("default account fields",
                () -> assertNull(acc.getName()),
                () -> assertEquals(0, acc.getAccountNumber()),
                () -> assertNull(acc.getPIN()),
                () -> assertEquals(0.0, acc.getAmount(), 0.0001)
        );
    }

    @Test
    @DisplayName("stores details and adds the default 1000 bonus in the account")
    @SpiraTestCase(testCaseId = 48288)
    public void parameterizedConstructor_addsDefaultBonus() {
        Account acc = new Account("Alice", 11112222, "1234", 500.0);

        assertAll("account fields after construction",
                () -> assertEquals("Alice", acc.getName()),
                () -> assertEquals(11112222, acc.getAccountNumber()),
                () -> assertEquals("1234", acc.getPIN()),
                () -> assertEquals(1500.0, acc.getAmount(), 0.0001)
        );
    }

    @Test
    @DisplayName("zero opening amount in the account")
    @SpiraTestCase(testCaseId = 48291)
    public void parameterizedConstructor_zeroAmount_boundary() {
        Account acc = new Account("Bob", 22223333, "0000", 0.0);

        assertEquals(1000.0, acc.getAmount(), 0.0001);
    }

    @Test
    @DisplayName("a negative opening amount account")
    @SpiraTestCase(testCaseId = 48292)
    public void parameterizedConstructor_negativeAmount_edgeCase() {
        Account acc = new Account("Zoe", 44445555, "1122", -1000.0);

        // 1000 (default bonus) + (-1000) = 0
        assertEquals(0.0, acc.getAmount(), 0.0001);
    }

    @Test
    @DisplayName("setters correctly update every field")
    @SpiraTestCase(testCaseId = 48293)
    public void setters_updateAllFields() {
        Account acc = new Account();

        acc.setName("Charlie");
        acc.setAccountNumber(33334444);
        acc.setPIN("9999");
        acc.setAmount(2500.75);

        assertAll("account fields after setters",
                () -> assertEquals("Charlie", acc.getName()),
                () -> assertEquals(33334444, acc.getAccountNumber()),
                () -> assertEquals("9999", acc.getPIN()),
                () -> assertEquals(2500.75, acc.getAmount(), 0.0001)
        );
    }

    @Test
    @DisplayName("setAmount accepts a negative balance in the account")
    @SpiraTestCase(testCaseId = 48294)
    public void setAmount_negativeValue_edgeCase() {
        Account acc = new Account();

        acc.setAmount(-50.0);

        assertEquals(-50.0, acc.getAmount(), 0.0001);
    }
}
