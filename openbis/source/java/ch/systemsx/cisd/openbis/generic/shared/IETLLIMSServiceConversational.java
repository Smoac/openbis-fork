/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.conversation.IConversationalRmiServer;
import ch.systemsx.cisd.common.conversation.IProgressListener;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AtomicOperationsPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;

/**
 * @author anttil
 */
public interface IETLLIMSServiceConversational extends IETLLIMSService, IConversationalRmiServer
{
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    @DatabaseUpdateModification(value =
        { ObjectKind.SAMPLE, ObjectKind.EXPERIMENT, ObjectKind.DATA_SET })
    @DatabaseCreateOrDeleteModification(value =
        { ObjectKind.SPACE, ObjectKind.PROJECT, ObjectKind.SAMPLE, ObjectKind.EXPERIMENT,
                ObjectKind.DATA_SET })
    public AtomicEntityOperationResult performEntityOperations(String sessionToken,
            @AuthorizationGuard(guardClass = AtomicOperationsPredicate.class)
            AtomicEntityOperationDetails operationDetails, IProgressListener progressListener);
}
