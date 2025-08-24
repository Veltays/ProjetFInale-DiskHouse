//************************************************//
//************** ARTISTE EDITOR CONTROLLER *******//
//************************************************//
// Contrôleur pour la création et l'édition d'un artiste.
// Gère la logique d'UI, la validation, la persistance DAO et les interactions utilisateur.

package DiskHouse.Controller;

import DiskHouse.model.DAO.AlbumFileDAO;
import DiskHouse.model.DAO.ArtisteFileDAO;
import DiskHouse.model.entity.Album;
import DiskHouse.model.entity.Artiste;
import DiskHouse.view.AlbumEditor;
import DiskHouse.view.ArtisteEditor;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ArtisteEditorController implements IController<ArtisteEditor> {

    //************************************************//
    //************** ATTRIBUTS ************************//
    //************************************************//
    private final ArtisteEditor view;
    private final String username;
    private final ArtisteFileDAO artisteDAO;
    private final AlbumFileDAO albumDAO;
    private Artiste currentArtiste; // null en création
    private String selectedPortraitPath; // chemin/URL de portrait (optionnel)

    //************************************************//
    //************** CONSTRUCTEUR *********************//
    //************************************************//
    public ArtisteEditorController(ArtisteEditor view, String username) {
        this.view = Objects.requireNonNull(view);
        this.username = username;
        this.artisteDAO = new ArtisteFileDAO("data/" + username + "_artistes.dat");
        this.albumDAO = new AlbumFileDAO("data/" + username + "_albums.dat");
    }

    //************************************************//
    //************** GETTEUR VUE **********************//
    //************************************************//
    @Override
    public ArtisteEditor getView() { return view; }

    //************************************************//
    //************** INITIALISATION *******************//
    //************************************************//
    @Override
    public void initController() {
        initPortraitImageListener();
        initAlbumButtons();
        initSaveButton();
    }

    private void initPortraitImageListener() {
        view.getPortraitLabel().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        view.getPortraitLabel().setToolTipText("Cliquer pour choisir une image de portrait");
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
                        selectedPortraitPath = imageUrl;
                    }
                }
            }
        });
    }

    private void initAlbumButtons() {
        // Bouton + : ouvrir AlbumEditor
        view.getAddAlbumButtonView().addActionListener(e -> {
            Window owner = SwingUtilities.getWindowAncestor(view.getOkButton());
            AlbumEditor dialog = new AlbumEditor(owner);
            AlbumEditorController controller = new AlbumEditorController(dialog, username);
            controller.initController();
            dialog.setVisible(true);
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
            if (lm instanceof DefaultListModel<?> dm) {
                dm.remove(idx);
                view.ensureAlbumsPlaceholder();
            }
        });
    }

    private void initSaveButton() {
        view.getOkButton().addActionListener(e -> {
            Artiste saved = saveArtisteToDAO();
            if (saved != null) {
                showInfo(view, "Artiste enregistré avec succès !", "Succès");
                view.dispose();
            }
        });
    }

    //************************************************//
    //************** OUVERTURE ************************//
    //************************************************//
    public void openForCreate(Window parent) {
        currentArtiste = null;
        selectedPortraitPath = null;
        view.setArtistName("");
        view.getAlbumsList().setModel(new DefaultListModel<>());
        view.setPortraitImage((String) null);
        view.getPortraitLabel().putClientProperty("imageURL", null);
        view.ensureAlbumsPlaceholder();
        showDialog(parent);
    }

    public void openForEdit(Window parent, Artiste artiste, List<Album> albumsOrNull) {
        currentArtiste = Objects.requireNonNull(artiste);
        selectedPortraitPath = null;
        loadArtisteData(artiste, albumsOrNull, null);
        showDialog(parent);
    }

    public void openForEditById(Window parent, String artisteId) {
        Artiste a = artisteDAO.getById(artisteId);
        if (a == null) {
            showError(view, "Artiste introuvable (id=" + artisteId + ").", "Information");
            return;
        }
        currentArtiste = a;
        selectedPortraitPath = null;
        List<Album> allAlbums = albumDAO.getAll();
        loadArtisteData(a, allAlbums, null);
        view.setPortraitImage(a.getImageURL());
        view.getPortraitLabel().putClientProperty("imageURL", a.getImageURL());
        showDialog(parent);
    }

    //************************************************//
    //************** CHARGEMENT DONNÉES ****************//
    //************************************************//
    public void loadArtisteData(Artiste artiste, List<Album> albums, Image portrait) {
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
        } else if (selectedPortraitPath != null) {
            Image loaded = tryLoadImage(selectedPortraitPath);
            if (loaded != null) view.setPortraitImage(loaded);
        } else {
            view.setPortraitImage((String) null);
        }
    }

    //************************************************//
    //************** PERSISTANCE DAO ******************//
    //************************************************//
    public Artiste saveArtisteToDAO() {
        String pseudo = safeTrim(view.getArtistNameField().getText());
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

    //************************************************//
    //************** AFFICHAGE DIALOG *****************//
    //************************************************//
    public void showDialog(Window parent) {
        view.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        if (view.getWidth() == 0 || view.getHeight() == 0) view.pack();
        view.setLocationRelativeTo(parent);
        view.setVisible(true);
        view.toFront();
        view.requestFocus();
    }

    //************************************************//
    //************** HELPERS **************************//
    //************************************************//
    private static String safeTrim(String s) { return s == null ? "" : s.trim(); }

    public static Image tryLoadImage(String path) {
        if (path == null || path.isBlank()) return null;
        ImageIcon icon = new ImageIcon(path);
        return (icon.getIconWidth() > 0 && icon.getIconHeight() > 0) ? icon.getImage() : null;
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
