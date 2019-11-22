package lab5.messages;

public class PingRequest {

    private String testUrl;
    private int count;

    public PingRequest(String testUrl, int count) {
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
