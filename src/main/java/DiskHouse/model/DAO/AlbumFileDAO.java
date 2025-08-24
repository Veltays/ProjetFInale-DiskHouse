package DiskHouse.model.DAO;

import DiskHouse.model.entity.Album;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AlbumFileDAO implements IDAO<Album> {

    private final File file;

    public AlbumFileDAO(String filePath) {
        this.file = new File(filePath);
    }

    // ------------------- Create -------------------
    @Override
    public void add(Album album) {
        try (DataOutputStream dos = new DataOutputStream(
                new FileOutputStream(file, true))) { // append = true
            dos.writeInt(album.getId());
            dos.writeUTF(album.getTitreAlbum());
            dos.writeUTF(album.getDateSortie().toString()); // LocalDate -> String
            dos.writeUTF(album.getCoverImageURL());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ------------------- Read -------------------
    @Override
    public Album getById(String id) {
        List<Album> all = getAll();
        for (Album a : all) {
            if (String.valueOf(a.getId()).equals(id)) {
                return a;
            }
        }
        return null;
    }

    @Override
    public Album getByName(String name) {
        List<Album> all = getAll();
        for (Album a : all) {
            if (a.getTitreAlbum().equalsIgnoreCase(name)) {
                return a;
            }
        }
        return null;
    }

    @Override
    public List<Album> getAll() {
        List<Album> albums = new ArrayList<>();
        if (!file.exists()) return albums;

        try (DataInputStream dis = new DataInputStream(
                new FileInputStream(file))) {
            while (dis.available() > 0) {
                int id = dis.readInt();
                String titre = dis.readUTF();
                LocalDate date = LocalDate.parse(dis.readUTF());
                String cover = dis.readUTF();
                albums.add(new Album(titre, date, new ArrayList<>(), cover));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return albums;
    }

    // ------------------- Update -------------------
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

    // ------------------- Delete -------------------
    @Override
    public void delete(String id) {
        List<Album> albums = getAll();
        albums.removeIf(a -> String.valueOf(a.getId()).equals(id));
        saveAll(albums);
    }

    // ------------------- Utils -------------------
    private void saveAll(List<Album> albums) {
        try (DataOutputStream dos = new DataOutputStream(
                new FileOutputStream(file, false))) { // écrase le fichier
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
}
