//************************************************//
//************** ALBUM EDITOR CONTROLLER *********//
//************************************************//
// Contrôleur pour la création et l'édition d'un album.
// Gère la logique d'UI, la validation, la persistance DAO et les interactions utilisateur.

package DiskHouse.Controller;

import DiskHouse.model.DAO.AlbumFileDAO;
import DiskHouse.model.DAO.MusicFileDAO;
import DiskHouse.model.entity.Album;
import DiskHouse.model.entity.Musique;
import DiskHouse.view.AlbumEditor;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AlbumEditorController implements IController<AlbumEditor> {

    //************************************************//
    //************** ATTRIBUTS ************************//
    //************************************************//
    private final AlbumEditor view;
    private final String username;
    private final AlbumFileDAO albumDAO;
    private final MusicFileDAO musicDAO;
    private DefaultTableModel songsTableModel;
    private String selectedImagePath;
    private Album currentAlbum; // null = création

    //************************************************//
    //************** CONSTRUCTEUR *********************//
    //************************************************//
    public AlbumEditorController(AlbumEditor view, String username) {
        this.view = Objects.requireNonNull(view);
        this.username = username;
        this.albumDAO = new AlbumFileDAO("data/" + username + "_albums.dat");
        this.musicDAO = new MusicFileDAO("data/" + username + "_musiques.dat");
    }

    //************************************************//
    //************** GETTEUR VUE **********************//
    //************************************************//
    @Override
    public AlbumEditor getView() { return view; }

    //************************************************//
    //************** INITIALISATION *******************//
    //************************************************//
    @Override
    public void initController() {
        initSongsTable();
        initCoverImageListener();
        initTitleEdition();
        initButtons();
        initShortcuts();
    }

    private void initSongsTable() {
        songsTableModel = new DefaultTableModel(new Object[]{"Titre", "Durée"}, 0);
        view.getSongsList().setModel(songsTableModel);
        view.getSongsList().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && view.getSongsList().getSelectedRow() >= 0) {
                    onRenameSong();
                }
            }
        });
    }

    private void initCoverImageListener() {
        view.getCoverLabel().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        view.getCoverLabel().setToolTipText("Cliquer pour choisir une pochette (png/jpg/jpeg)");
        view.getCoverLabel().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onChooseImage();
            }
        });
    }

    private void initTitleEdition() {
        view.getEditAlbumButton().addActionListener(e -> {
            JTextField tf = view.getAlbumTitleField();
            tf.requestFocusInWindow();
            tf.selectAll();
        });
        view.getAlbumTitleField().addActionListener(e -> onCommitTitle());
        view.getAlbumTitleField().addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                onCommitTitle();
            }
        });
    }

    private void initButtons() {
        view.getAddSongButton().addActionListener(e -> onAddSong());
        view.getRemoveSongButton().addActionListener(e -> onRemoveSong());
        view.getCancelButton().addActionListener(e -> view.dispose());
        view.getOkButton().addActionListener(e -> {
            Album saved = saveAlbumToDAO();
            if (saved != null) {
                view.dispose();
            }
        });
    }

    private void initShortcuts() {
        JRootPane root = view.getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "add-song");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "remove-song");
        am.put("add-song", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAddSong();
            }
        });
        am.put("remove-song", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onRemoveSong();
            }
        });
    }

    //************************************************//
    //************** OUVERTURE ************************//
    //************************************************//
    public void openForCreate() {
        currentAlbum = null;
        view.setAlbumTitle("");
        selectedImagePath = null;
        view.setCoverImage((String) null);
        songsTableModel.setRowCount(0);
        view.ensureSongsPlaceholder();
        view.setAlbumDate("");
        view.setLocationRelativeTo(SwingUtilities.getWindowAncestor(view));
        view.setVisible(true);
    }

    public void openForEdit(Album album) {
        currentAlbum = Objects.requireNonNull(album);
        view.setAlbumTitle(album.getTitreAlbum());
        selectedImagePath = album.getCoverImageURL();
        if (album.getDateSortie() != null) {
            view.setAlbumDate(album.getDateSortie().toString());
        } else {
            view.setAlbumDate("");
        }
        if (selectedImagePath != null && !selectedImagePath.isBlank()) {
            view.setCoverImage(selectedImagePath);
        } else {
            view.setCoverImage((String) null);
        }
        songsTableModel.setRowCount(0);
        try {
            for (Musique m : getSongsForAlbum(album)) {
                songsTableModel.addRow(new Object[]{m.getTitre(), floatMinutesToMmSs(m.getDuree())});
            }
        } catch (UnsupportedOperationException ignored) { }
        view.ensureSongsPlaceholder();
        view.setLocationRelativeTo(SwingUtilities.getWindowAncestor(view));
        view.setVisible(true);
    }

    //************************************************//
    //************** PERSISTANCE DAO ******************//
    //************************************************//
    public Album saveAlbumToDAO() {
        String title = safeTrim(view.getAlbumTitleField().getText());
        if (title == null || title.isEmpty() || "Titre de l’album".equals(title)) {
            showError(view, "Le nom de l'album ne peut pas être vide.", "Erreur");
            return null;
        }
        String dateStr = view.getAlbumDate();
        LocalDate date;
        if (dateStr != null && !dateStr.isBlank()) {
            try {
                date = LocalDate.parse(dateStr);
            } catch (Exception e) {
                showError(view, "Format de date invalide. Utilise AAAA-MM-JJ.", "Erreur");
                return null;
            }
        } else {
            date = LocalDate.now();
        }
        Album toPersist;
        if (currentAlbum == null) {
            toPersist = new Album(title, date, new ArrayList<>(), selectedImagePath);
            albumDAO.add(toPersist);
        } else {
            currentAlbum.setTitreAlbum(title);
            currentAlbum.setCoverImageURL(selectedImagePath);
            currentAlbum.setDateSortie(date);
            toPersist = currentAlbum;
            albumDAO.update(toPersist);
        }
        saveSongsFromTableToDAO(toPersist);
        return toPersist;
    }

    private void saveSongsFromTableToDAO(Album album) {
        for (int i = 0; i < songsTableModel.getRowCount(); i++) {
            String title = (String) songsTableModel.getValueAt(i, 0);
            String mmss = (String) songsTableModel.getValueAt(i, 1);
            if (title == null || mmss == null) continue;
            float minutes = mmSsToFloatMinutes(mmss);
            Musique m = new Musique(title, minutes, album, new ArrayList<>(), null);
            musicDAO.add(m);
        }
    }

    private List<Musique> getSongsForAlbum(Album album) {
        try {
            var m = MusicFileDAO.class.getMethod("getAllByAlbumId", int.class);
            @SuppressWarnings("unchecked")
            List<Musique> list = (List<Musique>) m.invoke(musicDAO, album.getId());
            return list != null ? list : List.of();
        } catch (NoSuchMethodException e) {
            throw new UnsupportedOperationException("MusicFileDAO.getAllByAlbumId(int) manquant");
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    //************************************************//
    //************** ACTIONS UI ************************//
    //************************************************//
    private void onChooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choisir une pochette");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("Images (png, jpg, jpeg)", "png", "jpg", "jpeg"));
        int result = chooser.showOpenDialog(view);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File file = chooser.getSelectedFile();
            if (file != null) {
                selectedImagePath = file.toURI().toString();
                view.setCoverImage(selectedImagePath);
            }
        }
    }

    private void onCommitTitle() {
        String name = safeTrim(view.getAlbumTitleField().getText());
        if (name == null || name.isEmpty() || "Titre de l’album".equals(name)) {
            showError(view, "Le nom de l'album ne peut pas être vide.", "Erreur");
            view.getAlbumTitleField().requestFocusInWindow();
            view.getAlbumTitleField().selectAll();
        } else {
            view.setTitle("DiskHouse - " + name);
        }
    }

    private void onAddSong() {
        String songTitle = view.getSongTitleField().getText();
        String songDuration = view.getSongDurationField().getText();
        if (songTitle == null || songTitle.isEmpty() || songDuration == null || !songDuration.matches("^\\d{1,2}:[0-5]\\d$")) {
            showError(view, "Titre requis et durée au format mm:ss (ex: 03:25).", "Erreur");
            return;
        }
        songsTableModel.addRow(new Object[]{songTitle, songDuration});
        int lastRow = songsTableModel.getRowCount() - 1;
        if (lastRow >= 0) {
            view.getSongsList().setRowSelectionInterval(lastRow, lastRow);
            view.getSongsList().scrollRectToVisible(view.getSongsList().getCellRect(lastRow, 0, true));
        }
        view.ensureSongsPlaceholder();
    }

    private void onRemoveSong() {
        int index = view.getSongsList().getSelectedRow();
        if (index < 0) {
            showInfo(view, "Sélectionne une musique à supprimer.", "Information");
            return;
        }
        String song = (String) songsTableModel.getValueAt(index, 0);
        int confirm = JOptionPane.showConfirmDialog(view, "Supprimer \"" + song + "\" ?",
                "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            songsTableModel.removeRow(index);
            view.ensureSongsPlaceholder();
        }
    }

    private void onRenameSong() {
        int index = view.getSongsList().getSelectedRow();
        if (index < 0) return;
        String current = (String) songsTableModel.getValueAt(index, 0);
        int p = current.lastIndexOf("  (");
        String base = (p > 0) ? current.substring(0, p) : current;
        String newName = (String) JOptionPane.showInputDialog(view, "Nouveau titre :", "Renommer la musique",
                JOptionPane.QUESTION_MESSAGE, null, null, base);
        if (newName != null) {
            newName = safeTrim(newName);
            if (newName != null && !newName.isEmpty()) {
                String tail = (p > 0) ? current.substring(p) : "";
                songsTableModel.setValueAt(newName + tail, index, 0);
            } else {
                showError(view, "Le titre ne peut pas être vide.", "Erreur");
            }
        }
    }

    //************************************************//
    //************** HELPERS **************************//
    //************************************************//
    @Override
    public void showError(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }
    @Override
    public void showInfo(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private static String safeTrim(String s) {
        if (s == null) return null;
        return s.trim();
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

    public String getSelectedImagePath() {
        return selectedImagePath;
    }
}
