package DiskHouse.Controller;

import DiskHouse.view.AlbumEditor;
import DiskHouse.view.MusicEditor;
import DiskHouse.view.ArtisteEditor;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.util.Collection;
import java.util.Objects;

public class MusicEditorController implements IController<MusicEditor> {

    private final MusicEditor view;
    private String selectedImagePath;

    private static final String LOGO_ABSOLUTE_PATH =
            "C:\\Users\\grany\\OneDrive\\HEPL\\BAC2\\Q2\\Programmation orientée objet en Java\\ProjetFInale-DiskHouse\\src\\main\\resources\\LogoMini.png";

    public MusicEditorController(MusicEditor view) {
        this.view = Objects.requireNonNull(view);
    }

    @Override
    public MusicEditor getView() { return view; }

    @Override
    public void initController() {
        // Image cliquable
        JLabel preview = view.getImagePreviewLabel();
        preview.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        preview.setToolTipText("Cliquer pour choisir une image (png/jpg/jpeg)");
        preview.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onChooseImage(); }
        });

        // Ouvrir sous-éditeurs
        view.getAddArtisteButton().addActionListener(e -> onOpenArtistEditor());
        view.getAddAlbumButton().addActionListener(e -> onOpenAlbumEditor());

        // Enter = valider
        JRootPane root = view.getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "do-validate");
        am.put("do-validate", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (validateForm()) {
                    showInfo(view, "Formulaire OK ✅", "Validation");
                }
            }
        });
    }

    /* ================= Actions UI ================= */

    private void onChooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choisir une image");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("Images (png, jpg, jpeg)", "png", "jpg", "jpeg"));

        if (chooser.showOpenDialog(view) == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
            selectedImagePath = chooser.getSelectedFile().getAbsolutePath();
            ImageIcon icon = new ImageIcon(selectedImagePath);
            view.setPreviewImage(icon.getImage());
        }
    }

    private void onOpenArtistEditor() {
        //dialogue test


        // Tout depuis le contrôleur (pas d’auto‑wiring dans la vue)
        SwingUtilities.invokeLater(() -> {
            ArtisteEditor artistView = new ArtisteEditor();             // vue simple, non visible
            artistView.setLogoFromAbsolutePath(LOGO_ABSOLUTE_PATH, 180, 48);

            ArtisteEditorController artistController = new ArtisteEditorController(artistView);
            artistController.initController();

            artistView.pack();
            artistView.setLocationRelativeTo(view);
            artistView.setVisible(true);
            artistView.toFront();
            artistView.requestFocus();
        });
    }

    private void onOpenAlbumEditor() {


        SwingUtilities.invokeLater(() -> {
            AlbumEditor albumView = new AlbumEditor();
            albumView.setLogoFromAbsolutePath(LOGO_ABSOLUTE_PATH, 180, 48);

            AlbumEditorController albumController = new AlbumEditorController(albumView);
            albumController.initController();

            albumView.pack();
            albumView.setLocationRelativeTo(view);
            albumView.setVisible(true);
            albumView.toFront();
            albumView.requestFocus();
        });
    }

    /* ================= API pour peupler ================= */

    public void setArtistes(Collection<String> artistes) {
        @SuppressWarnings("unchecked")
        DefaultComboBoxModel<String> model =
                (DefaultComboBoxModel<String>) view.getArtisteCombo().getModel();
        model.removeAllElements();
        if (artistes != null) {
            for (String a : artistes) model.addElement(a);
        }
        view.getArtisteCombo().setSelectedItem(null);
    }

    public void setAlbums(Collection<String> albums) {
        @SuppressWarnings("unchecked")
        DefaultComboBoxModel<String> model =
                (DefaultComboBoxModel<String>) view.getAlbumCombo().getModel();
        model.removeAllElements();
        if (albums != null) {
            for (String a : albums) model.addElement(a);
        }
        view.getAlbumCombo().setSelectedItem(null);
    }

    /* ================= Getters ================= */

    public String getSelectedImagePath() { return selectedImagePath; }

    public String getTitre() {
        String v = view.getTitreField().getText();
        return v == null ? "" : v.trim();
    }

    public String getArtiste() {
        Object sel = view.getArtisteCombo().getSelectedItem();
        return sel == null ? "" : sel.toString().trim();
    }

    public String getAlbum() {
        Object sel = view.getAlbumCombo().getSelectedItem();
        return sel == null ? "" : sel.toString().trim();
    }

    public String getDuree() {
        String v = view.getDureeField().getText();
        return v == null ? "" : v.trim();
    }

    public int getDurationInSeconds() {
        String d = getDuree();
        if (!d.matches("^\\d{1,2}:[0-5]\\d$")) return 0;
        String[] p = d.split(":");
        return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
    }

    /* ================= Validation ================= */

    public boolean validateForm() {
        if (getTitre().isEmpty()) {
            showError(view, "Le titre est requis.", "Erreur");
            return false;
        }
        if (getArtiste().isEmpty()) {
            showError(view, "Sélectionne un artiste.", "Erreur");
            return false;
        }
        if (getAlbum().isEmpty()) {
            showError(view, "Sélectionne un album.", "Erreur");
            return false;
        }
        if (getDuree().isEmpty()) {
            showError(view, "La durée est requise.", "Erreur");
            return false;
        }
        if (!getDuree().matches("^\\d{1,2}:[0-5]\\d$")) {
            showError(view, "Durée invalide. Utilise le format mm:ss (ex: 03:25).", "Erreur");
            return false;
        }
        return true;
    }
}
