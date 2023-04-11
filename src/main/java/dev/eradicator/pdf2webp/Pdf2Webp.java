package dev.eradicator.pdf2webp;

import net.sprd.image.webp.WebPRegister;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class Pdf2Webp extends JDialog {

    public static final Logger LOGGER = Logger.getLogger("Pdf2Webp");

    private JPanel paneContent;
    private JFileChooser chooserPdf;
    private JLabel fieldFile;
    private JComboBox<PdfManipulator> boxCollator;
    private JButton buttonConvert, buttonCancel;
    private JTextArea paneResults;

    public Pdf2Webp() {
        setContentPane(paneContent);
        setModal(true);
        getRootPane().setDefaultButton(buttonConvert);

        chooserPdf.addChoosableFileFilter(new PdfFilter());
        chooserPdf.addActionListener(event -> {
            if (event.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
                buttonConvert.setEnabled(true);
                getRootPane().setDefaultButton(buttonConvert);

                fieldFile.setText(chooserPdf.getSelectedFile().getPath());
                fieldFile.setVisible(true);
                chooserPdf.setVisible(false);
                pack();
            }
        });

        boxCollator.setModel(new DefaultComboBoxModel<>(PdfManipulator.values()));
        buttonConvert.addActionListener(event -> {
            final JFileChooser chooserDir = new JFileChooser();
            chooserDir.setSelectedFile(chooserPdf.getSelectedFile().getParentFile());
            chooserDir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooserDir.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    boxCollator.setEnabled(false);
                    convert(chooserPdf.getSelectedFile(), chooserDir.getSelectedFile());
                } catch (final Exception e) {
                    Pdf2Webp.LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    paneResults.setText(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n")));
                    paneResults.setCaretColor(Color.RED);
                    paneResults.setVisible(true);
                    buttonConvert.setEnabled(true);
                    pack();
                }
            }
        });
        buttonCancel.addActionListener(event -> dispose());

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent event) {
                dispose();
            }
        });

        paneContent.registerKeyboardAction(
                event -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
    }

    public static void main(final String[] args) {
        WebPRegister.registerImageTypes();

        final Pdf2Webp dialog = new Pdf2Webp();
        dialog.setTitle("PDF to WEBP Converter");
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    private void convert(final File file, final File dir) throws Exception {
        final PdfManipulator manipulator = (PdfManipulator) boxCollator.getModel().getSelectedItem();
        final Queue<File> collatedPages = manipulator.save(file, dir, manipulator.collate(manipulator.convert(file)));
        dispose();

        JOptionPane.showMessageDialog(null, "Saved %d page(s) in %s:\n%s".formatted(
                collatedPages.size(),
                dir.getName(),
                collatedPages.stream().map(File::getName).collect(Collectors.joining("\n\t"))
        ));
    }

}