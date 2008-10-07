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

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;

/**
 * Data access object for {@link SampleTypePE}. <br>
 * Note: this class has been copied from old lims project.
 * 
 * @author Christian Ribeaud
 */
final class SampleTypeDAO extends AbstractTypeDAO<SampleTypePE> implements ISampleTypeDAO
{
    private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());

    SampleTypeDAO(final SessionFactory sessionFactory, final DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance);
    }

    //
    // ISampleTypeDAO
    //

    public final List<SampleTypePE> listSampleTypes(final boolean onlyListable)
            throws DataAccessException
    {
        final List<SampleTypePE> list;

        String query =
                String.format("from %s st where st.databaseInstance = ?", getEntityClass()
                        .getSimpleName());
        if (onlyListable)
        {
            query += " and st.listable = true";
        }
        list = cast(getHibernateTemplate().find(query, toArray(getDatabaseInstance())));
        fetchPropertyTypes(list);

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("list" + getTypeDescription() + "s: " + list.size()
                    + " type(s) have been found.");
        }
        return list;
    }

    private void fetchPropertyTypes(final List<SampleTypePE> list)
    {
        for (final SampleTypePE sampleTypePE : list)
        {
            Hibernate.initialize(sampleTypePE.getSampleTypePropertyTypes());
        }
    }

    public SampleTypePE tryFindByExample(final SampleTypePE sampleType)
    {
        final List<SampleTypePE> result = cast(getHibernateTemplate().findByExample(sampleType));
        return tryFindEntity(result, "sample type");
    }

    public final SampleTypePE tryFindSampleTypeByCode(final String code) throws DataAccessException
    {
        return tryFindTypeByCode(code);
    }

    @Override
    Class<SampleTypePE> getEntityClass()
    {
        return SampleTypePE.class;
    }

}