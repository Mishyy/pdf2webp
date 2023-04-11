package dev.eradicator.pdf2webp;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public final class PdfFilter extends FileFilter {

    @Override
    public boolean accept(final File file) {
        return file != null && file.canRead() && file.isFile() && isPdf(file);
    }

    @Override
    public String getDescription() {
        return "PDF files";
    }

    private boolean isPdf(final File file) {
        try {
            return "pdf".equals(Util.getExtension(file.getName()))
                    || "application/pdf".equals(Files.probeContentType(file.toPath()));
        } catch (final IOException e) {
            return false;
        }
    }

}