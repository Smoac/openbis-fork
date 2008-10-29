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
import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SourceType;

/**
 * Implementation of {@link IExternalDataDAO} for databases.
 * 
 * @author Christian Ribeaud
 */
final class ExternalDataDAO extends AbstractDAO implements IExternalDataDAO
{
    private final static Class<ExternalDataPE> ENTITY_CLASS = ExternalDataPE.class;

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, ExternalDataDAO.class);

    private static final String TABLE_NAME = ENTITY_CLASS.getSimpleName();

    ExternalDataDAO(final SessionFactory sessionFactory, final DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance);
    }

    //
    // IExternalDataDAO
    //

    public final List<ExternalDataPE> listExternalData(final SamplePE sample,
            final SourceType sourceType) throws DataAccessException
    {
        assert sample != null : "Unspecified sample.";
        assert sourceType != null : "Unspecified source type.";

        final List<ExternalDataPE> list =
                cast(getHibernateTemplate().find(
                        String.format("from %s e where e.%s = ?", TABLE_NAME, sourceType
                                .getFieldName()), toArray(sample)));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d external data have been found for [sample=%s,sourceType=%s].", list.size(),
                    sample, sourceType));
        }
        return list;
    }
}
