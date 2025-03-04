/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.material;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.update.MaterialTypeUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.IObjectAuthorizationExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;

/**
 * @author pkupczyk
 */
public interface IMaterialTypeAuthorizationExecutor extends IObjectAuthorizationExecutor
{

    void canCreate(IOperationContext context, MaterialTypePE materialTypePE);

    void canGet(IOperationContext context);

    void canSearch(IOperationContext context);

    void canUpdate(IOperationContext context, MaterialTypePE materialTypePE, MaterialTypeUpdate update);

    void canDelete(IOperationContext context);

}
