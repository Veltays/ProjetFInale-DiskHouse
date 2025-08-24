package DiskHouse.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainPage extends JFrame {
    private JPanel MainWindow;
    private JTable tablePlaylist;
    private JTable TableMusicInPlaylistSelected;
    private JPanel InfoBouton;
    private JPanel Musique;
    private JPanel ListePlaylist;
    private JPanel ProfilPlaylist;
    private JPanel PlaylistNamePanel;
    private JPanel ButtonNamePanel;
    private JButton AjouterMusique;
    private JButton SupprimerMusique;
    private JButton ModifierMusique;
    private JLabel PhotoPlaylist;
    private JButton AjouterPlaylist;
    private JButton SupprimerPlaylist;
    private JButton ModifierPlaylist;
    private JComboBox<String> dateTypeComboBox;

    // === Menu ===
    private JMenuItem logoutMenuItem;
    private JMenuItem exportCsvMenuItem;
    private JMenuItem exportXmlMenuItem;

    // === Menu Date ===
    private JMenuItem datePatternMenuItem;
    private JMenuItem dateFullMenuItem;
    private JMenuItem dateLongMenuItem;
    private JMenuItem dateMediumMenuItem;
    private JMenuItem dateShortMenuItem;

    // === Menu Import ===
    private JMenuItem importCsvMenuItem;
    private JMenuItem importXmlMenuItem;

    public MainPage() {
        setTitle("DiskHouse");
        setContentPane(MainWindow);
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Rendre les JTable scrollables et la table playlist moins haute pour laisser la place aux boutons
        JScrollPane scrollPlaylist = new JScrollPane(tablePlaylist);
        scrollPlaylist.setPreferredSize(new Dimension(200, 300)); // Largeur 200px, hauteur 300px max
        scrollPlaylist.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        ListePlaylist.setLayout(new BoxLayout(ListePlaylist, BoxLayout.Y_AXIS));
        ListePlaylist.removeAll();
        ListePlaylist.add(scrollPlaylist);
        ListePlaylist.add(ButtonNamePanel);

        JScrollPane scrollMusic = new JScrollPane(TableMusicInPlaylistSelected);
        scrollMusic.setPreferredSize(new Dimension(1000, 800)); // Hauteur augmentée à 800px
        scrollMusic.setMaximumSize(new Dimension(Integer.MAX_VALUE, 800));
        Musique.setLayout(new BoxLayout(Musique, BoxLayout.Y_AXIS));

        // Panel header : titre à gauche, photo à droite
        JPanel panelHeaderMusique = new JPanel(new BorderLayout());
        if (PlaylistNamePanel != null) panelHeaderMusique.add(PlaylistNamePanel, BorderLayout.CENTER);
        if (PhotoPlaylist != null) panelHeaderMusique.add(PhotoPlaylist, BorderLayout.EAST);

        // Panel horizontal pour les boutons
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        if (AjouterMusique != null) panelBoutons.add(AjouterMusique);
        if (ModifierMusique != null) panelBoutons.add(ModifierMusique);
        if (SupprimerMusique != null) panelBoutons.add(SupprimerMusique);

        // Panel vertical pour header + boutons
        JPanel panelTopMusique = new JPanel();
        panelTopMusique.setLayout(new BoxLayout(panelTopMusique, BoxLayout.Y_AXIS));
        if (panelHeaderMusique != null) panelTopMusique.add(panelHeaderMusique);
        panelTopMusique.add(panelBoutons);

        if (panelTopMusique != null) Musique.add(panelTopMusique);
        if (scrollMusic != null) Musique.add(scrollMusic);
        if (dateTypeComboBox != null) Musique.add(dateTypeComboBox);

        styliserBouton(AjouterMusique);
        styliserBouton(SupprimerMusique);
        styliserBouton(ModifierMusique);
        styliserBouton(AjouterPlaylist);
        styliserBouton(SupprimerPlaylist);
        styliserBouton(ModifierPlaylist);

        // ===== Barre de menu =====
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // Menu Session
        JMenu sessionMenu = new JMenu("Session");
        menuBar.add(sessionMenu);
        logoutMenuItem = new JMenuItem("Logout");
        sessionMenu.add(logoutMenuItem);

        // Menu Export
        JMenu exportMenu = new JMenu("Export");
        menuBar.add(exportMenu);
        exportCsvMenuItem = new JMenuItem("CSV");
        exportXmlMenuItem = new JMenuItem("XML");
        exportMenu.add(exportCsvMenuItem);
        exportMenu.add(exportXmlMenuItem);

        // Menu Format Date
        JMenu dateMenu = new JMenu("Format Date");
        menuBar.add(dateMenu);
        datePatternMenuItem = new JMenuItem("Pattern (21/02/2024)");
        dateFullMenuItem = new JMenuItem("FULL (mercredi 21 février 2024)");
        dateLongMenuItem = new JMenuItem("LONG (21 février 2024)");
        dateMediumMenuItem = new JMenuItem("MEDIUM (21 févr. 2024)");
        dateShortMenuItem = new JMenuItem("SHORT (21/02/24)");

        dateMenu.add(datePatternMenuItem);
        dateMenu.add(dateFullMenuItem);
        dateMenu.add(dateLongMenuItem);
        dateMenu.add(dateMediumMenuItem);
        dateMenu.add(dateShortMenuItem);

        // Menu Import
        JMenu importMenu = new JMenu("Import");
        menuBar.add(importMenu);
        importCsvMenuItem = new JMenuItem("CSV");
        importXmlMenuItem = new JMenuItem("XML");
        importMenu.add(importCsvMenuItem);
        importMenu.add(importXmlMenuItem);

        // ❌ Pas de logique métier ici → le contrôleur branchera les listeners
    }

    private void styliserBouton(JButton b) {
        if (b == null) return;
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /* =============================
       === Getters pour MVC ========
       ============================= */

    public JTable getTablePlaylist() {
        return tablePlaylist != null ? tablePlaylist : TableMusicInPlaylistSelected;
    }

    public JTable getTableMusicInPlaylistSelected() {
        return TableMusicInPlaylistSelected;
    }

    public JButton getAjouterPlaylistButton() { return AjouterPlaylist; }
    public JButton getSupprimerPlaylistButton() { return SupprimerPlaylist; }
    public JButton getModifierPlaylistButton() { return ModifierPlaylist; }

    public JLabel getPhotoPlaylistLabel() { return PhotoPlaylist; }

    public JButton getAjouterMusiqueButton() { return AjouterMusique; }
    public JButton getSupprimerMusiqueButton() { return SupprimerMusique; }
    public JButton getModifierMusiqueButton() { return ModifierMusique; }

    // Getters Menu Session / Export
    public JMenuItem getLogoutMenuItem() { return logoutMenuItem; }
    public JMenuItem getExportCsvMenuItem() { return exportCsvMenuItem; }
    public JMenuItem getExportXmlMenuItem() { return exportXmlMenuItem; }

    // Getters Menu Date
    public JMenuItem getDatePatternMenuItem() { return datePatternMenuItem; }
    public JMenuItem getDateFullMenuItem() { return dateFullMenuItem; }
    public JMenuItem getDateLongMenuItem() { return dateLongMenuItem; }
    public JMenuItem getDateMediumMenuItem() { return dateMediumMenuItem; }
    public JMenuItem getDateShortMenuItem() { return dateShortMenuItem; }

    // Getters Menu Import
    public JMenuItem getImportCsvMenuItem() { return importCsvMenuItem; }
    public JMenuItem getImportXmlMenuItem() { return importXmlMenuItem; }

    public JComboBox<String> getDateTypeComboBox() {
        return dateTypeComboBox;
    }
}
