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

import java.util.Iterator;
import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.dao.DataAccessException;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISamplePropertyDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;

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

    public List<SamplePropertyPE> listSampleProperties(SampleOwnerIdentifier ownerIdentifier,
            SampleTypePE sampleType, List<PropertyTypePE> propertyCodes) throws DataAccessException
    {
        String query;
        Object[] args;

        String joinClouse =
                "join fetch sp.entityTypePropertyType"
                        + " join fetch sp.entityTypePropertyType.propertyType"
                        + " join fetch sp.entity";
        if (ownerIdentifier.isDatabaseInstanceLevel())
        {
            query =
                    "from %s sp "
                            + joinClouse
                            + " where sp.entityTypePropertyType.entityTypeInternal.code = ? and sp.entity.databaseInstance.code = ?"
                            + " and  %s";
            args =
                    new Object[]
                        {
                                sampleType.getCode(),
                                ownerIdentifier.getDatabaseInstanceLevel()
                                        .getDatabaseInstanceCode() };
        } else
        {
            query =
                    "from %s sp "
                            + joinClouse
                            + " where sp.entityTypePropertyType.entityTypeInternal.code = ? and sp.entity.group.code = ?"
                            + " and sp.entity.group.databaseInstance.code = ? and %s";
            args =
                    new Object[]
                        { sampleType.getCode(), ownerIdentifier.getGroupLevel().getGroupCode(),
                                ownerIdentifier.getGroupLevel().getDatabaseInstanceCode() };
        }
        final String format =
                String.format(query, SamplePropertyPE.class.getSimpleName(),
                        extractCodes(propertyCodes));
        return cast(getHibernateTemplate().find(format, args));
    }

    @Private
    public static String extractCodes(List<PropertyTypePE> propertyCodes)
    {
        assert propertyCodes.size() > 0 : "no properties specified";
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