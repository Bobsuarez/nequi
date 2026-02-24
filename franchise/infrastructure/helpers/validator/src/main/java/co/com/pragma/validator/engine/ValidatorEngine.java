package co.com.pragma.validator.engine;

import co.com.pragma.model.exceptions.ValidationException;
import co.com.pragma.model.validations.ValidationError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Set;

import static co.com.pragma.validator.constants.ValidatorEngineConstants.FIELD_TRANSLATIONS;

@UtilityClass
public class ValidatorEngine {
    private static final Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    public static <T> void validate(T object) {Set<ConstraintViolation<T>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            throw new ValidationException(
                    violations.stream()
                            .map(v -> {
                                String propertyPath = v.getPropertyPath().toString();
                                String translatedPath = translateFieldPath(propertyPath);
                                return new ValidationError(translatedPath, v.getMessage());
                            })
                            .toList()
            );
        }
    }

    public static String translateFieldPath(final String propertyPath) {
        List<String> pathElements = List.of(propertyPath.split("\\."));

        StringBuilder translatedPath = new StringBuilder();

        for (String element: pathElements) {
            if (FIELD_TRANSLATIONS.containsKey(element)) {
                translatedPath.append(FIELD_TRANSLATIONS.get(element)).append(".");
            } else if (isListElement(element)) {
                String baseName = element.split("\\[")[0];

                String indexPart = element.substring(element.indexOf('['));

                if (FIELD_TRANSLATIONS.containsKey(baseName)) {
                    translatedPath.append(FIELD_TRANSLATIONS.get(baseName)).append(indexPart).append(".");
                } else {
                    translatedPath.append(element).append(".");
                }
            } else {
                translatedPath.append(element).append(".");
            }
        }

        return translatedPath.substring(0, translatedPath.length() - 1);
    }

    private static boolean isListElement(String element) {
        int openBracket = element.indexOf('[');
        int closeBracket = element.indexOf(']');

        if (openBracket > -1 && closeBracket > openBracket) {
            try {
                String indexStr = element.substring(openBracket + 1, closeBracket);
                Integer.parseInt(indexStr);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }
}
