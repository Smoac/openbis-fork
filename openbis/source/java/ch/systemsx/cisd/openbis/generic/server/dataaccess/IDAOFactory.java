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

import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.ICodeSequenceDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * A <i>Data Access Object</i> factory.
 * 
 * @author Franz-Josef Elmer
 */
public interface IDAOFactory extends IAuthorizationDAOFactory
{

    /**
     * Returns the {@link ISampleTypeDAO} implementation.
     */
    public ISampleTypeDAO getSampleTypeDAO();

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
     * @return The implementation of the {@link IVocabularyDAO}.
     */
    public IVocabularyDAO getVocabularyDAO();

    /** Returns an implementation of {@link IAttachmentDAO}. */
    public IAttachmentDAO getAttachmentDAO();

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

    /** Returns an implementation of {@link ICodeSequenceDAO} */
    public ICodeSequenceDAO getCodeSequenceDAO();

    /** Returns an implementation of {@link IDataStoreDAO}. */
    public IDataStoreDAO getDataStoreDAO();

}
