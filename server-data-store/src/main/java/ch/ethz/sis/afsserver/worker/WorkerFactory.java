/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.afsserver.worker;

import ch.ethz.sis.afsserver.server.Worker;
import ch.ethz.sis.afsserver.startup.AtomicFileSystemServerParameter;
import ch.ethz.sis.afsserver.worker.providers.AuthenticationInfoProvider;
import ch.ethz.sis.afsserver.worker.providers.AuthorizationInfoProvider;
import ch.ethz.sis.afsserver.worker.proxy.*;
import ch.ethz.sis.shared.pool.AbstractFactory;
import ch.ethz.sis.shared.startup.Configuration;
import org.apache.commons.lang3.StringUtils;

public class WorkerFactory extends AbstractFactory<Configuration, Configuration, Worker> {

    @Override
    public Worker create(Configuration configuration) throws Exception {

        // 5. Execute the operation
        AuditorProxy executorProxy = new AuditorProxy(new ExecutorProxy(configuration));

        // 4. Check that the user have rights to do the operation
        AuthorizationInfoProvider authorizationInfoProvider = configuration.getInstance(AtomicFileSystemServerParameter.authorizationInfoProviderClass);
        Integer authorizationProxyCacheIdleTimeout = getIntegerParameter(configuration, AtomicFileSystemServerParameter.authorizationProxyCacheIdleTimeout);
        authorizationInfoProvider.init(configuration);
        AuditorProxy authorizationProxy = new AuditorProxy(new AuthorizationProxy(executorProxy,
                authorizationInfoProvider, authorizationProxyCacheIdleTimeout));

        // 3. Pre/Post check correctness of the call and modifications to avoid things that make little sense
        int maxReadSizeInBytes = configuration.getIntegerProperty(AtomicFileSystemServerParameter.maxReadSizeInBytes);
        AuditorProxy correctnessProxy = new AuditorProxy(new ValidationProxy(authorizationProxy,
                maxReadSizeInBytes));

        // 2. Authenticate user and check that have a valid session
        AuthenticationInfoProvider authenticationInfoProvider = configuration.getInstance(AtomicFileSystemServerParameter.authenticationInfoProviderClass);
        Integer authenticationProxyCacheIdleTimeout = getIntegerParameter(configuration, AtomicFileSystemServerParameter.authenticationProxyCacheIdleTimeout);
        authenticationInfoProvider.init(configuration);
        AuditorProxy authenticationProxy = new AuditorProxy(new AuthenticationProxy(correctnessProxy,
                authenticationInfoProvider, authenticationProxyCacheIdleTimeout));

        // 1. Log call
        AuditorProxy logProxy = new AuditorProxy(new LogProxy(authenticationProxy));

        return logProxy;
    }

    private static Integer getIntegerParameter(Configuration configuration, AtomicFileSystemServerParameter atomicFileSystemServerParameter)
    {
        String parameterStringValue = configuration.getStringProperty(atomicFileSystemServerParameter);

        if (StringUtils.isNotEmpty(parameterStringValue)) {
            try {
                return Integer.parseInt(parameterStringValue);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Configuration parameter '" + atomicFileSystemServerParameter + "' is not a valid integer.");
            }
        } else {
            return null;
        }
    }
}