// ValidationRuleインターフェース
public interface ValidationRule {
    void validate(Object value) throws ValidationException;
}

// ValidationExceptionクラス
public class ValidationException extends Exception {
    private final String errorCode;
    private final ErrorSeverity severity;

    public ValidationException(String errorCode, String message, ErrorSeverity severity) {
        super(message);
        this.errorCode = errorCode;
        this.severity = severity;
    }

    public String getErrorCode() { return errorCode; }
    public ErrorSeverity getSeverity() { return severity; }
}

// ErrorSeverity列挙型
public enum ErrorSeverity {
    INFO, WARNING, CRITICAL
}

// ValidationRuleSetクラス
public class ValidationRuleSet {
    private final Map<String, List<ValidationRule>> fieldRules = new HashMap<>();

    public ValidationRuleSet addRule(String fieldName, ValidationRule rule) {
        fieldRules.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(rule);
        return this;
    }

    public List<String> getFields() {
        return new ArrayList<>(fieldRules.keySet());
    }

    public List<ValidationRule> getRules(String fieldName) {
        return fieldRules.getOrDefault(fieldName, Collections.emptyList());
    }
}

// EmailRule実装
public class EmailRule implements ValidationRule {
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    @Override
    public void validate(Object value) throws ValidationException {
        if (value == null) {
            throw new ValidationException("EMAIL_001", "メールアドレス必須", ErrorSeverity.CRITICAL);
        }
        
        String email = value.toString();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("EMAIL_002", "無効なメール形式: " + email, ErrorSeverity.CRITICAL);
        }
    }
}

// RangeRule実装
public class RangeRule implements ValidationRule {
    private final Number min;
    private final Number max;
    
    public RangeRule(Number min, Number max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public void validate(Object value) throws ValidationException {
        if (value == null) return;
        
        if (!(value instanceof Number)) {
            throw new ValidationException("RANGE_001", "数値型が必要です", ErrorSeverity.CRITICAL);
        }
        
        double num = ((Number)value).doubleValue();
        if (num < min.doubleValue() || num > max.doubleValue()) {
            String msg = String.format("値が範囲外です (%s - %s)", min, max);
            throw new ValidationException("RANGE_002", msg, ErrorSeverity.WARNING);
        }
    }
}

// ComplexityRule実装（パスワード複雑性）
public class ComplexityRule implements ValidationRule {
    private final int minLength;
    private final boolean requireSpecialChar;

    public ComplexityRule(int minLength, boolean requireSpecialChar) {
        this.minLength = minLength;
        this.requireSpecialChar = requireSpecialChar;
    }

    @Override
    public void validate(Object value) throws ValidationException {
        if (value == null) {
            throw new ValidationException("PWD_001", "パスワード必須", ErrorSeverity.CRITICAL);
        }
        
        String password = value.toString();
        if (password.length() < minLength) {
            throw new ValidationException("PWD_002", "パスワードは" + minLength + "文字以上必要です", ErrorSeverity.CRITICAL);
        }
        
        if (requireSpecialChar && !password.matches(".*[!@#$%^&*()].*")) {
            throw new ValidationException("PWD_003", "特殊文字を含めてください", ErrorSeverity.WARNING);
        }
    }
}

// ValidationResultクラス
public class ValidationResult {
    private final List<ValidationError> errors = new ArrayList<>();
    private boolean hasCriticalError = false;

    public void addError(ValidationError error) {
        errors.add(error);
        if (error.getSeverity() == ErrorSeverity.CRITICAL) {
            hasCriticalError = true;
        }
    }

    public boolean isValid() { return errors.isEmpty(); }
    public boolean hasCriticalError() { return hasCriticalError; }
    public List<ValidationError> getErrors() { return Collections.unmodifiableList(errors); }
}

// ValidationErrorクラス
public class ValidationError {
    private final String fieldName;
    private final String errorCode;
    private final String message;
    private final ErrorSeverity severity;
    private final Instant timestamp;

    public ValidationError(String fieldName, String errorCode, String message, ErrorSeverity severity) {
        this.fieldName = fieldName;
        this.errorCode = errorCode;
        this.message = message;
        this.severity = severity;
        this.timestamp = Instant.now();
    }

    // ゲッター
    public String getFieldName() { return fieldName; }
    public String getErrorCode() { return errorCode; }
    public String getMessage() { return message; }
    public ErrorSeverity getSeverity() { return severity; }
    public Instant getTimestamp() { return timestamp; }
}

// ValidationConfigクラス
public class ValidationConfig {
    private int threadPoolSize = Runtime.getRuntime().availableProcessors();
    private boolean stopOnCriticalError = true;
    private boolean persistErrors = false;

    // ゲッター＆セッター
    public int getThreadPoolSize() { return threadPoolSize; }
    public void setThreadPoolSize(int size) { threadPoolSize = size; }
    
    public boolean isStopOnCriticalError() { return stopOnCriticalError; }
    public void setStopOnCriticalError(boolean flag) { stopOnCriticalError = flag; }
    
    public boolean isPersistErrors() { return persistErrors; }
    public void setPersistErrors(boolean flag) { persistErrors = flag; }
}

// ValidationErrorRepositoryインターフェース
public interface ValidationErrorRepository {
    void saveErrors(String dataType, ValidationResult result);
}

// テスト用のシンプルなリポジトリ実装
public class MemoryErrorRepository implements ValidationErrorRepository {
    private final Map<String, List<ValidationError>> errors = new ConcurrentHashMap<>();

    @Override
    public void saveErrors(String dataType, ValidationResult result) {
        errors.computeIfAbsent(dataType, k -> new ArrayList<>())
              .addAll(result.getErrors());
    }

    public Map<String, List<ValidationError>> getErrors() {
        return Collections.unmodifiableMap(errors);
    }
}