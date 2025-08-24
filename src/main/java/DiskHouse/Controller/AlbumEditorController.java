package DiskHouse.Controller;

import DiskHouse.view.AlbumEditor;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;

public class AlbumEditorController implements IController<AlbumEditor> {

    private final AlbumEditor view;
    private DefaultListModel<String> listModel;
    private String selectedImagePath;

    public AlbumEditorController(AlbumEditor view) {
        this.view = Objects.requireNonNull(view);
    }

    @Override
    public AlbumEditor getView() { return view; }

    @Override
    @SuppressWarnings("unchecked")
    public void initController() {
        // Modèle de liste
        if (view.getSongsList().getModel() instanceof DefaultListModel<?> m) {
            listModel = (DefaultListModel<String>) m;
        } else {
            listModel = new DefaultListModel<>();
            view.getSongsList().setModel(listModel);
        }

        // Image cliquable
        view.getCoverLabel().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        view.getCoverLabel().setToolTipText("Cliquer pour choisir une pochette (png/jpg/jpeg)");
        view.getCoverLabel().addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onChooseImage(); }
        });

        // Édition du titre (✎)
        view.getEditAlbumButton().addActionListener(e -> onEditTitle());

        // Commit titre : Enter ou focusLost
        view.getAlbumTitleField().addActionListener(e -> onCommitTitle());
        view.getAlbumTitleField().addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { onCommitTitle(); }
        });

        // Boutons liste
        view.getAddSongButton().addActionListener(e -> onAddItem());
        view.getRemoveSongButton().addActionListener(e -> onRemoveSelectedItem());

        // Double-clic -> renommer
        view.getSongsList().addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && view.getSongsList().getSelectedIndex() >= 0) {
                    onRenameSelectedItem();
                }
            }
        });

        // Raccourcis : Enter (ajout), Suppr (suppression)
        JRootPane root = view.getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "do-add");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "do-del");
        am.put("do-add", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { onAddItem(); }});
        am.put("do-del", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { onRemoveSelectedItem(); }});
    }

    /* ===================== Actions ===================== */

    private void onChooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choisir une pochette");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("Images (png, jpg, jpeg)", "png", "jpg", "jpeg"));

        if (chooser.showOpenDialog(view) == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
            selectedImagePath = chooser.getSelectedFile().getAbsolutePath();
            ImageIcon icon = new ImageIcon(selectedImagePath);
            view.setCoverImage(icon.getImage());
        }
    }

    private void onEditTitle() {
        JTextField tf = view.getAlbumTitleField();
        tf.requestFocusInWindow();
        tf.selectAll();
    }

    private void onCommitTitle() {
        String name = view.getAlbumTitleField().getText();
        if (name != null) name = name.trim();
        if (name == null || name.isEmpty()) {
            showError(view, "Le nom de l'album ne peut pas être vide.", "Erreur");
            view.setAlbumTitle("NomAlbum");
            view.getAlbumTitleField().requestFocusInWindow();
            view.getAlbumTitleField().selectAll();
        } else {
            view.setTitle("DiskHouse - " + name);
        }
    }

    private void onAddItem() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        JTextField title = new JTextField();
        JTextField duration = new JTextField(); // mm:ss
        panel.add(new JLabel("Titre :"));
        panel.add(title);
        panel.add(new JLabel("Durée (mm:ss) :"));
        panel.add(duration);

        int result = JOptionPane.showConfirmDialog(view, panel, "Ajouter une musique",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String t = title.getText().trim();
            String d = duration.getText().trim();
            if (t.isEmpty() || !d.matches("^\\d{1,2}:[0-5]\\d$")) {
                showError(view, "Titre requis et durée au format mm:ss (ex: 03:25).", "Erreur");
                return;
            }
            listModel.addElement(t + "  (" + d + ")");
            int last = listModel.size() - 1;
            view.getSongsList().setSelectedIndex(last);
            view.getSongsList().ensureIndexIsVisible(last);
        }
    }

    private void onRemoveSelectedItem() {
        int idx = view.getSongsList().getSelectedIndex();
        if (idx < 0) {
            showInfo(view, "Sélectionne une musique à supprimer.", "Information");
            return;
        }
        String name = listModel.get(idx);
        int ok = JOptionPane.showConfirmDialog(view, "Supprimer \"" + name + "\" ?",
                "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            listModel.remove(idx);
        }
    }

    private void onRenameSelectedItem() {
        int idx = view.getSongsList().getSelectedIndex();
        if (idx < 0) return;
        String current = listModel.get(idx);
        String nameOnly = current;
        int paren = current.lastIndexOf("  (");
        if (paren > 0) nameOnly = current.substring(0, paren);

        String newName = (String) JOptionPane.showInputDialog(
                view, "Nouveau titre :", "Renommer la musique",
                JOptionPane.QUESTION_MESSAGE, null, null, nameOnly
        );
        if (newName != null) {
            newName = newName.trim();
            if (!newName.isEmpty()) {
                String durationPart = "";
                int p = current.lastIndexOf("  (");
                if (p > 0) durationPart = current.substring(p);
                listModel.set(idx, newName + durationPart);
            } else {
                showError(view, "Le titre ne peut pas être vide.", "Erreur");
            }
        }
    }

    /* ===================== Accès modèle ===================== */

    public DefaultListModel<String> getListModel() { return listModel; }
    public String getAlbumTitle() { return view.getAlbumTitleField().getText(); }
    public String getSelectedImagePath() { return selectedImagePath; }
}
