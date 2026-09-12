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

public class bankSaveAndLoadTest {
    private static final File BANK_FILE = new File("BankRecord.txt");

    private final PrintStream originalOut = System.out;

    @BeforeAll
    static void fixLocale() {
        Locale.setDefault(Locale.US);
    }

    @AfterEach
    void restoreStreamsAndCleanUpFile() {
        System.setOut(originalOut);
        if (BANK_FILE.exists()) {
            BANK_FILE.delete();
        }
    }

    private String captureOutputOf(Runnable action) {
        ByteArrayOutputStream capture = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capture));
        action.run();
        return capture.toString();
    }

    @Test
    @DisplayName("save then load: round-trips account data through the record file")
    @SpiraTestCase(testCaseId = 48371)
    public void saveThenLoad_roundTripsData() {
        Bank bank = new Bank();
        bank.AL.add(new Account("Tara", 40000001, "1212", 500)); // Amount = 1500
        bank.AL.add(new Account("Umar", 40000002, "3434", 250)); // Amount = 1250

        bank.save();
        assertTrue(BANK_FILE.exists(), "save() should create the record file");

        Bank loaded = new Bank();
        loaded.load();

        assertEquals(2, loaded.AL.size());
        assertAll("first restored account",
                () -> assertEquals("Tara", loaded.AL.get(0).getName()),
                () -> assertEquals(40000001, loaded.AL.get(0).getAccountNumber()),
                () -> assertEquals(1500.0, loaded.AL.get(0).getAmount(), 0.0001)
        );
    }

    @Test
    @DisplayName("save: empty account list still produces a (empty) record file")
    @SpiraTestCase(testCaseId = 48376)
    public void save_emptyAccountList_boundary() {
        Bank bank = new Bank();
        bank.save();

        assertTrue(BANK_FILE.exists());

        Bank loaded = new Bank();
        loaded.load();
        assertTrue(loaded.AL.isEmpty());
    }

    @Test
    @DisplayName("load: a trailing null sentinel stops reading without adding a null entry")
    @SpiraTestCase(testCaseId = 48385)
    public void load_stopsAtNullSentinel() throws Exception {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(BANK_FILE))) {
            out.writeObject(new Account("Vic", 50000001, "5656", 0));
            out.writeObject(null);
        }

        Bank loaded = new Bank();
        loaded.load();

        assertEquals(1, loaded.AL.size());
        assertEquals("Vic", loaded.AL.get(0).getName());
    }

    @Test
    @DisplayName("load: missing record file is handled gracefully, leaving the list empty")
    @SpiraTestCase(testCaseId = 48386)
    public void load_missingFile_handledGracefully() {
        Bank bank = new Bank();
        if (BANK_FILE.exists()) {
            BANK_FILE.delete();
        }

        assertDoesNotThrow(() -> bank.load());

        assertTrue(bank.AL.isEmpty());
    }

    @Test
    @DisplayName("save: an I/O error is caught and handled gracefully")
    @SpiraTestCase(testCaseId = 48387)
    public void save_ioError_handledGracefully() {
        if (BANK_FILE.exists()) {
            BANK_FILE.delete();
        }
        assertTrue(BANK_FILE.mkdir(), "test setup: block the file path with a same-named directory");

        Bank bank = new Bank();
        bank.AL.add(new Account("Wendy", 60000001, "7878", 0));

        String printed = captureOutputOf(() -> assertDoesNotThrow(() -> bank.save()));

        assertAll("save() failure is handled without propagating an exception",
                () -> assertTrue(BANK_FILE.isDirectory(), "blocking directory should be untouched"),
                () -> assertTrue(printed.contains("Error Saving Data"))
        );
    }
}
