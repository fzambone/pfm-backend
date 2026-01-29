package dev.zambone.account.testing;

import dev.zambone.account.domain.AccountLogic;
import dev.zambone.account.domain.AccountRepository;
import dev.zambone.account.domain.CreditCardSettingsLogic;
import dev.zambone.account.domain.CreditCardSettingsRepository;

public class CreditCardSettingsTestRig {

    private final CreditCardSettingsRepository ccRepository =
            new FakeCreditCardSettingsRepository();
    private final CreditCardSettingsFactory ccFactory = new CreditCardSettingsFactory(ccRepository);
    private final CreditCardSettingsLogic ccLogic;

    public CreditCardSettingsTestRig(AccountLogic accountLogic) {
        AccountRepository accountRepository = new FakeAccountRepository();
        this.ccLogic = new CreditCardSettingsLogic(ccRepository, accountLogic);
    }

    //  public CreditCardSettings createPersistedCreditCardSettings() {
    //    return ccFactory.createAndPersistCreditCardSettings();
    //  }

    public CreditCardSettingsLogic logic() {
        return ccLogic;
    }
}
