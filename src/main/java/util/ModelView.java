package util;

import java.util.HashMap;


public class ModelView {
    private String url; 
    private HashMap<String, Object> data = new HashMap<>();

    
    public ModelView(String url) {
        this.url = url;
    }

    public void addItem(String key, Object value) {
        this.data.put(key, value);
    }

    
    public String getUrl() {
        return url;
    }

    public HashMap<String, Object> getData() {
        return data;
    }
}