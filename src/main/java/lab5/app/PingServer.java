package lab5.app;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;

import java.time.Duration;

public class PingServer {

    private static final String URL_PARAM_NAME = "testUrl";
    private static final String COUNT_PARAM_NAME = "count";
    private static final int PARALLELISM = 6;
    private static final Duration TIMEOUT_MILLIS = Duration.ofMillis(3000);
    private static final long NANO_TO_MS_FACTOR = 1_000_000L;

    private AsyncHttpClient httpClient = Dsl.asyncHttpClient();
}
