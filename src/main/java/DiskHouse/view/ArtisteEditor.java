package DiskHouse.view;

import DiskHouse.Controller.ArtisteEditorController;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Vue "Artist" (SANS écouteurs)
 * - Portrait (cliquable par le contrôleur)
 * - Nom d'artiste (JTextField éditable)
 * - JList des albums + boutons Ajouter/Supprimer
 *
 * Respect GridLayoutManager (IntelliJ) + MVC
 * Auto‑liaison contrôleur via wireController()
 */
public class ArtistEditor extends JFrame {

    // Root
    private JPanel mainPanel;

    // Header
    private JLabel logoLabel;
    private JLabel appTitleLabel;

    // Ligne d’entête artiste
    private JLabel portraitLabel;         // portrait (cliquable par le contrôleur)
    private JTextField artistNameField;   // nom artiste (éditable)
    private JButton editArtistButton;     // ✎ focus + selectAll

    // Zone liste des albums
    private JLabel albumsLabel;           // "Albums"
    private JList<String> albumsList;
    private JScrollPane albumsScroll;

    // Actions
    private JButton addAlbumButton;       // +
    private JButton removeAlbumButton;    // 🗑

    // Constantes UI
    private static final Dimension PORTRAIT_SIZE = new Dimension(140, 140);
    private static final Color BLUE = new Color(0x0E2A62);

    public ArtistEditor() {
        super("DiskHouse - Artiste");
        $$$setupUI$$$();

        setContentPane(mainPanel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Style léger (optionnel)
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        appTitleLabel.setText("DiskHouse");
        appTitleLabel.setFont(appTitleLabel.getFont().deriveFont(Font.BOLD, 18f));
        appTitleLabel.setForeground(new Color(0x3B5C8E));

        // Portrait
        portraitLabel.setPreferredSize(PORTRAIT_SIZE);
        portraitLabel.setMinimumSize(PORTRAIT_SIZE);
        portraitLabel.setOpaque(true);
        portraitLabel.setBackground(new Color(0xB3C4E1));
        portraitLabel.setBorder(new LineBorder(new Color(0x9AAAC3), 1, false));
        portraitLabel.setHorizontalAlignment(SwingConstants.CENTER);
        portraitLabel.setVerticalAlignment(SwingConstants.CENTER);

        // Nom artiste (EDITABLE)
        artistNameField.setText("NomArtiste");
        artistNameField.setBorder(null);
        artistNameField.setFont(artistNameField.getFont().deriveFont(Font.BOLD, 42f));
        artistNameField.setForeground(BLUE);
        artistNameField.setOpaque(false);

        // Bouton éditer
        styliserAction(editArtistButton, "✎");

        // Libellé "Albums"
        albumsLabel.setText("Albums");
        albumsLabel.setFont(albumsLabel.getFont().deriveFont(Font.BOLD, 14f));
        albumsLabel.setForeground(Color.DARK_GRAY);

        // Liste albums
        albumsList.setVisibleRowCount(10);
        albumsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        albumsList.setFixedCellHeight(28);
        albumsList.setBorder(new EmptyBorder(6, 12, 6, 12));
        albumsScroll.setBorder(new LineBorder(new Color(0x9AAAC3), 1, false));

        // Boutons actions
        styliserAction(addAlbumButton, "＋");
        styliserAction(removeAlbumButton, "🗑");

        // Auto‑wiring contrôleur
        wireController();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /** Instancie le contrôleur et branche ses listeners. */
    private void wireController() {
        new ArtisteEditorController(this).initController();
    }

    /* ===================== Helpers visuels ===================== */

    private void styliserAction(JButton b, String text) {
        b.setText(text);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BLUE, 2, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        b.setForeground(BLUE);
        b.setBackground(Color.WHITE);
    }

    /** Définit le logo appli depuis un chemin absolu et le redimensionne. */
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

    /** Définit le portrait centré dans un carré PORTRAIT_SIZE. */
    public void setPortraitImage(Image source) {
        if (source == null) {
            portraitLabel.setIcon(null);
            portraitLabel.setText("");
            return;
        }
        int box = PORTRAIT_SIZE.width;
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

        portraitLabel.setText(null);
        portraitLabel.setIcon(new ImageIcon(canvas));
    }

    /** Raccourci pratique si le contrôleur veut forcer un nom. */
    public void setArtistName(String name) {
        artistNameField.setText(name != null ? name : "");
    }

    /* ===================== Getters MVC ===================== */
    public JLabel getPortraitLabel() { return portraitLabel; }
    public JTextField getArtistNameField() { return artistNameField; }
    public JButton getEditArtistButton() { return editArtistButton; }
    public JList<String> getAlbumsList() { return albumsList; }
    public JScrollPane getAlbumsScroll() { return albumsScroll; }
    public JButton getAddAlbumButtonView() { return addAlbumButton; }     // nom distinct pour éviter collision avec contrôleur
    public JButton getRemoveAlbumButtonView() { return removeAlbumButton; }

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

        // Ligne Artiste (portrait + nom + bouton edit)
        JPanel row = new JPanel(new GridLayoutManager(1, 3, new Insets(0,0,0,0), 12, 0));
        row.setOpaque(false);
        mainPanel.add(row, new GridConstraints(2, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        portraitLabel = new JLabel();
        row.add(portraitLabel, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, PORTRAIT_SIZE, PORTRAIT_SIZE, 0, false));

        artistNameField = new JTextField("NomArtiste");
        row.add(artistNameField, new GridConstraints(0, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        editArtistButton = new JButton();
        row.add(editArtistButton, new GridConstraints(0, 2, 1, 1,
                GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // espace
        mainPanel.add(space(16), new GridConstraints(3, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // Label "Albums"
        albumsLabel = new JLabel("Albums");
        mainPanel.add(albumsLabel, new GridConstraints(4, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // Liste
        albumsList = new JList<>(new DefaultListModel<>());
        albumsScroll = new JScrollPane(albumsList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(albumsScroll, new GridConstraints(5, 1, 5, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW,
                null, new Dimension(560, 340), null, 0, false));

        // Actions ( + / 🗑 )
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
        SwingUtilities.invokeLater(ArtistEditor::new);
    }
}
