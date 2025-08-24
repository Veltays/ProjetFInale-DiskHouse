package DiskHouse.model.DAO;

import DiskHouse.model.entity.Artiste;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ArtisteFileDAO implements IDAO<Artiste> {

    private final File file;

    public ArtisteFileDAO(String filePath) {
        this.file = new File(filePath);
    }

    // ------------------- Create -------------------
    @Override
    public void add(Artiste artiste) {
        try (DataOutputStream dos = new DataOutputStream(
                new FileOutputStream(file, true))) { // append = true
            dos.writeInt(artiste.getId());
            dos.writeUTF(artiste.getNom());
            dos.writeUTF(artiste.getPrenom());
            dos.writeUTF(artiste.getPseudo());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ------------------- Read -------------------
    @Override
    public Artiste getById(String id) {
        List<Artiste> all = getAll();
        for (Artiste a : all) {
            if (String.valueOf(a.getId()).equals(id)) {
                return a;
            }
        }
        return null;
    }

    @Override
    public Artiste getByName(String name) {
        List<Artiste> all = getAll();
        for (Artiste a : all) {
            if (a.getNom().equalsIgnoreCase(name)
                    || a.getPseudo().equalsIgnoreCase(name)) {
                return a;
            }
        }
        return null;
    }

    @Override
    public List<Artiste> getAll() {
        List<Artiste> artistes = new ArrayList<>();
        if (!file.exists()) return artistes;

        try (DataInputStream dis = new DataInputStream(
                new FileInputStream(file))) {
            while (dis.available() > 0) {
                int id = dis.readInt();
                String nom = dis.readUTF();
                String prenom = dis.readUTF();
                String pseudo = dis.readUTF();
                artistes.add(new Artiste(nom, prenom, pseudo, new ArrayList<>()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return artistes;
    }

    // ------------------- Update -------------------
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

    // ------------------- Delete -------------------
    @Override
    public void delete(String id) {
        List<Artiste> artistes = getAll();
        artistes.removeIf(a -> String.valueOf(a.getId()).equals(id));
        saveAll(artistes);
    }

    // ------------------- Utils -------------------
    private void saveAll(List<Artiste> artistes) {
        try (DataOutputStream dos = new DataOutputStream(
                new FileOutputStream(file, false))) { // écrase le fichier
            for (Artiste artiste : artistes) {
                dos.writeInt(artiste.getId());
                dos.writeUTF(artiste.getNom());
                dos.writeUTF(artiste.getPrenom());
                dos.writeUTF(artiste.getPseudo());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
