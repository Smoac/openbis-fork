/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.jdbc.support.JdbcAccessor;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IVocabularyTermDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;

/**
 * <i>Data Access Object</i> implementation for {@link VocabularyTermPE}.
 * 
 * @author Piotr Buczek
 */
final class VocabularyTermDAO extends AbstractGenericEntityDAO<VocabularyTermPE> implements
        IVocabularyTermDAO
{

    /**
     * This logger does not output any SQL statement. If you want to do so, you had better set an
     * appropriate debugging level for class {@link JdbcAccessor}.
     */
    @SuppressWarnings("unused")
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, VocabularyTermDAO.class);

    VocabularyTermDAO(final SessionFactory sessionFactory, final DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance, VocabularyTermPE.class);
    }

}
