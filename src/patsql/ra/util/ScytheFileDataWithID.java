package patsql.ra.util;

public class ScytheFileDataWithID {
    private ScytheFileData fileData;
    private String source;
    private String group;
    private String id;

    public ScytheFileDataWithID(ScytheFileData fileData, String source, String group, String id) {
        this.fileData = fileData;
        this.source = source;
        this.group = group;
        this.id = id;
    }

    public ScytheFileData getFileData() {
        return fileData;
    }

    public String getSource() {
        return source;
    }

    public String getGroup() {
        return group;
    }

    public String getId() {
        return id;
    }
}
