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

public class BankWithdrawTest {
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
    @DisplayName("withdraw: account number not found at all")
    @SpiraTestCase(testCaseId = 48401)
    public void withdraw_accountNotFound_noNumberMatch() {
        Bank bank = new Bank();
        bank.AL.add(new Account("Nina", 20000001, "1234", 0)); // Amount = 1000
        provideInput("00000000\nbadpin\n");

        assertDoesNotThrow(() -> bank.withdraw());

        assertEquals(1000.0, bank.AL.get(0).getAmount(), 0.0001);
    }

    @Test
    @DisplayName("withdraw: account number matches but PIN is wrong")
    @SpiraTestCase(testCaseId = 48402)
    public void withdraw_accountNumberMatchesButPinIncorrect() {
        Bank bank = new Bank();
        bank.AL.add(new Account("Nina", 20000001, "1234", 0)); // Amount = 1000
        provideInput("20000001\nbadpin\n");

        assertDoesNotThrow(() -> bank.withdraw());

        assertEquals(1000.0, bank.AL.get(0).getAmount(), 0.0001);
    }

    @Test
    @DisplayName("withdraw: succeeds when sufficient balance is available")
    @SpiraTestCase(testCaseId = 48403)
    public void withdraw_success_sufficientBalance() {
        Bank bank = new Bank();
        Account acc = new Account("Oscar", 20000002, "2468", 500); // Amount = 1500
        bank.AL.add(acc);

        provideInput("20000002\n2468\n1000\n");
        bank.withdraw();

        assertEquals(500.0, acc.getAmount(), 0.0001);
    }

    @Test
    @DisplayName("withdraw: succeeds at the exact amount equals available balance")
    @SpiraTestCase(testCaseId = 48404)
    public void withdraw_boundary_amountEqualsBalance() {
        Bank bank = new Bank();
        Account acc = new Account("Peggy", 20000003, "1357", 0); // Amount = 1000

        bank.AL.add(acc);

        // withdrawal amount (1000) exactly equals the account's balance (1000)
        provideInput("20000003\n1357\n1000\n");
        bank.withdraw();

        assertEquals(0.0, acc.getAmount(), 0.0001);
    }

    @Test
    @DisplayName("withdraw: rejected when balance is insufficient")
    @SpiraTestCase(testCaseId = 48405)
    public void withdraw_insufficientBalance() {
        Bank bank = new Bank();
        Account acc = new Account("Quentin", 20000004, "2589", -900); // Amount = 100
        bank.AL.add(acc);

        provideInput("20000004\n2589\n5000\n");
        bank.withdraw();

        assertEquals(100.0, acc.getAmount(), 0.0001);
    }
}
