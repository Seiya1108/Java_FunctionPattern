public class Main {
    public static void main(String[] args) {
        ValidationConfig config = new ValidationConfig();
        MemoryErrorRepository errorRepo = new MemoryErrorRepository();
        ValidationEngine engine = new ValidationEngine(config, errorRepo);

        // テストデータ
        Map<String, Object> userData = Map.of(
            "email", "test@example.com",
            "password", "weak",
            "age", 150
        );

        ValidationResult result = engine.validate("user", userData);

        if (!result.isValid()) {
            System.out.println("検証エラーが発生しました:");
            result.getErrors().forEach(error -> 
                System.out.printf("[%s] %s: %s (コード: %s)%n",
                    error.getSeverity(),
                    error.getFieldName(),
                    error.getMessage(),
                    error.getErrorCode()
                )
            );
        }
    }
}