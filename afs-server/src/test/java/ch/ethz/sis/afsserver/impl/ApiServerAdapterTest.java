package ch.ethz.sis.afsserver.impl;

import ch.ethz.sis.afsserver.ServerClientEnvironmentFs;
import ch.ethz.sis.afsapi.api.PublicApi;
import ch.ethz.sis.afsserver.server.ApiServer;
import ch.ethz.sis.afsserver.server.impl.ApiServerAdapter;
import ch.ethz.sis.afsserver.startup.AtomicFileSystemServerParameter;
import ch.ethz.sis.afsjson.JsonObjectMapper;
import ch.ethz.sis.shared.startup.Configuration;

public class ApiServerAdapterTest extends ApiServerTest {

    @Override
    public PublicApi getPublicAPI() throws Exception {
        ApiServer apiServer = getAPIServer();
        Configuration configuration = ServerClientEnvironmentFs.getInstance().getDefaultServerConfiguration();
        JsonObjectMapper jsonObjectMapper = configuration.getSharableInstance(AtomicFileSystemServerParameter.jsonObjectMapperClass);
        ApiServerAdapter apiServerAdapter = new ApiServerAdapter(apiServer, jsonObjectMapper);
        return new ApiServerAdapterWrapper(apiServerAdapter, jsonObjectMapper);
    }
}
