package DiskHouse.model.entity;

public abstract class Identifier {
    private static int globalId = 1;  // Compteur partagé par toutes les sous-classes
    private int id;

    public Identifier() {
        this.id = globalId++;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ID: " + id;
    }
}
