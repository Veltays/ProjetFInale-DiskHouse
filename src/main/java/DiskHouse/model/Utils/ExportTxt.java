package DiskHouse.model.Utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

public class ExportTxt implements IExport {
    @Override
    public void export(Object data, String filePath) throws Exception {
        if (!(data instanceof List<?>)) {
            throw new IllegalArgumentException("Les données à exporter doivent être une liste.");
        }
        List<?> list = (List<?>) data;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Object obj : list) {
                writer.write(obj.toString());
                writer.newLine();
            }
        }
    }
}

