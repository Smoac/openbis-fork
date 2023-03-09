package ch.ethz.sis.afsclient.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

import ch.ethz.sis.afsapi.api.PublicAPI;
import ch.ethz.sis.afsapi.dto.ApiResponse;
import ch.ethz.sis.afsapi.dto.File;
import ch.ethz.sis.afsclient.client.exception.ClientExceptions;
import ch.ethz.sis.afsjson.JsonObjectMapper;
import ch.ethz.sis.afsjson.jackson.JacksonObjectMapper;
import lombok.NonNull;

public final class AfsClient implements PublicAPI
{

    private static final int DEFAULT_PACKAGE_SIZE_IN_BYTES = 1024;

    private static final int DEFAULT_TIMEOUT_IN_MILLIS = 30000;

    private final int maxReadSizeInBytes;

    private final int timeout;

    private String sessionToken;

    private final URI serverUri;

    private final JsonObjectMapper jsonObjectMapper;

    public AfsClient(final URI serverUri)
    {
        this(serverUri, DEFAULT_PACKAGE_SIZE_IN_BYTES, DEFAULT_TIMEOUT_IN_MILLIS);
    }

    public AfsClient(final URI serverUri, final int maxReadSizeInBytes, final int timeout)
    {
        this.maxReadSizeInBytes = maxReadSizeInBytes;
        this.timeout = timeout;
        this.serverUri = serverUri;
        this.jsonObjectMapper = new JacksonObjectMapper();
    }

    public URI getServerUri()
    {
        return serverUri;
    }

    public int getMaxReadSizeInBytes()
    {
        return maxReadSizeInBytes;
    }

    public String getSessionToken()
    {
        return sessionToken;
    }

    public void setSessionToken(final String sessionToken)
    {
        this.sessionToken = sessionToken;
    }

    private static String urlEncode(final String s)
    {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    @Override
    public @NonNull String login(@NonNull final String userId, @NonNull final String password)
            throws Exception
    {
        Map<String, String> credentials = Map.of("userId", userId, "password", password);
        String result = request("POST", "login", Map.of(),
                jsonObjectMapper.writeValue(credentials));
        setSessionToken(result);
        return result;
    }

    @Override
    public @NonNull Boolean isSessionValid() throws Exception
    {
        validateSessionToken();
        return request("GET", "isSessionValid", Map.of("sessionToken", getSessionToken()));
    }

    @Override
    public @NonNull Boolean logout() throws Exception
    {
        validateSessionToken();
        Boolean result = request("POST", "logout", Map.of(),
                jsonObjectMapper.writeValue(Map.of("sessionToken", getSessionToken())));
        setSessionToken(null);
        return result;
    }

    @Override
    public @NonNull List<File> list(@NonNull final String owner, @NonNull final String source,
            @NonNull final Boolean recursively) throws Exception
    {
        validateSessionToken();
        return request("GET", "list",
                Map.of("owner", owner, "source", source, "recursively",
                        recursively.toString(), "sessionToken", getSessionToken()));
    }

    @Override
    public @NonNull byte[] read(@NonNull final String owner, @NonNull final String source,
            @NonNull final Long offset, @NonNull final Integer limit) throws Exception
    {
        validateSessionToken();
        return request("GET", "read",
                Map.of("owner", owner, "source", source, "offset",
                        offset.toString(), "limit", limit.toString(), "sessionToken",
                        getSessionToken()));
    }

    @Override
    public @NonNull Boolean write(@NonNull final String owner, @NonNull final String source,
            @NonNull final Long offset, final byte @NonNull [] data,
            final byte @NonNull [] md5Hash) throws Exception
    {
        validateSessionToken();
        Map<String, Object> parameters =
                Map.of("owner", owner, "source", source,
                        "offset", offset, "data", data, "md5Hash", md5Hash,
                        "sessionToken", getSessionToken());

        return request("POST", "write", Map.of(), jsonObjectMapper.writeValue(parameters));
    }

    @Override
    public @NonNull Boolean delete(@NonNull final String owner, @NonNull final String source)
            throws Exception
    {
        validateSessionToken();
        Map<String, Object> parameters =
                Map.of("owner", owner, "source", source,
                        "sessionToken", getSessionToken());

        return request("DELETE", "delete", Map.of(), jsonObjectMapper.writeValue(parameters));
    }

    @Override
    public @NonNull Boolean copy(@NonNull final String sourceOwner, @NonNull final String source,
            @NonNull final String targetOwner,
            @NonNull final String target)
            throws Exception
    {
        validateSessionToken();
        Map<String, Object> parameters =
                Map.of("sourceOwner", sourceOwner, "source", source,
                        "targetOwner", targetOwner, "target", target,
                        "sessionToken", getSessionToken());

        return request("POST", "copy", Map.of(), jsonObjectMapper.writeValue(parameters));
    }

    @Override
    public @NonNull Boolean move(@NonNull final String sourceOwner, @NonNull final String source,
            @NonNull final String targetOwner,
            @NonNull final String target)
            throws Exception
    {
        validateSessionToken();
        Map<String, Object> parameters =
                Map.of("sourceOwner", sourceOwner, "source", source,
                        "targetOwner", targetOwner, "target", target,
                        "sessionToken", getSessionToken());

        return request("POST", "move", Map.of(), jsonObjectMapper.writeValue(parameters));
    }

    @Override
    public void begin(final UUID transactionId) throws Exception
    {

    }

    @Override
    public Boolean prepare() throws Exception
    {
        return null;
    }

    @Override
    public void commit() throws Exception
    {

    }

    @Override
    public void rollback() throws Exception
    {

    }

    @Override
    public List<UUID> recover() throws Exception
    {
        return null;
    }

    private <T> T request(@NonNull final String httpMethod, @NonNull final String apiMethod,
            @NonNull final Map<String, String> parameters) throws Exception
    {
        return request(httpMethod, apiMethod, parameters, new byte[0]);
    }

    @SuppressWarnings({ "OptionalGetWithoutIsPresent", "unchecked" })
    private <T> T request(@NonNull final String httpMethod, @NonNull final String apiMethod,
            @NonNull final Map<String, String> parameters, final byte @NonNull [] body)
            throws Exception
    {
        HttpClient.Builder clientBuilder = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofMillis(timeout));
        Map<String, String> params = parameters;

        HttpClient client = clientBuilder.build();

        final String query = Stream.concat(
                        Stream.of(new AbstractMap.SimpleImmutableEntry<>("method", apiMethod)),
                        params.entrySet().stream())
                .map(entry -> urlEncode(entry.getKey()) + "=" + urlEncode(entry.getValue()))
                .reduce((s1, s2) -> s1 + "&" + s2).get();

        final URI uri =
                new URI(serverUri.getScheme(), null, serverUri.getHost(), serverUri.getPort(),
                        serverUri.getPath(), query, null);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(uri)
                .version(HttpClient.Version.HTTP_1_1)
                .timeout(Duration.ofMillis(timeout))
                .method(httpMethod, HttpRequest.BodyPublishers.ofByteArray(body));

        final HttpRequest request = builder.build();

        final HttpResponse<byte[]> httpResponse =
                client.send(request, HttpResponse.BodyHandlers.ofByteArray());

        final int statusCode = httpResponse.statusCode();
        if (statusCode >= 200 && statusCode < 300)
        {
            if (!httpResponse.headers().map().containsKey("content-type"))
            {
                throw new IllegalArgumentException(
                        "Server error HTTP response. Missing content-type");
            }
            String content = httpResponse.headers().map().get("content-type").get(0);

            switch (content)
            {
                case "application/json":
                    return parseJsonResponse(httpResponse);
                case "application/octet-stream":
                    return (T) httpResponse.body();
                default:
                    throw new IllegalArgumentException(
                            "Client error HTTP response. Unsupported content-type received.");
            }
        } else if (statusCode >= 400 && statusCode < 500)
        {
            throw ClientExceptions.CLIENT_ERROR.getInstance(statusCode);
        } else if (statusCode >= 500 && statusCode < 600)
        {
            throw ClientExceptions.SERVER_ERROR.getInstance(statusCode);
        } else
        {
            throw ClientExceptions.OTHER_ERROR.getInstance(statusCode);
        }
    }

    private <T> T parseJsonResponse(final HttpResponse<byte[]> httpResponse) throws Exception
    {
        final ApiResponse response =
                jsonObjectMapper.readValue(new ByteArrayInputStream(httpResponse.body()),
                        ApiResponse.class);

        if (response.getError() != null)
        {
            throw ClientExceptions.API_ERROR.getInstance(response.getError());
        } else
        {
            return (T) response.getResult();
        }
    }

    private void validateSessionToken()
    {
        if (getSessionToken() == null)
        {
            throw new IllegalStateException("No session information detected!");
        }
    }

}
