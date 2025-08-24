package DiskHouse.model.DAO;

import DiskHouse.model.entity.Musique;
import DiskHouse.model.entity.Playlist;

import java.io.*;
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
        try (DataOutputStream dos = new DataOutputStream(
                new FileOutputStream(file, true))) {
            dos.writeInt(playlist.getId());
            dos.writeUTF(playlist.getNomPlaylist());
            dos.writeInt(playlist.getNombreMusique());
            dos.writeUTF(playlist.getCoverImageURL() != null ? playlist.getCoverImageURL() : "");

            // On stocke les IDs des musiques
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
        return getAll().stream()
                .filter(p -> String.valueOf(p.getId()).equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Playlist getByName(String name) {
        return getAll().stream()
                .filter(p -> p.getNomPlaylist().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Playlist> getAll() {
        List<Playlist> playlists = new ArrayList<>();
        if (!file.exists()) return playlists;

        try (DataInputStream dis = new DataInputStream(
                new FileInputStream(file))) {
            while (dis.available() > 0) {
                int id = dis.readInt();
                String nom = dis.readUTF();
                int nbMusiques = dis.readInt();
                String cover = dis.readUTF();

                // Pour l’instant on crée la playlist vide
                Playlist p = new Playlist(nom, new ArrayList<>());
                p.setCoverImageURL(cover);

                // Lire les IDs de musiques (sans les recharger)
                List<Integer> musiqueIds = new ArrayList<>();
                for (int i = 0; i < nbMusiques; i++) {
                    musiqueIds.add(dis.readInt());
                }

                // Ici tu pourrais aller chercher les musiques via MusicFileDAO si tu veux
                // Exemple :
                // MusicFileDAO musicDAO = new MusicFileDAO("musiques.dat");
                // for (int mid : musiqueIds) {
                //     Musique m = musicDAO.getById(String.valueOf(mid));
                //     if (m != null) p.ajouterMusique(m);
                // }

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
        List<Playlist> playlists = getAll();
        for (int i = 0; i < playlists.size(); i++) {
            if (playlists.get(i).getId() == playlist.getId()) {
                playlists.set(i, playlist);
                break;
            }
        }
        saveAll(playlists);
    }

    // ------------------- Delete -------------------
    @Override
    public void delete(String id) {
        List<Playlist> playlists = getAll();
        playlists.removeIf(p -> String.valueOf(p.getId()).equals(id));
        saveAll(playlists);
    }

    // ------------------- Utils -------------------
    private void saveAll(List<Playlist> playlists) {
        try (DataOutputStream dos = new DataOutputStream(
                new FileOutputStream(file, false))) {
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
}