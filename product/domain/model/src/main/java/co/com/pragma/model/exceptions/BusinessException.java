package co.com.pragma.model.exceptions;

public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String message) {
        super(message);
        this.code = null;
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static final String BRANCH_NOT_SYNCHRONIZED = "BRANCH_NOT_SYNCHRONIZED";
    public static final String PRODUCT_NOT_FOUND = "PRODUCT_NOT_FOUND";
}
