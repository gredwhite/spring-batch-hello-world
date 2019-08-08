package spring.boot.hello.world.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileSplitter {

    public static int splitTextFiles(File bigFile, int maxRows) throws IOException {

        int fileCount = 1;
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(bigFile.getPath()))) {
            String line = null;
            int lineNum = 1;
            Path splitFile = Paths.get(getSplitFileName(bigFile, fileCount));
            Path parent = splitFile.getParent();
            parent.toFile().mkdir();
            BufferedWriter writer = Files.newBufferedWriter(splitFile, StandardOpenOption.CREATE);

            while ((line = reader.readLine()) != null) {

                if (lineNum > maxRows) {
                    writer.close();
                    lineNum = 1;
                    fileCount++;
                    splitFile = Paths.get(getSplitFileName(bigFile, fileCount));
                    writer = Files.newBufferedWriter(splitFile, StandardOpenOption.CREATE);
                }

                writer.append(line);
                writer.newLine();
                lineNum++;
            }
            writer.close();
        }

        return fileCount;
    }

    private static String getSplitFileName(File bigFile, int fileCount) {
        return bigFile.getParent() + "/split/" + fileCount + "split.csv";
    }
}
