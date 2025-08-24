package DiskHouse.model.DAO;

import DiskHouse.model.entity.Artiste;
import DiskHouse.model.entity.Album;
import DiskHouse.model.entity.Musique;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Field;

public class MusicFileDAO implements IDAO<Musique> {

    private final File file;
    // DAOs pour réhydrater les liens
    private final ArtisteFileDAO artisteDAO;
    private final AlbumFileDAO albumDAO;

    /** Constructeur simple : DAOs par défaut vers data/*.dat */
    public MusicFileDAO(String filePath) {
        this.file = new File(filePath);
        this.artisteDAO = new ArtisteFileDAO("data/artistes.dat");
        this.albumDAO   = new AlbumFileDAO("data/albums.dat");
    }

    /** Constructeur avec DAOs injectés (si tu préfères). */
    public MusicFileDAO(String filePath, ArtisteFileDAO artisteDAO, AlbumFileDAO albumDAO) {
        this.file = new File(filePath);
        this.artisteDAO = artisteDAO;
        this.albumDAO   = albumDAO;
    }

    // ------------------- Create -------------------
    @Override
    public void add(Musique musique) {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file, true))) {
            dos.writeInt(musique.getId());
            dos.writeUTF(nz(musique.getTitre()));
            dos.writeFloat(musique.getDuree());

            // album ID
            int albumId = (musique.getAlbum() != null) ? musique.getAlbum().getId() : -1;
            dos.writeInt(albumId);

            // cover
            dos.writeUTF(nz(musique.getCoverImageURL()));

            // artistes (IDs)
            List<Artiste> arts = (musique.getArtistes() == null) ? List.of() : musique.getArtistes();
            dos.writeInt(arts.size());
            for (Artiste a : arts) {
                dos.writeInt(a.getId());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ------------------- Read -------------------
    @Override
    public Musique getById(String id) {
        for (Musique m : getAll()) {
            if (String.valueOf(m.getId()).equals(id)) return m;
        }
        return null;
    }

    @Override
    public Musique getByName(String name) {
        for (Musique m : getAll()) {
            if (m.getTitre().equalsIgnoreCase(name)) return m;
        }
        return null;
    }

    @Override
    public List<Musique> getAll() {
        List<Musique> musiques = new ArrayList<>();
        if (!file.exists()) return musiques;

        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            while (dis.available() > 0) {
                int id = dis.readInt();
                String titre = dis.readUTF();
                float duree = dis.readFloat();
                int albumId = dis.readInt();
                String cover = dis.readUTF();

                // artistes (IDs)
                int nbArtistes = 0;
                if (dis.available() >= 4) {
                    nbArtistes = dis.readInt();
                }
                List<Artiste> artistes = new ArrayList<>(nbArtistes);
                for (int i = 0; i < nbArtistes; i++) {
                    if (dis.available() >= 4) {
                        int aid = dis.readInt();
                        Artiste a = artisteDAO.getById(String.valueOf(aid));
                        if (a != null) artistes.add(a);
                    }
                }

                Album album = (albumId >= 0) ? albumDAO.getById(String.valueOf(albumId)) : null;

                Musique m = new Musique(titre, duree, album, artistes, cover);
                applyId(m, id); // IMPORTANT : remet l'ID persistant
                musiques.add(m);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return musiques;
    }

    // ------------------- Update/Delete -------------------
    @Override
    public void update(Musique musique) {
        List<Musique> all = getAll();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId() == musique.getId()) {
                all.set(i, musique);
                break;
            }
        }
        saveAll(all);
    }

    @Override
    public void delete(String id) {
        List<Musique> all = getAll();
        all.removeIf(m -> String.valueOf(m.getId()).equals(id));
        saveAll(all);
    }

    private void saveAll(List<Musique> musiques) {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file, false))) {
            for (Musique musique : musiques) {
                dos.writeInt(musique.getId());
                dos.writeUTF(nz(musique.getTitre()));
                dos.writeFloat(musique.getDuree());
                dos.writeInt((musique.getAlbum() != null) ? musique.getAlbum().getId() : -1);
                dos.writeUTF(nz(musique.getCoverImageURL()));

                List<Artiste> arts = (musique.getArtistes() == null) ? List.of() : musique.getArtistes();
                dos.writeInt(arts.size());
                for (Artiste a : arts) dos.writeInt(a.getId());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ------------------- Helpers -------------------
    private static String nz(String s) { return (s == null) ? "" : s; }

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
