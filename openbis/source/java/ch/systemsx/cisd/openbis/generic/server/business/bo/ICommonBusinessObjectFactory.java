/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * The <i>generic</i> specific <i>Business Object</i> factory. Each method creates one kind of a
 * business object.
 * 
 * @author Tomasz Pylak
 */
public interface ICommonBusinessObjectFactory
{
    public IAttachmentBO createAttachmentBO(final Session session);

    public IGroupBO createGroupBO(final Session session);

    public IRoleAssignmentTable createRoleAssignmentTable(final Session session);

    public ISampleTable createSampleTable(final Session session);

    public ISampleBO createSampleBO(final Session session);

    public IExternalDataBO createExternalDataBO(Session session);

    public IExternalDataTable createExternalDataTable(final Session session);

    public IExperimentTable createExperimentTable(final Session session);

    public IExperimentBO createExperimentBO(final Session session);

    public IPropertyTypeTable createPropertyTypeTable(final Session session);

    public IPropertyTypeBO createPropertyTypeBO(final Session session);

    public IVocabularyBO createVocabularyBO(final Session session);

    public IVocabularyTermBO createVocabularyTermBO(final Session session);

    public IEntityTypePropertyTypeBO createEntityTypePropertyTypeBO(Session session,
            EntityKind entityKind);

    public IProjectBO createProjectBO(final Session session);

    public IMaterialTable createMaterialTable(Session session);

    public IEntityTypeBO createEntityTypeBO(Session session);

    public IMaterialBO createMaterialBO(Session session);

    public IAuthorizationGroupBO createAuthorizationGroupBO(Session session);
}
