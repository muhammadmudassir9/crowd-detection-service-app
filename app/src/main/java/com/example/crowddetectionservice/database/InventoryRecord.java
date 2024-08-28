package com.example.crowddetectionservice.database;

public class InventoryRecord {
    private String filePath;
    private String fileType;
    private String date;
    private String time;

    public InventoryRecord(String filePath, String fileType, String date, String time) {
        this.filePath = filePath;
        this.fileType = fileType;
        this.date = date;
        this.time = time;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    // Method to extract the file name from the file path
    public String getFileName() {
        return filePath.substring(filePath.lastIndexOf('/') + 1);
    }
}
