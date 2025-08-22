package DiskHouse.view;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Vue "Album" (SANS écouteurs)
 * - Pochette (cliquable par le contrôleur)
 * - Titre d'album (modifiable via bouton "éditer" par le contrôleur)
 * - Liste de musiques (JList) + boutons Ajouter/Supprimer
 */
public class AddAlbum extends JFrame {

    // Root
    private JPanel mainPanel;

    // Header
    private JLabel logoLabel;
    private JLabel appTitleLabel;

    // Album header row
    private JLabel coverLabel;           // pochette
    private JLabel albumTitleLabel;      // "NomAlbum"
    private JButton editAlbumButton;     // ✎ (le contrôleur gère le click)

    // Zone liste des musiques
    private JLabel musiquesLabel;        // "Musique"
    private JList<String> songsList;
    private JScrollPane songsScroll;

    // Actions
    private JButton addSongButton;       // +
    private JButton removeSongButton;    // 🗑

    // Constantes UI
    private static final Dimension COVER_SIZE = new Dimension(140, 140);
    private static final Color BLUE = new Color(0x0E2A62);

    public AddAlbum() {
        super("DiskHouse - Album");
        $$$setupUI$$$();

        setContentPane(mainPanel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Style global
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        // En-tête
        appTitleLabel.setText("DiskHouse");
        appTitleLabel.setFont(appTitleLabel.getFont().deriveFont(Font.BOLD, 18f));
        appTitleLabel.setForeground(new Color(0x3B5C8E));

        // Pochette (pur visuel)
        coverLabel.setPreferredSize(COVER_SIZE);
        coverLabel.setMinimumSize(COVER_SIZE);
        coverLabel.setOpaque(true);
        coverLabel.setBackground(new Color(0xB3C4E1));
        coverLabel.setBorder(new LineBorder(new Color(0x9AAAC3), 1, false));
        coverLabel.setHorizontalAlignment(SwingConstants.CENTER);
        coverLabel.setVerticalAlignment(SwingConstants.CENTER);

        // Titre Album (visuel)
        albumTitleLabel.setText("NomAlbum");
        albumTitleLabel.setForeground(BLUE);
        albumTitleLabel.setFont(albumTitleLabel.getFont().deriveFont(Font.BOLD, 42f));

        // Bouton éditer (visuel)
        editAlbumButton.setText("✎");
        editAlbumButton.setFocusPainted(false);
        editAlbumButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BLUE, 2, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        editAlbumButton.setForeground(BLUE);
        editAlbumButton.setBackground(Color.WHITE);

        // Libellé "Musique"
        musiquesLabel.setText("Musique");
        musiquesLabel.setFont(musiquesLabel.getFont().deriveFont(Font.BOLD, 14f));
        musiquesLabel.setForeground(Color.DARK_GRAY);

        // Liste musiques
        songsList.setVisibleRowCount(10);
        songsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        songsList.setFixedCellHeight(28);
        songsList.setBorder(new EmptyBorder(6, 12, 6, 12));
        songsScroll.setBorder(new LineBorder(new Color(0x9AAAC3), 1, false));

        // Boutons actions
        addSongButton.setText("＋");
        addSongButton.setFocusPainted(false);
        addSongButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BLUE, 2, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        addSongButton.setForeground(BLUE);
        addSongButton.setBackground(Color.WHITE);

        removeSongButton.setText("🗑");
        removeSongButton.setFocusPainted(false);
        removeSongButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BLUE, 2, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        removeSongButton.setForeground(BLUE);
        removeSongButton.setBackground(Color.WHITE);

        pack();
        setLocationRelativeTo(null);
    }

    /* ===================== Helpers visuels ===================== */

    /** Définir le logo appli depuis un chemin absolu. */
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

    /** Définir la pochette centrée dans un carré COVER_SIZE. */
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

    /** Définir le titre affiché. */
    public void setAlbumTitle(String title) {
        albumTitleLabel.setText(title != null ? title : "");
    }

    /* ===================== Getters MVC ===================== */
    public JLabel getCoverLabel() { return coverLabel; }
    public JLabel getAlbumTitleLabel() { return albumTitleLabel; }
    public JButton getEditAlbumButton() { return editAlbumButton; }

    public JList<String> getSongsList() { return songsList; }
    public JScrollPane getSongsScroll() { return songsScroll; }
    public JButton getAddSongButton() { return addSongButton; }
    public JButton getRemoveSongButton() { return removeSongButton; }

    /* ===================== Layout (GridLayoutManager) ===================== */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(12, 3, new Insets(0, 0, 0, 0), 0, 0));

        // colonnes de respiration
        mainPanel.add(new JPanel() {{ setOpaque(false); }},
                new GridConstraints(0, 0, 12, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        mainPanel.add(new JPanel() {{ setOpaque(false); }},
                new GridConstraints(0, 2, 12, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        // Header : logo + titre appli
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

        // Ligne Album (pochette + titre + bouton edit)
        JPanel row = new JPanel(new GridLayoutManager(1, 3, new Insets(0,0,0,0), 12, 0));
        row.setOpaque(false);
        mainPanel.add(row, new GridConstraints(2, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        coverLabel = new JLabel();
        row.add(coverLabel, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, COVER_SIZE, COVER_SIZE, 0, false));

        albumTitleLabel = new JLabel("NomAlbum");
        row.add(albumTitleLabel, new GridConstraints(0, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        editAlbumButton = new JButton();
        row.add(editAlbumButton, new GridConstraints(0, 2, 1, 1,
                GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // espace
        mainPanel.add(space(16), new GridConstraints(3, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // Label "Musique"
        musiquesLabel = new JLabel("Musique");
        mainPanel.add(musiquesLabel, new GridConstraints(4, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // Liste
        songsList = new JList<>(new DefaultListModel<>());
        songsScroll = new JScrollPane(songsList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(songsScroll, new GridConstraints(5, 1, 5, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW,
                null, new Dimension(560, 340), null, 0, false));

        // Actions ( + / 🗑 )
        JPanel actions = new JPanel(new GridLayoutManager(1, 5, new Insets(0,0,0,0), 12, 0));
        actions.setOpaque(false);

        addSongButton = new JButton();
        removeSongButton = new JButton();

        actions.add(new JPanel(){ { setOpaque(false);} }, new GridConstraints(
                0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        actions.add(addSongButton, new GridConstraints(
                0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        actions.add(removeSongButton, new GridConstraints(
                0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        actions.add(new JPanel(){ { setOpaque(false);} }, new GridConstraints(
                0, 3, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        mainPanel.add(actions, new GridConstraints(10, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // espace bas
        mainPanel.add(space(12), new GridConstraints(11, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
            AddAlbum view = new AddAlbum();
            view.setLogoFromAbsolutePath(
                    "C:\\Users\\grany\\OneDrive\\HEPL\\BAC2\\Q2\\Programmation orientée objet en Java\\ProjetFInale-DiskHouse\\src\\main\\resources\\LogoMini.png",
                    180, 48
            );

            // Contrôleur
            DiskHouse.Controller.AlbumController controller = new DiskHouse.Controller.AlbumController(view);
            controller.initController();

            // éviter le focus auto sur la liste
            SwingUtilities.invokeLater(() -> view.getRootPane().requestFocusInWindow());

            view.setVisible(true);
        });
    }
}
