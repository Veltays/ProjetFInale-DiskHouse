package DiskHouse.view;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Vue "Playlist" (passive : aucun listener ici)
 * - Pochette + titre playlist
 * - Liste des titres avec renderer custom (cover + titre + artiste + album + durée)
 * - Boutons + / 🗑 (listeners câblés dans le contrôleur)
 */
public class PlaylistView extends JFrame {

    // Root
    private JPanel mainPanel;

    // Header appli
    private JLabel logoLabel;
    private JLabel appTitleLabel;

    // Ligne d’entête playlist
    private JLabel coverLabel;            // pochette
    private JLabel playlistTitleLabel;    // "NomPlaylist"
    private JButton editPlaylistButton;   // ✎

    // Zone liste
    private JLabel tracksLabel;           // "Musique"
    private JList<TrackRow> trackList;
    private JScrollPane trackScroll;

    // Actions
    private JButton addTrackButton;       // +
    private JButton removeTrackButton;    // 🗑

    // Constantes UI
    private static final Dimension COVER_SIZE = new Dimension(140, 140);
    private static final Color BLUE = new Color(0x0E2A62);

    public PlaylistView() {
        super("DiskHouse - Playlist");
        $$$setupUI$$$();

        setContentPane(mainPanel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Style global
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        // En-tête application
        appTitleLabel.setText("DiskHouse");
        appTitleLabel.setFont(appTitleLabel.getFont().deriveFont(Font.BOLD, 18f));
        appTitleLabel.setForeground(new Color(0x3B5C8E));

        // Pochette
        coverLabel.setPreferredSize(COVER_SIZE);
        coverLabel.setMinimumSize(COVER_SIZE);
        coverLabel.setOpaque(true);
        coverLabel.setBackground(new Color(0xB3C4E1));
        coverLabel.setBorder(new LineBorder(new Color(0x9AAAC3), 1, false));
        coverLabel.setHorizontalAlignment(SwingConstants.CENTER);
        coverLabel.setVerticalAlignment(SwingConstants.CENTER);

        // Titre Playlist
        playlistTitleLabel.setText("NomPlaylist");
        playlistTitleLabel.setForeground(BLUE);
        playlistTitleLabel.setFont(playlistTitleLabel.getFont().deriveFont(Font.BOLD, 42f));

        // Bouton éditer
        editPlaylistButton.setText("✎");
        editPlaylistButton.setFocusPainted(false);
        editPlaylistButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BLUE, 2, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        editPlaylistButton.setForeground(BLUE);
        editPlaylistButton.setBackground(Color.WHITE);

        // Label "Musique"
        tracksLabel.setText("Musique");
        tracksLabel.setFont(tracksLabel.getFont().deriveFont(Font.BOLD, 14f));
        tracksLabel.setForeground(Color.DARK_GRAY);

        // Liste des titres
        trackList.setVisibleRowCount(12);
        trackList.setFixedCellHeight(112);
        trackList.setCellRenderer(new TrackCellRenderer());
        trackList.setBorder(new EmptyBorder(6, 4, 6, 4));
        trackScroll.setBorder(new LineBorder(new Color(0x9AAAC3), 1, false));

        // Boutons
        addTrackButton.setText("＋");
        addTrackButton.setFocusPainted(false);
        addTrackButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BLUE, 2, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        addTrackButton.setForeground(BLUE);
        addTrackButton.setBackground(Color.WHITE);

        removeTrackButton.setText("🗑");
        removeTrackButton.setFocusPainted(false);
        removeTrackButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BLUE, 2, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        removeTrackButton.setForeground(BLUE);
        removeTrackButton.setBackground(Color.WHITE);

        pack();
        setLocationRelativeTo(null);
    }

    /* ===================== Helpers visuels ===================== */

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

    public void setCoverImage(Image source) {
        if (source == null) {
            coverLabel.setIcon(null);
            coverLabel.setText("");
            return;
        }
        int box = COVER_SIZE.width;
        int w = source.getWidth(null), h = source.getHeight(null);
        if (w <= 0 || h <= 0) return;

        double s = Math.min(box / (double) w, box / (double) h);
        int nw = (int) Math.round(w * s);
        int nh = (int) Math.round(h * s);

        Image scaled = source.getScaledInstance(nw, nh, Image.SCALE_SMOOTH);
        BufferedImage canvas = new BufferedImage(box, box, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        int x = (box - nw) / 2, y = (box - nh) / 2;
        g.drawImage(scaled, x, y, null);
        g.dispose();

        coverLabel.setText(null);
        coverLabel.setIcon(new ImageIcon(canvas));
    }

    public void setPlaylistTitle(String title) {
        playlistTitleLabel.setText(title != null ? title : "");
    }

    /** Centrage utilitaire (réutilisable au besoin) */
    public static Image centeredThumb(Image src, int box) {
        if (src == null) return null;
        int w = src.getWidth(null), h = src.getHeight(null);
        if (w <= 0 || h <= 0) return null;
        double s = Math.min(box / (double) w, box / (double) h);
        int nw = (int) Math.round(w * s), nh = (int) Math.round(h * s);
        Image scaled = src.getScaledInstance(nw, nh, Image.SCALE_SMOOTH);
        BufferedImage canvas = new BufferedImage(box, box, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        g.drawImage(scaled, (box - nw) / 2, (box - nh) / 2, null);
        g.dispose();
        return canvas;
    }

    /* ===================== Data + Renderer ===================== */

    /** Donnée d’une ligne de musique. */
    public static class TrackRow {
        public final Image cover;     // pochette titre (optionnelle)
        public final String title;    // ex: "La goat"
        public final String artist;   // ex: "Sabrina Carpenter"
        public final String album;    // ex: "Short and sweet"
        public final String duration; // ex: "3 min 07" (libre)

        public TrackRow(Image cover, String title, String artist, String album, String duration) {
            this.cover = cover;
            this.title = title;
            this.artist = artist;
            this.album = album;
            this.duration = duration;
        }
    }

    /** Renderer visuel pour une ligne de musique. */
    private static class TrackCellRenderer extends JPanel implements ListCellRenderer<TrackRow> {
        private final JLabel cover = new JLabel();
        private final JLabel leftTitle = new JLabel();          // Titre (gras)
        private final JLabel leftSubtitleTop = new JLabel();    // Artiste
        private final JLabel leftSubtitleBottom = new JLabel(); // Album
        private final JLabel rightTitle = new JLabel("Album");  // intitulé colonne (maquette)
        private final JLabel rightDuration = new JLabel();      // Durée

        private static final Dimension THUMB_SIZE = new Dimension(112, 112);
        private static final Color BLUE = new Color(0x0E2A62);

        public TrackCellRenderer() {
            setOpaque(true);
            setLayout(new GridLayoutManager(1, 3, new Insets(8, 8, 8, 8), 12, 0));

            // vignette
            cover.setOpaque(true);
            cover.setBackground(new Color(0xB3C4E1));
            cover.setBorder(new LineBorder(new Color(0x9AAAC3), 1, false));
            add(cover, new GridConstraints(0, 0, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                    null, THUMB_SIZE, THUMB_SIZE, 0, false));

            // bloc centre : titre + artiste + album
            JPanel center = new JPanel(new GridLayoutManager(3, 1, new Insets(0,0,0,0), 0, 2));
            center.setOpaque(false);
            leftTitle.setFont(leftTitle.getFont().deriveFont(Font.BOLD, 20f));
            leftSubtitleTop.setFont(leftSubtitleTop.getFont().deriveFont(Font.PLAIN, 14f));
            leftSubtitleBottom.setFont(leftSubtitleBottom.getFont().deriveFont(Font.PLAIN, 14f));
            add(center, new GridConstraints(0, 1, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH,
                    GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null, 0, false));
            center.add(leftTitle, new GridConstraints(0, 0, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null, 0, false));
            center.add(leftSubtitleTop, new GridConstraints(1, 0, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null, 0, false));
            center.add(leftSubtitleBottom, new GridConstraints(2, 0, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null, 0, false));

            // bloc droit : "Album" + durée
            JPanel right = new JPanel(new GridLayoutManager(2, 1, new Insets(0,0,0,0), 0, 2));
            right.setOpaque(false);
            rightTitle.setFont(rightTitle.getFont().deriveFont(Font.BOLD, 18f));
            rightTitle.setForeground(BLUE);
            rightDuration.setFont(rightDuration.getFont().deriveFont(Font.PLAIN, 14f));
            add(right, new GridConstraints(0, 2, 1, 1,
                    GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null, 0, false));
            right.add(rightTitle, new GridConstraints(0, 0, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null, 0, false));
            right.add(rightDuration, new GridConstraints(1, 0, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null, 0, false));
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends TrackRow> list, TrackRow value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            if (value.cover != null) {
                Image thumb = PlaylistView.centeredThumb(value.cover, THUMB_SIZE.width);
                cover.setIcon(new ImageIcon(thumb));
                cover.setText(null);
            } else {
                cover.setIcon(null);
                cover.setText("");
            }
            leftTitle.setText(value.title != null ? value.title : "");
            leftSubtitleTop.setText(value.artist != null ? value.artist : "");
            leftSubtitleBottom.setText(value.album != null ? value.album : "");
            rightDuration.setText(value.duration != null ? value.duration : "");

            setBackground(isSelected ? new Color(0xEAF1FF) : Color.WHITE);
            return this;
        }
    }

    /* ===================== Getters MVC ===================== */
    public JLabel getCoverLabel() { return coverLabel; }
    public JLabel getPlaylistTitleLabel() { return playlistTitleLabel; }
    public JButton getEditPlaylistButton() { return editPlaylistButton; }
    public JList<TrackRow> getTrackList() { return trackList; }
    public JScrollPane getTrackScroll() { return trackScroll; }
    public JButton getAddTrackButton() { return addTrackButton; }
    public JButton getRemoveTrackButton() { return removeTrackButton; }

    /* ===================== Layout (GridLayoutManager) ===================== */
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

        // Header
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
        mainPanel.add(space(16), new GridConstraints(1, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // Ligne playlist (cover + title + ✎)
        JPanel row = new JPanel(new GridLayoutManager(1, 3, new Insets(0,0,0,0), 12, 0));
        row.setOpaque(false);
        mainPanel.add(row, new GridConstraints(2, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        coverLabel = new JLabel();
        row.add(coverLabel, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, COVER_SIZE, COVER_SIZE, 0, false));

        playlistTitleLabel = new JLabel("NomPlaylist");
        row.add(playlistTitleLabel, new GridConstraints(0, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        editPlaylistButton = new JButton();
        row.add(editPlaylistButton, new GridConstraints(0, 2, 1, 1,
                GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // espace
        mainPanel.add(space(12), new GridConstraints(3, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // label "Musique"
        tracksLabel = new JLabel("Musique");
        mainPanel.add(tracksLabel, new GridConstraints(4, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // liste
        trackList = new JList<>(new DefaultListModel<>());
        trackScroll = new JScrollPane(trackList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(trackScroll, new GridConstraints(5, 1, 5, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW,
                null, new Dimension(700, 480), null, 0, false));

        // actions
        JPanel actions = new JPanel(new GridLayoutManager(1, 5, new Insets(0,0,0,0), 12, 0));
        actions.setOpaque(false);
        addTrackButton = new JButton();
        removeTrackButton = new JButton();

        actions.add(new JPanel(){ { setOpaque(false);} }, new GridConstraints(
                0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        actions.add(addTrackButton, new GridConstraints(
                0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        actions.add(removeTrackButton, new GridConstraints(
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

    // Main de test autonome (tu peux garder ou supprimer selon ton projet)
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PlaylistView view = new PlaylistView();
            view.setVisible(true);
        });
    }
}
