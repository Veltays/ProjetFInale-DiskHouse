package DiskHouse.view;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Vue "Ajouter une musique" (SANS contrôleurs / SANS listeners)
 * - La vue ne fait que dessiner et expose des getters + helpers d'affichage
 * - Styles via GridLayoutManager : on n’y touche pas
 */
public class AddMusic extends JFrame {

    // Root
    private JPanel mainPanel;

    // Header
    private JLabel logoLabel;      // icône
    private JLabel titleLabel;     // "DiskHouse"

    // Image preview
    private JLabel imagePreviewLabel;

    // Fields
    private JTextField titreField;
    private JTextField artisteField;
    private JTextField albumField;
    private JTextField dureeField;

    // Constantes UI
    private static final Dimension PREVIEW_SIZE = new Dimension(200, 200);
    private static final Dimension FIELD_SIZE   = new Dimension(440, 56);

    public AddMusic() {
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
        titleLabel.setForeground(new Color(0x3B5C8E));

        // --- IMAGE PREVIEW ---
        imagePreviewLabel.setPreferredSize(PREVIEW_SIZE);
        imagePreviewLabel.setMinimumSize(PREVIEW_SIZE);
        imagePreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imagePreviewLabel.setVerticalAlignment(SwingConstants.CENTER);
        imagePreviewLabel.setOpaque(true);
        imagePreviewLabel.setBackground(new Color(0xB3C4E1));
        imagePreviewLabel.setForeground(new Color(0xE9FFF6));
        imagePreviewLabel.setText("image");
        imagePreviewLabel.setBorder(new LineBorder(new Color(0x9AAAC3), 1, false));

        // --- FIELDS (pas de placeholder ici, c’est le contrôleur) ---
        setupFieldLikeCard(titreField);
        setupFieldLikeCard(artisteField);
        setupFieldLikeCard(albumField);
        setupFieldLikeCard(dureeField);

        pack();
        setLocationRelativeTo(null);
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

    /**
     * Met l’aperçu d’image.
     * Le contrôleur fournit l'image (ex: depuis un JFileChooser), la vue la centre dans 200x200.
     */
    public void setPreviewImage(Image source) {
        if (source == null) {
            imagePreviewLabel.setIcon(null);
            imagePreviewLabel.setText("image");
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
        field.setBackground(new Color(0xE6E6E6));
        field.setOpaque(true);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xDDDDDD), 1, false),
                new EmptyBorder(10, 16, 10, 16)
        ));
    }

    // ===== Getters exposés pour le contrôleur =====
    public JTextField getTitreField()    { return titreField; }
    public JTextField getArtisteField()  { return artisteField; }
    public JTextField getAlbumField()    { return albumField; }
    public JTextField getDureeField()    { return dureeField; }
    public JLabel getImagePreviewLabel() { return imagePreviewLabel; }
    public JLabel getLogoLabel()         { return logoLabel; }

    /* ========= Layout (GridLayoutManager) ========= */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(12, 3, new Insets(0, 0, 0, 0), 0, 0));

        // Colonne gauche vide
        mainPanel.add(new JPanel(){{
            setOpaque(false);
        }}, new GridConstraints(0, 0, 12, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null, 0, false));

        // Colonne droite vide
        mainPanel.add(new JPanel(){{
            setOpaque(false);
        }}, new GridConstraints(0, 2, 12, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null, 0, false));

        // Colonne centrale
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

        // Espace
        mainPanel.add(space(24), new GridConstraints(1, 1, 1, 1,
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
                null, PREVIEW_SIZE, PREVIEW_SIZE, 0, false));
        mainPanel.add(imageRow, new GridConstraints(2, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        // Espace
        mainPanel.add(space(24), new GridConstraints(3, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        // Titre
        titreField = new JTextField();
        mainPanel.add(titreField, new GridConstraints(4, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, FIELD_SIZE, null, 0, false));

        // Espace
        mainPanel.add(space(16), new GridConstraints(5, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        // Artiste
        artisteField = new JTextField();
        mainPanel.add(artisteField, new GridConstraints(6, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, FIELD_SIZE, null, 0, false));

        // Espace
        mainPanel.add(space(16), new GridConstraints(7, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        // Album
        albumField = new JTextField();
        mainPanel.add(albumField, new GridConstraints(8, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, FIELD_SIZE, null, 0, false));

        // Espace
        mainPanel.add(space(16), new GridConstraints(9, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        // Durée
        dureeField = new JTextField();
        mainPanel.add(dureeField, new GridConstraints(10, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, FIELD_SIZE, null, 0, false));

        // Espace final
        mainPanel.add(space(24), new GridConstraints(11, 1, 1, 1,
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AddMusic view = new AddMusic();
            // Logo (optionnel) – la vue ne fait qu’afficher
            view.setLogoFromAbsolutePath(
                    "C:\\Users\\grany\\OneDrive\\HEPL\\BAC2\\Q2\\Programmation orientée objet en Java\\ProjetFInale-DiskHouse\\src\\main\\resources\\LogoMini.png",
                    180, 48
            );

            // === Contrôleur (même logique que Login) ===
            DiskHouse.Controller.AddMusicController controller = new DiskHouse.Controller.AddMusicController(view);
            controller.initController();

            // Éviter le focus automatique dans un champ
            SwingUtilities.invokeLater(() -> view.getRootPane().requestFocusInWindow());

            view.setVisible(true);
        });
    }
}
