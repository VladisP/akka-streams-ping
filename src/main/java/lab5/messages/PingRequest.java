package lab5.entities;

public class PingConfig {

    private String testUrl;
    private int count;

    public PingConfig(String testUrl, int count) {
        this.testUrl = testUrl;
        this.count = count;
    }

    public String getTestUrl() {
        return testUrl;
    }

    public int getCount() {
        return count;
    }
}
