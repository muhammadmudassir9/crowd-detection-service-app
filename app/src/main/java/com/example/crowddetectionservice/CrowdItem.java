package com.example.crowddetectionservice;

public class CrowdItem {
    private int id;
    private int crowd_count;
    private String time;
    private String alert;

    public CrowdItem(int id, int crowd_count, String time, String alert) {
        this.id = id;
        this.crowd_count = crowd_count;
        this.time = time;
        this.alert = alert;
    }

    public int getId() {
        return id;
    }

    public int getCrowdCount() {
        return crowd_count;
    }

    public String getTime() {
        return time;
    }

    public String getAlert() {
        return alert;
    }
}
