package DiskHouse.Controller;

import DiskHouse.model.entity.Album;
import DiskHouse.model.entity.Artiste;
import DiskHouse.view.ArtisteEditor;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Contrôleur de la vue ArtisteEditor.
 * - Reçoit un Artiste et sa liste d'albums à afficher.
 * - Initialise la vue et l'affiche (respect MVC).
 */
public class ArtisteEditorController implements IController<ArtisteEditor> {

    private final ArtisteEditor view;

    public ArtisteEditorController(ArtisteEditor view) {
        this.view = view;
    }

    @Override
    public ArtisteEditor getView() {
        return view;
    }

    @Override
    public void initController() {
        // Brancher ici les listeners des boutons (＋ / 🗑) si nécessaire
    }

    /** Charge les données dans la vue (nom + liste d'albums + portrait éventuel). */
    public void loadData(Artiste artiste, List<Album> albums, Image portraitOrNull) {
        view.setArtistName(displayName(artiste));

        // Liste des albums (uniques, dans l'ordre reçu)
        DefaultListModel<String> model = new DefaultListModel<>();
        Set<String> uniques = new LinkedHashSet<>();
        for (Album a : albums) {
            if (a == null) continue;
            String title = a.getTitreAlbum() == null ? "Sans titre" : a.getTitreAlbum();
            if (uniques.add(title)) model.addElement(title);
        }
        view.getAlbumsList().setModel(model);

        // Portrait si présent
        if (portraitOrNull != null) {
            view.setPortraitImage(portraitOrNull);
        }
    }

    /** Affiche la fenêtre proprement (centrage par rapport au parent). */
    public void show(Window parentOrNull) {
        view.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        if (view.getWidth() == 0 || view.getHeight() == 0) view.pack();
        view.setLocationRelativeTo(parentOrNull);
        view.setVisible(true);
        view.toFront();
        view.requestFocus();
    }

    /* ======================= Helpers ======================= */

    private String displayName(Artiste a) {
        if (a == null) return "";
        try {
            String pseudo = safe(a.getPseudo());
            if (!pseudo.isBlank()) return pseudo;
        } catch (Throwable ignored) {}
        String prenom = safe(a.getPrenom());
        String nom    = safe(a.getNom());
        String full   = (prenom + " " + nom).trim();
        return full.isBlank() ? "Inconnu" : full;
    }

    private String safe(String s) { return s == null ? "" : s; }

    /** Convertit une URL/chemin en Image (ou null si échec). */
    public static Image tryLoadImage(String urlOrPath) {
        try {
            if (urlOrPath == null || urlOrPath.isBlank()) return null;
            ImageIcon ic = new ImageIcon(urlOrPath);
            return (ic.getIconWidth() > 0 && ic.getIconHeight() > 0) ? ic.getImage() : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    /** Extrait les titres uniques d'une liste d'albums. */
    public static List<String> toTitles(List<Album> albums) {
        return albums.stream()
                .filter(a -> a != null && a.getTitreAlbum() != null)
                .map(Album::getTitreAlbum)
                .distinct()
                .collect(Collectors.toList());
    }
}
