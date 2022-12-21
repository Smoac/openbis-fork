package ch.ethz.sis.afsclient.client;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ch.ethz.sis.afsapi.api.PublicAPI;
import ch.ethz.sis.afsapi.api.dto.File;
import lombok.NonNull;

public final class Client implements PublicAPI {

    private final String serverUrl;

    public Client(final String serverUrl) {
        this.serverUrl = serverUrl;
    }

    private byte[] doAction(@NonNull final Map<String, String> parameters,
            @NonNull final String method, final byte @NonNull [] body) throws Exception {
        final HttpClient client = HttpClient.newHttpClient();
        
        final String query = parameters.entrySet().stream()
                .map(entry -> urlEncode(entry.getKey()) + "=" + urlEncode(entry.getValue()))
                .reduce((s1, s2) -> s1 + "&" + s2).orElse(null);

        final HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + (query != null ? "?" + query : "")))
                .method(method, HttpRequest.BodyPublishers.ofByteArray(body))
                .header("Accept", "application/json");

        final HttpRequest request = builder.build();

        final HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        return response.body();
    }
    
    private static String urlEncode(final String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    @Override
    public String login(@NonNull final String userId, @NonNull final String password) throws Exception {
        return null;
    }

    @Override
    public Boolean isSessionValid() throws Exception {
        return null;
    }

    @Override
    public Boolean logout() throws Exception {
        return null;
    }

    @Override
    public @NonNull List<File> list(@NonNull final String owner, @NonNull final String source,
            @NonNull final Boolean recursively) throws Exception {
        return null;
    }

    @Override
    public byte @NonNull [] read(@NonNull final String owner, @NonNull final String source,
            @NonNull final Long offset, @NonNull final Integer limit) throws Exception {
        return new byte[0];
    }

    @Override
    public @NonNull Boolean write(@NonNull final String owner, @NonNull final String source,
            @NonNull final Long offset, final byte @NonNull [] data,
            final byte @NonNull [] md5Hash) throws Exception {
        return null;
    }

    @Override
    public @NonNull Boolean delete(@NonNull final String owner, @NonNull final String source)
            throws Exception {
        return null;
    }

    @Override
    public @NonNull Boolean copy(@NonNull final String sourceOwner, @NonNull final String source, @NonNull final String targetOwner,
            @NonNull final String target)
            throws Exception
    {
        return null;
    }

    @Override
    public @NonNull Boolean move(@NonNull final String sourceOwner, @NonNull final String source, @NonNull final String targetOwner,
            @NonNull final String target)
            throws Exception
    {
        return null;
    }

    @Override
    public void begin(final UUID transactionId) throws Exception {

    }

    @Override
    public Boolean prepare() throws Exception {
        return null;
    }

    @Override
    public void commit() throws Exception {

    }

    @Override
    public void rollback() throws Exception {

    }

    @Override
    public List<UUID> recover() throws Exception {
        return null;
    }

}
