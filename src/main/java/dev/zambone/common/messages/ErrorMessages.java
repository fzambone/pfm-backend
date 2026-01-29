package dev.zambone.common.messages;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

public class ErrorMessages {

  private ErrorMessages() {
  }

  // CreditCardSettings errors
  public static final String CREDIT_CARD_SETTINGS_INVALID_ACCOUNT_TYPE =
      "Credit card settings can only be created for CREDIT CARD accounts";

  public static final String CREDIT_CARD_SETTINGS_INVALID_CLOSING_DAY =
      "Closing day must be between 1 and 31";

  public static final String CREDIT_CARD_SETTINGS_INVALID_DUE_DAY =
      "Due day must be between 1 and 31";

  public static final String CREDIT_CARD_SETTINGS_DUPLICATED_ENTRY =
      "Credit card settings already exist for this account";

  public static final String CONCURRENT_DATA_MODIFIED = "Data was modified by another user";

  public static String creditCardSettingsNotFound(UUID accountId) {
    return "Credit card settings not found for Account [id=%s]".formatted(
        accountId);
  }

  public static String accountNotFound(UUID accountId) {
    return "Account not found for [id=%s]".formatted(accountId);
  }
}
