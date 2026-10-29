package util;

public class MapFile {
    private String fileName;  // ex: "cin_sanda.png"
    private String extension; // ex: "png"
    private byte[] bytes;     // le contenu binaire du fichier

    public MapFile() {}

    public MapFile(String fileName, String extension, byte[] bytes) {
        this.fileName = fileName;
        this.extension = extension;
        this.bytes = bytes;
    }

    // Getters et Setters
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getExtension() { return extension; }
    public void setExtension(String extension) { this.extension = extension; }

    public byte[] getBytes() { return bytes; }
    public void setBytes(byte[] bytes) { this.bytes = bytes; }

    public void saveToDirectory(String targetDirectory) throws java.io.IOException {
    if (bytes == null || bytes.length == 0 || fileName == null) return;

    java.io.File dir = new java.io.File(targetDirectory);
    if (!dir.exists()) dir.mkdirs();

    java.io.File targetFile = new java.io.File(dir, fileName);
    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(targetFile)) {
        fos.write(bytes);
    }
}
}