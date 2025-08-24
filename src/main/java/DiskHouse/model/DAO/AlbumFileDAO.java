package DiskHouse.model.DAO;

import DiskHouse.model.entity.Album;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Field;

public class AlbumFileDAO implements IDAO<Album> {

    private final File file;

    public AlbumFileDAO(String filePath) {
        this.file = new File(filePath);
    }

    @Override
    public void add(Album album) {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file, true))) {
            dos.writeInt(album.getId());
            dos.writeUTF(album.getTitreAlbum());
            dos.writeUTF(album.getDateSortie().toString());
            dos.writeUTF(album.getCoverImageURL());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Album getById(String id) {
        for (Album a : getAll()) {
            if (String.valueOf(a.getId()).equals(id)) return a;
        }
        return null;
    }

    @Override
    public Album getByName(String name) {
        for (Album a : getAll()) {
            if (a.getTitreAlbum().equalsIgnoreCase(name)) return a;
        }
        return null;
    }

    @Override
    public List<Album> getAll() {
        List<Album> albums = new ArrayList<>();
        if (!file.exists()) return albums;

        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            while (dis.available() > 0) {
                int id = dis.readInt();
                String titre = dis.readUTF();
                LocalDate date = LocalDate.parse(dis.readUTF());
                String cover = dis.readUTF();
                Album a = new Album(titre, date, new ArrayList<>(), cover);
                applyId(a, id);
                albums.add(a);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return albums;
    }

    @Override
    public void update(Album album) {
        List<Album> albums = getAll();
        for (int i = 0; i < albums.size(); i++) {
            if (albums.get(i).getId() == album.getId()) {
                albums.set(i, album);
                break;
            }
        }
        saveAll(albums);
    }

    @Override
    public void delete(String id) {
        List<Album> albums = getAll();
        albums.removeIf(a -> String.valueOf(a.getId()).equals(id));
        saveAll(albums);
    }

    private void saveAll(List<Album> albums) {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file, false))) {
            for (Album album : albums) {
                dos.writeInt(album.getId());
                dos.writeUTF(album.getTitreAlbum());
                dos.writeUTF(album.getDateSortie().toString());
                dos.writeUTF(album.getCoverImageURL());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remplace tous les albums par la liste fournie (pour import/export global).
     */
    public void replaceAll(List<Album> albums) {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file, false))) {
            for (Album album : albums) {
                dos.writeInt(album.getId());
                dos.writeUTF(album.getTitreAlbum());
                dos.writeUTF(album.getDateSortie().toString());
                dos.writeUTF(album.getCoverImageURL());
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
