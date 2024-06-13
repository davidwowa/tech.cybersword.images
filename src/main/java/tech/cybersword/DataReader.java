package tech.cybersword;

import java.util.List;
import java.util.Map;

public class DataReader {

    List<String> filesPaths;

    public DataReader(List<String> filesPaths) {
        this.filesPaths = filesPaths;
    }

    public Map<String, String> getData() {
        if (null != filesPaths && !filesPaths.isEmpty()) {
            // Read data from files and return
        }
        return null;
    }
}