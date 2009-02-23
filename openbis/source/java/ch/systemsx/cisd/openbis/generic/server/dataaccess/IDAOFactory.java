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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * A <i>Data Access Object</i> factory.
 * 
 * @author Franz-Josef Elmer
 */
public interface IDAOFactory extends IAuthorizationDAOFactory
{

    /**
     * Returns the {@link ISampleDAO} implementation.
     */
    public ISampleDAO getSampleDAO();

    /**
     * Returns the {@link ISampleTypeDAO} implementation.
     */
    public ISampleTypeDAO getSampleTypeDAO();

    /**
     * Returns the {@link IExternalDataDAO} implementation.
     */
    public IExternalDataDAO getExternalDataDAO();

    /**
     * Returns the {@link IHibernateSearchDAO} implementation.
     */
    public IHibernateSearchDAO getHibernateSearchDAO();

    /**
     * Returns the {@link IPropertyTypeDAO} implementation.
     */
    public IPropertyTypeDAO getPropertyTypeDAO();

    /**
     * Returns the {@link IEntityTypeDAO} implementation.
     */
    public IEntityTypeDAO getEntityTypeDAO(EntityKind entityKind);

    /**
     * Returns the {@link IEntityPropertyTypeDAO} implementation.
     */
    public IEntityPropertyTypeDAO getEntityPropertyTypeDAO(EntityKind entityKind);

    /**
     * Returns the implementation of {@link IExperimentDAO}.
     */
    public IExperimentDAO getExperimentDAO();

    /**
     * @return The implementation of the {@link IProjectDAO}.
     */
    public IProjectDAO getProjectDAO();

    /**
     * @return The implementation of the {@link IVocabularyDAO}.
     */
    public IVocabularyDAO getVocabularyDAO();

    /**
     * @return The implementation of the {@link IProcedureTypeDAO}.
     */
    public IProcedureTypeDAO getProcedureTypeDAO();

    /**
     * @return The implementation of the {@link IProcedureDAO}.
     */
    public IProcedureDAO getProcedureDAO();

    /** Returns an implementation of {@link IExperimentAttachmentDAO}. */
    public IExperimentAttachmentDAO getExperimentAttachmentDAO();

    /** Returns an implementation of {@link IDataSetTypeDAO}. */
    public IDataSetTypeDAO getDataSetTypeDAO();

    /** Returns an implementation of {@link IFileFormatTypeDAO}. */
    public IFileFormatTypeDAO getFileFormatTypeDAO();

    /** Returns an implementation of {@link ILocatorTypeDAO}. */
    public ILocatorTypeDAO getLocatorTypeDAO();

    /**
     * Returns the {@link IMaterialDAO} implementation.
     */
    public IMaterialDAO getMaterialDAO();
}
