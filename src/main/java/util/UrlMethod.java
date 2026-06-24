package util;

public class UrlMethod {
    private String url;
    private String method;
    public UrlMethod(String url, String method) {
        this.url = url;
        this.method = method;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getMethod() {
        return method;
    }
    public void setMethod(String method) {
        this.method = method;
    }
}
