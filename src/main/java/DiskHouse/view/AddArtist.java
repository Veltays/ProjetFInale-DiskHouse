package DiskHouse.view;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Vue "Ajouter un artiste" (SANS contrôleurs / SANS listeners)
 * - Styles via GridLayoutManager (IntelliJ)
 * - Expose uniquement des getters et helpers visuels
 */
public class AddArtist extends JFrame {

    // Root
    private JPanel mainPanel;

    // Header
    private JLabel logoLabel;
    private JLabel titleLabel;

    // Champs
    private JTextField pseudoField;
    private JTextField nomField;
    private JTextField prenomField;

    // Albums (liste)
    private JLabel albumTitleLabel;
    private JList<String> albumList;
    private JScrollPane albumScroll;

    // Boutons (ajout/suppression)
    private JButton addAlbumButton;
    private JButton removeAlbumButton;

    // Constantes UI
    private static final Dimension FIELD_SIZE = new Dimension(560, 56);
    private static final Color CARD_BG = new Color(0xE6E6E6);
    private static final Color CARD_BORDER = new Color(0xDDDDDD);

    public AddArtist() {
        super("DiskHouse - Ajouter un artiste");
        $$$setupUI$$$();

        setContentPane(mainPanel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Header
        titleLabel.setText("DiskHouse");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(new Color(0x0E2A62)); // bleu foncé

        // Champs (pas de placeholder ici — contrôleur)
        setupFieldLikeCard(pseudoField);
        setupFieldLikeCard(nomField);
        setupFieldLikeCard(prenomField);

        // Liste Albums (visuel)
        albumTitleLabel.setText("Album");
        albumTitleLabel.setFont(albumTitleLabel.getFont().deriveFont(Font.BOLD, 24f));
        albumTitleLabel.setForeground(new Color(0x0E2A62));
        albumList.setVisibleRowCount(8);
        albumList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        albumList.setFixedCellHeight(28);
        albumList.setBorder(new EmptyBorder(8, 12, 8, 12));

        albumScroll.setBorder(new LineBorder(new Color(0x9AAAC3), 1, false));

        // Boutons (purs visuels)
        addAlbumButton.setText("＋");
        addAlbumButton.setFocusPainted(false);
        addAlbumButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0x0E2A62), 2, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        addAlbumButton.setForeground(new Color(0x0E2A62));
        addAlbumButton.setBackground(Color.WHITE);

        removeAlbumButton.setText("🗑");
        removeAlbumButton.setFocusPainted(false);
        removeAlbumButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0x0E2A62), 2, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        removeAlbumButton.setForeground(new Color(0x0E2A62));
        removeAlbumButton.setBackground(Color.WHITE);

        pack();
        setLocationRelativeTo(null);
    }

    private void setupFieldLikeCard(JTextField field) {
        field.setPreferredSize(FIELD_SIZE);
        field.setMinimumSize(FIELD_SIZE);
        field.setFont(field.getFont().deriveFont(Font.PLAIN, 20f));
        field.setBackground(CARD_BG);
        field.setOpaque(true);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(CARD_BORDER, 1, false),
                new EmptyBorder(10, 16, 10, 16)
        ));
    }

    /* ===================== Helpers visuels ===================== */

    /** Affiche le logo à partir d’un chemin absolu. */
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

    /** Pour d’éventuels mini-aperçus (non utilisé ici, mais utile si tu ajoutes une image d’artiste) */
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

    /* ===================== Getters MVC ===================== */
    public JTextField getPseudoField() { return pseudoField; }
    public JTextField getNomField() { return nomField; }
    public JTextField getPrenomField() { return prenomField; }

    public JList<String> getAlbumList() { return albumList; }
    public JScrollPane getAlbumScroll() { return albumScroll; }

    public JButton getAddAlbumButton() { return addAlbumButton; }
    public JButton getRemoveAlbumButton() { return removeAlbumButton; }

    public JLabel getAlbumTitleLabel() { return albumTitleLabel; }
    public JLabel getLogoLabel() { return logoLabel; }

    /* ===================== Layout (GridLayoutManager) ===================== */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(14, 3, new Insets(0, 0, 0, 0), 0, 0));

        // colonnes gauche/droite pour respirations
        mainPanel.add(new JPanel(){ { setOpaque(false);} },
                new GridConstraints(0, 0, 14, 1,
                        GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW,
                        null, null, null, 0, false));

        mainPanel.add(new JPanel(){ { setOpaque(false);} },
                new GridConstraints(0, 2, 14, 1,
                        GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW,
                        null, null, null, 0, false));

        // Header (logo + titre)
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
        mainPanel.add(space(24), new GridConstraints(1, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        // Pseudo
        pseudoField = new JTextField();
        mainPanel.add(pseudoField, new GridConstraints(2, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, FIELD_SIZE, null, 0, false));

        // Nom
        mainPanel.add(space(16), new GridConstraints(3, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        nomField = new JTextField();
        mainPanel.add(nomField, new GridConstraints(4, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, FIELD_SIZE, null, 0, false));

        // Prenom
        mainPanel.add(space(16), new GridConstraints(5, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        prenomField = new JTextField();
        mainPanel.add(prenomField, new GridConstraints(6, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, FIELD_SIZE, null, 0, false));

        // Titre Album
        mainPanel.add(space(18), new GridConstraints(7, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        albumTitleLabel = new JLabel("Album");
        mainPanel.add(albumTitleLabel, new GridConstraints(8, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        // Liste Albums
        mainPanel.add(space(8), new GridConstraints(9, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        albumList = new JList<>(new DefaultListModel<>());
        albumScroll = new JScrollPane(albumList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(albumScroll, new GridConstraints(10, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW,
                null, new Dimension(560, 200), null, 0, false));

        // Boutons
        JPanel actions = new JPanel(new GridLayoutManager(1, 5, new Insets(0,0,0,0), 12, 0));
        actions.setOpaque(false);
        addAlbumButton = new JButton();
        removeAlbumButton = new JButton();

        actions.add(new JPanel(){ { setOpaque(false);} }, new com.intellij.uiDesigner.core.GridConstraints(
                0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null, 0, false));

        actions.add(addAlbumButton, new com.intellij.uiDesigner.core.GridConstraints(
                0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        actions.add(removeAlbumButton, new com.intellij.uiDesigner.core.GridConstraints(
                0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        actions.add(new JPanel(){ { setOpaque(false);} }, new com.intellij.uiDesigner.core.GridConstraints(
                0, 3, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null, 0, false));

        mainPanel.add(actions, new GridConstraints(12, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        // espace bas
        mainPanel.add(space(12), new GridConstraints(13, 1, 1, 1,
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
            AddArtist view = new AddArtist();
            // Logo optionnel
            view.setLogoFromAbsolutePath(
                    "C:\\Users\\grany\\OneDrive\\HEPL\\BAC2\\Q2\\Programmation orientée objet en Java\\ProjetFInale-DiskHouse\\src\\main\\resources\\LogoMini.png",
                    180, 48
            );

            // Contrôleur
            DiskHouse.Controller.AddArtistController controller =
                    new DiskHouse.Controller.AddArtistController(view);
            controller.initController();

            // éviter le focus auto dans un field
            SwingUtilities.invokeLater(() -> view.getRootPane().requestFocusInWindow());

            view.setVisible(true);
        });
    }
}
