package DiskHouse.model.Utils;

public interface IExport {
    /**
     * Exporte les données dans un fichier au chemin donné.
     * @param data Les données à exporter (type générique pour flexibilité)
     * @param filePath Le chemin du fichier de destination
     * @throws Exception en cas d'erreur d'export
     */
    void export(Object data, String filePath) throws Exception;
}

