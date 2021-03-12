/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.microservices.download.server.services.store;

import ch.systemsx.cisd.common.http.JettyHttpClientFactory;
import org.eclipse.jetty.client.api.ContentResponse;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class PingHandlerTest
{

    public static void main(String[] args) throws Exception
    {
        final long start = System.currentTimeMillis();

        try
        {
            final ContentResponse clientResponse = JettyHttpClientFactory.getHttpClient()
                    .GET("http://localhost:8080/ping");
            final int statusCode = clientResponse.getStatus();
            if (statusCode != 200)
            {
                throw new RuntimeException(String.format("Error code received: %d (%s)",
                        statusCode, clientResponse.getReason()));
            }
        } catch (final InterruptedException | TimeoutException | ExecutionException e)
        {
            throw new RuntimeException("Error sending request.", e);
        }

        final long end = System.currentTimeMillis();
        System.out.println("Time: " + (end - start) + " ms");
    }

}
