package DiskHouse.Controller;

import DiskHouse.model.DAO.AlbumFileDAO;
import DiskHouse.model.DAO.ArtisteFileDAO;
import DiskHouse.model.entity.Album;
import DiskHouse.model.entity.Artiste;
import DiskHouse.view.AlbumEditor;   // <-- ouvert par le bouton +
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
        initController();
    }

    @Override public ArtisteEditor getView() { return view; }

    @Override
    public void initController() {
        // Gestion du clic sur le portrait pour choisir une image
        view.getPortraitLabel().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Choisir une image de portrait");
                if (chooser.showOpenDialog(view) == JFileChooser.APPROVE_OPTION) {
                    java.io.File file = chooser.getSelectedFile();
                    if (file != null) {
                        String imageUrl = file.toURI().toString();
                        view.setPortraitImage(imageUrl);
                        view.getPortraitLabel().putClientProperty("imageURL", imageUrl);
                    }
                }
            }
        });

        // Bouton + : ouvrir AlbumEditor
        view.getAddAlbumButtonView().addActionListener(e -> {
            Window owner = SwingUtilities.getWindowAncestor(view.getOkButton());
            AlbumEditor dialog = new AlbumEditor(owner); // JDialog modal (ta classe vue)
            dialog.setVisible(true); // Le contrôleur parent décidera plus tard comment récupérer les données
        });

        // Bouton 🗑 : supprimer l’album sélectionné dans la liste
        view.getRemoveAlbumButtonView().addActionListener(e -> {
            JList<String> list = view.getAlbumsList();
            int idx = list.getSelectedIndex();
            if (idx < 0) {
                showInfo(view, "Sélectionnez un album à supprimer.", "Information");
                return;
            }
            ListModel<String> lm = list.getModel();
            if (lm instanceof DefaultListModel<String> dm) {
                dm.remove(idx);
                view.ensureAlbumsPlaceholder();
            }
        });

        // Bouton Enregistrer : sauvegarder l'artiste et fermer si succès
        view.getOkButton().addActionListener(e -> {
            Artiste saved = saveToDAO();
            if (saved != null) {
                showInfo(view, "Artiste enregistré avec succès !", "Succès");
                view.dispose();
            }
        });
    }

    public void openForCreate(Window parent) {
        currentArtiste = null;
        portraitPathOrUrl = null;
        view.setArtistName("");
        view.getAlbumsList().setModel(new DefaultListModel<>());
        view.setPortraitImage((String) null);
        view.getPortraitLabel().putClientProperty("imageURL", null);
        view.ensureAlbumsPlaceholder();
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
        view.setPortraitImage(a.getImageURL());
        view.getPortraitLabel().putClientProperty("imageURL", a.getImageURL());
        show(parent);
    }

    public void loadData(Artiste artiste, List<Album> albums, Image portrait) {
        view.setArtistName(artiste != null ? artiste.getPseudo() : "");

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
        view.ensureAlbumsPlaceholder();

        // Afficher l'image de l'artiste si elle existe
        if (artiste != null && artiste.getImageURL() != null && !artiste.getImageURL().isBlank()) {
            view.setPortraitImage(artiste.getImageURL());
            view.getPortraitLabel().putClientProperty("imageURL", artiste.getImageURL());
        } else if (portrait != null) {
            view.setPortraitImage(portrait);
        } else if (portraitPathOrUrl != null) {
            Image loaded = tryLoadImage(portraitPathOrUrl);
            if (loaded != null) view.setPortraitImage(loaded);
        } else {
            view.setPortraitImage((String) null);
        }
    }

    public Artiste saveToDAO() {
        String pseudo = safe(view.getArtistNameField().getText());
        if (pseudo.equals("Choisir un artiste")) pseudo = "";
        if (pseudo.isEmpty()) {
            showError(view, "Le pseudo de l'artiste ne peut pas être vide.", "Erreur");
            return null;
        }
        String imageURL = (String) view.getPortraitLabel().getClientProperty("imageURL");
        if (currentArtiste == null) {
            currentArtiste = new Artiste(pseudo, new java.util.ArrayList<>(), imageURL);
            artisteDAO.add(currentArtiste);
        } else {
            currentArtiste.setPseudo(pseudo);
            currentArtiste.setImageURL(imageURL);
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

    private static String safe(String s) { return s == null ? "" : s.trim(); }

    public static Image tryLoadImage(String path) {
        if (path == null || path.isBlank()) return null;
        ImageIcon icon = new ImageIcon(path);
        return (icon.getIconWidth() > 0 && icon.getIconHeight() > 0) ? icon.getImage() : null;
    }

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
