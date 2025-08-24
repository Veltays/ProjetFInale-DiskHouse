package DiskHouse.view;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;

/**
 * Vue "Album" (JDialog MODAL, MVC strict)
 * - Preview pochette cliquable (le contrôleur ajoute le listener sur coverLabel)
 * - Champs pour Titre d'album + Date de sortie (texte)
 * - Tableau (JTable) des musiques + boutons Ajouter/Supprimer
 * - Boutons Annuler / Enregistrer
 *
 * Cette vue n'appelle PAS setVisible(true). L'affichage est piloté par le contrôleur.
 */
public class AlbumEditor extends JDialog {

    // ==== Root ====
    private JPanel mainPanel;

    // ==== Header ====
    private JLabel logoLabel;
    private JLabel appTitleLabel;

    // ==== Ligne album ====
    private JLabel coverLabel;           // preview cliquable
    private JTextField albumTitleField;  // titre de l'album
    private JButton editAlbumButton;     // (optionnel) bouton crayon
    private JTextField dateField;        // date de sortie (AAAA-MM-JJ)
    private JLabel dateLabel;
    private JPanel rowPanel;

    // ==== Musiques ====
    private JLabel musiquesLabel;
    private JTable songsTable;           // remplace JList -> demandé
    private JScrollPane songsScroll;
    private JPanel songsPlaceholderPanel;
    // Champs de saisie pour l'ajout de musique
    private JTextField songTitleField;
    private JTextField songDurationField;

    // ==== Actions musiques ====
    private JButton addSongButton;
    private JButton removeSongButton;

    // ==== Bas ====
    private JButton cancelButton;
    private JButton okButton;

    // ==== UI ====
    private static final Dimension COVER_SIZE = new Dimension(120, 120);
    private static final Color BLUE = new Color(0x0E2A62);
    private static final Color PLACEHOLDER = new Color(0x9AAAC3);

    // ==== Wiring ====
    private boolean controllerWired = false;

    // Champ logique simple (si besoin de synchroniser à la sortie de focus)
    private String titreAlbum;

    /* =================== Constructeurs =================== */

    public AlbumEditor(Window owner) { this(owner, true); }

    public AlbumEditor() { this(null, true); }

    /**
     * Constructeur avancé : possibilité de ne pas auto câbler un contrôleur.
     */
    public AlbumEditor(Window owner, boolean autoWireController) {
        super(owner, "DiskHouse - Album", ModalityType.APPLICATION_MODAL);
        $$$setupUI$$$();
        setContentPane(mainPanel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(760, 560));
        setLocationRelativeTo(owner);
        styliser();

        // Placeholder "Ajouter une musique" quand table vide
        refreshSongsPlaceholder();

        // Focus initial : la pochette (cliquable)
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowOpened(java.awt.event.WindowEvent e) {
                SwingUtilities.invokeLater(() -> coverLabel.requestFocusInWindow());
            }
        });

        // synchronise "titreAlbum" quand on quitte le champ
        albumTitleField.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                titreAlbum = albumTitleField.getText();
            }
        });

        initSongFields(); // Initialisation des champs de saisie pour la musique

        if (autoWireController) {
            wireController();
        }
    }

    /* =================== Wiring contrôleur =================== */

    /** A appeler une seule fois (ex: depuis constructeur si autoWire). */
    public void wireController() {
        if (controllerWired) return;
        // Exemple : new AlbumEditorController(this).initController();
        controllerWired = true;
    }

    /**
     * Variante qui retourne le contrôleur si tu veux l'utiliser directement côté appelant.
     * (laisse vide si ton contrôleur est créé ailleurs)
     */
    public Object wireControllerWith() {
        if (controllerWired) return null;
        // Exemple :
        // AlbumEditorController c = new AlbumEditorController(this);
        // c.initController();
        controllerWired = true;
        return null;
    }

    /* =================== Styling & placeholders =================== */

    private void styliser() {
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Header
        appTitleLabel.setText("DiskHouse");
        appTitleLabel.setFont(appTitleLabel.getFont().deriveFont(Font.BOLD, 20f));
        appTitleLabel.setForeground(new Color(0x3B5C8E));

        // Pochette
        coverLabel.setPreferredSize(COVER_SIZE);
        coverLabel.setMinimumSize(COVER_SIZE);
        coverLabel.setOpaque(true);
        coverLabel.setBackground(new Color(0xB3C4E1));
        coverLabel.setBorder(new LineBorder(new Color(0x9AAAC3), 1, false));
        coverLabel.setHorizontalAlignment(SwingConstants.CENTER);
        coverLabel.setVerticalAlignment(SwingConstants.CENTER);
        coverLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        coverLabel.setToolTipText("Choisir une image");
        coverLabel.setFocusable(true);
        setCoverPlaceholder();

        // Titre
        albumTitleField.setBorder(null);
        albumTitleField.setFont(albumTitleField.getFont().deriveFont(Font.BOLD, 24f));
        albumTitleField.setForeground(BLUE);
        albumTitleField.setOpaque(false);
        installTextPlaceholder(albumTitleField, "Titre de l’album");

        // Bouton crayon (optionnel)
        editAlbumButton.setText("✎");
        styliserAction(editAlbumButton);

        // Date
        dateLabel.setText("Date de sortie :");
        dateField.setToolTipText("Date de sortie (AAAA-MM-JJ)");
        installTextPlaceholder(dateField, "AAAA-MM-JJ");

        // Section musiques
        musiquesLabel.setText("Musiques");
        musiquesLabel.setFont(musiquesLabel.getFont().deriveFont(Font.BOLD, 14f));
        musiquesLabel.setForeground(Color.DARK_GRAY);

        // Table des musiques (modèle simple : Titre, Durée, Artiste)
        if (songsTable.getModel() instanceof DefaultTableModel) {
            // déjà configurée dans $$$setupUI$$$
        }

        songsScroll.setBorder(new LineBorder(new Color(0xD0D7E2), 1, true));
        songsScroll.setPreferredSize(new Dimension(540, 220));

        // Placeholder pour la table vide
        songsPlaceholderPanel = new JPanel(new GridBagLayout());
        songsPlaceholderPanel.setBackground(Color.WHITE);
        JLabel ph = new JLabel("Ajouter une musique");
        ph.setForeground(PLACEHOLDER);
        ph.setFont(ph.getFont().deriveFont(Font.ITALIC, 13f));
        songsPlaceholderPanel.add(ph);

        // Actions musiques
        addSongButton.setText("＋");
        styliserAction(addSongButton);
        addSongButton.setToolTipText("Ajouter une musique");

        removeSongButton.setText("🗑");
        styliserAction(removeSongButton);
        removeSongButton.setToolTipText("Supprimer la musique sélectionnée");

        // Bas
        styleGhost(cancelButton, "Annuler");
        stylePrimary(okButton, "Enregistrer");
    }

    private void setCoverPlaceholder() {
        coverLabel.setIcon(null);
        coverLabel.setText("<html><div style='text-align:center;color:#5b6b85;'>Choisir<br>une image</div></html>");
    }

    private void installTextPlaceholder(JTextField field, String phText) {
        Color normal = BLUE;

        Runnable apply = () -> {
            if (field.getText() == null || field.getText().isBlank() || field.getText().equals(phText)) {
                field.setForeground(PLACEHOLDER);
                field.setFont(field.getFont().deriveFont(Font.ITALIC, field.getFont().getSize2D()));
                field.setText(phText);
            }
        };
        Runnable remove = () -> {
            if (PLACEHOLDER.equals(field.getForeground()) && phText.equals(field.getText())) {
                field.setText("");
                field.setFont(field.getFont().deriveFont(Font.PLAIN, field.getFont().getSize2D()));
                field.setForeground(normal);
            }
        };

        apply.run();
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { remove.run(); }
            @Override public void focusLost(FocusEvent e)  { apply.run(); }
        });
    }

    private void refreshSongsPlaceholder() {
        boolean empty = songsTable.getRowCount() == 0;
        songsScroll.setViewportView(empty ? songsPlaceholderPanel : songsTable);
        songsScroll.revalidate();
        songsScroll.repaint();
    }

    private void styliserAction(JButton b) {
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BLUE, 2, true),
                new EmptyBorder(6, 10, 6, 10)));
        b.setForeground(BLUE);
        b.setBackground(Color.WHITE);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void stylePrimary(JButton b, String text) {
        b.setText(text);
        b.setFocusPainted(false);
        b.setBackground(BLUE);
        b.setForeground(Color.WHITE);
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleGhost(JButton b, String text) {
        b.setText(text);
        b.setFocusPainted(false);
        b.setBackground(new Color(0xF5F7FA));
        b.setBorder(new LineBorder(new Color(0xD0D7E2), 1, true));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /* =================== API pour le contrôleur (getters conservés) =================== */

    public JLabel getCoverLabel()                { return coverLabel; }
    public JTextField getAlbumTitleField()       { return albumTitleField; }
    public JButton getEditAlbumButton()          { return editAlbumButton; }
    public JTable getSongsList()                 { return songsTable; }      // <-- même nom, type JTable
    public JScrollPane getSongsScroll()          { return songsScroll; }
    public JButton getAddSongButton()            { return addSongButton; }
    public JButton getRemoveSongButton()         { return removeSongButton; }
    public JButton getCancelButton()             { return cancelButton; }
    public JButton getOkButton()                 { return okButton; }
    public JTextField getDateField()             { return dateField; }

    public JTextField getSongTitleField() { return songTitleField; }
    public JTextField getSongDurationField() { return songDurationField; }

    public void setAlbumDate(String date)        { dateField.setText(date); }
    public String getAlbumDate()                 { return dateField.getText(); }

    /** Utilitaire si besoin de forcer le placeholder quand la table est vidée. */
    public void ensureSongsPlaceholder()         { refreshSongsPlaceholder(); }

    /** Fixe le titre visuellement (et force style non-placeholder). */
    public void setAlbumTitle(String title) {
        if (title == null || title.isBlank()) {
            albumTitleField.setText("");
            albumTitleField.postActionEvent();
        } else {
            albumTitleField.setText(title);
            albumTitleField.setCaretPosition(albumTitleField.getText().length());
            albumTitleField.setForeground(BLUE);
            albumTitleField.setFont(albumTitleField.getFont().deriveFont(Font.BOLD, 24f));
        }
    }

    /** Fixe la pochette à partir d'un chemin ou URL. */
    public void setCoverImage(String path) {
        if (path == null || path.isEmpty()) {
            setCoverPlaceholder();
            return;
        }
        try {
            ImageIcon icon;
            if (path.startsWith("http") || path.startsWith("file:")) {
                java.net.URL url = java.net.URI.create(path).toURL();
                icon = new ImageIcon(url);
            } else {
                icon = new ImageIcon(path);
            }
            Image img = icon.getImage().getScaledInstance(COVER_SIZE.width, COVER_SIZE.height, Image.SCALE_SMOOTH);
            coverLabel.setIcon(new ImageIcon(img));
            coverLabel.setText("");
        } catch (Exception e) {
            setCoverPlaceholder();
        }
    }

    /** Fixe la pochette à partir d'une Image (centrée dans un carré). */
    public void setCoverImage(Image source) {
        if (source == null) { setCoverPlaceholder(); return; }
        int box = COVER_SIZE.width;
        int w = source.getWidth(null), h = source.getHeight(null);
        int tries = 0;
        while ((w <= 0 || h <= 0) && tries < 10) {
            try { Thread.sleep(10); } catch (InterruptedException ignored) {}
            w = source.getWidth(null);
            h = source.getHeight(null);
            tries++;
        }
        if (w <= 0 || h <= 0) { setCoverPlaceholder(); return; }
        double s = Math.min(box / (double) w, box / (double) h);
        int nw = (int) Math.round(w * s);
        int nh = (int) Math.round(h * s);
        Image scaled = source.getScaledInstance(nw, nh, Image.SCALE_SMOOTH);
        BufferedImage canvas = new BufferedImage(box, box, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        g.drawImage(scaled, (box - nw) / 2, (box - nh) / 2, null);
        g.dispose();
        coverLabel.setText(null);
        coverLabel.setIcon(new ImageIcon(canvas));
    }

    /* =================== Helpers layout =================== */

    private JPanel empty() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        return p;
    }

    private JPanel space(int h) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(0, h));
        return p;
    }

    /* =================== Layout (GridLayoutManager) =================== */

    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(14, 3, new Insets(0, 0, 0, 0), 0, 0));

        // Colonnes latérales (marges)
        mainPanel.add(empty(), new GridConstraints(0, 0, 14, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        mainPanel.add(empty(), new GridConstraints(0, 2, 14, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        // Header
        JPanel header = new JPanel(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), 6, 0));
        header.setOpaque(false);
        mainPanel.add(header, new GridConstraints(0, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        logoLabel = new JLabel();
        header.add(logoLabel, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        appTitleLabel = new JLabel("DiskHouse");
        header.add(appTitleLabel, new GridConstraints(0, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        mainPanel.add(space(10), new GridConstraints(1, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // Ligne Album (pochette + champs)
        rowPanel = new JPanel(new GridLayoutManager(2, 6, new Insets(0, 0, 0, 0), 10, 6));
        rowPanel.setOpaque(false);
        mainPanel.add(rowPanel, new GridConstraints(2, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        coverLabel = new JLabel();
        rowPanel.add(coverLabel, new GridConstraints(0, 0, 2, 1,
                GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, COVER_SIZE, COVER_SIZE, 0, false));

        JLabel albumTitleLbl = new JLabel("Titre de l’album :");
        rowPanel.add(albumTitleLbl, new GridConstraints(0, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        albumTitleField = new JTextField();
        rowPanel.add(albumTitleField, new GridConstraints(0, 2, 1, 3,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        editAlbumButton = new JButton();
        rowPanel.add(editAlbumButton, new GridConstraints(0, 5, 1, 1,
                GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        dateLabel = new JLabel("Date de sortie :");
        rowPanel.add(dateLabel, new GridConstraints(1, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        dateField = new JTextField();
        rowPanel.add(dateField, new GridConstraints(1, 2, 1, 2,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        mainPanel.add(space(16), new GridConstraints(3, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // Label "Musiques"
        musiquesLabel = new JLabel("Musiques");
        mainPanel.add(musiquesLabel, new GridConstraints(4, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // Table + Scroll
        songsTable = new JTable(new DefaultTableModel(
                new Object[][]{}, new String[]{"Titre", "Durée", "Artiste"}
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        });
        songsTable.setRowHeight(24);
        songsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        songsScroll = new JScrollPane(songsTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(songsScroll, new GridConstraints(5, 1, 5, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW,
                null, new Dimension(540, 220), null, 0, false));

        // Barre d'actions Ajouter / Supprimer
        JPanel actions = new JPanel(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), 8, 0));
        actions.setOpaque(false);
        addSongButton = new JButton("＋");
        removeSongButton = new JButton("🗑");
        actions.add(addSongButton, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        actions.add(removeSongButton, new GridConstraints(0, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        mainPanel.add(actions, new GridConstraints(10, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        mainPanel.add(space(12), new GridConstraints(11, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // Bas : boutons Annuler / Enregistrer
        JPanel bottom = new JPanel(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), 10, 0));
        bottom.setOpaque(false);
        cancelButton = new JButton("Annuler");
        okButton = new JButton("Enregistrer");
        bottom.add(empty(), new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        bottom.add(cancelButton, new GridConstraints(0, 1, 1, 1,
                GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        bottom.add(okButton, new GridConstraints(0, 2, 1, 1,
                GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        mainPanel.add(bottom, new GridConstraints(12, 1, 1, 1,
                GridConstraints.ANCHOR_EAST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        mainPanel.add(space(8), new GridConstraints(13, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

    // Dans le constructeur ou la méthode d'init UI (après la section musiques)
    private void initSongFields() {
        songTitleField = new JTextField();
        songTitleField.setColumns(20);
        songTitleField.setToolTipText("Titre de la chanson");
        installTextPlaceholder(songTitleField, "Titre de la chanson");
        songDurationField = new JTextField();
        songDurationField.setColumns(6);
        songDurationField.setToolTipText("Durée (mm:ss)");
        installTextPlaceholder(songDurationField, "mm:ss");
        // Ajout dans le layout (exemple simple, à adapter selon le layout manager)
        JPanel songInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        songInputPanel.add(new JLabel("Titre : "));
        songInputPanel.add(songTitleField);
        songInputPanel.add(new JLabel("Durée : "));
        songInputPanel.add(songDurationField);
        // Ajouter ce panel juste avant la table des musiques
        mainPanel.add(songInputPanel, new GridConstraints(9, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }
}
