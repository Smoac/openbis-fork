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
package ch.ethz.sis.afsserver.server;

import ch.ethz.sis.afsapi.api.PublicAPI;
import ch.ethz.sis.afsserver.server.performance.PerformanceAuditor;

public interface Worker<CONNECTION> extends PublicAPI {
    void createContext(PerformanceAuditor performanceAuditor);

    void cleanContext();

    void setConnection(CONNECTION connection);

    CONNECTION getConnection();

    void cleanConnection() throws Exception;

    void setSessionToken(String sessionToken) throws Exception;

    String getSessionToken();

    void setTransactionManagerMode(boolean transactionManagerMode);

    boolean isTransactionManagerMode();

    void setInteractiveSessionMode(boolean interactiveSessionMode);

    boolean isInteractiveSessionMode();

}
