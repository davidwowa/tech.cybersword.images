package tech.cybersword;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App {

    private static final Logger logger = LogManager.getLogger(App.class);

    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println(
                    "Usage: java -jar tech.cybersword.images-*.jar <test name> <target path> <source path> <file endings> <setExif true|false> <set cybersword true|false> <use glitch true|false>");
            System.exit(1);
        }

        String testName = args[0];
        String targetPath = args[1];
        String sourcePath = args[2];
        String fileEndings = args[3];
        Boolean setExif = Boolean.parseBoolean(args[4]);
        Boolean setCybersword = Boolean.parseBoolean(args[5]);
        Boolean useGlitch = Boolean.parseBoolean(args[6]);

        if (logger.isInfoEnabled()) {
            logger.info(String.format("start images creator for %s", testName));
        }

        DirectoryReader reader = new DirectoryReader(sourcePath, fileEndings);
        Map<String, String> payloads = reader.readTxtFilesToHashMap();

        ImagesCreator generator = new ImagesCreator();

        SecureRandom random = new SecureRandom();

        if (payloads != null && payloads.size() > 0) {
            Set<String> keys = payloads.keySet();
            for (String key : keys) {
                if (null != key && key.length() > 0) {

                    String rndStr = new String(generateRandomArray(random.nextInt(10) + 1));
                    String toAdd = payloads.get(key).concat(rndStr);

                    if (logger.isInfoEnabled()) {
                        logger.info(String.format("%s -> %s", key, toAdd));
                    }

                    generator.generateImagesAsJpegPngBmpGifWbmp(targetPath, true,
                            toAdd.getBytes(), testName, setExif, setCybersword, useGlitch);
                }
            }
        }

        generator.saveMappingData(targetPath, testName);

        if (logger.isInfoEnabled()) {
            logger.info("end images creator");
        }
    }

    public static byte[] generateRandomArray(int size) {
        byte[] randomBytes = new byte[size];

        SecureRandom random = new SecureRandom();

        random.nextBytes(randomBytes);

        // for (byte b : randomBytes) {
        // System.out.printf("%02x ", b);
        // }

        return randomBytes;
    }
}
