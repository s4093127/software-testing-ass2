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
public class bankTransferTest {
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
    @DisplayName("transfer: sender account number not found at all -> graceful failure")
    @SpiraTestCase(testCaseId = 48390)
    public void transfer_senderNotFound_noNumberMatch() {
        Bank bank = new Bank();
        bank.AL.add(new Account("Frank", 10000001, "1111", 1000)); // Amount = 2000
        provideInput("99999999\nwrongpin\n");

        assertDoesNotThrow(() -> bank.transfer());

        assertEquals(2000.0, bank.AL.get(0).getAmount(), 0.0001);
    }

    @Test
    @DisplayName("transfer: sender number matches but PIN is wrong")
    @SpiraTestCase(testCaseId = 48392)
    public void transfer_senderNumberMatchesButPinIncorrect() {
        Bank bank = new Bank();
        bank.AL.add(new Account("Frank", 10000001, "1111", 1000)); // Amount = 2000
        provideInput("10000001\nwrongpin\n");

        assertDoesNotThrow(() -> bank.transfer());

        assertEquals(2000.0, bank.AL.get(0).getAmount(), 0.0001);
    }

    @Test
    @DisplayName("transfer: sender found but receiver account number not found")
    @SpiraTestCase(testCaseId = 48393)
    public void transfer_receiverNotFound() {
        Bank bank = new Bank();
        bank.AL.add(new Account("Grace", 10000002, "2222", 500)); // Amount = 1500
        provideInput("10000002\n2222\n99999999\n");

        assertDoesNotThrow(() -> bank.transfer());

        assertEquals(1500.0, bank.AL.get(0).getAmount(), 0.0001);
    }

    @Test
    @DisplayName("transfer: moves funds when sender has sufficient balance")
    @SpiraTestCase(testCaseId = 48394)
    public void transfer_success_sufficientBalance() {
        Bank bank = new Bank();
        Account sender = new Account("Heidi", 10000003, "3333", 1000);  // Amount = 2000
        Account receiver = new Account("Ivan", 10000004, "4444", 0);    // Amount = 1000
        bank.AL.add(sender);
        bank.AL.add(receiver);

        provideInput("10000003\n3333\n10000004\n500\n");
        bank.transfer();

        assertAll("balances after successful transfer",
                () -> assertEquals(1500.0, sender.getAmount(), 0.0001),
                () -> assertEquals(1500.0, receiver.getAmount(), 0.0001)
        );
    }

    @Test
    @DisplayName("transfer: exact boundary where amount equals available balance")
    @SpiraTestCase(testCaseId = 48395)
    public void transfer_boundary_amountEqualsBalance() {
        Bank bank = new Bank();
        Account sender = new Account("Judy", 10000005, "5555", -900);   // Amount = 100
        Account receiver = new Account("Karl", 10000006, "6666", -1000); // Amount = 0
        bank.AL.add(sender);
        bank.AL.add(receiver);

        // transfer amount (100) exactly equals sender's balance (100)
        provideInput("10000005\n5555\n10000006\n100\n");
        bank.transfer();

        assertAll("balances after exact-balance boundary transfer",
                () -> assertEquals(0.0, sender.getAmount(), 0.0001),
                () -> assertEquals(100.0, receiver.getAmount(), 0.0001)
        );
    }

    @Test
    @DisplayName("transfer: rejected when sender has insufficient balance")
    @SpiraTestCase(testCaseId = 48396)
    public void transfer_insufficientBalance() {
        Bank bank = new Bank();
        Account sender = new Account("Leo", 10000007, "7777", -900); // Amount = 100
        Account receiver = new Account("Mona", 10000008, "8888", 0); // Amount = 1000
        bank.AL.add(sender);
        bank.AL.add(receiver);

        provideInput("10000007\n7777\n10000008\n5000\n");
        bank.transfer();

        assertAll("balances unchanged after rejected transfer",
                () -> assertEquals(100.0, sender.getAmount(), 0.0001),
                () -> assertEquals(1000.0, receiver.getAmount(), 0.0001)
        );
    }
}
