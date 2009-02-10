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

import java.util.Date;
import java.util.List;

import net.sf.beanlib.hibernate3.Hibernate3SequenceGenerator;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SequenceNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.SourceType;

/**
 * Implementation of {@link IExternalDataDAO} for databases.
 * 
 * @author Christian Ribeaud
 */
final class ExternalDataDAO extends AbstractDAO implements IExternalDataDAO
{
    private final static String DATA_CODE_DATE_FORMAT_PATTERN = "yyyyMMddHHmmssSSS";
    
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

    public ExternalDataPE tryToFindDataSetByCode(String dataSetCode)
    {
        assert dataSetCode != null : "Unspecified data set code.";

        final List<ExternalDataPE> list =
                cast(getHibernateTemplate().find(
                        String.format("select e from %s e where e.code = ?", ENTITY_CLASS.getSimpleName()),
                        toArray(dataSetCode)));
        final ExternalDataPE entity = tryFindEntity(list, "data set");
        if (operationLog.isDebugEnabled())
        {
            String methodName = MethodUtils.getCurrentMethod().getName();
            operationLog.debug(String.format("%s(%s): '%s'.", methodName, dataSetCode, entity));
        }
        return entity;
    }

    public String createDataSetCode()
    {
        long id =
                Hibernate3SequenceGenerator.nextval(SequenceNames.DATA_SEQUENCE, getSession(true));
        return DateFormatUtils.format(new Date(), DATA_CODE_DATE_FORMAT_PATTERN) + "-"
                + Long.toString(id);
    }

    public void createDataSet(ExternalDataPE dataset)
    {
        assert dataset != null : "Unspecified data set.";
        
        dataset.setCode(CodeConverter.tryToDatabase(dataset.getCode()));
        final HibernateTemplate template = getHibernateTemplate();
        template.save(dataset);
        template.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("ADD: data set '%s'.", dataset));
        }
    }

    public void updateDataSet(ExternalDataPE dataset)
    {
        assert dataset != null : "Given data set can not be null.";
        validatePE(dataset);

        final HibernateTemplate template = getHibernateTemplate();
        template.update(dataset);
        template.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("UPDATE: data set '%s'.", dataset));
        }
    }
}
