package DiskHouse.model.DAO;

import DiskHouse.model.entity.Musique;
import DiskHouse.model.entity.Playlist;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class PlaylistFileDAO implements IDAO<Playlist> {

    private final File file;

    public PlaylistFileDAO(String filePath) {
        this.file = new File(filePath);
    }

    // ------------------- Create -------------------
    @Override
    public void add(Playlist playlist) {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file, true))) {
            dos.writeInt(playlist.getId());
            dos.writeUTF(playlist.getNomPlaylist());
            dos.writeInt(playlist.getNombreMusique());
            dos.writeUTF(playlist.getCoverImageURL() != null ? playlist.getCoverImageURL() : "");
            for (Musique m : playlist.getMusiques()) {
                dos.writeInt(m.getId());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ------------------- Read -------------------
    @Override
    public Playlist getById(String id) {
        for (Playlist p : getAll()) {
            if (String.valueOf(p.getId()).equals(id)) return p;
        }
        return null;
    }

    @Override
    public Playlist getByName(String name) {
        for (Playlist p : getAll()) {
            if (p.getNomPlaylist().equalsIgnoreCase(name)) return p;
        }
        return null;
    }

    /**
     * Lecture "shallow" mais on CONSERVE les IDs des musiques
     * via des placeholders, pour ne pas les perdre lors d'un saveAll/update.
     */
    @Override
    public List<Playlist> getAll() {
        List<Playlist> playlists = new ArrayList<>();
        if (!file.exists()) return playlists;

        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            while (dis.available() > 0) {
                int pid = dis.readInt();
                String nom = dis.readUTF();
                int nbMusiques = dis.readInt();
                String cover = dis.readUTF();

                Playlist p = new Playlist(nom, new ArrayList<>());
                applyId(p, pid);
                p.setCoverImageURL(cover);

                // Lire les IDs et créer des placeholders (uniquement l'id)
                List<Musique> placeholders = new ArrayList<>(nbMusiques);
                for (int i = 0; i < nbMusiques; i++) {
                    int mid = dis.readInt();
                    Musique ph = new Musique("(lazy)", 0f, null, new ArrayList<>(), "");
                    applyId(ph, mid);
                    placeholders.add(ph);
                }
                p.setMusiques(placeholders);

                playlists.add(p);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return playlists;
    }

    /** Lecture complète : réhydrate les musiques avec le DAO musique. */
    public List<Playlist> getAllWithSongs(MusicFileDAO musicDAO) {
        List<Playlist> playlists = new ArrayList<>();
        if (!file.exists()) return playlists;

        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            while (dis.available() > 0) {
                int pid = dis.readInt();
                String nom = dis.readUTF();
                int nbMusiques = dis.readInt();
                String cover = dis.readUTF();

                Playlist p = new Playlist(nom, new ArrayList<>());
                applyId(p, pid);
                p.setCoverImageURL(cover);

                for (int i = 0; i < nbMusiques; i++) {
                    int mid = dis.readInt();
                    Musique m = musicDAO.getById(String.valueOf(mid));
                    if (m != null) p.ajouterMusique(m);
                }
                playlists.add(p);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return playlists;
    }

    // ------------------- Update -------------------
    @Override
    public void update(Playlist playlist) {
        List<Playlist> all = getAll(); // conserve les IDs via placeholders
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId() == playlist.getId()) {
                all.set(i, playlist);
                break;
            }
        }
        saveAll(all);
    }

    // ------------------- Delete -------------------
    @Override
    public void delete(String id) {
        List<Playlist> all = getAll(); // conserve les IDs des autres playlists
        all.removeIf(p -> String.valueOf(p.getId()).equals(id));
        saveAll(all);
    }

    // ------------------- Utils -------------------
    private void saveAll(List<Playlist> playlists) {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file, false))) {
            for (Playlist p : playlists) {
                dos.writeInt(p.getId());
                dos.writeUTF(p.getNomPlaylist());
                dos.writeInt(p.getNombreMusique());
                dos.writeUTF(p.getCoverImageURL() != null ? p.getCoverImageURL() : "");
                for (Musique m : p.getMusiques()) {
                    dos.writeInt(m.getId());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void applyId(Object o, int id) {
        try {
            Field f = findIdField(o.getClass());
            if (f != null) { f.setAccessible(true); f.setInt(o, id); }
        } catch (Exception ignored) {}
    }
    private Field findIdField(Class<?> c) {
        while (c != null) {
            try { return c.getDeclaredField("id"); }
            catch (NoSuchFieldException e) { c = c.getSuperclass(); }
        }
        return null;
    }
}
