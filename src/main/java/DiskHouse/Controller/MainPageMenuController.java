package DiskHouse.Controller;

import DiskHouse.model.Utils.ExportCsv;
import DiskHouse.model.Utils.ExportXml;
import DiskHouse.model.Utils.IExport;
import DiskHouse.model.Utils.IImport;
import DiskHouse.model.Utils.ImportCsv;
import DiskHouse.model.Utils.ImportXml;
import javax.swing.*;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;

/**
 * Placeholder pour la logique de menu/barre d’actions globale de MainPage.
 * (Rien pour l’instant – tu pourras y ajouter les actions quand tu auras les items du menu.)
 */
public class MainPageMenuController {
    private final MainPageController root;
    public MainPageMenuController(MainPageController root) { this.root = root; }
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public DateTimeFormatter getDateFormatter() { return dateFormatter; }

    // Ajoute les listeners sur les items du menu pour afficher un dialogue avec le choix
    public void wireMenuListeners() {
        var view = root.getView();
        // Gestion du format de date
        if (view.getDatePatternMenuItem() != null) {
            view.getDatePatternMenuItem().addActionListener(e -> {
                dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                root.refreshMusicTableFromMenu();
            });
        }
        if (view.getDateFullMenuItem() != null) {
            view.getDateFullMenuItem().addActionListener(e -> {
                dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale.FRENCH);
                root.refreshMusicTableFromMenu();
            });
        }
        if (view.getDateLongMenuItem() != null) {
            view.getDateLongMenuItem().addActionListener(e -> {
                dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.FRENCH);
                root.refreshMusicTableFromMenu();
            });
        }
        if (view.getDateMediumMenuItem() != null) {
            view.getDateMediumMenuItem().addActionListener(e -> {
                dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.FRENCH);
                root.refreshMusicTableFromMenu();
            });
        }
        if (view.getDateShortMenuItem() != null) {
            view.getDateShortMenuItem().addActionListener(e -> {
                dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(Locale.FRENCH);
                root.refreshMusicTableFromMenu();
            });
        }
        // Session
        view.getLogoutMenuItem().addActionListener(e -> {
            new DiskHouse.view.Login(); // Affiche la page de login
            root.getView().dispose();  // Ferme la fenêtre principale
        });
        // Export CSV
        view.getExportCsvMenuItem().addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Exporter en CSV");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Fichiers CSV (*.csv)", "csv"));
            int userSelection = fileChooser.showSaveDialog(view);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                String path = fileToSave.getAbsolutePath();
                if (!path.toLowerCase().endsWith(".csv")) {
                    fileToSave = new File(path + ".csv");
                }
                try {
                    new ExportCsv().export(root.getPlaylists(), fileToSave.getAbsolutePath());
                    JOptionPane.showMessageDialog(view, "Export CSV réussi !", "Export", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(view, "Erreur lors de l'export CSV : " + ex.getMessage(), "Erreur Export", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        // Export XML
        view.getExportXmlMenuItem().addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Exporter en XML");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Fichiers XML (*.xml)", "xml"));
            int userSelection = fileChooser.showSaveDialog(view);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                String path = fileToSave.getAbsolutePath();
                if (!path.toLowerCase().endsWith(".xml")) {
                    fileToSave = new File(path + ".xml");
                }
                try {
                    new ExportXml().export(root.getPlaylists(), fileToSave.getAbsolutePath());
                    JOptionPane.showMessageDialog(view, "Export XML réussi !", "Export", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(view, "Erreur lors de l'export XML : " + ex.getMessage(), "Erreur Export", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        // Import CSV
        view.getImportCsvMenuItem().addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Importer un fichier CSV");
            int userSelection = fileChooser.showOpenDialog(view);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToImport = fileChooser.getSelectedFile();
                try {
                    new ImportCsv().importData(fileToImport.getAbsolutePath());
                    JOptionPane.showMessageDialog(view, "Import CSV réussi !", "Import", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(view, "Erreur lors de l'import CSV : " + ex.getMessage(), "Erreur Import", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        // Import XML
        view.getImportXmlMenuItem().addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Importer un fichier XML");
            int userSelection = fileChooser.showOpenDialog(view);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToImport = fileChooser.getSelectedFile();
                try {
                    new ImportXml().importData(fileToImport.getAbsolutePath());
                    JOptionPane.showMessageDialog(view, "Import XML réussi !", "Import", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(view, "Erreur lors de l'import XML : " + ex.getMessage(), "Erreur Import", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        // Couleur
        // Suppression des actions liées au menu Couleur (Noir/Blanc)
    }
    // À compléter plus tard (actions de menu, raccourcis, etc.)
}
