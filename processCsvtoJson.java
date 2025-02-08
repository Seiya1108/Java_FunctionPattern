import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * CSVをJSONに変換するバッチ処理を行うクラス
 */
public class CsvBatchProcessor {

    /**
     * 指定されたCSVファイルを読み込み、JSON形式に変換して出力ファイルに書き込むメソッド。
     *
     * @param inputPath  入力CSVファイルのパス
     * @param outputPath 出力JSONファイルのパス
     * @throws BatchException バッチ処理全般の例外
     */
    public void processCsvToJson(Path inputPath, Path outputPath) throws BatchException {
        // try-with-resources文でBufferedReader, BufferedWriterを確実にクローズ
        try (BufferedReader reader = Files.newBufferedReader(inputPath);
             BufferedWriter writer = Files.newBufferedWriter(outputPath)) {

            // 最初の行をヘッダーとして読み込み、検証する
            String header = reader.readLine();
            validateHeader(header);

            // JSON配列の開始シンボルを書く
            writer.write("[");
            boolean isFirstLine = true;

            // CSVの実データ行を1行ずつ読み込む
            String line;
            while ((line = reader.readLine()) != null) {
                // JSON配列上、要素の区切りにカンマが必要
                // ただし最初の要素の前にはカンマを書かない
                if (!isFirstLine) {
                    writer.write(",");
                }

                // CSVの1行をJSONオブジェクト形式に変換して書き込む
                processLine(line, writer);

                // 最初の行処理が終わったらフラグをfalseにする
                isFirstLine = false;
            }

            // JSON配列の終了シンボルを書く
            writer.write("]");
        } catch (IOException ex) {
            // 例外発生時は独自のBatchExceptionに包んで上位にスロー
            throw new BatchException("バッチ処理失敗", ex);
        }
    }

    /**
     * CSVヘッダーを検証するメソッド。
     * ヘッダーが期待する形式("id,name,price")でない場合はエラーを投げる。
     *
     * @param header CSVのヘッダー行
     * @throws InvalidFormatException 想定外のヘッダー形式の場合に発生
     */
    private void validateHeader(String header) throws InvalidFormatException {
        // ヘッダーが"id,name,price"でない場合はエラー
        if (!"id,name,price".equals(header)) {
            throw new InvalidFormatException("不正なCSVフォーマット: " + header);
        }
    }

    /**
     * CSVの1行をJSON形式に変換して、BufferedWriterに書き込むメソッド。
     *
     * @param line   CSVの1行
     * @param writer JSON出力先
     * @throws IOException 書き込み時のIO例外
     */
    private void processLine(String line, BufferedWriter writer) throws IOException {
        // カンマ区切りで分割
        String[] columns = line.split(",");
        // 列数が3列 (id, name, price) でない場合は処理しない
        if (columns.length != 3) {
            return;
        }

        // id, name, priceをJSONオブジェクトとして書き込む
        // ここではエスケープなど細かい処理は省略している
        String json = String.format(
            "{\"productId\":%s,\"productName\":\"%s\",\"price\":%s}",
            columns[0], columns[1], columns[2]
        );

        // JSONオブジェクトを書き込み、改行
        writer.write(json);
        writer.newLine();
    }
}