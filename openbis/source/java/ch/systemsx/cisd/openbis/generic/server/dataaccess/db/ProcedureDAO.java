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
import org.hibernate.SessionFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProcedureDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcedurePE;

/**
 * Implementation of {@link IProcedureDAO} for databases.
 * 
 * @author Christian Ribeaud
 */
public class ProcedureDAO extends AbstractDAO implements IProcedureDAO
{
    private static final Class<ProcedurePE> ENTITY_CLASS = ProcedurePE.class;

    private static final String TABLE_NAME = ENTITY_CLASS.getSimpleName();

    /**
     * This logger does not output any SQL statement. If you want to do so, you had better set an
     * appropriate debugging level for class {@link JdbcAccessor}.
     * </p>
     */
    public static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, ProcedureDAO.class);

    ProcedureDAO(final SessionFactory sessionFactory, final DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance);
    }

    //
    // IProcedureDAO
    //

    public final void createProcedure(final ProcedurePE procedure) throws DataAccessException
    {
        assert procedure != null : "Given procedure can not be null.";
        validatePE(procedure);

        final HibernateTemplate template = getHibernateTemplate();
        template.save(procedure);
        template.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("ADD: procedure '%s'.", procedure));
        }
    }

    public final List<ProcedurePE> listProcedures(final long experimentId)
            throws DataAccessException
    {
        final List<ProcedurePE> procedures =
                cast(getHibernateTemplate().find(
                        String.format("from %s p where p.experimentInternal.id = ?", TABLE_NAME),
                        toArray(experimentId)));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("listProcedures(" + experimentId + "): " + procedures.size()
                    + " procedure(s) have been found.");
        }
        return procedures;
    }

    public final ProcedurePE getProcedure(final long procedureId) throws DataAccessException
    {
        final ProcedurePE procedure =
                (ProcedurePE) getHibernateTemplate().load(ENTITY_CLASS, procedureId);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("getProcedure(" + procedureId + "): '" + procedure + "'.");
        }
        return procedure;
    }

    public final ProcedurePE tryGetProcedure(final String procedureTypeCode, final long experimentId)
    {
        assert procedureTypeCode != null : "Unspecified procedure type code";

        final List<ProcedurePE> procedures =
                cast(getHibernateTemplate().find(
                        String.format("from %s p where p.procedureType.code = ? "
                                + "and p.experimentInternal.id = ?", TABLE_NAME), new Object[]
                            { procedureTypeCode, experimentId }));
        final ProcedurePE procedure =
                tryFindEntity(procedures, "procedures", procedureTypeCode, experimentId);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("tryGetProcedure(" + procedureTypeCode + ", " + experimentId
                    + "): '" + procedure + "'.");
        }
        return procedure;
    }
}
