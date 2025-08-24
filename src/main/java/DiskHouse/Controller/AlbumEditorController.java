package DiskHouse.Controller;

import DiskHouse.model.DAO.AlbumFileDAO;
import DiskHouse.model.DAO.MusicFileDAO;
import DiskHouse.model.entity.Album;
import DiskHouse.model.entity.Musique;
import DiskHouse.view.AlbumEditor;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AlbumEditorController implements IController<AlbumEditor> {

    private final AlbumEditor view;

    // ===== DAO =====
    private final AlbumFileDAO albumDAO = new AlbumFileDAO("data/albums.dat");
    private final MusicFileDAO musicDAO = new MusicFileDAO("data/musiques.dat");

    // ===== Etat =====
    private DefaultListModel<String> songsListModel;
    private String selectedImagePath;
    private Album currentAlbum; // null en création

    /* ===================== CONSTRUCTEUR ===================== */
    public AlbumEditorController(AlbumEditor view) {
        this.view = Objects.requireNonNull(view);
    }

    /* ===================== INIT CONTROLLER ===================== */
    @Override
    public AlbumEditor getView() { return view; }

    @Override
    public void initController() {
        initSongsList();
        initCoverImageClick();
        initTitleEdition();
        initButtons();
        initShortcuts();
    }

    /* ===================== MODES OUVERTURE ===================== */
    public void openForCreate() {
        currentAlbum = null;
        view.setAlbumTitle("Nouvel album");
        view.getAlbumTitleField().setText("Nouvel album");
        selectedImagePath = null;
        view.setCoverImage(null);
        songsListModel.clear();
        view.setVisible(true);
    }

    public void openForEdit(Album album) {
        currentAlbum = Objects.requireNonNull(album);

        view.setAlbumTitle(album.getTitreAlbum());
        view.getAlbumTitleField().setText(album.getTitreAlbum());

        selectedImagePath = album.getCoverImageURL();
        if (selectedImagePath != null && !selectedImagePath.isBlank()) {
            ImageIcon icon = new ImageIcon(selectedImagePath);
            view.setCoverImage(icon.getImage());
        } else {
            view.setCoverImage(null);
        }

        songsListModel.clear();
        try {
            List<Musique> songs = getSongsForAlbum(album);
            for (Musique m : songs) {
                String mmss = floatMinutesToMmSs(m.getDuree());
                songsListModel.addElement(m.getTitre() + "  (" + mmss + ")");
            }
        } catch (UnsupportedOperationException ignored) { }
        view.setVisible(true);
    }

    /* ===================== PERSISTENCE ===================== */
    public Album saveToDAO() {
        String title = safeTrim(view.getAlbumTitleField().getText());
        if (title == null || title.isEmpty()) {
            showError(view, "Le nom de l'album ne peut pas être vide.", "Erreur");
            return null;
        }

        Album toPersist;
        if (currentAlbum == null) {
            toPersist = new Album(title, LocalDate.now(), new ArrayList<>(), selectedImagePath);
            albumDAO.add(toPersist);
        } else {
            currentAlbum.setTitreAlbum(title);
            currentAlbum.setCoverImageURL(selectedImagePath);
            toPersist = currentAlbum;
            albumDAO.update(toPersist);
        }
        persistSongsOfUI(toPersist);
        return toPersist;
    }

    private void persistSongsOfUI(Album album) {
        for (int i = 0; i < songsListModel.size(); i++) {
            String raw = songsListModel.get(i);
            ParsedSong ps = parseSongLine(raw);
            if (ps == null) continue;
            float minutes = mmSsToFloatMinutes(ps.mmss);
            Musique m = new Musique(ps.title, minutes, album, new ArrayList<>(), null);
            musicDAO.add(m);
        }
    }

    private List<Musique> getSongsForAlbum(Album album) {
        try {
            var method = MusicFileDAO.class.getMethod("getAllByAlbumId", int.class);
            @SuppressWarnings("unchecked")
            List<Musique> list = (List<Musique>) method.invoke(musicDAO, album.getId());
            return list != null ? list : List.of();
        } catch (NoSuchMethodException e) {
            throw new UnsupportedOperationException("MusicFileDAO.getAllByAlbumId(int) manquant");
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /* ===================== UI INIT ===================== */
    private void initSongsList() {
        if (view.getSongsList().getModel() instanceof DefaultListModel) {
            songsListModel = (DefaultListModel<String>) view.getSongsList().getModel();
        } else {
            songsListModel = new DefaultListModel<>();
            view.getSongsList().setModel(songsListModel);
        }
        view.getSongsList().addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && view.getSongsList().getSelectedIndex() >= 0) onRenameSong();
            }
        });
    }

    private void initCoverImageClick() {
        view.getCoverLabel().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        view.getCoverLabel().setToolTipText("Cliquer pour choisir une pochette (png/jpg/jpeg)");
        view.getCoverLabel().addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onChooseImage(); }
        });
    }

    private void initTitleEdition() {
        view.getEditAlbumButton().addActionListener(e -> onEditTitle());
        view.getAlbumTitleField().addActionListener(e -> onCommitTitle());
        view.getAlbumTitleField().addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { onCommitTitle(); }
        });
    }

    private void initButtons() {
        view.getAddSongButton().addActionListener(e -> onAddSong());
        view.getRemoveSongButton().addActionListener(e -> onRemoveSong());
    }

    private void initShortcuts() {
        JRootPane root = view.getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "add-song");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "remove-song");
        am.put("add-song", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { onAddSong(); }});
        am.put("remove-song", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { onRemoveSong(); }});
    }

    /* ===================== ACTIONS ===================== */
    private void onChooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choisir une pochette");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("Images (png, jpg, jpeg)", "png", "jpg", "jpeg"));
        int result = chooser.showOpenDialog(view);
        if (result == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
            selectedImagePath = chooser.getSelectedFile().getAbsolutePath();
            ImageIcon icon = new ImageIcon(selectedImagePath);
            view.setCoverImage(icon.getImage());
        }
    }

    private void onEditTitle() {
        JTextField titleField = view.getAlbumTitleField();
        titleField.requestFocusInWindow();
        titleField.selectAll();
    }

    private void onCommitTitle() {
        String name = safeTrim(view.getAlbumTitleField().getText());
        if (name == null || name.isEmpty()) {
            showError(view, "Le nom de l'album ne peut pas être vide.", "Erreur");
            view.setAlbumTitle("NomAlbum");
            view.getAlbumTitleField().requestFocusInWindow();
            view.getAlbumTitleField().selectAll();
        } else {
            view.setTitle("DiskHouse - " + name);
        }
    }

    private void onAddSong() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        JTextField title = new JTextField();
        JTextField duration = new JTextField();
        panel.add(new JLabel("Titre :")); panel.add(title);
        panel.add(new JLabel("Durée (mm:ss) :")); panel.add(duration);
        int result = JOptionPane.showConfirmDialog(view, panel, "Ajouter une musique",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String songTitle = safeTrim(title.getText());
            String songDuration = safeTrim(duration.getText());
            if (songTitle == null || songTitle.isEmpty() || songDuration == null || !songDuration.matches("^\\d{1,2}:[0-5]\\d$")) {
                showError(view, "Titre requis et durée au format mm:ss (ex: 03:25).", "Erreur");
                return;
            }
            songsListModel.addElement(songTitle + "  (" + songDuration + ")");
            int lastIndex = songsListModel.size() - 1;
            view.getSongsList().setSelectedIndex(lastIndex);
            view.getSongsList().ensureIndexIsVisible(lastIndex);
        }
    }

    private void onRemoveSong() {
        int index = view.getSongsList().getSelectedIndex();
        if (index < 0) { showInfo(view, "Sélectionne une musique à supprimer.", "Information"); return; }
        String song = songsListModel.get(index);
        int confirm = JOptionPane.showConfirmDialog(view, "Supprimer \"" + song + "\" ?",
                "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) songsListModel.remove(index);
    }

    private void onRenameSong() {
        int index = view.getSongsList().getSelectedIndex();
        if (index < 0) return;
        String current = songsListModel.get(index);
        int parenIndex = current.lastIndexOf("  (");
        String nameOnly = (parenIndex > 0) ? current.substring(0, parenIndex) : current;
        String newName = (String) JOptionPane.showInputDialog(
                view, "Nouveau titre :", "Renommer la musique",
                JOptionPane.QUESTION_MESSAGE, null, null, nameOnly
        );
        if (newName != null) {
            newName = safeTrim(newName);
            if (newName != null && !newName.isEmpty()) {
                String durationPart = (parenIndex > 0) ? current.substring(parenIndex) : "";
                songsListModel.set(index, newName + durationPart);
            } else {
                showError(view, "Le titre ne peut pas être vide.", "Erreur");
            }
        }
    }

    /* ===================== HELPERS ===================== */
    @Override
    public void showError(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void showInfo(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private static String safeTrim(String s) { return s == null ? null : s.trim(); }

    private static class ParsedSong {
        final String title; final String mmss;
        ParsedSong(String t, String d) { this.title = t; this.mmss = d; }
    }

    private static ParsedSong parseSongLine(String raw) {
        if (raw == null) return null;
        int p = raw.lastIndexOf("  (");
        int q = raw.endsWith(")") ? raw.length() - 1 : -1;
        if (p > 0 && q > p+3) {
            String title = raw.substring(0, p).trim();
            String mmss  = raw.substring(p + 3, q).trim();
            if (!mmss.matches("^\\d{1,2}:[0-5]\\d$")) return null;
            return new ParsedSong(title, mmss);
        }
        return null;
    }

    private static float mmSsToFloatMinutes(String mmss) {
        String[] parts = mmss.split(":");
        int m = Integer.parseInt(parts[0]);
        int s = Integer.parseInt(parts[1]);
        return m + (s / 60f);
    }

    private static String floatMinutesToMmSs(float minutes) {
        int totalSec = Math.round(minutes * 60f);
        int mm = totalSec / 60;
        int ss = totalSec % 60;
        return String.format("%02d:%02d", mm, ss);
    }

    public DefaultListModel<String> getSongsListModel() { return songsListModel; }
    public String getAlbumTitle() { return view.getAlbumTitleField().getText(); }
    public String getSelectedImagePath() { return selectedImagePath; }
}
