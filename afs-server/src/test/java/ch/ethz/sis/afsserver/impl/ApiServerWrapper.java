package ch.ethz.sis.afsserver.impl;


import ch.ethz.sis.afsserver.core.AbstractPublicApiWrapper;
import ch.ethz.sis.afsserver.server.ApiServer;
import ch.ethz.sis.afsserver.server.Response;
import ch.ethz.sis.afsserver.server.impl.ApiRequest;
import ch.ethz.sis.afsserver.server.impl.ApiResponseBuilder;
import ch.ethz.sis.afsserver.server.performance.PerformanceAuditor;
import ch.ethz.sis.shared.log.LogManager;
import ch.ethz.sis.shared.log.Logger;

import java.util.Map;
import java.util.UUID;

public class ApiServerWrapper extends AbstractPublicApiWrapper
{

    private static final Logger logger = LogManager.getLogger(ApiServerWrapper.class);

    private ApiServer apiServer;
    private final ApiResponseBuilder apiResponseBuilder;

    public ApiServerWrapper(ApiServer apiServer) {
        this.apiServer = apiServer;
        this.apiResponseBuilder = new ApiResponseBuilder();
    }

    public <E> E process(String method, Map<String, Object> args) {
        PerformanceAuditor performanceAuditor = new PerformanceAuditor();
        // Random Session token just works for tests with dummy authentication
        ApiRequest request = new ApiRequest("test", method, args, UUID.randomUUID().toString(), null, null);

        try {
            Response response = apiServer.processOperation(request, apiResponseBuilder, performanceAuditor);
            return (E) response.getResult();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

}
