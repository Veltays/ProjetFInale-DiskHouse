package DiskHouse.model.entity;

public abstract class Identifier {
    private static int globalId = 1; // compteur partagé
    private int id;

    public Identifier() {
        this.id = globalId++;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [id=" + id + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Identifier that = (Identifier) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}