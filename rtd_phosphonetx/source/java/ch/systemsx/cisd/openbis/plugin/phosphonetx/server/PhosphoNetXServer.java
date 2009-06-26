/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import net.lemnik.eodsql.DataSet;

import org.springframework.stereotype.Component;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IPhosphoNetXDAOFactory;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IPhosphoNetXServer;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReference;

/**
 * @author Franz-Josef Elmer
 */
@Component(ResourceNames.PHOSPHONETX_PLUGIN_SERVER)
public class PhosphoNetXServer extends AbstractServer<IPhosphoNetXServer> implements
        IPhosphoNetXServer
{
    @Resource(name = ResourceNames.PHOSPHONETX_DAO_FACTORY)
    private IPhosphoNetXDAOFactory specificDAOFactory;

    public PhosphoNetXServer()
    {
        super();
    }

    @Private
    PhosphoNetXServer(ISessionManager<Session> sessionManager, IDAOFactory daoFactory,
            IPhosphoNetXDAOFactory specificDAOFactory,
            ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin,
            IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin)
    {
        super(sessionManager, daoFactory, sampleTypeSlaveServerPlugin, dataSetTypeSlaveServerPlugin);
        this.specificDAOFactory = specificDAOFactory;
    }

    @Override
    protected Class<IPhosphoNetXServer> getProxyInterface()
    {
        return IPhosphoNetXServer.class;
    }

    public IPhosphoNetXServer createLogger(boolean invocationSuccessful, long elapsedTime)
    {
        return new PhosphoNetXServerLogger(getSessionManager(), invocationSuccessful, elapsedTime);
    }

    public List<ProteinReference> listProteinReferencesByExperiment(String sessionToken,
            TechId experimentId) throws UserFailureException
    {
        ExperimentPE experiment = getDAOFactory().getExperimentDAO().getByTechId(experimentId);
        String permId = experiment.getPermId();
        DataSet<ProteinReference> resultSet =
                specificDAOFactory.getProteinQueryDAO().listProteinsByExperiment(permId);
        ArrayList<ProteinReference> refrences = new ArrayList<ProteinReference>();
        refrences.addAll(resultSet);
        return refrences;
    }

}
