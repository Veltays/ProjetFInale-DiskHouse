package DiskHouse.view;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Vue "Artiste" (SANS écouteurs / SANS logique)
 * - Titre artiste + bouton ✎ (contrôleur gère)
 * - Liste d'albums (JList<AlbumRow>) rendue comme carte (cover + textes + nb titres)
 * - Boutons ajouter / supprimer album (contrôleur gère)
 */
public class ArtisteView extends JFrame {

    // Root
    private JPanel mainPanel;

    // Header appli
    private JLabel logoLabel;
    private JLabel appTitleLabel;

    // Bandeau artiste
    private JLabel artistNameLabel;
    private JButton editArtistButton;  // ✎

    // Titre section
    private JLabel albumsTitleLabel;

    // Liste d'albums
    private JList<AlbumRow> albumList;
    private JScrollPane albumScroll;

    // Actions
    private JButton addAlbumButton;     // +
    private JButton removeAlbumButton;  // 🗑

    // Constantes UI
    private static final Color BLUE = new Color(0x0E2A62);

    public ArtisteView() {
        super("DiskHouse - Artiste");
        $$$setupUI$$$();

        setContentPane(mainPanel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Styles globaux (purement visuels)
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Header app
        appTitleLabel.setText("DiskHouse");
        appTitleLabel.setFont(appTitleLabel.getFont().deriveFont(Font.BOLD, 18f));
        appTitleLabel.setForeground(new Color(0x3B5C8E));

        // Titre artiste
        artistNameLabel.setText("Nom Prénom");
        artistNameLabel.setForeground(BLUE);
        artistNameLabel.setFont(artistNameLabel.getFont().deriveFont(Font.BOLD, 48f));

        editArtistButton.setText("✎");
        editArtistButton.setFocusPainted(false);
        editArtistButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BLUE, 2, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        editArtistButton.setForeground(BLUE);
        editArtistButton.setBackground(Color.WHITE);

        // Titre "Album"
        albumsTitleLabel.setText("Album");
        albumsTitleLabel.setFont(albumsTitleLabel.getFont().deriveFont(Font.BOLD, 36f));
        albumsTitleLabel.setForeground(BLUE);

        // Liste d'albums (renderer custom, modèle fourni par contrôleur)
        albumList.setVisibleRowCount(7);
        albumList.setFixedCellHeight(140);
        albumList.setCellRenderer(new AlbumCellRenderer());
        albumList.setBorder(new EmptyBorder(4, 4, 4, 4));
        albumScroll.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Boutons
        addAlbumButton.setText("＋");
        addAlbumButton.setFocusPainted(false);
        addAlbumButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BLUE, 2, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        addAlbumButton.setForeground(BLUE);
        addAlbumButton.setBackground(Color.WHITE);

        removeAlbumButton.setText("🗑");
        removeAlbumButton.setFocusPainted(false);
        removeAlbumButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BLUE, 2, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        removeAlbumButton.setForeground(BLUE);
        removeAlbumButton.setBackground(Color.WHITE);

        pack();
        setLocationRelativeTo(null);
    }

    /* ==================== Helpers visuels ==================== */

    /** Affiche le logo de l’application depuis un chemin absolu. */
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

    public void setArtistName(String name) {
        artistNameLabel.setText(name != null ? name : "");
    }

    /** Centre une image dans un carré de taille 'box'. */
    public static Image centeredThumb(Image src, int box) {
        if (src == null) return null;
        int w = src.getWidth(null), h = src.getHeight(null);
        if (w <= 0 || h <= 0) return null;
        double s = Math.min(box / (double) w, box / (double) h);
        int nw = (int) Math.round(w * s), nh = (int) Math.round(h * s);
        Image scaled = src.getScaledInstance(nw, nh, Image.SCALE_SMOOTH);
        BufferedImage canvas = new BufferedImage(box, box, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        int x = (box - nw) / 2, y = (box - nh) / 2;
        g.drawImage(scaled, x, y, null);
        g.dispose();
        return canvas;
    }

    /* ==================== Data class pour la JList ==================== */
    public static class AlbumRow {
        public final Image cover;     // peut être null
        public final String title;     // “Album”
        public final String subtitle;  // ex. “Short and sweet delux”
        public final int trackCount;   // ex. 17

        public AlbumRow(Image cover, String title, String subtitle, int trackCount) {
            this.cover = cover;
            this.title = title;
            this.subtitle = subtitle;
            this.trackCount = trackCount;
        }
    }

    /* ==================== Renderer pour AlbumRow ==================== */
    private static class AlbumCellRenderer extends JPanel implements ListCellRenderer<AlbumRow> {
        private final JLabel cover = new JLabel();
        private final JLabel title = new JLabel();
        private final JLabel subtitle = new JLabel();
        private final JLabel rightTitle = new JLabel("Nombre de titres");
        private final JLabel rightValue = new JLabel();

        private static final Dimension COVER_SIZE = new Dimension(140, 140);
        private static final Color BLUE = new Color(0x0E2A62);

        public AlbumCellRenderer() {
            setOpaque(true);
            setLayout(new GridLayoutManager(1, 3, new Insets(8, 8, 8, 8), 12, 0));

            // Cover
            cover.setOpaque(true);
            cover.setBackground(new Color(0xB3C4E1));
            cover.setBorder(new LineBorder(new Color(0x9AAAC3), 1, false));
            add(cover, new GridConstraints(0, 0, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                    null, COVER_SIZE, COVER_SIZE, 0, false));

            // Bloc centre (title + subtitle)
            JPanel center = new JPanel(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), 0, 2));
            center.setOpaque(false);
            title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
            subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 14f));
            add(center, new GridConstraints(0, 1, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH,
                    GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null, 0, false));
            center.add(title, new GridConstraints(0, 0, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null, 0, false));
            center.add(subtitle, new GridConstraints(1, 0, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null, 0, false));

            // Bloc droit (Nombre de titres)
            JPanel right = new JPanel(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), 0, 2));
            right.setOpaque(false);
            rightTitle.setFont(rightTitle.getFont().deriveFont(Font.BOLD, 20f));
            rightTitle.setForeground(BLUE);
            rightValue.setFont(rightValue.getFont().deriveFont(Font.PLAIN, 14f));
            add(right, new GridConstraints(0, 2, 1, 1,
                    GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null, 0, false));
            right.add(rightTitle, new GridConstraints(0, 0, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null, 0, false));
            right.add(rightValue, new GridConstraints(1, 0, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null, 0, false));
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends AlbumRow> list, AlbumRow value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            // Cover
            if (value.cover != null) {
                Image thumb = ArtisteView.centeredThumb(value.cover, COVER_SIZE.width);
                cover.setIcon(new ImageIcon(thumb));
                cover.setText(null);
            } else {
                cover.setIcon(null);
                cover.setText("");
            }
            // Textes
            title.setText(value.title != null ? value.title : "");
            subtitle.setText(value.subtitle != null ? value.subtitle : "");
            rightValue.setText(value.trackCount + " musiques");

            // Sélection
            if (isSelected) {
                setBackground(new Color(0xEAF1FF));
            } else {
                setBackground(Color.WHITE);
            }
            return this;
        }
    }

    /* ==================== Getters MVC ==================== */
    public JLabel getArtistNameLabel() { return artistNameLabel; }
    public JButton getEditArtistButton() { return editArtistButton; }
    public JList<AlbumRow> getAlbumList() { return albumList; }
    public JScrollPane getAlbumScroll() { return albumScroll; }
    public JButton getAddAlbumButton() { return addAlbumButton; }
    public JButton getRemoveAlbumButton() { return removeAlbumButton; }

    /* ==================== Layout (GridLayoutManager) ==================== */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(12, 3, new Insets(0, 0, 0, 0), 0, 0));

        // colonnes respirations
        mainPanel.add(new JPanel(){ { setOpaque(false);} }, new GridConstraints(
                0, 0, 12, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        mainPanel.add(new JPanel(){ { setOpaque(false);} }, new GridConstraints(
                0, 2, 12, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        // Header applis
        JPanel header = new JPanel(new GridLayoutManager(1, 2, new Insets(0,0,0,0), 8, 0));
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

        // espace
        mainPanel.add(space(12), new GridConstraints(1, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // Ligne artiste : nom + ✎
        JPanel artistRow = new JPanel(new GridLayoutManager(1, 2, new Insets(0,0,0,0), 12, 0));
        artistRow.setOpaque(false);
        mainPanel.add(artistRow, new GridConstraints(2, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        artistNameLabel = new JLabel("Sabrina Carpenter");
        artistRow.add(artistNameLabel, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        editArtistButton = new JButton();
        artistRow.add(editArtistButton, new GridConstraints(0, 1, 1, 1,
                GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // espace
        mainPanel.add(space(8), new GridConstraints(3, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // Titre "Album"
        albumsTitleLabel = new JLabel("Album");
        mainPanel.add(albumsTitleLabel, new GridConstraints(4, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // Liste
        albumList = new JList<>(new DefaultListModel<>());
        albumScroll = new JScrollPane(albumList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(albumScroll, new GridConstraints(5, 1, 5, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW,
                null, new Dimension(700, 480), null, 0, false));

        // Actions
        JPanel actions = new JPanel(new GridLayoutManager(1, 5, new Insets(0,0,0,0), 12, 0));
        actions.setOpaque(false);
        addAlbumButton = new JButton();
        removeAlbumButton = new JButton();

        actions.add(new JPanel(){ { setOpaque(false);} }, new GridConstraints(
                0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        actions.add(addAlbumButton, new GridConstraints(
                0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        actions.add(removeAlbumButton, new GridConstraints(
                0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        actions.add(new JPanel(){ { setOpaque(false);} }, new GridConstraints(
                0, 3, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        mainPanel.add(actions, new GridConstraints(11, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
            ArtisteView view = new ArtisteView();
            view.setLogoFromAbsolutePath(
                    "C:\\Users\\grany\\OneDrive\\HEPL\\BAC2\\Q2\\Programmation orientée objet en Java\\ProjetFInale-DiskHouse\\src\\main\\resources\\LogoMini.png",
                    180, 48
            );

            // Contrôleur
            DiskHouse.Controller.ArtisteController controller = new DiskHouse.Controller.ArtisteController(view);
            controller.initController();

            SwingUtilities.invokeLater(() -> view.getRootPane().requestFocusInWindow());
            view.setVisible(true);
        });
    }
}
