package DiskHouse.Controller;

import DiskHouse.model.entity.Album;
import DiskHouse.model.entity.Artiste;
import DiskHouse.view.ArtistEditor;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Contrôleur de la vue ArtistEditor.
 * - Pas d'accès au modèle global ici: on reçoit l'artiste et sa liste d'albums à afficher.
 * - Ne gère QUE l'initialisation de la vue et son affichage.
 */
public class ArtistEditorController implements IController<ArtistEditor> {

    private final ArtistEditor view;

    public ArtistEditorController(ArtistEditor view) {
        this.view = view;
    }

    @Override
    public ArtistEditor getView() {
        return view;
    }

    @Override
    public void initController() {
        // Rien pour le moment : les listeners de boutons + / 🗑 seront branchés plus tard.
    }

    /** Charge les données dans la vue (nom + liste d'albums + portrait éventuel). */
    public void loadData(Artiste artiste, List<Album> albums, Image portraitOrNull) {
        view.setArtistName(displayName(artiste));

        // Liste des albums (uniques, dans l'ordre reçu).
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
        // On préfère le pseudo s'il existe, sinon "Prénom Nom".
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

    /** Convertit une URL (http/https/file/jar) en Image (ou null si échec). */
    public static Image tryLoadImage(String urlOrPath) {
        try {
            if (urlOrPath == null || urlOrPath.isBlank()) return null;
            ImageIcon ic = new ImageIcon(urlOrPath);
            return (ic.getIconWidth() > 0 && ic.getIconHeight() > 0) ? ic.getImage() : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    /** Utilitaire : depuis une liste d'albums, renvoie juste les titres uniques. */
    public static List<String> toTitles(List<Album> albums) {
        return albums.stream()
                .filter(a -> a != null && a.getTitreAlbum() != null)
                .map(Album::getTitreAlbum)
                .distinct()
                .collect(Collectors.toList());
    }
}
