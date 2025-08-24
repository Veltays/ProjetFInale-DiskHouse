package DiskHouse.view;

import DiskHouse.Controller.PlaylistEditorController;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage; // <- corrige l'erreur d'import

/**
 * Vue "AddPlaylist" (indépendante).
 * - Layout via GridLayoutManager (UI Designer)
 * - Pas d'écouteurs ici : le contrôleur se branche via wireController()
 * - Titre éditable (JTextField)
 * - Pochette : carré bleu avec placeholder
 */
public class PlaylistEditor extends JFrame {

    // root
    private JPanel mainPanel;

    // header
    private JLabel logoLabel;
    private JLabel appTitleLabel;

    // cover + titre + edit
    private JLabel coverLabel;          // cliquable par le contrôleur
    private JTextField playlistTitleField; // EDITABLE
    private JButton editPlaylistButton; // met juste le focus sur le titre

    // "Musique" + liste
    private JLabel musiquesLabel;
    private JList<String> trackList;
    private JScrollPane trackScroll;

    // actions
    private JButton addTrackButton;
    private JButton removeTrackButton;

    // contrôle double-branchement
    private boolean controllerWired = false;

    // constantes visuelles
    private static final int COVER_BOX = 140;
    private static final Color COVER_BG = new Color(0xB3C4E1);

    public PlaylistEditor() {
        super("DiskHouse - Nouvelle playlist");
        $$$setupUI$$$();

        setContentPane(mainPanel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // micro-style
        appTitleLabel.setText("DiskHouse");
        musiquesLabel.setText("Musique");
        editPlaylistButton.setText("✎");
        addTrackButton.setText("＋");
        removeTrackButton.setText("🗑");

        // placeholder cover (carré bleu)
        coverLabel.setPreferredSize(new Dimension(COVER_BOX, COVER_BOX));
        coverLabel.setOpaque(true);
        coverLabel.setBackground(COVER_BG);
        coverLabel.setHorizontalAlignment(SwingConstants.CENTER);
        coverLabel.setVerticalAlignment(SwingConstants.CENTER);

        // titre par défaut
        playlistTitleField.setText("NomPlaylist");
        playlistTitleField.setFont(playlistTitleField.getFont().deriveFont(Font.BOLD, 36f));
        playlistTitleField.setBorder(BorderFactory.createEmptyBorder());

        pack();
        setLocationRelativeTo(null);

        // branchement contrôleur (demande du projet)
        wireController();

        setVisible(true);
    }

    /** Instancie et initialise le contrôleur une seule fois. */
    private void wireController() {
        if (controllerWired) return;
        new PlaylistEditorController(this).initController();
        controllerWired = true;
    }

    /* ====== Helpers d’affichage ====== */

    /** Change l’image de pochette en la centrant dans un carré COVER_BOX. */
    public void setCoverImage(Image source) {
        if (source == null) {
            coverLabel.setIcon(null);
            coverLabel.setText("");
            coverLabel.setBackground(COVER_BG);
            return;
        }
        int w = source.getWidth(null), h = source.getHeight(null);
        if (w <= 0 || h <= 0) return;

        double s = Math.min(COVER_BOX / (double) w, COVER_BOX / (double) h);
        int nw = (int) Math.round(w * s);
        int nh = (int) Math.round(h * s);

        Image scaled = source.getScaledInstance(nw, nh, Image.SCALE_SMOOTH);
        BufferedImage canvas = new BufferedImage(COVER_BOX, COVER_BOX, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        g.setColor(COVER_BG);
        g.fillRect(0, 0, COVER_BOX, COVER_BOX);
        int x = (COVER_BOX - nw) / 2, y = (COVER_BOX - nh) / 2;
        g.drawImage(scaled, x, y, null);
        g.dispose();

        coverLabel.setIcon(new ImageIcon(canvas));
        coverLabel.setText(null);
    }

    /** Raccourci simple pour maj du titre depuis le contrôleur. */
    public void setPlaylistTitle(String title) {
        playlistTitleField.setText(title != null ? title : "");
    }

    /* ====== Getters exposés au contrôleur ====== */
    public JLabel getCoverLabel() { return coverLabel; }
    public JTextField getPlaylistTitleField() { return playlistTitleField; }
    public JButton getEditPlaylistButton() { return editPlaylistButton; }
    public JList<String> getTrackList() { return trackList; }
    public JButton getAddTrackButton() { return addTrackButton; }
    public JButton getRemoveTrackButton() { return removeTrackButton; }
    public JLabel getLogoLabel() { return logoLabel; }

    /* ====== Mise en page (GridLayoutManager) ====== */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(10, 3, new Insets(12, 12, 12, 12), 8, 8));

        // colonnes 0 et 2 = respiration
        mainPanel.add(new JPanel(), new GridConstraints(0, 0, 10, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null, 0, false));
        mainPanel.add(new JPanel(), new GridConstraints(0, 2, 10, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null, 0, false));

        // header
        JPanel header = new JPanel(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), 8, 0));
        header.setOpaque(false);
        mainPanel.add(header, new GridConstraints(0, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        logoLabel = new JLabel();
        header.add(logoLabel, gc(0, 0));

        appTitleLabel = new JLabel("DiskHouse");
        header.add(appTitleLabel, gc(0, 1));

        // ligne: cover + titre + ✎
        JPanel row = new JPanel(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), 12, 0));
        mainPanel.add(row, new GridConstraints(1, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        coverLabel = new JLabel();
        coverLabel.setPreferredSize(new Dimension(COVER_BOX, COVER_BOX));
        row.add(coverLabel, gc(0, 0));

        playlistTitleField = new JTextField();
        row.add(playlistTitleField, new GridConstraints(0, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        editPlaylistButton = new JButton();
        row.add(editPlaylistButton, gc(0, 2));

        // label "Musique"
        musiquesLabel = new JLabel("Musique");
        mainPanel.add(musiquesLabel, new GridConstraints(2, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        // liste
        trackList = new JList<>(new DefaultListModel<>());
        trackScroll = new JScrollPane(trackList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(trackScroll, new GridConstraints(3, 1, 5, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW,
                null, new Dimension(560, 360), null, 0, false));

        // actions (+ / 🗑)
        JPanel actions = new JPanel(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), 12, 0));
        addTrackButton = new JButton();
        removeTrackButton = new JButton();
        actions.add(new JPanel(), gc(0, 0));
        actions.add(addTrackButton, gc(0, 1));
        actions.add(removeTrackButton, gc(0, 2));
        mainPanel.add(actions, new GridConstraints(8, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        // espace bas
        mainPanel.add(new JPanel(), new GridConstraints(9, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));
    }

    private static GridConstraints gc(int r, int c) {
        return new GridConstraints(r, c, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false);
    }

    // main de test
    public static void main(String[] args) {
        SwingUtilities.invokeLater(PlaylistEditor::new);
    }
}
