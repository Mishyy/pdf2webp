package dev.eradicator.pdf2webp;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;

public enum PdfManipulator {

    SINGLE("Single"),
    DOUBLE("Double") {
        @Override
        public Queue<BufferedImage> collate(final Queue<BufferedImage> pages) {
            final int size = pages.size();
            final Queue<BufferedImage> collated = new ArrayDeque<>(size / 2);
            for (int i = 0; i < size; i += 2) {
                System.out.printf("%d/%d of %d, %d remaining%n", i + 1, i + 2, size, pages.size());
                final BufferedImage image1 = pages.remove(), image2 = pages.remove();
                final int height = Math.max(image1.getHeight(), image2.getHeight());
                final int width = image1.getWidth() + image2.getWidth();

                final BufferedImage page = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                final Graphics2D graphics = page.createGraphics();
                graphics.drawImage(image1, 0, 0, null);
                graphics.drawImage(image2, image1.getWidth(), 0, null);
                graphics.dispose();
                collated.add(page);
            }

            if (!pages.isEmpty()) {
                System.out.println(pages.size());
                collated.addAll(pages);
            }
            return collated;
        }
    },
    DOUBLE_TITLED("Double (with Title Page)") {
        @Override
        public Queue<BufferedImage> collate(final Queue<BufferedImage> pages) {
            final Queue<BufferedImage> collated = new ArrayDeque<>(pages.size() / 2);
            collated.add(pages.poll());
            collated.addAll(DOUBLE.collate(pages));
            return collated;
        }
    };

    private final String name;

    PdfManipulator(final String name) {
        this.name = name;
    }

    public Queue<BufferedImage> convert(final File file) throws IOException {
        final Queue<BufferedImage> images;
        try (final PDDocument document = Loader.loadPDF(file)) {
            images = new ArrayDeque<>(document.getNumberOfPages());
            final PDFRenderer renderer = new PDFRenderer(document);
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                images.add(renderer.renderImageWithDPI(i, 300, ImageType.RGB));
            }
        }
        return images;
    }

    public Queue<BufferedImage> collate(final Queue<BufferedImage> pages) {
        return pages;
    }

    public Queue<File> save(final File file, final File dir, final Queue<BufferedImage> pages) throws Exception {
       final int size = pages.size();
        final Queue<File> files = new ArrayDeque<>(size);
        for (int i = 0; i < size; i++) {
            files.add(Util.toWebp(dir, Util.getFileName(file.getName(), size, i), pages.remove()));
        }
        return files;
    }

    @Override
    public String toString() {
        return name;
    }

}