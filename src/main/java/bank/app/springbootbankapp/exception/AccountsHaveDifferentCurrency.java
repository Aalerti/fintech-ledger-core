package bank.app.springbootbankapp.exception;

public class AccountsHaveDifferentCurrency extends RuntimeException {
    public AccountsHaveDifferentCurrency(String message) {
        super(message);
    }
}
