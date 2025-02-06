Path source = Paths.get("input/data.csv");
Path target = Paths.get("output/processed.csv");

try (Stream<String> stream = Files.lines(source, StandardCharsets.UTF_8);
     BufferedWriter writer = Files.newBufferedWriter(target, StandardOpenOption.CREATE)) {
    
    stream.map(line -> processLine(line))  // 各行を加工
          .forEach(processedLine -> {
              try {
                  writer.write(processedLine);
                  writer.newLine();
              } catch (IOException e) {
                  throw new UncheckedIOException(e);
              }
          });
}