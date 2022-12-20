/*
 * Copyright 2022 ETH Zürich, SIS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.ethz.sis.afsapi.startup;

import ch.ethz.sis.afsapi.api.PublicAPI;
import ch.ethz.sis.afsapi.api.dto.File;
import ch.ethz.sis.shared.json.JSONObjectMapper;
import ch.ethz.sis.shared.json.jackson.JacksonObjectMapper;
import lombok.NonNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AtomicFileSystemClient implements PublicAPI {

    private static final int DEFAULT_PACKAGE_SIZE_IN_BYTES = 1024;
    private static final int DEFAULT_TIMEOUT_IN_MILLIS = 30000;

    private final String serverURI;
    private final int maxReadSizeInBytes;
    private final int timeout;
    private final JSONObjectMapper jsonObjectMapperClass;

    private String sessionToken;

    public AtomicFileSystemClient(String serverURI) {
        this(serverURI, DEFAULT_PACKAGE_SIZE_IN_BYTES, DEFAULT_TIMEOUT_IN_MILLIS);
    }

    public AtomicFileSystemClient(String serverURI, int maxReadSizeInBytes, int timeout) {
        this.serverURI = serverURI;
        this.maxReadSizeInBytes = maxReadSizeInBytes;
        this.timeout = timeout;
        this.jsonObjectMapperClass = new JacksonObjectMapper();
    }

    public String getServerURI() {
        return serverURI;
    }

    public int getMaxReadSizeInBytes() {
        return maxReadSizeInBytes;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    @Override
    public String login(@NonNull String userId, @NonNull String password) throws Exception {
        return request("POST", "login", Map.of("userId", userId, "password", password));
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
    public @NonNull List<File> list(@NonNull String owner, @NonNull String source, @NonNull Boolean recursively) throws Exception {
        return request("GET", "list", Map.of("owner", owner, "source", source, "recursively", Boolean.toString(recursively)));
    }

    @Override
    public @NonNull byte[] read(@NonNull String owner, @NonNull String source, @NonNull Long offset, @NonNull Integer limit) throws Exception {
        return request("GET", "read", Map.of("owner", owner, "source", source, "offset", Long.toString(offset), "limit", Integer.toString(limit)));
    }

    @Override
    public @NonNull Boolean write(@NonNull String owner, @NonNull String source, @NonNull Long offset, @NonNull byte[] data, @NonNull byte[] md5Hash) throws Exception {
        return null;
    }

    @Override
    public @NonNull Boolean delete(@NonNull String owner, @NonNull String source) throws Exception {
        return null;
    }

    @Override
    public @NonNull Boolean copy(@NonNull String sourceOwner, @NonNull String source, @NonNull String targetOwner, @NonNull String target) throws Exception {
        return null;
    }

    @Override
    public @NonNull Boolean move(@NonNull String sourceOwner, @NonNull String source, @NonNull String targetOwner, @NonNull String target) throws Exception {
        return null;
    }

    @Override
    public void begin(UUID transactionId) throws Exception {

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

    //
    //
    //


    private <T> T request(String httpMethod, String method, Map<String, String> parameters) throws Exception {
        return request(httpMethod, method, parameters, new byte[0]);
    }

    private <T> T request(String httpMethod, String method, Map<String, String> parameters, byte[] body) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofMillis(timeout))
                .build();

        HttpRequest request = null;
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverURI))
                .timeout(Duration.ofMillis(timeout));

        switch (httpMethod) {
            case "GET":
                request = builder.GET().build();
            break;
            case "POST":
                request = builder.POST(HttpRequest.BodyPublishers.ofByteArray(body)).build();
            break;
        }

        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

        return null;
    }
}
