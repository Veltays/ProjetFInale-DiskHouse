package DiskHouse.model.Utils;

import java.util.List;

public interface IImport {
    /**
     * Importe les données depuis un fichier au chemin donné.
     * @param filePath Le chemin du fichier à importer
     * @return Liste d'objets importés
     * @throws Exception en cas d'erreur d'import
     */
    List<?> importData(String filePath) throws Exception;
}

