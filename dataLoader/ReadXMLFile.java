package kg.trade.swis.restore.dataLoader;

import lombok.Getter;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

@Getter
class DocDetails {
    String docId;
    String docStartDate;
    String docEndDate;

    public DocDetails(String docId, String docStartDate, String docEndDate) {
        this.docId = docId;
        this.docStartDate = docStartDate;
        this.docEndDate = docEndDate;
    }

    @Override
    public String toString() {
        return docId + ", " + docStartDate;
    }
}

public class ReadXMLFile {
    static List<DocDetails> docDetailsList;
    static List<String> names = new ArrayList<>();
    static List<String> formattedPairs = new ArrayList<>();  // List to hold formatted pairs for output
    static List<String> nonPairs = new ArrayList<>();        // List to hold non-pair filenames

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        File rootDir = new File("//Users/bambook/Desktop/IN");
        processDirectory(rootDir);
        findPairs();
        saveLogs(formattedPairs, "pairs.txt");
        saveLogs(nonPairs, "non_pairs.txt");
    }

    public static void saveLogs(List<String> logs, String filename) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (String log : logs) {
                writer.write(log);
                writer.newLine(); // Adds a new line after each record
            }
        }
    }

    public static void processDirectory(File directory) throws IOException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    processDirectory(file);
                } else if (file.getName().endsWith(".xml")) {
                    names.add(file.getName());
                }
            }
        }
    }

    public static void findPairs() {
        Map<String, List<String>> uuidMap = new HashMap<>();
        for (String filename : names) {
            String uuid = filename.substring(filename.indexOf("uuid-"), filename.length() - 4);
            if (uuidMap.containsKey(uuid)) {
                uuidMap.get(uuid).add(filename);
            } else {
                List<String> newList = new ArrayList<>();
                newList.add(filename);
                uuidMap.put(uuid, newList);
            }
        }

        for (Map.Entry<String, List<String>> entry : uuidMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                // Check combinations for pairing
                for (int i = 0; i < entry.getValue().size(); i++) {
                    for (int j = i + 1; j < entry.getValue().size(); j++) {
                        String file1 = entry.getValue().get(i);
                        String file2 = entry.getValue().get(j);
                        if ((file1.contains("MSG.PRSurn") && !file2.contains("MSG.PRSurn")) ||
                                (file2.contains("MSG.PRSurn") && !file1.contains("MSG.PRSurn"))) {
                            formattedPairs.add(file1 + " | " + file2);
                        }
                    }
                }
            } else {
                // If only one file with this UUID, it has no pair
                nonPairs.add(entry.getValue().get(0));
            }
        }
    }
}
