package src.main.java;

import com.inflectra.spiratest.addons.junitextension.SpiraTestCase;
import com.inflectra.spiratest.addons.junitextension.SpiraTestConfiguration;
import org.junit.jupiter.api.*;

import java.io.*;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@SpiraTestConfiguration (
        url = "https://rmit.spiraservice.net",
        login = "s4093127",
        rssToken = "{03BBFE50-3D3D-4AEE-8451-D913FC2D0A00}",
        projectId = 1198
)

public class bankAddNewRecordTest {
    private static final File BANK_FILE = new File("BankRecord.txt");

    private final InputStream originalIn = System.in;

    @BeforeAll
    static void fixLocale() {
        Locale.setDefault(Locale.US);
    }

    @AfterEach
    void restoreStreamsAndCleanUpFile() {
        System.setIn(originalIn);
        if (BANK_FILE.exists()) {
            BANK_FILE.delete();
        }
    }

    private void provideInput(String data) {
        System.setIn(new ByteArrayInputStream(data.getBytes()));
    }

    @Test
    @DisplayName("reads input and adds a new account with the default bonus applied")
    @SpiraTestCase(testCaseId = 48366)
    public void addNewRecord_addsAccountFromInput() {
        Bank bank = new Bank();
        provideInput("David\n55556666\n4321\n750\n");

        bank.addNewRecord();

        assertEquals(1, bank.AL.size());
        Account added = bank.AL.get(0);
        assertAll("newly added account",
                () -> assertEquals("David", added.getName()),
                () -> assertEquals(55556666, added.getAccountNumber()),
                () -> assertEquals("4321", added.getPIN()),
                () -> assertEquals(1750.0, added.getAmount(), 0.0001)
        );
    }

    @Test
    @DisplayName("zero extra amount in the bank for new record")
    @SpiraTestCase(testCaseId = 48367)
    public void addNewRecord_zeroAmount_boundary() {
        Bank bank = new Bank();
        provideInput("Eve\n77778888\n1111\n0\n");

        bank.addNewRecord();

        assertEquals(1000.0, bank.AL.get(0).getAmount(), 0.0001);
    }
}
