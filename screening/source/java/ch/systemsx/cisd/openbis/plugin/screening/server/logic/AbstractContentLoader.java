/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic;

import java.sql.Connection;
import java.util.Set;

import net.lemnik.eodsql.QueryTool;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.DatabaseContextUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.IScreeningQuery;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentReference;

/**
 * Abstract superclass for screening loaders.
 * 
 * @author Tomasz Pylak
 */
abstract class AbstractContentLoader
{
    protected final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            AbstractContentLoader.class);

    protected final Session session;

    protected final IScreeningBusinessObjectFactory businessObjectFactory;

    protected final IDAOFactory daoFactory;

    protected AbstractContentLoader(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory)
    {
        this.session = session;
        this.businessObjectFactory = businessObjectFactory;
        this.daoFactory = daoFactory;
    }

    protected static IScreeningQuery createDAO(IDAOFactory daoFactory)
    {
        Connection connection = DatabaseContextUtils.getConnection(daoFactory);
        return QueryTool.getQuery(connection, IScreeningQuery.class);
    }

    protected final ExperimentReference loadExperimentByPermId(String experimentPermId)
    {
        final ExperimentPE experiment =
                daoFactory.getExperimentDAO().tryGetByPermID(experimentPermId);
        return createExperimentReference(experiment, "permId = " + experimentPermId);
    }

    protected final ExperimentReference loadExperimentByTechId(TechId experimentTechId)
    {
        final ExperimentPE experiment =
                daoFactory.getExperimentDAO().tryGetByTechId(experimentTechId);
        return createExperimentReference(experiment, "id = " + experimentTechId);
    }

    private ExperimentReference createExperimentReference(final ExperimentPE experimentOrNull,
            String criteriaDesc)
    {
        if (experimentOrNull == null)
        {
            throw new UserFailureException("Unkown experiment for " + criteriaDesc + ".");
        }
        ProjectPE project = experimentOrNull.getProject();
        return new ExperimentReference(experimentOrNull.getId(), experimentOrNull.getPermId(),
                experimentOrNull.getCode(), experimentOrNull.getEntityType().getCode(),
                project.getCode(), project.getSpace().getCode());
    }

    protected final FeatureVectorDatasetLoader createDatasetsRetriever(Set<PlateIdentifier> plates)
    {
        return new FeatureVectorDatasetLoader(session, businessObjectFactory, null, plates);
    }

}
