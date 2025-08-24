package DiskHouse.model.DAO;

import DiskHouse.model.entity.Musique;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MusicFileDAO implements IDAO<Musique> {

    private final File file;

    public MusicFileDAO(String filePath) {
        this.file = new File(filePath);
    }

    // ------------------- Create -------------------
    @Override
    public void add(Musique musique) {
        try (DataOutputStream dos = new DataOutputStream(
                new FileOutputStream(file, true))) {
            dos.writeInt(musique.getId());
            dos.writeUTF(musique.getTitre());
            dos.writeFloat(musique.getDuree());
            // on stocke seulement l’ID de l’album (ou -1 si null)
            dos.writeInt(musique.getAlbum() != null ? musique.getAlbum().getId() : -1);
            dos.writeUTF(musique.getCoverImageURL() != null ? musique.getCoverImageURL() : "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ------------------- Read -------------------
    @Override
    public Musique getById(String id) {
        List<Musique> all = getAll();
        for (Musique m : all) {
            if (String.valueOf(m.getId()).equals(id)) {
                return m;
            }
        }
        return null;
    }

    @Override
    public Musique getByName(String name) {
        List<Musique> all = getAll();
        for (Musique m : all) {
            if (m.getTitre().equalsIgnoreCase(name)) {
                return m;
            }
        }
        return null;
    }

    @Override
    public List<Musique> getAll() {
        List<Musique> musiques = new ArrayList<>();
        if (!file.exists()) return musiques;

        try (DataInputStream dis = new DataInputStream(
                new FileInputStream(file))) {
            while (dis.available() > 0) {
                int id = dis.readInt();
                String titre = dis.readUTF();
                float duree = dis.readFloat();
                int albumId = dis.readInt(); // pour plus tard si tu veux relier avec AlbumDAO
                String cover = dis.readUTF();

                Musique musique = new Musique(titre, duree, null, new ArrayList<>(), cover);
                musiques.add(musique);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return musiques;
    }

    // ------------------- Update -------------------
    @Override
    public void update(Musique musique) {
        List<Musique> musiques = getAll();
        for (int i = 0; i < musiques.size(); i++) {
            if (musiques.get(i).getId() == musique.getId()) {
                musiques.set(i, musique);
                break;
            }
        }
        saveAll(musiques);
    }

    // ------------------- Delete -------------------
    @Override
    public void delete(String id) {
        List<Musique> musiques = getAll();
        musiques.removeIf(m -> String.valueOf(m.getId()).equals(id));
        saveAll(musiques);
    }

    // ------------------- Utils -------------------
    private void saveAll(List<Musique> musiques) {
        try (DataOutputStream dos = new DataOutputStream(
                new FileOutputStream(file, false))) {
            for (Musique musique : musiques) {
                dos.writeInt(musique.getId());
                dos.writeUTF(musique.getTitre());
                dos.writeFloat(musique.getDuree());
                dos.writeInt(musique.getAlbum() != null ? musique.getAlbum().getId() : -1);
                dos.writeUTF(musique.getCoverImageURL() != null ? musique.getCoverImageURL() : "");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
