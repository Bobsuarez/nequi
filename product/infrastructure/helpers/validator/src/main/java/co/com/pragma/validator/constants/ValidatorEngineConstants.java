package co.com.pragma.validator.constants;

import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public final class ValidatorEngineConstants {

    public static final String KEY_DATA = "data";
    public static final String KEY_MEANS_PAYMENT = "meansPayment";
    public static final String KEY_BANK_ID = "bankId";

    public static final String VALUE_DATA = "datos";
    public static final String MUST_BE_NUMBER = "debe ser un numero";
    public static final String VALUE_MEANS_PAYMENT = "medioDePago";
    public static final String VALUE_BANK_ID = "bancoId";

    public static final Map<String, String> FIELD_TRANSLATIONS;

    static {
        Map<String, String> translations = new HashMap<>();

        translations.put(KEY_DATA, VALUE_DATA);
        translations.put(KEY_MEANS_PAYMENT, VALUE_MEANS_PAYMENT);
        translations.put(KEY_BANK_ID, VALUE_BANK_ID);

        FIELD_TRANSLATIONS = Collections.unmodifiableMap(translations);
    }
}
