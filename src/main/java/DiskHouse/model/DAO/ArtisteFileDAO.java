package DiskHouse.model.DAO;

import DiskHouse.model.entity.Artiste;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Field;

public class ArtisteFileDAO implements IDAO<Artiste> {

    private final File file;

    public ArtisteFileDAO(String filePath) {
        this.file = new File(filePath);
    }

    @Override
    public void add(Artiste artiste) {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file, true))) {
            dos.writeInt(artiste.getId());
            dos.writeUTF(artiste.getPseudo());
            dos.writeUTF(artiste.getImageURL() == null ? "" : artiste.getImageURL());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Artiste getById(String id) {
        for (Artiste a : getAll()) {
            if (String.valueOf(a.getId()).equals(id)) return a;
        }
        return null;
    }

    @Override
    public Artiste getByName(String name) {
        for (Artiste a : getAll()) {
            if (a.getPseudo().equalsIgnoreCase(name)) return a;
        }
        return null;
    }

    @Override
    public List<Artiste> getAll() {
        List<Artiste> artistes = new ArrayList<>();
        if (!file.exists()) return artistes;

        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            while (dis.available() > 0) {
                int id = dis.readInt();
                String pseudo = dis.readUTF();
                String imageURL = dis.readUTF();
                Artiste a = new Artiste(pseudo, new ArrayList<>(), imageURL.isEmpty() ? null : imageURL);
                applyId(a, id);
                artistes.add(a);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return artistes;
    }

    @Override
    public void update(Artiste artiste) {
        List<Artiste> artistes = getAll();
        for (int i = 0; i < artistes.size(); i++) {
            if (artistes.get(i).getId() == artiste.getId()) {
                artistes.set(i, artiste);
                break;
            }
        }
        saveAll(artistes);
    }

    @Override
    public void delete(String id) {
        List<Artiste> artistes = getAll();
        artistes.removeIf(a -> String.valueOf(a.getId()).equals(id));
        saveAll(artistes);
    }

    private void saveAll(List<Artiste> artistes) {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file, false))) {
            for (Artiste artiste : artistes) {
                dos.writeInt(artiste.getId());
                dos.writeUTF(artiste.getPseudo());
                dos.writeUTF(artiste.getImageURL() == null ? "" : artiste.getImageURL());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void applyId(Object o, int id) {
        try {
            var f = findIdField(o.getClass());
            if (f != null) { f.setAccessible(true); f.setInt(o, id); }
        } catch (Exception ignored) {}
    }
    private java.lang.reflect.Field findIdField(Class<?> c) {
        while (c != null) {
            try { return c.getDeclaredField("id"); }
            catch (NoSuchFieldException e) { c = c.getSuperclass(); }
        }
        return null;
    }
}
