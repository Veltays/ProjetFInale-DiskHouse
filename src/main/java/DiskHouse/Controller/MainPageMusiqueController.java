package DiskHouse.Controller;

import DiskHouse.model.DAO.AlbumFileDAO;
import DiskHouse.model.DAO.ArtisteFileDAO;
import DiskHouse.model.DAO.MusicFileDAO;
import DiskHouse.model.DAO.PlaylistFileDAO;
import DiskHouse.model.entity.Album;
import DiskHouse.model.entity.Artiste;
import DiskHouse.model.entity.Musique;
import DiskHouse.model.entity.Playlist;
import DiskHouse.view.ArtisteEditor;
import DiskHouse.view.MusicEditor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainPageMusiqueController {

    private final MainPageController root;

    // ===== DAO =====
    private final PlaylistFileDAO playlistDAO = new PlaylistFileDAO("data/playlists.dat");
    private final MusicFileDAO    musicDAO    = new MusicFileDAO("data/musiques.dat");
    private final ArtisteFileDAO  artisteDAO  = new ArtisteFileDAO("data/artistes.dat");
    private final AlbumFileDAO    albumDAO    = new AlbumFileDAO("data/albums.dat");

    /* ===================== CONSTRUCTEUR ===================== */

    public MainPageMusiqueController(MainPageController root) {
        this.root = root;
    }

    /* ===================== INIT TABLE ===================== */

    public void initMusicTable(JTable table) {
        String[] columns = {"ID", "CoverURL", "Titre / Artistes / Album", "Durée"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) { return String.class; }
        };
        table.setModel(model);

        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setRowHeight(72);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.setPreferredSize(new Dimension(0, 0));
            header.setVisible(false);
        }

        TableColumnModel cm = table.getColumnModel();
        hideColumn(cm.getColumn(0)); // ID
        hideColumn(cm.getColumn(1)); // CoverURL

        cm.getColumn(2).setPreferredWidth(600);
        cm.getColumn(2).setCellRenderer(new MusicCellRenderer(table));

        cm.getColumn(3).setPreferredWidth(80);
        cm.getColumn(3).setMaxWidth(100);
        cm.getColumn(3).setCellRenderer(new MainPagePlaylistController.RightHintRenderer());

        // Double clic -> ouvrir ArtisteEditor
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    onOpenArtisteEditorForSelectedMusic();
                }
            }
        });
    }

    private void hideColumn(TableColumn column) {
        column.setMinWidth(0);
        column.setMaxWidth(0);
        column.setPreferredWidth(0);
        column.setResizable(false);
    }

    public void clearMusicTable(JTable table) {
        ((DefaultTableModel) table.getModel()).setRowCount(0);
    }

    public void loadMusicsForPlaylist(JTable table, Playlist playlist) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        if (playlist == null) return;

        String defaultCover = playlist.getCoverImageURL();

        for (Musique m : playlist.getMusiques()) {
            String title = m.getTitre();
            String artists = joinArtists(m.getArtistes());
            String album = (m.getAlbum() != null) ? m.getAlbum().getTitreAlbum() : "";

            String subtitle = "";
            if (!artists.isBlank()) subtitle += artists;
            if (!album.isBlank()) subtitle += (subtitle.isBlank() ? "" : " • ") + album;

            String duration = formatDuration(m.getDuree());

            String cover = (m.getCoverImageURL() != null && !m.getCoverImageURL().isBlank())
                    ? m.getCoverImageURL()
                    : defaultCover;

            model.addRow(new Object[]{m.getId(), cover, title + "\n" + subtitle, duration});
        }
    }

    /* ===================== BOUTONS CRUD (persistants) ===================== */

    public void wireButtons() {
        if (root.getView().getAjouterMusiqueButton() != null) {
            root.getView().getAjouterMusiqueButton().addActionListener(e -> onAddMusicPersisted());
        }
        if (root.getView().getModifierMusiqueButton() != null) {
            root.getView().getModifierMusiqueButton().addActionListener(e -> onEditMusicPersisted());
        }
        if (root.getView().getSupprimerMusiqueButton() != null) {
            root.getView().getSupprimerMusiqueButton().addActionListener(e -> onDeleteMusicPersisted());
        }
    }

    /* ======= Ajouter ======= */
    private void onAddMusicPersisted() {
        JTable playlistTable = root.getView().getTablePlaylist();
        JTable musicTable = root.getView().getTableMusicInPlaylistSelected();
        if (playlistTable == null || musicTable == null) return;

        Playlist playlist = getSelectedPlaylist(playlistTable);
        if (playlist == null) {
            root.showInfo(root.getView(), "Sélectionne d'abord une playlist.", "Ajouter musique");
            return;
        }

        // -- Dialogue léger (fonctionne tout de suite). Remplaçable par MusicEditor si tu exposes des getters/OK. --
        JPanel panel = new JPanel(new GridLayout(5, 2, 8, 6));
        JTextField fTitle   = new JTextField();
        JTextField fArtists = new JTextField(); // "Prenom Nom, Prenom2 Nom2"
        JTextField fAlbum   = new JTextField();
        JTextField fDuration= new JTextField(); // "mm:ss"
        JTextField fCover   = new JTextField();

        panel.add(new JLabel("Titre :"));     panel.add(fTitle);
        panel.add(new JLabel("Artistes :"));  panel.add(fArtists);
        panel.add(new JLabel("Album :"));     panel.add(fAlbum);
        panel.add(new JLabel("Durée (mm:ss) :")); panel.add(fDuration);
        panel.add(new JLabel("Cover URL (opt) :")); panel.add(fCover);

        int res = JOptionPane.showConfirmDialog(root.getView(), panel, "Ajouter une musique",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        String title = safe(fTitle.getText());
        String artistsCsv = safe(fArtists.getText());
        String albumName = safe(fAlbum.getText());
        String mmss = safe(fDuration.getText());
        String cover = safe(fCover.getText());

        if (title.isEmpty() || !mmss.matches("^\\d{1,2}:[0-5]\\d$")) {
            root.showError(root.getView(), "Titre requis et durée au format mm:ss.", "Erreur");
            return;
        }

        // Trouver/créer album
        Album album = null;
        if (!albumName.isEmpty()) {
            album = albumDAO.getByName(albumName);
            if (album == null) {
                album = new Album(albumName, LocalDate.now(), new ArrayList<>(), null);
                albumDAO.add(album);
            }
        }

        // Trouver/créer artistes
        List<Artiste> artistes = new ArrayList<>();
        if (!artistsCsv.isEmpty()) {
            for (String part : artistsCsv.split(",")) {
                String name = safe(part);
                if (name.isEmpty()) continue;
                Artiste a = findOrCreateArtistByDisplayName(name);
                artistes.add(a);
            }
        }

        // Créer + persister musique
        float minutes = mmSsToFloatMinutes(mmss);
        Musique m = new Musique(title, minutes, album, artistes, cover.isEmpty()? null : cover);
        musicDAO.add(m);

        // Lier à la playlist + persister playlist
        playlist.ajouterMusique(m);
        playlistDAO.update(playlist);

        // Rafraîchir la table de droite
        loadMusicsForPlaylist(musicTable, playlist);
    }

    /* ======= Modifier ======= */
    private void onEditMusicPersisted() {
        JTable playlistTable = root.getView().getTablePlaylist();
        JTable musicTable = root.getView().getTableMusicInPlaylistSelected();
        if (playlistTable == null || musicTable == null) return;

        Playlist playlist = getSelectedPlaylist(playlistTable);
        if (playlist == null) {
            root.showInfo(root.getView(), "Sélectionne d'abord une playlist.", "Modifier musique");
            return;
        }

        int sel = musicTable.getSelectedRow();
        if (sel < 0) {
            root.showInfo(root.getView(), "Sélectionne une musique.", "Modifier musique");
            return;
        }

        int modelRow = musicTable.convertRowIndexToModel(sel);
        Integer musicId = (Integer) musicTable.getModel().getValueAt(modelRow, 0);
        Musique music = findMusicById(playlist, musicId);
        if (music == null) {
            root.showError(root.getView(), "Musique introuvable.", "Erreur");
            return;
        }

        // Préremplir
        String title0 = music.getTitre();
        String artists0 = joinArtists(music.getArtistes());
        String album0 = (music.getAlbum() != null) ? music.getAlbum().getTitreAlbum() : "";
        String mmss0 = formatDuration(music.getDuree());
        String cover0 = (music.getCoverImageURL() != null) ? music.getCoverImageURL() : "";

        JPanel panel = new JPanel(new GridLayout(5, 2, 8, 6));
        JTextField fTitle   = new JTextField(title0);
        JTextField fArtists = new JTextField(artists0);
        JTextField fAlbum   = new JTextField(album0);
        JTextField fDuration= new JTextField(mmss0);
        JTextField fCover   = new JTextField(cover0);

        panel.add(new JLabel("Titre :"));     panel.add(fTitle);
        panel.add(new JLabel("Artistes :"));  panel.add(fArtists);
        panel.add(new JLabel("Album :"));     panel.add(fAlbum);
        panel.add(new JLabel("Durée (mm:ss) :")); panel.add(fDuration);
        panel.add(new JLabel("Cover URL (opt) :")); panel.add(fCover);

        int res = JOptionPane.showConfirmDialog(root.getView(), panel, "Modifier la musique",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        String title = safe(fTitle.getText());
        String artistsCsv = safe(fArtists.getText());
        String albumName = safe(fAlbum.getText());
        String mmss = safe(fDuration.getText());
        String cover = safe(fCover.getText());

        if (title.isEmpty() || !mmss.matches("^\\d{1,2}:[0-5]\\d$")) {
            root.showError(root.getView(), "Titre requis et durée au format mm:ss.", "Erreur");
            return;
        }

        // Album
        Album album = null;
        if (!albumName.isEmpty()) {
            album = albumDAO.getByName(albumName);
            if (album == null) {
                album = new Album(albumName, LocalDate.now(), new ArrayList<>(), null);
                albumDAO.add(album);
            }
        }

        // Artistes
        List<Artiste> artistes = new ArrayList<>();
        if (!artistsCsv.isEmpty()) {
            for (String part : artistsCsv.split(",")) {
                String name = safe(part);
                if (name.isEmpty()) continue;
                Artiste a = findOrCreateArtistByDisplayName(name);
                artistes.add(a);
            }
        }

        // Mettre à jour l'objet en mémoire
        music.setTitre(title);
        music.setCoverImageURL(cover.isEmpty() ? null : cover);
        music.setAlbum(album);
        trySetArtists(music, artistes);
        trySetDuration(music, mmSsToFloatMinutes(mmss));

        // Persister la musique et la playlist
        musicDAO.update(music);
        playlistDAO.update(playlist);

        // Refresh UI
        loadMusicsForPlaylist(musicTable, playlist);
        musicTable.setRowSelectionInterval(modelRow, modelRow);
    }

    /* ======= Supprimer ======= */
    private void onDeleteMusicPersisted() {
        JTable playlistTable = root.getView().getTablePlaylist();
        JTable musicTable = root.getView().getTableMusicInPlaylistSelected();
        if (playlistTable == null || musicTable == null) return;

        Playlist playlist = getSelectedPlaylist(playlistTable);
        if (playlist == null) {
            root.showInfo(root.getView(), "Sélectionne une playlist.", "Supprimer musique");
            return;
        }

        int sel = musicTable.getSelectedRow();
        if (sel < 0) {
            root.showInfo(root.getView(), "Sélectionne une musique.", "Supprimer musique");
            return;
        }

        int modelRow = musicTable.convertRowIndexToModel(sel);
        Integer musicId = (Integer) musicTable.getModel().getValueAt(modelRow, 0);
        Musique music = findMusicById(playlist, musicId);
        if (music == null) {
            root.showError(root.getView(), "Musique introuvable.", "Erreur");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                root.getView(),
                "Supprimer la musique « " + music.getTitre() + " » ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        // Supprimer côté DAO + côté playlist + UI
        musicDAO.delete(String.valueOf(music.getId()));
        playlist.getMusiques().removeIf(m -> Objects.equals(m.getId(), music.getId()));
        playlistDAO.update(playlist);

        ((DefaultTableModel) musicTable.getModel()).removeRow(modelRow);
    }

    /* ===================== DOUBLE CLIC : ARTISTE ===================== */

    private void onOpenArtisteEditorForSelectedMusic() {
        JTable playlistTable = root.getView().getTablePlaylist();
        JTable musicTable = root.getView().getTableMusicInPlaylistSelected();
        if (playlistTable == null || musicTable == null) return;

        Playlist playlist = getSelectedPlaylist(playlistTable);
        if (playlist == null) {
            root.showInfo(root.getView(), "Sélectionne d'abord une playlist.", "Artiste");
            return;
        }

        int sel = musicTable.getSelectedRow();
        if (sel < 0) {
            root.showInfo(root.getView(), "Double-clique une musique.", "Artiste");
            return;
        }

        int modelRow = musicTable.convertRowIndexToModel(sel);
        Integer musicId = (Integer) musicTable.getModel().getValueAt(modelRow, 0);
        Musique music = findMusicById(playlist, musicId);
        if (music == null) {
            root.showError(root.getView(), "Musique introuvable.", "Erreur");
            return;
        }

        Artiste artist = firstArtistOf(music);
        if (artist == null) {
            root.showInfo(root.getView(), "Aucun artiste associé.", "Artiste");
            return;
        }

        List<Album> albums = collectAlbumsOfArtist(artist);

        String portraitUrl = (music.getCoverImageURL() != null && !music.getCoverImageURL().isBlank())
                ? music.getCoverImageURL()
                : (playlist.getCoverImageURL() != null ? playlist.getCoverImageURL() : null);

        Image portrait = ArtisteEditorController.tryLoadImage(portraitUrl);

        SwingUtilities.invokeLater(() -> {
            ArtisteEditor dlg = new ArtisteEditor(root.getView());
            ArtisteEditorController ctrl = new ArtisteEditorController(dlg);
            ctrl.initController();
            ctrl.loadData(artist, albums, portrait);
            ctrl.show(root.getView());
        });
    }

    /* ===================== HELPERS ===================== */

    private Playlist getSelectedPlaylist(JTable table) {
        int sel = table.getSelectedRow();
        if (sel < 0) return null;
        int mr = table.convertRowIndexToModel(sel);
        Integer id = (Integer) table.getModel().getValueAt(mr, 0);

        for (Playlist p : root.getPlaylists()) {
            if (Objects.equals(p.getId(), id)) return p;
        }
        return null;
    }

    private Musique findMusicById(Playlist p, Integer id) {
        if (p == null || id == null) return null;
        for (Musique m : p.getMusiques()) {
            if (Objects.equals(m.getId(), id)) return m;
        }
        return null;
    }

    private Artiste firstArtistOf(Musique m) {
        if (m.getArtistes() == null || m.getArtistes().isEmpty()) return null;
        return m.getArtistes().get(0);
    }

    private List<Album> collectAlbumsOfArtist(Artiste artist) {
        List<Album> albums = new ArrayList<>();
        for (Playlist p : root.getPlaylists()) {
            for (Musique mu : p.getMusiques()) {
                if (mu.getArtistes() == null) continue;
                for (Artiste a : mu.getArtistes()) {
                    if (sameArtist(a, artist) && mu.getAlbum() != null) {
                        albums.add(mu.getAlbum());
                    }
                }
            }
        }
        return albums;
    }

    private boolean sameArtist(Artiste a, Artiste b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        String ak = (safe(a.getPrenom()) + "|" + safe(a.getNom()) + "|" + safe(a.getPseudo())).toLowerCase();
        String bk = (safe(b.getPrenom()) + "|" + safe(b.getNom()) + "|" + safe(b.getPseudo())).toLowerCase();
        return ak.equals(bk);
    }

    private String joinArtists(List<Artiste> artistes) {
        if (artistes == null || artistes.isEmpty()) return "";
        List<String> names = new ArrayList<>();
        for (Artiste a : artistes) {
            String prenom = safe(a.getPrenom());
            String nom = safe(a.getNom());
            String full = (prenom + " " + nom).trim();
            if (full.isBlank()) full = safe(a.getPseudo());
            if (full.isBlank()) full = "Inconnu";
            names.add(full);
        }
        return String.join(", ", names);
    }

    private String formatDuration(float minutes) {
        int totalSeconds = Math.max(0, Math.round(minutes * 60));
        int mm = totalSeconds / 60;
        int ss = totalSeconds % 60;
        return String.format("%d:%02d", mm, ss);
    }

    private static float mmSsToFloatMinutes(String mmss) {
        String[] parts = mmss.split(":");
        int m = Integer.parseInt(parts[0]);
        int s = Integer.parseInt(parts[1]);
        return m + (s / 60f);
    }

    private String safe(String s) { return (s == null) ? "" : s.trim(); }

    private Artiste findOrCreateArtistByDisplayName(String display) {
        // On tente d'abord par pseudo (le plus distinctif)
        Artiste a = artisteDAO.getByName(display);
        if (a != null) return a;

        // Heuristique simple Prenom Nom
        String prenom = "", nom = "";
        String[] parts = display.split("\\s+");
        if (parts.length >= 2) {
            prenom = parts[0];
            nom = display.substring(prenom.length()).trim();
        } else {
            // si un seul token, on met en pseudo
            return createArtist("", "", display);
        }
        return createArtist(nom, prenom, "");
    }

    private Artiste createArtist(String nom, String prenom, String pseudo) {
        Artiste a = new Artiste(nom, prenom, pseudo, new ArrayList<>());
        artisteDAO.add(a);
        return a;
    }

    private void trySetArtists(Musique m, List<Artiste> artistes) {
        try { Musique.class.getMethod("setArtistes", List.class).invoke(m, artistes); }
        catch (Exception ignored) {}
    }

    private void trySetDuration(Musique m, float minutes) {
        try { Musique.class.getMethod("setDuree", float.class).invoke(m, minutes); }
        catch (Exception ignored) {}
    }

    /* ===================== RENDERER ===================== */

    static class MusicCellRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final JLabel cover = new JLabel();
        private final JLabel title = new JLabel();
        private final JLabel subtitle = new JLabel();
        private final Image defaultImg;

        MusicCellRenderer(JTable table) {
            setLayout(new BorderLayout(12, 0));
            setOpaque(true);

            JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
            textPanel.setOpaque(false);

            title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
            subtitle.setForeground(new Color(110, 110, 110));

            textPanel.add(title);
            textPanel.add(subtitle);

            add(cover, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);
            setBorder(new EmptyBorder(8, 12, 8, 12));

            Image d = null;
            try {
                URL u = MainPageController.class.getResource("/PP.png");
                if (u != null) d = new ImageIcon(u).getImage();
            } catch (Exception ignored) {}
            defaultImg = d;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            String raw = (value == null) ? "" : value.toString();
            String[] parts = raw.split("\n", 2);
            String t = (parts.length > 0) ? parts[0] : "";
            String st = (parts.length > 1) ? parts[1] : "";

            String url = null;
            try {
                int modelRow = table.convertRowIndexToModel(row);
                url = String.valueOf(table.getModel().getValueAt(modelRow, 1));
            } catch (Exception ignored) {}

            cover.setIcon(loadScaledIcon(url, 56, 56, defaultImg));
            title.setText(t);
            subtitle.setText(st);
            setBackground(isSelected ? new Color(232, 239, 255) : Color.WHITE);
            return this;
        }
    }

    /* ===================== UTILS IMAGES ===================== */

    static ImageIcon loadScaledIcon(String urlOrPath, int w, int h, Image fallback) {
        Image img = null;
        try {
            if (urlOrPath != null && !urlOrPath.isBlank()) {
                if (urlOrPath.startsWith("http") || urlOrPath.startsWith("jar:") || urlOrPath.startsWith("file:")) {
                    img = new ImageIcon(URI.create(urlOrPath).toURL()).getImage();
                } else {
                    img = new ImageIcon(urlOrPath).getImage();
                }
            }
        } catch (Exception ignored) {}

        if (img == null) img = fallback;
        if (img == null) return null;

        Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}
