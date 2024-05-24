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

import ch.ethz.sis.afs.api.TransactionalFileSystem;
import ch.ethz.sis.afsserver.server.performance.PerformanceAuditor;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class WorkerContext {
    private PerformanceAuditor performanceAuditor;
    private UUID transactionId;
    private TransactionalFileSystem connection;
    private String sessionToken;
    private Boolean sessionExists;
    private boolean transactionManagerMode;
    private boolean interactiveSessionMode;
    private String ownerShareId;
    private String[] ownerShards;
    private String ownerFolder;
}
