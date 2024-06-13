package tech.cybersword;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DirectoryReader {

    private static final Logger logger = LogManager.getLogger(DirectoryReader.class);

    private String directoryPath;
    private String ends;

    public DirectoryReader(String path, String filesEnds) {
        directoryPath = path;
        // .txt
        ends = filesEnds;
    }

    public Map<String, String> readTxtFilesToHashMap() {
        Map<String, String> lineMap = new HashMap<>();

        try {
            Files.list(Paths.get(directoryPath))
                    .filter(path -> path.toString().endsWith(ends))
                    .forEach(path -> {
                        try {
                            List<String> lines = Files.readAllLines(path);

                            for (String line : lines) {
                                String sha256 = calculateSHA256(line);

                                if (sha256 != null) {
                                    lineMap.put(sha256, line);
                                }
                            }
                        } catch (IOException e) {
                            if (logger.isErrorEnabled()) {
                                logger.error(
                                        String.format("error on file read: %s -> %s", path, e.getMessage()));
                            }
                            System.err.println(
                                    String.format("error on file read: %s -> %s", path, e.getMessage()));
                        }
                    });

        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error(String.format("error on directory access: %s", e.getMessage()));
            }
            System.err.println(String.format("error on directory access: %s", e.getMessage()));
            return new HashMap<>();
        }

        return lineMap;
    }

    public String calculateSHA256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            if (logger.isErrorEnabled()) {
                logger.error(String.format("error by calculating SHA-256-hash: %s", e.getMessage()));
            }
            System.err.println(String.format("error by calculating SHA-256-hash: %s", e.getMessage()));
            return null;
        }
    }
}
