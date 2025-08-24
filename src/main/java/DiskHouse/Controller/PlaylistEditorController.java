package DiskHouse.Controller;

import DiskHouse.view.PlaylistEditor;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Objects;

public class PlaylistEditorController implements IController<PlaylistEditor> {

    private final PlaylistEditor view;
    private DefaultListModel<String> listModel;
    private String selectedImagePath;

    public PlaylistEditorController(PlaylistEditor view) {
        this.view = Objects.requireNonNull(view);
    }

    @Override
    public PlaylistEditor getView() { return view; }

    @Override
    @SuppressWarnings("unchecked")
    public void initController() {
        // modèle liste
        ListModel<String> m = view.getTrackList().getModel();
        if (m instanceof DefaultListModel<?> dm) {
            listModel = (DefaultListModel<String>) dm;
        } else {
            listModel = new DefaultListModel<>();
            view.getTrackList().setModel(listModel);
        }

        // cover -> choisir image
        view.getCoverLabel().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        view.getCoverLabel().setToolTipText("Cliquer pour choisir une pochette (png/jpg/jpeg)");
        view.getCoverLabel().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { onChooseImage(); }
        });

        // ✎ -> focus titre
        view.getEditPlaylistButton().addActionListener(e -> onEditTitle());

        // + / 🗑
        view.getAddTrackButton().addActionListener(e -> onAddItem());
        view.getRemoveTrackButton().addActionListener(e -> onRemoveSelectedItem());

        // Raccourci Suppr
        JRootPane root = view.getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "del");
        am.put("del", new AbstractAction() { @Override public void actionPerformed(java.awt.event.ActionEvent e) { onRemoveSelectedItem(); }});
    }

    /* ================= Actions ================= */

    private void onChooseImage() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Choisir une pochette");
        fc.setAcceptAllFileFilterUsed(false);
        fc.addChoosableFileFilter(new FileNameExtensionFilter("Images (png, jpg, jpeg)", "png", "jpg", "jpeg"));
        if (fc.showOpenDialog(view) == JFileChooser.APPROVE_OPTION && fc.getSelectedFile() != null) {
            selectedImagePath = fc.getSelectedFile().getAbsolutePath();
            ImageIcon icon = new ImageIcon(selectedImagePath);
            view.setCoverImage(icon.getImage());
        }
    }

    private void onEditTitle() {
        view.getPlaylistTitleField().requestFocusInWindow();
        view.getPlaylistTitleField().selectAll();
    }

    private void onAddItem() {
        String name = JOptionPane.showInputDialog(view, "Nom de la musique :", "Ajouter", JOptionPane.QUESTION_MESSAGE);
        if (name == null) return;
        name = name.trim();
        if (name.isEmpty()) {
            showError(view, "Le nom ne peut pas être vide.", "Erreur");
            return;
        }
        listModel.addElement(name);
        int i = listModel.size() - 1;
        view.getTrackList().setSelectedIndex(i);
        view.getTrackList().ensureIndexIsVisible(i);
    }

    private void onRemoveSelectedItem() {
        int idx = view.getTrackList().getSelectedIndex();
        if (idx < 0) return;
        String val = listModel.get(idx);
        int ok = JOptionPane.showConfirmDialog(view,
                "Supprimer \"" + val + "\" ?", "Confirmation",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            listModel.remove(idx);
        }
    }

    /* ================= Accès modèle ================= */

    public DefaultListModel<String> getListModel() { return listModel; }
    public String getSelectedImagePath() { return selectedImagePath; }
}
