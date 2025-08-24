package DiskHouse.Controller;

import DiskHouse.model.DAO.AlbumFileDAO;
import DiskHouse.model.DAO.ArtisteFileDAO;
import DiskHouse.model.DAO.MusicFileDAO;
import DiskHouse.model.entity.Album;
import DiskHouse.model.entity.Artiste;
import DiskHouse.model.entity.Musique;
import DiskHouse.view.MusicEditor;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class MusicEditorController implements IController<MusicEditor> {

    public interface Listener {
        void onMusicCreated(Musique created);
        void onMusicUpdated(Musique updated);
    }

    private final MusicEditor view;
    private final Listener listener;

    private final MusicFileDAO   musicDAO   = new MusicFileDAO("data/musiques.dat");
    private final ArtisteFileDAO artisteDAO = new ArtisteFileDAO("data/artistes.dat");
    private final AlbumFileDAO   albumDAO   = new AlbumFileDAO("data/albums.dat");

    private String selectedImagePath;
    private Musique current; // null en création

    public MusicEditorController(MusicEditor view) { this(view, null); }
    public MusicEditorController(MusicEditor view, Listener listener) {
        this.view = Objects.requireNonNull(view);
        this.listener = listener;
    }

    @Override public MusicEditor getView() { return view; }

    @Override
    public void initController() {
        initImageChooser();
        initSubEditors();
        initValidationShortcut();
        preloadCombosFromDAO();
    }

    private void initImageChooser() {
        JLabel preview = view.getImagePreviewLabel();
        preview.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        preview.setToolTipText("Cliquer pour choisir une image (png/jpg/jpeg)");
        preview.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onChooseImage(); }
        });
    }

    private void initSubEditors() {
        view.getAddArtisteButton().addActionListener(e -> onOpenArtistEditor());
        view.getAddAlbumButton().addActionListener(e -> onOpenAlbumEditor());
    }

    private void initValidationShortcut() {
        JRootPane rootPane = view.getRootPane();
        InputMap im = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = rootPane.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "validate-form");
        am.put("validate-form", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { onValidateAndSave(); }
        });
    }

    /* ===================== OUVERTURE ===================== */
    public void openForCreate(Window parent) {
        current = null;
        selectedImagePath = null;
        view.setPreviewImage((Image) null); // éviter l'ambiguïté
        view.getTitreField().setText("");
        view.getDureeField().setText("");
        view.getArtisteCombo().setSelectedItem(null);
        view.getAlbumCombo().setSelectedItem(null);
        show(parent);
    }

    public void openForEdit(Window parent, Musique musique) {
        current = Objects.requireNonNull(musique);

        view.getTitreField().setText(safe(musique.getTitre()));
        view.getDureeField().setText(floatMinutesToMmSs(musique.getDuree()));

        selectedImagePath = musique.getCoverImageURL();
        if (selectedImagePath != null && !selectedImagePath.isBlank()) {
            view.setPreviewImage(new ImageIcon(selectedImagePath).getImage());
        } else {
            view.setPreviewImage((Image) null);
        }

        if (musique.getArtistes() != null && !musique.getArtistes().isEmpty()) {
            Artiste a0 = musique.getArtistes().get(0);
            view.getArtisteCombo().setSelectedItem(displayName(a0));
        } else {
            view.getArtisteCombo().setSelectedItem(null);
        }
        if (musique.getAlbum() != null) {
            view.getAlbumCombo().setSelectedItem(safe(musique.getAlbum().getTitreAlbum()));
        } else {
            view.getAlbumCombo().setSelectedItem(null);
        }
        show(parent);
    }

    private void show(Window parent) {
        if (view.getWidth() == 0 || view.getHeight() == 0) view.pack();
        view.setLocationRelativeTo(parent);
        view.setVisible(true);
        view.toFront();
        view.requestFocus();
    }

    /* ===================== ACTIONS ===================== */
    private void onChooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choisir une image");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("Images (png, jpg, jpeg)", "png", "jpg", "jpeg"));
        int result = chooser.showOpenDialog(view);
        if (result == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
            selectedImagePath = chooser.getSelectedFile().getAbsolutePath();
            ImageIcon icon = new ImageIcon(selectedImagePath);
            view.setPreviewImage(icon.getImage());
        }
    }

    private void onOpenArtistEditor() {
        JOptionPane.showMessageDialog(view, "Ouvre l'éditeur d'artiste (à intégrer).", "Artiste", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onOpenAlbumEditor() {
        JOptionPane.showMessageDialog(view, "Ouvre l'éditeur d'album (à intégrer).", "Album", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onValidateAndSave() {
        if (!validateForm()) return;

        String titre = getTitre();
        String artisteDisplay = getArtiste();
        String albumName = getAlbum();
        String mmss = getDuree();

        Artiste artiste = findOrCreateArtist(artisteDisplay);
        Album album = findOrCreateAlbum(albumName);
        float minutes = mmSsToFloatMinutes(mmss);

        if (current == null) {
            List<Artiste> artistes = new ArrayList<>();
            if (artiste != null) artistes.add(artiste);
            Musique m = new Musique(titre, minutes, album, artistes, selectedImagePath);
            musicDAO.add(m);
            if (listener != null) listener.onMusicCreated(m);
        } else {
            current.setTitre(titre);
            current.setCoverImageURL(selectedImagePath);
            current.setAlbum(album);
            trySetArtists(current, (artiste != null) ? List.of(artiste) : new ArrayList<>());
            trySetDuration(current, minutes);
            musicDAO.update(current);
            if (listener != null) listener.onMusicUpdated(current);
        }

        JOptionPane.showMessageDialog(view, "Enregistré ✅", "Musique", JOptionPane.INFORMATION_MESSAGE);
        view.dispose();
    }

    /* ===================== CHARGEMENT COMBOS (DAO) ===================== */
    private void preloadCombosFromDAO() {
        var allArtistes = artisteDAO.getAll();
        java.util.Set<String> names = new java.util.LinkedHashSet<>();
        for (Artiste a : allArtistes) names.add(displayName(a));
        setArtistes(names);

        var allAlbums = albumDAO.getAll();
        java.util.Set<String> albums = new java.util.LinkedHashSet<>();
        for (Album a : allAlbums) albums.add(safe(a.getTitreAlbum()));
        setAlbums(albums);
    }

    /* ===================== SETTERS (combos) ===================== */
    public void setArtistes(Collection<String> artistes) {
        @SuppressWarnings("unchecked")
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) view.getArtisteCombo().getModel();
        model.removeAllElements();
        if (artistes != null) for (String a : artistes) model.addElement(a);
        view.getArtisteCombo().setSelectedItem(null);
    }

    public void setAlbums(Collection<String> albums) {
        @SuppressWarnings("unchecked")
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) view.getAlbumCombo().getModel();
        model.removeAllElements();
        if (albums != null) for (String a : albums) model.addElement(a);
        view.getAlbumCombo().setSelectedItem(null);
    }

    /* ===================== GETTERS FORM ===================== */
    public String getSelectedImagePath() { return selectedImagePath; }

    public String getTitre() {
        String text = view.getTitreField().getText();
        return text == null ? "" : text.trim();
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
        String text = view.getDureeField().getText();
        return text == null ? "" : text.trim();
    }

    public int getDurationInSeconds() {
        String d = getDuree();
        if (!d.matches("^\\d{1,2}:[0-5]\\d$")) return 0;
        String[] parts = d.split(":");
        int minutes = Integer.parseInt(parts[0]);
        int seconds = Integer.parseInt(parts[1]);
        return minutes * 60 + seconds;
    }

    /* ===================== VALIDATION ===================== */
    public boolean validateForm() {
        if (getTitre().isEmpty()) { showError(view, "Le titre est requis.", "Erreur"); return false; }
        if (getArtiste().isEmpty()) { showError(view, "Sélectionne un artiste.", "Erreur"); return false; }
        if (getAlbum().isEmpty()) { showError(view, "Sélectionne un album.", "Erreur"); return false; }
        if (getDuree().isEmpty()) { showError(view, "La durée est requise.", "Erreur"); return false; }
        if (!getDuree().matches("^\\d{1,2}:[0-5]\\d$")) {
            showError(view, "Durée invalide. Utilise le format mm:ss (ex: 03:25).", "Erreur");
            return false;
        }
        return true;
    }

    /* ===================== HELPERS DAO & UI ===================== */
    private Artiste findOrCreateArtist(String display) {
        String d = safe(display);
        if (d.isEmpty()) return null;
        Artiste a = artisteDAO.getByName(d);
        if (a != null) return a;

        String prenom = "", nom = "";
        String[] parts = d.split("\\s+");
        if (parts.length >= 2) {
            prenom = parts[0];
            nom = d.substring(prenom.length()).trim();
            a = new Artiste(nom, prenom, "", new ArrayList<>());
        } else {
            a = new Artiste("", "", d, new ArrayList<>());
        }
        artisteDAO.add(a);
        return a;
    }

    private Album findOrCreateAlbum(String name) {
        String n = safe(name);
        if (n.isEmpty()) return null;
        Album a = albumDAO.getByName(n);
        if (a != null) return a;
        a = new Album(n, LocalDate.now(), new ArrayList<>(), null);
        albumDAO.add(a);
        return a;
    }

    private static float mmSsToFloatMinutes(String mmss) {
        String[] parts = mmss.split(":");
        int m = Integer.parseInt(parts[0]);
        int s = Integer.parseInt(parts[1]);
        return m + (s / 60f);
    }

    private static String floatMinutesToMmSs(float minutes) {
        int total = Math.round(minutes * 60f);
        int mm = total / 60;
        int ss = total % 60;
        return String.format("%d:%02d", mm, ss);
    }

    private static String displayName(Artiste a) {
        if (a == null) return "";
        String pseudo = safe(a.getPseudo());
        if (!pseudo.isBlank()) return pseudo;
        String prenom = safe(a.getPrenom());
        String nom = safe(a.getNom());
        String full = (prenom + " " + nom).trim();
        return full.isBlank() ? "Inconnu" : full;
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }

    private void trySetArtists(Musique m, List<Artiste> artistes) {
        try { Musique.class.getMethod("setArtistes", List.class).invoke(m, artistes); }
        catch (Exception ignored) {}
    }

    private void trySetDuration(Musique m, float minutes) {
        try { Musique.class.getMethod("setDuree", float.class).invoke(m, minutes); }
        catch (Exception ignored) {}
    }

    @Override
    public void showError(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void showInfo(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
}
