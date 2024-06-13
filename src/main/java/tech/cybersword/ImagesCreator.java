package tech.cybersword;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ImagesCreator {

    private static final Logger logger = LogManager.getLogger(ImagesCreator.class);

    private Properties properties;

    public ImagesCreator() {
        properties = new Properties();
    }

    public void generateImagesAsJpegPngBmpGifWbmp(String path, boolean useGraphics, byte[] payload,
            String textOnPicture, boolean changeExif, boolean cybersword, boolean useGlitch) {
        int width = 400;
        int height = 400;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        if (useGraphics) {
            Graphics2D graphics = image.createGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.setColor(Color.BLUE);
            graphics.fillOval(50, 50, 100, 100);
            if (cybersword) {
                graphics.setColor(Color.RED);
                graphics.drawString("cybersword.tech", 45, 100);
            }
            graphics.setColor(Color.BLACK);
            graphics.drawString(new Date().toString(), 45, 200);
            graphics.setColor(Color.GREEN);
            graphics.drawString(textOnPicture, 45, 300);

            graphics.dispose();
        }

        // String[] formats = { "jpeg", "png", "bmp", "gif", "wbmp" };
        String[] formats = ImageIO.getWriterFormatNames();

        for (String format : formats) {
            try {
                String preFix = toHexString(getSHA(payload));
                File file = new File(path + System.getProperty("file.separator") + preFix + "_"
                        + UUID.randomUUID().toString() + "." + format);

                String fileName = file.getName();
                properties.setProperty(fileName, getBase64(payload));

                // ImageIO.write(image, format, file);
                saveFile(image, file, format);
                if (logger.isInfoEnabled()) {
                    logger.info(String.format("picture saved as %s", file.getPath()));
                }

                if (file.exists() && useGlitch) {
                    Path pathI = Paths.get(file.getAbsolutePath());

                    byte[] data = Files.readAllBytes(pathI);
                    byte[] extendedData = new byte[data.length + payload.length];

                    System.arraycopy(data, 0, extendedData, 0, data.length);
                    System.arraycopy(payload, 0, extendedData, data.length, payload.length);

                    SecureRandom random = new SecureRandom();
                    int rnd = random.nextInt(data.length - payload.length) - 1;

                    for (int i = 0; i < payload.length; i++) {
                        extendedData[rnd + i] = payload[i];
                    }

                    // Files.write(pathI, extendedData, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

                    if (changeExif) {
                        addExifPayload(pathI, payload);
                        // changeExifMetadata(file, file);
                    }
                }

            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error(String.format("error saving picture as %s: %s", format, e.getMessage()));
                }
                e.printStackTrace();
            }
        }
    }

    // here next
    public void changeExifMetadata(final File jpegImageFile, final File dst)
            throws IOException, ImagingException {

        try (FileOutputStream fos = new FileOutputStream(dst);
                OutputStream os = new BufferedOutputStream(fos)) {

            TiffOutputSet outputSet = null;

            // note that metadata might be null if no metadata is found.
            final ImageMetadata metadata = Imaging.getMetadata(jpegImageFile);
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            if (null != jpegMetadata) {
                // note that exif might be null if no Exif metadata is found.
                final TiffImageMetadata exif = jpegMetadata.getExif();

                if (null != exif) {
                    // TiffImageMetadata class is immutable (read-only).
                    // TiffOutputSet class represents the Exif data to write.
                    //
                    // Usually, we want to update existing Exif metadata by
                    // changing
                    // the values of a few fields, or adding a field.
                    // In these cases, it is easiest to use getOutputSet() to
                    // start with a "copy" of the fields read from the image.
                    outputSet = exif.getOutputSet();
                    outputSet.setGPSInDegrees(Double.MAX_VALUE, Double.MIN_VALUE);
                }
            }

            // if file does not contain any exif metadata, we create an empty
            // set of exif metadata. Otherwise, we keep all of the other
            // existing tags.
            if (null == outputSet) {
                outputSet = new TiffOutputSet();
            }

            {
                // Example of how to add a field/tag to the output set.
                //
                // Note that you should first remove the field/tag if it already
                // exists in this directory, or you may end up with duplicate
                // tags. See above.
                //
                // Certain fields/tags are expected in certain Exif directories;
                // Others can occur in more than one directory (and often have a
                // different meaning in different directories).
                //
                // TagInfo constants often contain a description of what
                // directories are associated with a given tag.
                //
                final TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();
                // make sure to remove old value if present (this method will
                // not fail if the tag does not exist).
                exifDirectory.removeField(ExifTagConstants.EXIF_TAG_APERTURE_VALUE);
                exifDirectory.add(ExifTagConstants.EXIF_TAG_APERTURE_VALUE, new RationalNumber(3, 10));
            }

            {
                outputSet.setGPSInDegrees(Double.MAX_VALUE, Double.MIN_VALUE);
            }

            // printTagValue(jpegMetadata, TiffConstants.TIFF_TAG_DATE_TIME);

            new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os, outputSet);
        }
    }

    private void addExifPayload(Path pathI, byte[] payload) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addExifPayload'");
    }

    public void saveMappingData(String folder, String pentestName) {
        try (FileWriter writer = new FileWriter(folder + "mapping.properties")) {
            properties.store(writer, pentestName);
            if (logger.isInfoEnabled()) {
                logger.info(null != pentestName ? String.format("saved mapping data for %s", pentestName)
                        : "saved mapping data");
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(String.format("error saving mapping data: %s", e.getMessage()));
            }
        } finally {
            properties.clear();
        }
    }

    public void saveFile(BufferedImage image, File file, String format) throws IOException {
        // through Apache Commons Imaging
        // ImageFormat imageFormat = ImageFormat.IMAGE_FORMAT_JPEG;
        // Imaging.writeImage(image, file, format, null);
        // though ImageIO
        ImageIO.write(image, format, file);
    }

    public byte[] getSHA(byte[] payload) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(payload);
        } catch (NoSuchAlgorithmException e) {
            if (logger.isErrorEnabled()) {
                logger.error(String.format("Exception thrown for incorrect algorithm: %s", e));
            }
            e.printStackTrace();
            return new byte[0];
        }
    }

    public String toHexString(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public String getBase64(byte[] str) {
        return java.util.Base64.getEncoder().encodeToString(str);
    }
}
