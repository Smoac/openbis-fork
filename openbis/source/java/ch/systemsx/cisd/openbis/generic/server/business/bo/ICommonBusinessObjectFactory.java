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

/**
 * The <i>generic</i> specific <i>Business Object</i> factory.
 * 
 * @author Tomasz Pylak
 */
public interface ICommonBusinessObjectFactory
{
    public IGroupBO createGroupBO(final Session session);

    public IRoleAssignmentTable createRoleAssignmentTable(final Session session);

    public ISampleTable createSampleTable(final Session session);

    /**
     * Creates a {@link ISampleBO} <i>Business Object</i>.
     */
    public ISampleBO createSampleBO(final Session session);

    /**
     * Creates a {@link IExternalDataTable} <i>Business Object</i>.
     */
    public IExternalDataTable createExternalDataTable(final Session session);

    /**
     * Creates a {@link IExperimentTable} <i>Business Object</i>.
     */
    public IExperimentTable createExperimentTable(final Session session);

    /**
     * Creates a {@link IExperimentBO} <i>Business Object</i>.
     */
    public IExperimentBO createExperimentBO(final Session session);

    /**
     * Creates a {@link IPropertyTypeTable} <i>Business Object</i>.
     */
    public IPropertyTypeTable createPropertyTypeTable(final Session session);

    /**
     * Creates a {@link IPropertyTypeBO} <i>Business Object</i>.
     */
    public IPropertyTypeBO createPropertyTypeBO(final Session session);

    /**
     * Creates a {@link IVocabularyBO} <i>Business Object</i>.
     */
    public IVocabularyBO createVocabularyBO(final Session session);
}
