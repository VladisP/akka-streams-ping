package lab5.messages;

public class PingResult {

    private String testUrl;
    private Long averageResponseTime;

    public PingResult(String testUrl, Long averageResponseTime) {
        this.testUrl = testUrl;
        this.averageResponseTime = averageResponseTime;
    }

    public String getTestUrl() {
        return testUrl;
    }

    public Long getAverageResponseTime() {
        return averageResponseTime;
    }
}
