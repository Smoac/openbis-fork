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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IVocabularyDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;

/**
 * Implementation of {@link IVocabularyDAO} for databases.
 * 
 * @author Christian Ribeaud
 */
final class VocabularyDAO extends AbstractDAO implements IVocabularyDAO
{
    private static final Class<VocabularyPE> ENTITY_CLASS = VocabularyPE.class;

    private static final String TABLE_NAME = ENTITY_CLASS.getSimpleName();

    /**
     * This logger does not output any SQL statement. If you want to do so, you had better set an
     * appropriate debugging level for class {@link JdbcAccessor}.
     * </p>
     */
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, VocabularyDAO.class);

    VocabularyDAO(final SessionFactory sessionFactory, final DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance);
    }

    private final static void checkIsUserNamespace(final VocabularyPE vocabulary)
    {
        if (vocabulary.isInternalNamespace())
        {
            throw new DataIntegrityViolationException(String.format(
                    "User defined vocabulary must begin with prefix "
                            + "'%s'. Provided code was: '%s'.", CodeConverter.USER_PROPERTY_PREFIX,
                    vocabulary.getCode()));
        }
    }

    //
    // IVocabularyDAO
    //

    public final void createVocabulary(final VocabularyPE vocabularyPE)
    {
        assert vocabularyPE != null : "Given vocabulary can not be null.";
        validatePE(vocabularyPE);
        checkIsUserNamespace(vocabularyPE);

        final HibernateTemplate template = getHibernateTemplate();
        template.save(vocabularyPE);
        template.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("ADD: vocabulary '%s'.", vocabularyPE));
        }

    }

    public final VocabularyPE tryFindVocabularyByCode(final String vocabularyCode)
    {
        assert vocabularyCode != null : "Unspecified vocabulary code.";

        final String mangledVocabularyCode = CodeConverter.tryToDatabase(vocabularyCode);
        final boolean internalNamespace = CodeConverter.isInternalNamespace(vocabularyCode);
        final List<VocabularyPE> list =
                cast(getHibernateTemplate().find(
                        String.format("select v from %s v where v.simpleCode = ? "
                                + "and v.databaseInstance = ? and v.internalNamespace = ?",
                                TABLE_NAME),
                        toArray(mangledVocabularyCode, getDatabaseInstance(), internalNamespace)));
        final VocabularyPE entity = tryFindEntity(list, "vocabulary", vocabularyCode);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%s): '%s'.", MethodUtils.getCurrentMethod()
                    .getName(), vocabularyCode, entity));
        }
        return entity;
    }

    public final List<VocabularyPE> listVocabularies()
    {
        final List<VocabularyPE> list =
                cast(getHibernateTemplate().find(
                        String.format("from %s v where v.internalNamespace = false "
                                + "and v.databaseInstance = ?", TABLE_NAME),
                        toArray(getDatabaseInstance())));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(list.size() + " vocabulary(ies) have been found.");
        }
        return list;
    }
}
