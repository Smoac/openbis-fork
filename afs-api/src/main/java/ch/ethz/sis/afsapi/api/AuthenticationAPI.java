package ch.ethz.sis.afsapi.api;

import lombok.NonNull;

public interface AuthenticationAPI {

    @NonNull
    String login(@NonNull String userId, @NonNull String password) throws Exception;

    @NonNull
    Boolean isSessionValid() throws Exception;

    @NonNull
    Boolean logout() throws Exception;

}
