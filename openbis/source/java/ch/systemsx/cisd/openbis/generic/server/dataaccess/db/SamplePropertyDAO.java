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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.springframework.dao.DataAccessException;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISamplePropertyDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Data access object for {@link SamplePropertyPE}.
 * 
 * @author Izabela Adamczyk
 */
final class SamplePropertyDAO extends AbstractDAO implements ISamplePropertyDAO
{

    SamplePropertyDAO(final SessionFactory sessionFactory, final DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance);
    }

    public Map<SampleIdentifier, List<SamplePropertyPE>> listSampleProperties(
            List<SampleIdentifier> sampleIdentifiers, List<PropertyTypePE> propertyCodes)
            throws DataAccessException
    {
        Map<SampleIdentifier, List<SamplePropertyPE>> result =
                new HashMap<SampleIdentifier, List<SamplePropertyPE>>();
        if (sampleIdentifiers.size() != 0 && propertyCodes.size() != 0)
        {
            for (SampleIdentifier sampleIdentifier : sampleIdentifiers)
            {
                List<SamplePropertyPE> list = null;
                if (sampleIdentifier.isDatabaseInstanceLevel())
                {
                    final String format =
                            String.format(
                                    "from %s sp where sp.entity.code = ? and sp.entity.databaseInstance.code = ?"
                                            + " and  %s", SamplePropertyPE.class.getSimpleName(),
                                    extractCodes(propertyCodes));
                    list =
                            cast(getHibernateTemplate().find(
                                    format,
                                    new Object[]
                                        {
                                                sampleIdentifier.getSampleCode(),
                                                sampleIdentifier.getDatabaseInstanceLevel()
                                                        .getDatabaseInstanceCode() }));
                } else
                {
                    final String format =
                            String.format(
                                    "from %s sp where sp.entity.code = ? and sp.entity.group.code = ?"
                                            + " and sp.entity.group.databaseInstance.code = ?"
                                            + " and %s", SamplePropertyPE.class.getSimpleName(),
                                    extractCodes(propertyCodes));
                    list =
                            cast(getHibernateTemplate().find(
                                    format,
                                    new Object[]
                                        {
                                                sampleIdentifier.getSampleCode(),
                                                sampleIdentifier.getGroupLevel().getGroupCode(),
                                                sampleIdentifier.getGroupLevel()
                                                        .getDatabaseInstanceCode() }));
                }
                result.put(sampleIdentifier, list);
            }
        }
        return result;
    }

    @Private
    public static String extractCodes(List<PropertyTypePE> propertyCodes)
    {
        assert propertyCodes.size() > 0;
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        Iterator<PropertyTypePE> it = propertyCodes.iterator();
        appendNextEncoded(sb, it);
        while (it.hasNext())
        {
            sb.append(" OR ");
            appendNextEncoded(sb, it);
        }
        sb.append(")");
        return sb.toString();
    }

    private static void appendNextEncoded(StringBuilder sb, Iterator<PropertyTypePE> it)
    {
        final PropertyTypePE next = it.next();
        sb.append("sp.entityTypePropertyType.propertyType.simpleCode = '" + next.getSimpleCode()
                + "' AND sp.entityTypePropertyType.propertyType.internalNamespace = "
                + next.isInternalNamespace() + "");
    }
}