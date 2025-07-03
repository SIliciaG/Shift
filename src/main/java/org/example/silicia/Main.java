package org.example.silicia;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class Main {

    private static final String DEFAULT_INT_FILE = "integers.txt";
    private static final String DEFAULT_FLOAT_FILE = "floats.txt";
    private static final String DEFAULT_STRING_FILE = "strings.txt";

    public static void main(String[] args) {

        // Парсинг аргументов командной строки
        List<String> inputFiles = new ArrayList<>();
        String outputPath = "";
        String prefix = "";
        boolean appendMode = false;
        boolean shortStats = false;
        boolean fullStats = false;

        try {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "-o":
                        if (i + 1 >= args.length)
                            throw new IllegalArgumentException("Опция -o требует аргумент");
                        outputPath = args[++i];
                        break;
                    case "-p":
                        if (i + 1 >= args.length)
                            throw new IllegalArgumentException("Опция -p требует аргумент");
                        prefix = args[++i];
                        break;
                    case "-a":
                        appendMode = true;
                        break;
                    case "-s":
                        shortStats = true;
                        break;
                    case "-f":
                        fullStats = true;
                        break;
                    default:
                        if (args[i].startsWith("-")) {
                            throw new IllegalArgumentException("Неизвестная опция: " + args[i]);
                        } else {
                            inputFiles.add(args[i]);
                        }
                }
            }

            if (!shortStats && !fullStats) {
                // По умолчанию краткая статистика
                shortStats = true;
            }
            if (shortStats && fullStats) {
                // ПОлная статистика в приоритете
                shortStats = false;
            }

            if (inputFiles.isEmpty()) {
                System.err.println("Ошибка: не указаны входные файлы");
                printUsage();
                return;
            }

            // Проверка существования входных файлов
            // Чтение данных по очереди из входных файлов
            List<String> allLines = new ArrayList<>();

            for (String fileName : inputFiles) {
                Path filePath = Paths.get(fileName);
                if (!Files.exists(filePath)) {
                    System.err.printf("Предупреждение: файл %s не найден, пропускается%n", fileName);
                    continue;
                }
                try (BufferedReader br = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        allLines.add(line.trim());
                    }
                } catch (IOException e) {
                    System.err.printf("Ошибка чтения файла %s: %s%n", fileName, e.getMessage());

                }
            }

            // отсутствие данных после чтения файлов -> сообщение+завершение
            if (allLines.isEmpty()) {
                System.err.println("Нет данных для обработки.");
                return;
            }

            // Объекты статистики и буферы для записи данных по типам
            IntegerStatistics integerStats = new IntegerStatistics();
            FloatStatistics floatStats = new FloatStatistics();
            StringStatistics stringStats = new StringStatistics();

            List<String> integersData = new ArrayList<>();
            List<String> floatsData = new ArrayList<>();
            List<String> stringsData = new ArrayList<>();

            for (String line : allLines) {
                if (line.isEmpty()) continue;

                DataType type = detectType(line);

                switch (type) {
                    case INTEGER -> {
                        integerStats.add(line);
                        integersData.add(line);
                    }
                    case FLOAT -> {
                        floatStats.add(line);
                        floatsData.add(line);
                    }
                    case STRING -> {
                        stringStats.add(line);
                        stringsData.add(line);
                    }
                }
            }

            // Пути для выходных файлов с учетом опций -o и -p
            Path outDirPath;

            if (!outputPath.isEmpty()) {
                outDirPath = Paths.get(outputPath);
                try {
                    Files.createDirectories(outDirPath);
                } catch (IOException e) {
                    System.err.printf("Ошибка создания каталога вывода %s: %s%n", outputPath, e.getMessage());
                    return;
                }
            } else {
                outDirPath = Paths.get(".");
            }

            Map<DataType, Path> outputFilesMap = new HashMap<>();

            if (!integersData.isEmpty()) {
                outputFilesMap.put(DataType.INTEGER,
                        outDirPath.resolve(prefix + DEFAULT_INT_FILE));
            }
            if (!floatsData.isEmpty()) {
                outputFilesMap.put(DataType.FLOAT,
                        outDirPath.resolve(prefix + DEFAULT_FLOAT_FILE));
            }
            if (!stringsData.isEmpty()) {
                outputFilesMap.put(DataType.STRING,
                        outDirPath.resolve(prefix + DEFAULT_STRING_FILE));
            }

            // Запись данных в файлы по типам

            for (var entry : outputFilesMap.entrySet()) {
                DataType type = entry.getKey();
                Path path = entry.getValue();

                List<String> dataToWrite;
                switch (type) {
                    case INTEGER -> dataToWrite = integersData;
                    case FLOAT -> dataToWrite = floatsData;
                    case STRING -> dataToWrite = stringsData;
                    default -> dataToWrite = Collections.emptyList();
                }

                OpenOption openOption = appendMode ? StandardOpenOption.APPEND : StandardOpenOption.CREATE;

                try (BufferedWriter writer = Files.newBufferedWriter(
                        path,
                        StandardCharsets.UTF_8,
                        openOption,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE)) {

                    for (String s : dataToWrite) {
                        writer.write(s);
                        writer.newLine();
                    }

                } catch (IOException e) {
                    System.err.printf("Ошибка записи в файл %s: %s%n", path, e.getMessage());

                }
            }

            // Вывод статистики
            boolean isFullStat = fullStats;

            integerStats.print(isFullStat);
            floatStats.print(isFullStat);
            stringStats.print(isFullStat);

        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка аргументов: " + e.getMessage());
            printUsage();
        } catch (Exception e) {
            System.err.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    private static void printUsage() {
        System.out.println("""
                Утилита фильтрации содержимого файлов.
                
                Инструкция:
                
                 java -jar shift.jar file1.txt [file2.txt ...] опции(-o, -p, -a, -f)
                 
                  Опции:
                  -o PATH   путь к каталогу для выходных файлов (по умолчанию текущий каталог)
                  -p PREFIX префикс имен выходных файлов (по умолчанию пустой)
                  -a       добавлять в существующие файлы вместо перезаписи
                  -s       краткая статистика ( выводится по умолчанию)
                  -f       полная статистика
                
                 Пример:
                  java -jar shift.jar -s -a -p prefix 
                  (путь)in1.txt (путь)in2.txt -o (путь к каталогу выходных файлов)  
                  
                  
                
                 """);
    }


    //Определение типа данных строки:

    private static DataType detectType(String line) {

        // Проверка на целое число (+ знак, цифры, большие числа)
        try {
            new java.math.BigInteger(line);
            return DataType.INTEGER;
        } catch (NumberFormatException ignored) {}

        // Проверка float/double (+экспоненциальная форма)
        try {
            Double.parseDouble(line);
            return DataType.FLOAT;
        } catch (NumberFormatException ignored) {}

        return DataType.STRING;
    }
}




