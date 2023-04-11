package dev.eradicator.pdf2webp;

import net.sprd.image.webp.WebPWriteParam;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;

public final class Util {

    public static String getExtension(final String name) {
        final int i = name.lastIndexOf('.');
        return i > 0 && i < (name.length() - 1) ? name.substring(i + 1) : null;
    }

    public static String getFileName(final String name, final int size, final int page) {
        final StringBuilder builder = new StringBuilder(stripExtension(name));
        if (size > 1) {
            builder.append("-").append(page + 1);
        }
        return builder.append(".webp").toString();
    }

    public static File toWebp(final File dir, final String name, final BufferedImage image) throws IOException {
        final ImageWriter writer = ImageIO.getImageWritersByMIMEType("image/webp").next();
        final ImageWriteParam param = writer.getDefaultWriteParam();
        final WebPWriteParam writeParam = (WebPWriteParam) param;
        writeParam.setCompressionType(WebPWriteParam.LOSSLESS);

        final File file = new File(dir, name);
        try (final ImageOutputStream stream = ImageIO.createImageOutputStream(file)) {
            writer.setOutput(stream);
            writer.write(null, new IIOImage(image, null, null), writeParam);
        }
        return file;
    }

    private static String stripExtension(final String name) {
        final int i = name.lastIndexOf('.');
        return i > 0 && i < (name.length() - 1) ? name.substring(0, i) : name;
    }

}