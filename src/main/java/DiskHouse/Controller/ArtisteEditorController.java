package DiskHouse.Controller;

import DiskHouse.model.DAO.AlbumFileDAO;
import DiskHouse.model.DAO.ArtisteFileDAO;
import DiskHouse.model.entity.Album;
import DiskHouse.model.entity.Artiste;
import DiskHouse.view.ArtisteEditor;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ArtisteEditorController implements IController<ArtisteEditor> {

    private final ArtisteEditor view;

    private final ArtisteFileDAO artisteDAO = new ArtisteFileDAO("data/artistes.dat");
    private final AlbumFileDAO   albumDAO   = new AlbumFileDAO("data/albums.dat");

    private Artiste currentArtiste;     // null en création
    private String portraitPathOrUrl;   // chemin/URL de portrait (optionnel)

    public ArtisteEditorController(ArtisteEditor view) {
        this.view = Objects.requireNonNull(view);
    }

    @Override public ArtisteEditor getView() { return view; }

    @Override
    public void initController() {
        // listeners éventuels
    }

    public void openForCreate(Window parent) {
        currentArtiste = null;
        portraitPathOrUrl = null;
        view.setArtistName("Nouvel artiste");
        view.getAlbumsList().setModel(new DefaultListModel<>());
        view.setPortraitImage(null);
        show(parent);
    }

    public void openForEdit(Window parent, Artiste artiste, List<Album> albumsOrNull) {
        currentArtiste = Objects.requireNonNull(artiste);
        portraitPathOrUrl = null;
        loadData(artiste, albumsOrNull, null);
        show(parent);
    }

    public void openForEditById(Window parent, String artisteId) {
        Artiste a = artisteDAO.getById(artisteId);
        if (a == null) {
            JOptionPane.showMessageDialog(view, "Artiste introuvable (id=" + artisteId + ").",
                    "Information", JOptionPane.WARNING_MESSAGE);
            return;
        }
        currentArtiste = a;
        portraitPathOrUrl = null;
        List<Album> allAlbums = albumDAO.getAll();
        loadData(a, allAlbums, null);
        show(parent);
    }

    public void loadData(Artiste artiste, List<Album> albums, Image portrait) {
        view.setArtistName(formatArtistName(artiste));

        DefaultListModel<String> model = new DefaultListModel<>();
        Set<String> titlesSeen = new LinkedHashSet<>();
        if (albums != null) {
            for (Album album : albums) {
                if (album == null) continue;
                String title = album.getTitreAlbum();
                if (title == null || title.isBlank()) title = "Sans titre";
                if (titlesSeen.add(title)) model.addElement(title);
            }
        }
        view.getAlbumsList().setModel(model);

        if (portrait != null) view.setPortraitImage(portrait);
        else if (portraitPathOrUrl != null) {
            Image loaded = tryLoadImage(portraitPathOrUrl);
            if (loaded != null) view.setPortraitImage(loaded);
        }
    }

    public Artiste saveToDAO() {
        // ⚠️ ta vue n'a pas getArtistName(); on lit le champ :
        String display = safe(view.getArtistNameField().getText());
        if (display.isEmpty()) {
            showError(view, "Le nom/pseudo de l'artiste ne peut pas être vide.", "Erreur");
            return null;
        }

        if (currentArtiste == null) {
            currentArtiste = new Artiste("", "", display, new java.util.ArrayList<>());
            artisteDAO.add(currentArtiste);
        } else {
            if (currentArtiste.getPseudo() != null && !currentArtiste.getPseudo().isBlank()) {
                trySetPseudo(currentArtiste, display);
            } else {
                trySetPrenom(currentArtiste, display);
            }
            artisteDAO.update(currentArtiste);
        }
        return currentArtiste;
    }

    public void show(Window parent) {
        view.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        if (view.getWidth() == 0 || view.getHeight() == 0) view.pack();
        view.setLocationRelativeTo(parent);
        view.setVisible(true);
        view.toFront();
        view.requestFocus();
    }

    private String formatArtistName(Artiste artiste) {
        if (artiste == null) return "Inconnu";
        String pseudo = safe(artiste.getPseudo());
        if (!pseudo.isEmpty()) return pseudo;
        String prenom = safe(artiste.getPrenom());
        String nom = safe(artiste.getNom());
        String fullName = (prenom + " " + nom).trim();
        return fullName.isEmpty() ? "Inconnu" : fullName;
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }

    public static Image tryLoadImage(String path) {
        if (path == null || path.isBlank()) return null;
        ImageIcon icon = new ImageIcon(path);
        return (icon.getIconWidth() > 0 && icon.getIconHeight() > 0) ? icon.getImage() : null;
    }

    private void trySetPseudo(Artiste a, String pseudo) {
        try { Artiste.class.getMethod("setPseudo", String.class).invoke(a, pseudo); }
        catch (Exception ignored) {}
    }
    private void trySetPrenom(Artiste a, String prenom) {
        try { Artiste.class.getMethod("setPrenom", String.class).invoke(a, prenom); }
        catch (Exception ignored) {}
    }

    public void setPortraitPath(String path) { this.portraitPathOrUrl = path; }

    /* ==== Implémentations IController ==== */
    @Override
    public void showError(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void showInfo(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
}
