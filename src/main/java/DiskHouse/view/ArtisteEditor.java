package DiskHouse.view;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Vue "Artiste" sous forme de JDialog MODAL.
 * - Uniquement UI (respect MVC). Aucune logique métier ici.
 * - GridLayoutManager (IntelliJ) respecté.
 *
 * Le contrôleur utilise notamment :
 *   - setArtistName(String)
 *   - setPortraitImage(Image)
 *   - getAlbumsList(), getAddAlbumButtonView(), getRemoveAlbumButtonView()
 *   - getOkButton(), getCancelButton(), getEditArtistButton(), getPortraitLabel(), getArtistNameField()
 */
public class ArtisteEditor extends JDialog {

    // Root
    private JPanel mainPanel;

    // Header
    private JLabel logoLabel;
    private JLabel appTitleLabel;

    // Ligne d’entête artiste
    private JLabel portraitLabel;         // portrait (cliquable par le contrôleur)
    private JTextField artistNameField;   // nom artiste (éditable)
    private JButton editArtistButton;     // ✎

    // Zone liste des albums
    private JLabel albumsLabel;           // "Albums"
    private JList<String> albumsList;
    private JScrollPane albumsScroll;

    // Actions liste
    private JButton addAlbumButton;       // +
    private JButton removeAlbumButton;    // 🗑

    // Bas de dialogue
    private JButton cancelButton;
    private JButton okButton;

    // Constantes UI
    private static final Dimension PORTRAIT_SIZE = new Dimension(140, 140);
    private static final Color BLUE = new Color(0x0E2A62);

    /** Constructeur conseillé : attaché à une fenêtre parente, modale. */
    public ArtisteEditor(Window owner) {
        super(owner, "DiskHouse - Artiste", ModalityType.APPLICATION_MODAL);
        $$$setupUI$$$();

        setContentPane(mainPanel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        styliser();

        setMinimumSize(new Dimension(680, 520));
        setLocationRelativeTo(owner);
    }

    /** Fallback démo sans parent. */
    public ArtisteEditor() { this(null); }

    /* ===================== Styling ===================== */

    private void styliser() {
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

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
        artistNameField.setText("");
        artistNameField.setBorder(null);
        artistNameField.setFont(artistNameField.getFont().deriveFont(Font.BOLD, 42f));
        artistNameField.setForeground(BLUE);
        artistNameField.setOpaque(false);

        // Bouton éditer (focus + selectAll géré par controller)
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

        // Boutons liste
        styliserAction(addAlbumButton, "＋");
        styliserAction(removeAlbumButton, "🗑");

        // Bas
        styleGhost(cancelButton, "Annuler");
        stylePrimary(okButton, "Enregistrer");
    }

    private void styliserAction(JButton b, String text) {
        b.setText(text);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BLUE, 2, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
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

    /* ===================== API attendue par le contrôleur ===================== */

    /** Définit le nom affiché dans le champ. */
    public void setArtistName(String name) {
        artistNameField.setText(name != null ? name : "");
        artistNameField.setCaretPosition(artistNameField.getText().length());
    }

    /** Portrait centré dans PORTRAIT_SIZE. */
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

    /* ===================== Getters MVC ===================== */
    public JLabel getPortraitLabel() { return portraitLabel; }
    public JTextField getArtistNameField() { return artistNameField; }
    public JButton getEditArtistButton() { return editArtistButton; }
    public JList<String> getAlbumsList() { return albumsList; }
    public JScrollPane getAlbumsScroll() { return albumsScroll; }
    public JButton getAddAlbumButtonView() { return addAlbumButton; }
    public JButton getRemoveAlbumButtonView() { return removeAlbumButton; }
    public JButton getCancelButton() { return cancelButton; }
    public JButton getOkButton() { return okButton; }

    /* ===================== Layout (GridLayoutManager) ===================== */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(14, 3, new Insets(0, 0, 0, 0), 0, 0));

        // Colonnes de respiration (0 et 2)
        mainPanel.add(empty(),
                new GridConstraints(0, 0, 14, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        mainPanel.add(empty(),
                new GridConstraints(0, 2, 14, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        // Header : logo + titre appli
        JPanel header = new JPanel(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), 8, 0));
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

        // Ligne Artiste (portrait + nom + bouton edit)
        JPanel row = new JPanel(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), 12, 0));
        row.setOpaque(false);
        mainPanel.add(row, new GridConstraints(2, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        portraitLabel = new JLabel();
        row.add(portraitLabel, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, PORTRAIT_SIZE, PORTRAIT_SIZE, 0, false));

        artistNameField = new JTextField();
        row.add(artistNameField, new GridConstraints(0, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        editArtistButton = new JButton();
        row.add(editArtistButton, new GridConstraints(0, 2, 1, 1,
                GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // espace
        mainPanel.add(space(12), new GridConstraints(3, 1, 1, 1,
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
        mainPanel.add(albumsScroll, new GridConstraints(5, 1, 6, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW,
                null, new Dimension(560, 340), null, 0, false));

        // Actions ( + / 🗑 )
        JPanel actions = new JPanel(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), 12, 0));
        actions.setOpaque(false);

        addAlbumButton = new JButton();
        removeAlbumButton = new JButton();

        actions.add(empty(), new GridConstraints(
                0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        actions.add(addAlbumButton, new GridConstraints(
                0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        actions.add(removeAlbumButton, new GridConstraints(
                0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        actions.add(empty(), new GridConstraints(
                0, 3, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        mainPanel.add(actions, new GridConstraints(11, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // Bas : Annuler / Enregistrer
        JPanel bottom = new JPanel(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), 8, 0));
        bottom.setOpaque(false);
        bottom.add(empty(), new GridConstraints(
                0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        cancelButton = new JButton();
        okButton = new JButton();
        bottom.add(cancelButton, new GridConstraints(
                0, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        bottom.add(okButton, new GridConstraints(
                0, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mainPanel.add(bottom, new GridConstraints(12, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // espace bas
        mainPanel.add(space(8), new GridConstraints(13, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    private JPanel space(int h) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(0, h));
        return p;
    }

    private JPanel empty() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        return p;
    }

    /** Démo locale. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ArtisteEditor().setVisible(true));
    }
}
