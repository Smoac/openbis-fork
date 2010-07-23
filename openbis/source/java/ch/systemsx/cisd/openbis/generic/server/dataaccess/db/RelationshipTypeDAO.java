/*
 * Copyright 2010 ETH Zuerich, CISD
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

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRelationshipTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelationshipTypePE;

/**
 * @author Izabela Adamczyk
 */
public class RelationshipTypeDAO extends AbstractGenericEntityDAO<RelationshipTypePE> implements
        IRelationshipTypeDAO
{

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, RelationshipTypeDAO.class);

    public RelationshipTypeDAO(SessionFactory sessionFactory, DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance, RelationshipTypePE.class);
    }

    public RelationshipTypePE tryFindRelationshipTypeByCode(String code)
    {
        assert code != null : "Unspecified relationship type code";

        final String mangledCode = CodeConverter.tryToDatabase(code);
        final boolean internalNamespace = CodeConverter.isInternalNamespace(code);
        final List<RelationshipTypePE> list =
                cast(getHibernateTemplate().find(
                        String.format("select pt from %s pt where pt.simpleCode = ? "
                                + "and pt.databaseInstance = ? and pt.internalNamespace = ?",
                                RelationshipTypePE.class.getSimpleName()),
                        toArray(mangledCode, getDatabaseInstance(), internalNamespace)));
        final RelationshipTypePE entity = tryFindEntity(list, "relationship type", code);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%s): '%s'.", MethodUtils.getCurrentMethod()
                    .getName(), code, entity));
        }
        return entity;
    }

}
