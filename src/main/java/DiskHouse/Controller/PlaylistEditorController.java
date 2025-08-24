//************************************************//
//************** PLAYLIST EDITOR CONTROLLER *******//
//************************************************//
// Contrôleur pour la création et l'édition d'une playlist.
// Gère la logique d'UI, la validation, la persistance DAO et les interactions utilisateur.

package DiskHouse.Controller;

import DiskHouse.model.DAO.AlbumFileDAO;
import DiskHouse.model.DAO.ArtisteFileDAO;
import DiskHouse.model.DAO.MusicFileDAO;
import DiskHouse.model.entity.Album;
import DiskHouse.model.entity.Artiste;
import DiskHouse.model.entity.Musique;
import DiskHouse.model.entity.Playlist;
import DiskHouse.view.PlaylistEditor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.Objects;

public class PlaylistEditorController {

    //************************************************//
    //************** INTERFACE CALLBACK **************//
    //************************************************//
    public interface Listener {
        void onPlaylistCreated(Playlist created);
        void onPlaylistUpdated(Playlist updated);
    }

    //************************************************//
    //************** DAO *****************************//
    //************************************************//
    private final MusicFileDAO musicDAO = new MusicFileDAO("data/musiques.dat");
    private final ArtisteFileDAO artisteDAO = new ArtisteFileDAO("data/artistes.dat");
    private final AlbumFileDAO albumDAO = new AlbumFileDAO("data/albums.dat");

    //************************************************//
    //************** ATTRIBUTS ************************//
    //************************************************//
    private final PlaylistEditor view;
    private final Listener listener;
    private Playlist currentPlaylist; // null → création
    private String selectedCoverPath;
    private final Set<Integer> originalMusicIds = new LinkedHashSet<>();

    //************************************************//
    //************** CONSTRUCTEUR *********************//
    //************************************************//
    public PlaylistEditorController(PlaylistEditor view, Listener listener) {
        this.view = Objects.requireNonNull(view);
        this.listener = Objects.requireNonNull(listener);
        initListeners();
    }

    //************************************************//
    //************** INITIALISATION *******************//
    //************************************************//
    private void initListeners() {
        view.getCoverLabel().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { onChooseImage(); }
        });
        view.getAddMusicBtn().addActionListener(e -> onAddMusic());
        view.getDelMusicBtn().addActionListener(e -> onDeleteMusic());
        view.getSaveBtn().addActionListener(e -> onSave());
        view.getCancelBtn().addActionListener(e -> view.dispose());
    }

    //************************************************//
    //************** OUVERTURE ************************//
    //************************************************//
    public void openForCreate(Window owner) {
        currentPlaylist = null;
        selectedCoverPath = null;
        originalMusicIds.clear();
        setCoverIcon(null);
        view.getNameField().setText("");
        clearTable();
        showDialog(owner);
    }

    public void openForEdit(Window owner, Playlist playlist) {
        currentPlaylist = Objects.requireNonNull(playlist);
        selectedCoverPath = playlist.getCoverImageURL();
        setCoverIcon(selectedCoverPath);
        view.getNameField().setText(playlist.getNomPlaylist());
        reloadFromPlaylist(playlist);
        originalMusicIds.clear();
        for (Musique m : playlist.getMusiques()) {
            originalMusicIds.add(m.getId());
        }
        showDialog(owner);
    }

    //************************************************//
    //************** ACTIONS UI ************************//
    //************************************************//
    private void onChooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choisir une image");
        if (chooser.showOpenDialog(view) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file != null) {
                selectedCoverPath = file.toURI().toString();
                setCoverIcon(selectedCoverPath);
            }
        }
    }

    private void onAddMusic() {
        JTextField tfTitle = new JTextField();
        JTextField tfArtists = new JTextField();
        JTextField tfAlbum = new JTextField();
        JTextField tfDuration = new JTextField();
        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.add(new JLabel("Titre :")); panel.add(tfTitle);
        panel.add(new JLabel("Artistes (séparés par ,) :")); panel.add(tfArtists);
        panel.add(new JLabel("Album :")); panel.add(tfAlbum);
        panel.add(new JLabel("Durée (mm:ss) :")); panel.add(tfDuration);
        int result = JOptionPane.showConfirmDialog(view, panel, "Ajouter une musique",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;
        String titre = tfTitle.getText().trim();
        String artists = tfArtists.getText().trim();
        String album = tfAlbum.getText().trim();
        String duree = tfDuration.getText().trim();
        if (titre.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Titre obligatoire.", "Musique", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<Artiste> artistes = findOrCreateArtists(artists);
        Album al = album.isEmpty() ? null : findOrCreateAlbum(album);
        float minutes = parseMmSs(duree);
        Musique music = new Musique(titre, minutes, al, artistes);
        musicDAO.add(music);
        DefaultTableModel model = (DefaultTableModel) view.getMusicTable().getModel();
        model.addRow(new Object[]{
                String.valueOf(music.getId()),
                music.getTitre(),
                joinArtists(artistes),
                (al != null ? al.getTitreAlbum() : ""),
                formatMmSs(minutes),
                (music.getCoverImageURL() == null ? "" : music.getCoverImageURL())
        });
    }

    private void onDeleteMusic() {
        JTable table = view.getMusicTable();
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(view, "Sélectionne une musique.", "Supprimer", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        Object idObj = table.getModel().getValueAt(modelRow, 0);
        if (idObj != null) {
            try {
                int id = Integer.parseInt(idObj.toString());
                musicDAO.delete(String.valueOf(id));
                originalMusicIds.remove(id);
            } catch (NumberFormatException ignored) {}
        }
        ((DefaultTableModel) table.getModel()).removeRow(modelRow);
    }

    private void onSave() {
        String name = view.getNameField().getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Le nom de la playlist est obligatoire.", "Playlist", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<Musique> musiques = new ArrayList<>();
        DefaultTableModel model = (DefaultTableModel) view.getMusicTable().getModel();
        Set<Integer> currentIds = new LinkedHashSet<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            String idStr = asString(model.getValueAt(i, 0));
            String titre = asString(model.getValueAt(i, 1));
            String artists = asString(model.getValueAt(i, 2));
            String album = asString(model.getValueAt(i, 3));
            String duree = asString(model.getValueAt(i, 4));
            String trackCover = asString(model.getValueAt(i, 5));
            List<Artiste> as = findOrCreateArtists(artists);
            Album al = album.isEmpty() ? null : findOrCreateAlbum(album);
            float minutes = parseMmSs(duree);
            Musique m;
            if (!idStr.isBlank()) {
                m = musicDAO.getById(idStr);
                if (m == null) {
                    m = new Musique(titre, minutes, al, as);
                    if (!trackCover.isEmpty()) m.setCoverImageURL(trackCover);
                    musicDAO.add(m);
                } else {
                    m.setTitre(titre);
                    m.setAlbum(al);
                    trySetArtists(m, as);
                    trySetDuration(m, minutes);
                    if (!trackCover.isEmpty()) m.setCoverImageURL(trackCover);
                    musicDAO.update(m);
                }
                try { currentIds.add(Integer.parseInt(idStr)); } catch (NumberFormatException ignored) {}
            } else {
                m = new Musique(titre, minutes, al, as);
                if (!trackCover.isEmpty()) m.setCoverImageURL(trackCover);
                musicDAO.add(m);
                currentIds.add(m.getId());
            }
            musiques.add(m);
        }
        if (currentPlaylist != null) {
            for (Integer oldId : originalMusicIds) {
                if (!currentIds.contains(oldId)) {
                    musicDAO.delete(String.valueOf(oldId));
                }
            }
        }
        if (currentPlaylist == null) {
            Playlist created = new Playlist(name, musiques);
            if (selectedCoverPath != null && !selectedCoverPath.isEmpty()) created.setCoverImageURL(selectedCoverPath);
            listener.onPlaylistCreated(created);
        } else {
            currentPlaylist.setNomPlaylist(name);
            currentPlaylist.setMusiques(musiques);
            if (selectedCoverPath != null) currentPlaylist.setCoverImageURL(selectedCoverPath);
            listener.onPlaylistUpdated(currentPlaylist);
        }
        view.dispose();
    }

    //************************************************//
    //************** HELPERS **************************//
    //************************************************//
    private void showDialog(Window owner) {
        if (owner != null) view.setLocationRelativeTo(owner);
        view.pack();
        view.setVisible(true);
    }

    private void clearTable() {
        ((DefaultTableModel) view.getMusicTable().getModel()).setRowCount(0);
    }

    private void reloadFromPlaylist(Playlist playlist) {
        clearTable();
        DefaultTableModel model = (DefaultTableModel) view.getMusicTable().getModel();
        for (Musique m : playlist.getMusiques()) {
            String album = (m.getAlbum() != null ? m.getAlbum().getTitreAlbum() : "");
            model.addRow(new Object[]{
                    String.valueOf(m.getId()),
                    m.getTitre(),
                    joinArtists(m.getArtistes()),
                    album,
                    formatMmSs(m.getDuree()),
                    (m.getCoverImageURL() == null ? "" : m.getCoverImageURL())
            });
        }
    }

    private String joinArtists(List<Artiste> artistes) {
        if (artistes == null || artistes.isEmpty()) return "";
        List<String> names = new ArrayList<>();
        for (Artiste a : artistes) {
            String pseudo = safe(a.getPseudo());
            if (pseudo.isEmpty()) pseudo = "Inconnu";
            names.add(pseudo);
        }
        return String.join(", ", names);
    }

    private List<Artiste> findOrCreateArtists(String csv) {
        List<Artiste> list = new ArrayList<>();
        if (csv == null || csv.isBlank()) return list;
        for (String part : csv.split(",")) {
            String n = part.trim();
            if (n.isEmpty()) continue;
            Artiste a = artisteDAO.getByName(n);
            if (a == null) {
                a = new Artiste(n, new ArrayList<>());
                artisteDAO.add(a);
            }
            list.add(a);
        }
        return list;
    }

    private Album findOrCreateAlbum(String name) {
        if (name == null || name.isBlank()) return null;
        Album a = albumDAO.getByName(name.trim());
        if (a != null) return a;
        a = new Album(name.trim(), LocalDate.now(), new ArrayList<>(), null);
        albumDAO.add(a);
        return a;
    }

    private String formatMmSs(float minutes) {
        int total = Math.max(0, Math.round(minutes * 60f));
        int mm = total / 60;
        int ss = total % 60;
        return String.format("%d:%02d", mm, ss);
    }

    private float parseMmSs(String mmss) {
        if (mmss == null || mmss.isEmpty()) return 0f;
        try {
            String[] p = mmss.trim().split(":");
            int mm = Integer.parseInt(p[0]);
            int ss = (p.length > 1) ? Integer.parseInt(p[1]) : 0;
            return (mm * 60 + ss) / 60f;
        } catch (Exception e) {
            return 0f;
        }
    }

    private String safe(String s) { return s == null ? "" : s.trim(); }
    private String asString(Object o) { return (o == null) ? "" : o.toString(); }

    private void setCoverIcon(String urlOrFile) {
        Image img = null;
        try {
            if (urlOrFile != null && !urlOrFile.isEmpty()) {
                if (urlOrFile.startsWith("http") || urlOrFile.startsWith("file:")) {
                    img = new ImageIcon(URI.create(urlOrFile).toURL()).getImage();
                } else {
                    img = new ImageIcon(urlOrFile).getImage();
                }
            }
        } catch (Exception ignored) {}
        if (img == null) img = placeholder(160, 160);
        Image scaled = img.getScaledInstance(160, 160, Image.SCALE_SMOOTH);
        view.getCoverLabel().setIcon(new ImageIcon(scaled));
    }

    private Image placeholder(int w, int h) {
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        g.setColor(new Color(240, 243, 252));
        g.fillRoundRect(0, 0, w, h, 24, 24);
        g.setColor(new Color(100, 120, 180));
        String text = "Cliquer pour choisir l'image";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, (w - fm.stringWidth(text)) / 2, h / 2);
        g.dispose();
        return bi;
    }

    private void trySetArtists(Musique m, List<Artiste> artistes) {
        try { Musique.class.getMethod("setArtistes", List.class).invoke(m, artistes); }
        catch (Exception ignored) {}
    }
    private void trySetDuration(Musique m, float minutes) {
        try { Musique.class.getMethod("setDuree", float.class).invoke(m, minutes); }
        catch (Exception ignored) {}
    }
}
