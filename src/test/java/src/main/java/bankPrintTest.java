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

public class bankPrintTest {
    private final PrintStream originalOut = System.out;

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
    }

    private String captureOutputOf(Runnable action) {
        ByteArrayOutputStream capture = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capture));
        action.run();
        return capture.toString();
    }

    @Test
    @DisplayName("print: empty account list produces no account output")
    @SpiraTestCase(testCaseId = 48368)
    public void print_emptyList_noOutput() {
        Bank bank = new Bank();
        String printed = captureOutputOf(() -> bank.print());

        assertFalse(printed.contains("Name:"));
    }

    @Test
    @DisplayName("print: outputs name, number and balance for every account in the list")
    @SpiraTestCase(testCaseId = 48369)
    public void print_withAccounts_printsAllDetails() {
        Bank bank = new Bank();
        bank.AL.add(new Account("Rita", 30000001, "1111", 0));
        bank.AL.add(new Account("Sam", 30000002, "2222", 0));

        String printed = captureOutputOf(() -> bank.print());

        assertAll("printed output contains every account's details",
                () -> assertTrue(printed.contains("Rita")),
                () -> assertTrue(printed.contains("30000001")),
                () -> assertTrue(printed.contains("Sam")),
                () -> assertTrue(printed.contains("30000002"))
        );
    }
}
