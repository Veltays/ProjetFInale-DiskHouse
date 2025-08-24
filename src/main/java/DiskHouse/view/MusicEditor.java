package DiskHouse.view;

import DiskHouse.Controller.MusicEditorController;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Vue "Ajouter une musique" (SANS contrôleurs / SANS listeners)
 * - La vue dessine et expose des getters + helpers (MVC)
 * - GridLayoutManager respecté (UI Designer)
 * - Auto‑wiring du contrôleur via wireController()
 */
public class MusicEditor extends JFrame {

    // Root
    private JPanel mainPanel;

    // Header
    private JLabel logoLabel;      // icône
    private JLabel titleLabel;     // "DiskHouse"

    // Image preview (label cliquable côté contrôleur)
    private JLabel imagePreviewLabel;

    // Champs
    private PlaceholderTextField titreField;
    private JComboBox<String> artisteCombo;
    private JButton addArtisteButton;

    private JComboBox<String> albumCombo;
    private JButton addAlbumButton;

    private PlaceholderTextField dureeField;

    // Constantes UI
    private static final Dimension PREVIEW_SIZE = new Dimension(200, 200);
    private static final Dimension FIELD_SIZE   = new Dimension(440, 56);
    private static final Color BRAND            = new Color(0x3B5C8E);
    private static final Color CARD_BG          = new Color(0xE6E6E6);
    private static final Color CARD_BORDER      = new Color(0xDDDDDD);

    public MusicEditor() {
        super("DiskHouse - Ajouter une musique");
        $$$setupUI$$$();

        setContentPane(mainPanel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setBackground(Color.WHITE);

        // STYLE GLOBAL (strictement visuel)
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        // --- HEADER ---
        titleLabel.setText("DiskHouse");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(BRAND);

        // --- IMAGE (placeholder "Ajouter image") ---
        imagePreviewLabel.setPreferredSize(PREVIEW_SIZE);
        imagePreviewLabel.setMinimumSize(PREVIEW_SIZE);
        imagePreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imagePreviewLabel.setVerticalAlignment(SwingConstants.CENTER);
        imagePreviewLabel.setOpaque(true);
        imagePreviewLabel.setBackground(new Color(0xB3C4E1));
        imagePreviewLabel.setForeground(new Color(255, 255, 255, 230));
        imagePreviewLabel.setText("Ajouter image");
        imagePreviewLabel.setBorder(new LineBorder(new Color(0x9AAAC3), 1, false));

        // --- CHAMPS (purement visuel) ---
        setupFieldLikeCard(titreField);
        setupFieldLikeCard(dureeField);

        setupComboLikeCard(artisteCombo, "Sélectionner un artiste");
        stylePlusButton(addArtisteButton);

        setupComboLikeCard(albumCombo, "Sélectionner un album");
        stylePlusButton(addAlbumButton);

        // --- Auto‑wiring du contrôleur (simple) ---
        wireController();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /** Instancie le contrôleur et branche ses listeners. */
    private void wireController() {
        new MusicEditorController(this).initController();
    }

    // ===== Helpers purement VISUELS (la vue reste passive) =====

    /** Affiche le logo à partir d’un chemin absolu, redimensionné. */
    public void setLogoFromAbsolutePath(String absolutePath, int targetW, int targetH) {
        try {
            ImageIcon icon = new ImageIcon(absolutePath);
            if (icon.getIconWidth() > 0) {
                Image scaled = icon.getImage().getScaledInstance(targetW, targetH, Image.SCALE_SMOOTH);
                logoLabel.setIcon(new ImageIcon(scaled));
                logoLabel.setText(null);
            } else {
                logoLabel.setText("DiskHouse");
            }
        } catch (Exception ex) {
            logoLabel.setText("DiskHouse");
        }
    }

    /** Met l’aperçu d’image (centré dans 200x200). */
    public void setPreviewImage(Image source) {
        if (source == null) {
            imagePreviewLabel.setIcon(null);
            imagePreviewLabel.setText("Ajouter image");
            return;
        }
        int box = PREVIEW_SIZE.width;
        int w = source.getWidth(null), h = source.getHeight(null);
        if (w <= 0 || h <= 0) return;

        double scale = Math.min(box / (double) w, box / (double) h);
        int nw = (int) Math.round(w * scale);
        int nh = (int) Math.round(h * scale);

        Image scaled = source.getScaledInstance(nw, nh, Image.SCALE_SMOOTH);
        BufferedImage canvas = new BufferedImage(box, box, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = canvas.createGraphics();
        g2.setColor(new Color(0xB3C4E1));
        g2.fillRect(0,0,box,box);
        int x = (box - nw) / 2, y = (box - nh) / 2;
        g2.drawImage(scaled, x, y, null);
        g2.dispose();

        imagePreviewLabel.setText(null);
        imagePreviewLabel.setIcon(new ImageIcon(canvas));
    }

    private void setupFieldLikeCard(JTextField field) {
        field.setPreferredSize(FIELD_SIZE);
        field.setMinimumSize(FIELD_SIZE);
        field.setFont(field.getFont().deriveFont(Font.PLAIN, 18f));
        field.setBackground(CARD_BG);
        field.setOpaque(true);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(CARD_BORDER, 1, false),
                new EmptyBorder(10, 16, 10, 16)
        ));
    }

    /** ComboBox<String> avec placeholder. */
    private void setupComboLikeCard(JComboBox<String> combo, String placeholder) {
        combo.setFont(combo.getFont().deriveFont(Font.PLAIN, 18f));
        combo.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        combo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(CARD_BORDER, 1, false),
                new EmptyBorder(6, 12, 6, 12)
        ));
        combo.setBackground(Color.WHITE);

        combo.setSelectedItem(null);
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null && index == -1) {
                    c.setText(placeholder);
                    c.setForeground(new Color(0,0,0,90));
                }
                return c;
            }
        });
    }

    private void stylePlusButton(JButton b) {
        b.setText("＋");
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BRAND, 2, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        b.setForeground(BRAND);
        b.setBackground(Color.WHITE);
        b.setContentAreaFilled(true);
        b.setOpaque(true);
    }

    // ===== Getters exposés pour le contrôleur =====
    public JLabel getImagePreviewLabel()       { return imagePreviewLabel; }
    public PlaceholderTextField getTitreField() { return titreField; }
    public PlaceholderTextField getDureeField() { return dureeField; }
    public JComboBox<String> getArtisteCombo() { return artisteCombo; }
    public JButton getAddArtisteButton()       { return addArtisteButton; }
    public JComboBox<String> getAlbumCombo()   { return albumCombo; }
    public JButton getAddAlbumButton()         { return addAlbumButton; }
    public JLabel getLogoLabel()               { return logoLabel; }

    /* ========= Layout (GridLayoutManager) ========= */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(16, 3, new Insets(0, 0, 0, 0), 0, 0));

        // colonnes latérales (respiration)
        mainPanel.add(new JPanel() {{ setOpaque(false); }},
                new GridConstraints(0, 0, 16, 1,
                        GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW,
                        null, null, null, 0, false));

        mainPanel.add(new JPanel() {{ setOpaque(false); }},
                new GridConstraints(0, 2, 16, 1,
                        GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW,
                        null, null, null, 0, false));

        // HEADER (logo + titre)
        JPanel header = new JPanel(new GridLayoutManager(1, 2, new Insets(0,0,0,0), 8, 0));
        header.setOpaque(false);
        mainPanel.add(header, new GridConstraints(0, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        logoLabel = new JLabel();
        header.add(logoLabel, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        titleLabel = new JLabel("DiskHouse");
        header.add(titleLabel, new GridConstraints(0, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        // espace
        mainPanel.add(space(16), new GridConstraints(1, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        // Image
        JPanel imageRow = new JPanel(new GridLayoutManager(1,1, new Insets(0,0,0,0), 0, 0));
        imageRow.setOpaque(false);
        imagePreviewLabel = new JLabel();
        imageRow.add(imagePreviewLabel, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                PREVIEW_SIZE, PREVIEW_SIZE, null, 0, false));
        mainPanel.add(imageRow, new GridConstraints(2, 1, 2, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        // espace
        mainPanel.add(space(16), new GridConstraints(4, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        // Titre
        titreField = new PlaceholderTextField("Titre");
        mainPanel.add(titreField, new GridConstraints(5, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, FIELD_SIZE, null, 0, false));

        // espace
        mainPanel.add(space(12), new GridConstraints(6, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        // Artiste (combo + +)
        JPanel artisteRow = new JPanel(new GridLayoutManager(1, 5, new Insets(0,0,0,0), 8, 0));
        artisteRow.setOpaque(false);
        artisteCombo = new JComboBox<>(new DefaultComboBoxModel<>());
        addArtisteButton = new JButton();

        artisteRow.add(artisteCombo, new com.intellij.uiDesigner.core.GridConstraints(
                0, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, FIELD_SIZE, null, 0, false));

        artisteRow.add(addArtisteButton, new com.intellij.uiDesigner.core.GridConstraints(
                0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        mainPanel.add(artisteRow, new GridConstraints(7, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        // espace
        mainPanel.add(space(12), new GridConstraints(8, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        // Album (combo + +)
        JPanel albumRow = new JPanel(new GridLayoutManager(1, 5, new Insets(0,0,0,0), 8, 0));
        albumRow.setOpaque(false);
        albumCombo = new JComboBox<>(new DefaultComboBoxModel<>());
        addAlbumButton = new JButton();

        albumRow.add(albumCombo, new com.intellij.uiDesigner.core.GridConstraints(
                0, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, FIELD_SIZE, null, 0, false));

        albumRow.add(addAlbumButton, new com.intellij.uiDesigner.core.GridConstraints(
                0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        mainPanel.add(albumRow, new GridConstraints(9, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        // espace
        mainPanel.add(space(12), new GridConstraints(10, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        // Durée
        dureeField = new PlaceholderTextField("Durée (mm:ss)");
        mainPanel.add(dureeField, new GridConstraints(11, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, FIELD_SIZE, null, 0, false));

        // espace bas
        mainPanel.add(space(16), new GridConstraints(12, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));
    }

    private JPanel space(int h) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(0, h));
        return p;
    }

    public JComponent $$$getRootComponent$$$() { return mainPanel; }

    /* ===================== Classes utilitaires (placeholders) ===================== */

    /** TextField qui dessine un placeholder quand il est vide. */
    public static class PlaceholderTextField extends JTextField {
        private String hint;
        public PlaceholderTextField(String hint) {
            super();
            this.hint = hint;
            setColumns(1);
        }
        public void setHint(String hint) { this.hint = hint; repaint(); }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && hint != null && !hint.isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0,0,0,90));
                g2.setFont(getFont().deriveFont(Font.PLAIN, getFont().getSize2D()));
                Insets ins = getInsets();
                g2.drawString(hint, ins.left, getHeight()/2f + g2.getFontMetrics().getAscent()/2f - 4);
                g2.dispose();
            }
        }
    }

    /* ===================== Main de test (optionnel) ===================== */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MusicEditor::new); // new AddMusic() suffit
    }
}
