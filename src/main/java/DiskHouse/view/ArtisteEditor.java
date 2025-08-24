package DiskHouse.view;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

/**
 * Vue "Artiste" (JDialog MODAL, sans logique métier).
 * - Placeholders visuels:
 *    * Portrait: "Choisir une image"
 *    * Champ artiste: "Choisir un artiste"
 *    * Albums: "Ajouter un album" si liste vide
 * - Hauteurs/tailles compactées.
 * - Aucun focus texte au premier affichage (focus sur la pochette).
 *
 * Le contrôleur utilise :
 *   - setArtistName(String), setPortraitImage(Image)
 *   - getPortraitLabel(), getArtistNameField(), getEditArtistButton()
 *   - getAlbumsList(), getAlbumsScroll(), getAddAlbumButtonView(), getRemoveAlbumButtonView()
 *   - getCancelButton(), getOkButton(), ensureAlbumsPlaceholder()
 */
public class ArtisteEditor extends JDialog {

    // Root
    private JPanel mainPanel;

    // Header
    private JLabel logoLabel;
    private JLabel appTitleLabel;

    // Ligne artiste
    private JLabel portraitLabel;       // pochette cliquable
    private JTextField artistNameField; // pseudo
    private JButton editArtistButton;   // ✎

    // Albums
    private JLabel albumsLabel;
    private JList<String> albumsList;
    private JScrollPane albumsScroll;
    private JPanel albumsPlaceholderPanel; // placeholder si vide

    // Actions albums
    private JButton addAlbumButton;     // +
    private JButton removeAlbumButton;  // 🗑

    // Bas
    private JButton cancelButton;
    private JButton okButton;

    // UI
    private static final Dimension PORTRAIT_SIZE = new Dimension(120, 120); // ← plus petit
    private static final Color BLUE = new Color(0x0E2A62);
    private static final Color PLACEHOLDER = new Color(0x9AAAC3);

    public ArtisteEditor(Window owner) {
        super(owner, "DiskHouse - Artiste", ModalityType.APPLICATION_MODAL);
        $$$setupUI$$$();
        setContentPane(mainPanel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        styliser();
        refreshAlbumsPlaceholder();

        // Pas de focus texte à l’ouverture : on force le focus sur la pochette
        addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent e) {
                SwingUtilities.invokeLater(() -> portraitLabel.requestFocusInWindow());
            }
        });

        setMinimumSize(new Dimension(640, 480));
        setLocationRelativeTo(owner);
        // NE PAS setVisible ici (le contrôleur gère)
    }

    public ArtisteEditor() { this(null); }

    /* -------------------- Styling -------------------- */

    private void styliser() {
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(16, 16, 16, 16)); // marges un peu plus petites

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
        portraitLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        portraitLabel.setToolTipText("Choisir une image");
        portraitLabel.setFocusable(true); // pour pouvoir y mettre le focus au démarrage
        setPortraitPlaceholder();

        // Champ artiste + placeholder + taille plus compacte
        artistNameField.setBorder(null);
        artistNameField.setFont(artistNameField.getFont().deriveFont(Font.BOLD, 28f));
        artistNameField.setForeground(BLUE);
        artistNameField.setOpaque(false);
        installTextPlaceholder(artistNameField, "Choisir un pseudo");

        // Bouton ✎
        styliserAction(editArtistButton, "✎");
        editArtistButton.setToolTipText("Éditer le pseudo");

        // Albums
        albumsLabel.setText("Albums");
        albumsLabel.setFont(albumsLabel.getFont().deriveFont(Font.BOLD, 13f));
        albumsLabel.setForeground(Color.DARK_GRAY);

        // Liste albums plus compacte
        albumsList.setVisibleRowCount(5);              // ← 6 -> 5
        albumsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        albumsList.setFixedCellHeight(24);             // ← 28 -> 24
        albumsList.setBorder(new EmptyBorder(4, 10, 4, 10));
        albumsScroll.setBorder(new LineBorder(new Color(0x9AAAC3), 1, false));
        albumsScroll.setPreferredSize(new Dimension(540, 170)); // ← 560x220 -> 540x170

        // Placeholder "Ajouter un album"
        albumsPlaceholderPanel = new JPanel(new GridBagLayout());
        albumsPlaceholderPanel.setBackground(Color.WHITE);
        JLabel ph = new JLabel("Ajouter un album");
        ph.setForeground(PLACEHOLDER);
        ph.setFont(ph.getFont().deriveFont(Font.ITALIC, 13f));
        albumsPlaceholderPanel.add(ph);
        if (albumsList.getModel() instanceof DefaultListModel<?> dm) {
            dm.addListDataListener(new javax.swing.event.ListDataListener() {
                @Override public void intervalAdded(javax.swing.event.ListDataEvent e) { refreshAlbumsPlaceholder(); }
                @Override public void intervalRemoved(javax.swing.event.ListDataEvent e) { refreshAlbumsPlaceholder(); }
                @Override public void contentsChanged(javax.swing.event.ListDataEvent e) { refreshAlbumsPlaceholder(); }
            });
        }

        // Boutons
        styliserAction(addAlbumButton, "＋");
        addAlbumButton.setToolTipText("Ajouter un album");
        styliserAction(removeAlbumButton, "🗑");
        removeAlbumButton.setToolTipText("Supprimer l’album sélectionné");

        // Bas
        styleGhost(cancelButton, "Annuler");
        stylePrimary(okButton, "Enregistrer");
    }

    private void setPortraitPlaceholder() {
        portraitLabel.setIcon(null);
        portraitLabel.setText("<html><div style='text-align:center;color:#5b6b85;'>Choisir<br>une image</div></html>");
    }

    private void installTextPlaceholder(JTextField field, String placeholderText) {
        field.setToolTipText(placeholderText);
        Color normalColor = BLUE;

        Runnable applyPh = () -> {
            if (field.getText() == null || field.getText().isBlank()) {
                field.setForeground(PLACEHOLDER);
                field.setFont(field.getFont().deriveFont(Font.ITALIC, field.getFont().getSize2D()));
                field.setText(placeholderText);
            }
        };
        Runnable removePh = () -> {
            if (field.getForeground().equals(PLACEHOLDER) && placeholderText.equals(field.getText())) {
                field.setText("");
                field.setFont(field.getFont().deriveFont(Font.PLAIN, field.getFont().getSize2D()));
                field.setForeground(normalColor);
            }
        };

        applyPh.run();
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { removePh.run(); }
            @Override public void focusLost(FocusEvent e)  { applyPh.run(); }
        });
    }

    private void refreshAlbumsPlaceholder() {
        boolean empty = albumsList.getModel().getSize() == 0;
        if (empty) albumsScroll.setViewportView(albumsPlaceholderPanel);
        else       albumsScroll.setViewportView(albumsList);
        albumsScroll.revalidate();
        albumsScroll.repaint();
    }

    private void styliserAction(JButton b, String text) {
        b.setText(text);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BLUE, 2, true),
                new EmptyBorder(6, 10, 6, 10)
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
        b.setBorder(new EmptyBorder(7, 14, 7, 14)); // compact
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleGhost(JButton b, String text) {
        b.setText(text);
        b.setFocusPainted(false);
        b.setBackground(new Color(0xF5F7FA));
        b.setBorder(new LineBorder(new Color(0xD0D7E2), 1, true));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /* -------------------- API pour contrôleur -------------------- */

    public void setArtistName(String name) {
        if (name == null || name.isBlank()) {
            artistNameField.setText("");
            artistNameField.postActionEvent();
        } else {
            artistNameField.setText(name);
            artistNameField.setCaretPosition(artistNameField.getText().length());
            artistNameField.setForeground(BLUE);
            artistNameField.setFont(artistNameField.getFont().deriveFont(Font.BOLD, 28f));
        }
    }

    /** Centre l’image dans un carré PORTRAIT_SIZE. */
    public void setPortraitImage(Image source) {
        if (source == null) { setPortraitPlaceholder(); return; }
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
        g.setComposite(AlphaComposite.SrcOver);
        g.drawImage(scaled, x, y, null);
        g.dispose();

        portraitLabel.setText(null);
        portraitLabel.setIcon(new ImageIcon(canvas));
    }

    public void setPortraitImage(String imageURL) {
        if (imageURL == null || imageURL.isEmpty()) {
            setPortraitPlaceholder();
            return;
        }
        try {
            ImageIcon icon;
            if (imageURL.startsWith("http") || imageURL.startsWith("file:")) {
                java.net.URL url = java.net.URI.create(imageURL).toURL();
                icon = new ImageIcon(url);
            } else {
                icon = new ImageIcon(imageURL);
            }
            Image img = icon.getImage().getScaledInstance(PORTRAIT_SIZE.width, PORTRAIT_SIZE.height, Image.SCALE_SMOOTH);
            portraitLabel.setIcon(new ImageIcon(img));
            portraitLabel.setText("");
        } catch (Exception e) {
            setPortraitPlaceholder();
        }
    }

    public void ensureAlbumsPlaceholder() { refreshAlbumsPlaceholder(); }

    // Getters MVC
    public JLabel getPortraitLabel() { return portraitLabel; }
    public JTextField getArtistNameField() { return artistNameField; }
    public JButton getEditArtistButton() { return editArtistButton; }
    public JList<String> getAlbumsList() { return albumsList; }
    public JScrollPane getAlbumsScroll() { return albumsScroll; }
    public JButton getAddAlbumButtonView() { return addAlbumButton; }
    public JButton getRemoveAlbumButtonView() { return removeAlbumButton; }
    public JButton getCancelButton() { return cancelButton; }
    public JButton getOkButton() { return okButton; }

    /* -------------------- Layout (IntelliJ GridLayoutManager) -------------------- */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(14, 3, new Insets(0, 0, 0, 0), 0, 0));

        // colonnes respirations
        mainPanel.add(empty(), new GridConstraints(0, 0, 14, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        mainPanel.add(empty(), new GridConstraints(0, 2, 14, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        // header
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

        // espace
        mainPanel.add(space(8), new GridConstraints(1, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // ligne artiste
        JPanel row = new JPanel(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), 10, 0));
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
        mainPanel.add(space(8), new GridConstraints(3, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // label albums
        albumsLabel = new JLabel("Albums");
        mainPanel.add(albumsLabel, new GridConstraints(4, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // liste albums
        albumsList = new JList<>(new DefaultListModel<>());
        albumsScroll = new JScrollPane(albumsList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(albumsScroll, new GridConstraints(5, 1, 5, 1, // ← hauteur globale moindre (6 -> 5 rows)
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW,
                null, new Dimension(540, 170), null, 0, false));

        // actions
        JPanel actions = new JPanel(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), 10, 0));
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

        mainPanel.add(actions, new GridConstraints(10, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // bas : boutons
        JPanel bottom = new JPanel(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), 6, 0));
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
        mainPanel.add(bottom, new GridConstraints(11, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        // espace bas
        mainPanel.add(space(6), new GridConstraints(12, 1, 2, 1,
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
}
