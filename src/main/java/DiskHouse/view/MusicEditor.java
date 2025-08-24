package DiskHouse.view;

import DiskHouse.Controller.MusicEditorController;   // Contrôleur MVC
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * MusicEditor - Vue compacte en JDialog (modale) - GridLayoutManager + MVC
 * - Aperçu image centré (box fixe 170x170)
 * - Champs/combos 36px + placeholders
 * - Boutons "+" 36x36
 * - Chargement image fiable (ImageIO) depuis URL/fichier/ressource
 *
 * Ouverture typique depuis MainPage (JFrame) :
 *   // sans listener (comportement interne)
 *   new MusicEditor(mainFrame).setVisible(true);
 *
 *   // AVEC listener (ex: pour rafraîchir playlist après save)
 *   MusicEditor dlg = new MusicEditor(mainFrame, /*autoWire* / false);
 *   new MusicEditorController(dlg, myListener).initController();
 *   // OU encore plus simple :
 *   dlg.wireControllerWith(myListener);
 *   // puis :
 *   ctrl.openForCreate(mainFrame);  // ou openForEdit(...)
 */
public class MusicEditor extends JDialog {

    private JPanel mainPanel;
    private JLabel logoLabel;

    private JLabel imagePreviewLabel;

    private JTextField titreField;
    private JComboBox<String> artisteCombo;
    private JButton addArtisteButton;
    private JComboBox<String> albumCombo;
    private JButton addAlbumButton;
    private JTextField dureeField;

    private JButton saveButton;
    private JButton cancelButton;

    private static final int PREVIEW_BOX = 170;

    // éviter de rebranchement multiple du contrôleur
    private boolean controllerWired = false;

    /** Constructeur modale, attaché à une fenêtre parente (recommandé). */
    public MusicEditor(Window owner) {
        this(owner, true);
    }

    /** Constructeur sans parent (fallback / démo). */
    public MusicEditor() { this(null, true); }

    /**
     * Constructeur avancé : possibilité de désactiver l'auto‑wiring du contrôleur
     * pour pouvoir injecter un Listener depuis l'extérieur.
     */
    public MusicEditor(Window owner, boolean autoWireController) {
        super(owner, "DiskHouse - Music Editor", ModalityType.APPLICATION_MODAL);
        $$$setupUI$$$();                // crée TOUT (y compris les boutons)
        setContentPane(mainPanel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        styleUI();                      // puis on les stylise (non-null)

        mainPanel.setPreferredSize(new Dimension(760, 520));
        pack();
        setLocationRelativeTo(owner);

        if (autoWireController) {
            wireController(); // version interne, sans listener
        }

        cancelButton.addActionListener(e -> dispose());
    }

    /** Instancie et initialise le contrôleur une seule fois (sans listener). */
    private void wireController() {
        if (controllerWired) return;
        new MusicEditorController(this).initController();
        controllerWired = true;
    }

    /**
     * Variante : branche le contrôleur avec un Listener fourni par l'appelant (MainPage).
     * Renvoie l'instance créée pour que l'appelant puisse appeler openForCreate/openForEdit.
     */
    public MusicEditorController wireControllerWith(MusicEditorController.Listener listener) {
        if (controllerWired) return null;
        MusicEditorController c = new MusicEditorController(this, listener);
        c.initController();
        controllerWired = true;
        return c;
    }

    /* ======== API contrôleurs ======== */
    public JLabel getImagePreviewLabel()       { return imagePreviewLabel; }
    public JButton getAddArtisteButton()       { return addArtisteButton; }
    public JButton getAddAlbumButton()         { return addAlbumButton; }
    public JComboBox<String> getArtisteCombo() { return artisteCombo; }
    public JComboBox<String> getAlbumCombo()   { return albumCombo; }
    public JTextField getTitreField()          { return titreField; }
    public JTextField getDureeField()          { return dureeField; }
    public JButton getSaveButton()             { return saveButton; }
    public JButton getCancelButton()           { return cancelButton; }

    /* ======== Image preview ======== */
    public void setPreviewImage(Image img) {
        if (img == null) {
            imagePreviewLabel.setIcon(null);
            imagePreviewLabel.setText("image");
            return;
        }
        int w = img.getWidth(null), h = img.getHeight(null);
        // Forcer le chargement de l'image si besoin (boucle courte)
        int tries = 0;
        while ((w <= 0 || h <= 0) && tries < 10) {
            try { Thread.sleep(10); } catch (InterruptedException ignored) {}
            w = img.getWidth(null);
            h = img.getHeight(null);
            tries++;
        }
        if (w <= 0 || h <= 0) {
            imagePreviewLabel.setIcon(null);
            imagePreviewLabel.setText("image");
            return;
        }
        double s = Math.min(PREVIEW_BOX / (double) w, PREVIEW_BOX / (double) h);
        int nw = Math.max(1, (int) Math.round(w * s));
        int nh = Math.max(1, (int) Math.round(h * s));
        Image scaled = img.getScaledInstance(nw, nh, Image.SCALE_SMOOTH);

        BufferedImage canvas = new BufferedImage(PREVIEW_BOX, PREVIEW_BOX, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        int x = (PREVIEW_BOX - nw) / 2, y = (PREVIEW_BOX - nh) / 2;
        g.drawImage(scaled, x, y, null);
        g.dispose();

        imagePreviewLabel.setText(null);
        imagePreviewLabel.setIcon(new ImageIcon(canvas));
    }

    public void setPreviewImage(String pathOrUrl) {
        if (pathOrUrl == null || pathOrUrl.isEmpty()) {
            imagePreviewLabel.setIcon(null);
            imagePreviewLabel.setText("image");
            return;
        }
        try {
            BufferedImage img = loadImageBlocking(pathOrUrl);
            if (img == null) throw new IOException();
            int w = img.getWidth(null), h = img.getHeight(null);
            int box = PREVIEW_BOX;
            double s = Math.min(box / (double) w, box / (double) h);
            int nw = Math.max(1, (int) Math.round(w * s));
            int nh = Math.max(1, (int) Math.round(h * s));
            Image scaled = img.getScaledInstance(nw, nh, Image.SCALE_SMOOTH);
            BufferedImage canvas = new BufferedImage(box, box, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = canvas.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            int x = (box - nw) / 2, y = (box - nh) / 2;
            g.drawImage(scaled, x, y, null);
            g.dispose();
            imagePreviewLabel.setText(null);
            imagePreviewLabel.setIcon(new ImageIcon(canvas));
        } catch (Exception e) {
            imagePreviewLabel.setIcon(null);
            imagePreviewLabel.setText("image");
        }
    }

    public void loadData(String titre, String artistesCsv, String albumName, String dureeMmSs, String coverPathOrUrl) {
        titreField.setText(titre != null ? titre : "");
        dureeField.setText(dureeMmSs != null ? dureeMmSs : "");

        artisteCombo.removeAllItems();
        if (artistesCsv != null && !artistesCsv.isBlank()) {
            for (String a : artistesCsv.split("\\s*,\\s*")) if (!a.isBlank()) artisteCombo.addItem(a);
            if (artisteCombo.getItemCount() > 0) artisteCombo.setSelectedIndex(0);
        } else artisteCombo.setSelectedItem(null);

        albumCombo.removeAllItems();
        if (albumName != null && !albumName.isBlank()) {
            albumCombo.addItem(albumName);
            albumCombo.setSelectedIndex(0);
        } else albumCombo.setSelectedItem(null);

        if (coverPathOrUrl != null && !coverPathOrUrl.isBlank()) setPreviewImage(coverPathOrUrl);
        else setPreviewImage((Image) null);
    }

    private BufferedImage loadImageBlocking(String pathOrUrl) throws IOException {
        if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")
                || pathOrUrl.startsWith("jar:") || pathOrUrl.startsWith("file:")) {
            URL url = URI.create(pathOrUrl).toURL();
            return ImageIO.read(url);
        }
        if (Files.exists(Paths.get(pathOrUrl))) return ImageIO.read(new File(pathOrUrl));
        String res = pathOrUrl.startsWith("/") ? pathOrUrl : "/" + pathOrUrl;
        URL cp = getClass().getResource(res);
        if (cp != null) return ImageIO.read(cp);
        throw new IOException("Image introuvable: " + pathOrUrl);
    }

    /* ======== Style ======== */
    private void styleUI() {
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(14, 14, 14, 14));

        logoLabel.setText("DiskHouse");
        logoLabel.setFont(logoLabel.getFont().deriveFont(Font.BOLD, 24f));
        logoLabel.setForeground(new Color(0x123A6B));
        logoLabel.setBorder(new EmptyBorder(0, 6, 6, 0));

        imagePreviewLabel.setText("image");
        imagePreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imagePreviewLabel.setVerticalAlignment(SwingConstants.CENTER);
        imagePreviewLabel.setOpaque(true);
        imagePreviewLabel.setBackground(new Color(0xC9D7EE));
        imagePreviewLabel.setBorder(new LineBorder(new Color(0xAFC0DB), 1, true));
        Dimension fixed = new Dimension(PREVIEW_BOX, PREVIEW_BOX);
        imagePreviewLabel.setPreferredSize(fixed);
        imagePreviewLabel.setMinimumSize(fixed);
        imagePreviewLabel.setMaximumSize(fixed);

        float F = 15f;
        int H = 36;
        setFieldLook((PlaceholderTextField) titreField, F, H, "Titre");
        setComboLook(artisteCombo, F, H, "Artiste");
        setComboLook(albumCombo, F, H, "Album");
        setFieldLook((PlaceholderTextField) dureeField, F, H, "Durée (mm:ss)");

        stylePlus(addArtisteButton);
        stylePlus(addAlbumButton);

        stylePrimary(saveButton);
        styleGhost(cancelButton);
    }

    private void setFieldLook(PlaceholderTextField tf, float fontPx, int height, String placeholder) {
        tf.setPlaceholder(placeholder);
        tf.setFont(tf.getFont().deriveFont(Font.BOLD, fontPx));
        tf.setBorder(new EmptyBorder(6, 10, 6, 10));
        tf.setBackground(new Color(0xE9EAED));
        tf.setCaretColor(new Color(0x333333));
        tf.setPreferredSize(new Dimension(0, height));
        tf.setMinimumSize(new Dimension(120, height));
    }

    private void setComboLook(JComboBox<String> cb, float fontPx, int height, String placeholder) {
        cb.setFont(cb.getFont().deriveFont(Font.PLAIN, fontPx));
        cb.setBackground(new Color(0xE9EAED));
        cb.setBorder(new EmptyBorder(4, 8, 4, 8));
        cb.setPreferredSize(new Dimension(0, height));
        cb.setMinimumSize(new Dimension(120, height));
        cb.setRenderer(new ComboPlaceholderRenderer(placeholder));
        cb.setSelectedItem(null);
    }

    private void stylePlus(JButton b) {
        b.setText("＋");
        b.setFocusPainted(false);
        b.setBackground(new Color(0xECECEC));
        b.setBorder(new LineBorder(new Color(0xD0D0D0), 2, true));
        b.setFont(b.getFont().deriveFont(Font.BOLD, 16f));
        b.setPreferredSize(new Dimension(36, 36));
        b.setMinimumSize(new Dimension(36, 36));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void stylePrimary(JButton b) {
        b.setText("Enregistrer");
        b.setFocusPainted(false);
        b.setBackground(new Color(0x123A6B));
        b.setForeground(Color.WHITE);
        b.setBorder(new EmptyBorder(8, 14, 8, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleGhost(JButton b) {
        b.setText("Annuler");
        b.setFocusPainted(false);
        b.setBackground(new Color(0xF5F7FA));
        b.setBorder(new LineBorder(new Color(0xD0D7E2), 1, true));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /* ======== Placeholders ======== */
    private static final class PlaceholderTextField extends JTextField {
        private String placeholder;
        public void setPlaceholder(String text) { this.placeholder = text; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (placeholder == null || !getText().isEmpty() || isFocusOwner()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(new Color(0x9F9494));
            g2.setFont(getFont().deriveFont(Font.BOLD));
            Insets i = getInsets();
            int y = (getHeight() + g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent()) / 2;
            g2.drawString(placeholder, i.left, y);
            g2.dispose();
        }
    }

    private static final class ComboPlaceholderRenderer extends BasicComboBoxRenderer {
        private final String placeholder;
        private final Color hint = new Color(0x9F9494);
        ComboPlaceholderRenderer(String placeholder) { this.placeholder = placeholder; }
        @Override
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public Component getListCellRendererComponent(JList list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (index == -1 && value == null) {
                setText(placeholder);
                setForeground(hint);
                setFont(getFont().deriveFont(Font.BOLD));
            }
            return this;
        }
    }

    /* ======== UI (GridLayoutManager) ======== */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(15, 12, new Insets(0, 0, 0, 0), 6, 6));

        // colonnes latérales vides (marges)
        mainPanel.add(space(), gc(0, 0, 15, 1, GridConstraints.FILL_BOTH));
        mainPanel.add(space(), gc(0, 11, 15, 1, GridConstraints.FILL_BOTH));

        // header
        JPanel header = new JPanel(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), 0, 0));
        header.setOpaque(false);
        logoLabel = new JLabel();
        header.add(logoLabel, gc(0, 0, 1, 1, GridConstraints.FILL_NONE, GridConstraints.ANCHOR_WEST));
        mainPanel.add(header, gc(0, 1, 1, 10, GridConstraints.FILL_HORIZONTAL, GridConstraints.ANCHOR_WEST));

        mainPanel.add(space(4), gc(1, 1, 1, 10, GridConstraints.FILL_BOTH));

        // === APERÇU IMAGE CENTRÉ PLEINE LARGEUR UTILE (COL 1→10) ===
        JPanel previewWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        previewWrapper.setOpaque(false);
        imagePreviewLabel = new JLabel();
        previewWrapper.add(imagePreviewLabel);
        mainPanel.add(
                previewWrapper,
                gc(2, 1, 2, 10, GridConstraints.FILL_NONE, GridConstraints.ANCHOR_CENTER)
        );

        mainPanel.add(space(4), gc(4, 1, 1, 10, GridConstraints.FILL_BOTH));

        JPanel rowTitle = new JPanel(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), 0, 0));
        rowTitle.setOpaque(false);
        titreField = new PlaceholderTextField();
        rowTitle.add(titreField, gc(0, 0, 1, 1, GridConstraints.FILL_HORIZONTAL));
        mainPanel.add(rowTitle, gc(5, 2, 1, 8, GridConstraints.FILL_HORIZONTAL));

        mainPanel.add(space(2), gc(6, 2, 1, 8, GridConstraints.FILL_BOTH));

        JPanel rowArtist = new JPanel(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), 8, 0));
        rowArtist.setOpaque(false);
        artisteCombo = new JComboBox<>();
        rowArtist.add(artisteCombo, gc(0, 0, 1, 1, GridConstraints.FILL_HORIZONTAL));
        addArtisteButton = new JButton();
        rowArtist.add(addArtisteButton, gc(0, 1, 1, 1, GridConstraints.FILL_NONE, GridConstraints.ANCHOR_EAST));
        mainPanel.add(rowArtist, gc(7, 2, 1, 8, GridConstraints.FILL_HORIZONTAL));

        mainPanel.add(space(2), gc(8, 2, 1, 8, GridConstraints.FILL_BOTH));

        JPanel rowAlbum = new JPanel(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), 8, 0));
        rowAlbum.setOpaque(false);
        albumCombo = new JComboBox<>();
        rowAlbum.add(albumCombo, gc(0, 0, 1, 1, GridConstraints.FILL_HORIZONTAL));
        addAlbumButton = new JButton();
        rowAlbum.add(addAlbumButton, gc(0, 1, 1, 1, GridConstraints.FILL_NONE, GridConstraints.ANCHOR_EAST));
        mainPanel.add(rowAlbum, gc(9, 2, 1, 8, GridConstraints.FILL_HORIZONTAL));

        mainPanel.add(space(2), gc(10, 2, 1, 8, GridConstraints.FILL_BOTH));

        JPanel rowDur = new JPanel(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), 0, 0));
        rowDur.setOpaque(false);
        dureeField = new PlaceholderTextField();
        rowDur.add(dureeField, gc(0, 0, 1, 1, GridConstraints.FILL_HORIZONTAL));
        mainPanel.add(rowDur, gc(11, 2, 1, 8, GridConstraints.FILL_HORIZONTAL));

        // Actions
        JPanel actions = new JPanel(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), 8, 0));
        actions.setOpaque(false);
        actions.add(space(), gc(0, 0, 1, 1, GridConstraints.FILL_BOTH));
        cancelButton = new JButton();
        actions.add(cancelButton, gc(0, 1, 1, 1, GridConstraints.FILL_NONE, GridConstraints.ANCHOR_EAST));
        saveButton = new JButton();
        actions.add(saveButton, gc(0, 2, 1, 1, GridConstraints.FILL_NONE, GridConstraints.ANCHOR_EAST));
        mainPanel.add(actions, gc(12, 1, 1, 10, GridConstraints.FILL_HORIZONTAL));

        mainPanel.add(space(4), gc(13, 1, 1, 10, GridConstraints.FILL_BOTH));
        mainPanel.add(space(), gc(14, 1, 1, 10, GridConstraints.FILL_BOTH));
    }

    private GridConstraints gc(int r, int c, int rs, int cs, int fill) {
        return new GridConstraints(
                r, c, rs, cs,
                GridConstraints.ANCHOR_CENTER, fill,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false
        );
    }

    private GridConstraints gc(int r, int c, int rs, int cs, int fill, int anchor) {
        return new GridConstraints(
                r, c, rs, cs,
                anchor, fill,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false
        );
    }

    private JComponent space() { return space(0); }
    private JComponent space(int h) {
        JPanel p = new JPanel(); p.setOpaque(false);
        if (h > 0) p.setPreferredSize(new Dimension(0, h));
        return p;
    }

    public JComponent $$$getRootComponent$$$() { return mainPanel; }

    /** Petite démo indépendante : ouvre le dialog sans parent. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MusicEditor().setVisible(true));
    }
}
