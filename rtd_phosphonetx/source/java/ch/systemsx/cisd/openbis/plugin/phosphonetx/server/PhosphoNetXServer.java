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
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business.IBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business.IProteinReferenceTable;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IPhosphoNetXDAOFactory;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IProteinQueryDAO;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IPhosphoNetXServer;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinByExperiment;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinSequence;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReference;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.Sequence;

/**
 * @author Franz-Josef Elmer
 */
@Component(ResourceNames.PHOSPHONETX_PLUGIN_SERVER)
public class PhosphoNetXServer extends AbstractServer<IPhosphoNetXServer> implements
        IPhosphoNetXServer
{
    @Resource(name = ResourceNames.PHOSPHONETX_DAO_FACTORY)
    private IPhosphoNetXDAOFactory specificDAOFactory;
    
    @Resource(name = ResourceNames.PHOSPHONETX_BO_FACTORY)
    private IBusinessObjectFactory specificBOFactory;

    public PhosphoNetXServer()
    {
        super();
    }

    @Private
    PhosphoNetXServer(ISessionManager<Session> sessionManager, IDAOFactory daoFactory,
            IPhosphoNetXDAOFactory specificDAOFactory,
            IBusinessObjectFactory specificBOFactory,
            ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin,
            IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin)
    {
        super(sessionManager, daoFactory, sampleTypeSlaveServerPlugin, dataSetTypeSlaveServerPlugin);
        this.specificDAOFactory = specificDAOFactory;
        this.specificBOFactory = specificBOFactory;
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

    public List<ProteinReference> listProteinsByExperiment(String sessionToken,
            TechId experimentId, double falseDiscoveryRate) throws UserFailureException
    {
        final Session session = getSessionManager().getSession(sessionToken);
        IProteinReferenceTable table = specificBOFactory.createProteinReferenceTable(session);
        ExperimentPE experiment = getDAOFactory().getExperimentDAO().getByTechId(experimentId);
        table.load(experiment.getPermId(), falseDiscoveryRate);
        return table.getProteinReferences();
    }

    public ProteinByExperiment getProteinByExperiment(String sessionToken, TechId experimentID,
            TechId proteinReferenceID) throws UserFailureException
    {
        getSessionManager().getSession(sessionToken);
        IProteinQueryDAO proteinQueryDAO = specificDAOFactory.getProteinQueryDAO();
        ProteinByExperiment proteinByExperiment = new ProteinByExperiment();
        ProteinReference proteinReference =
                proteinQueryDAO.tryToGetProteinReference(proteinReferenceID.getId());
        if (proteinReference == null)
        {
            throw new UserFailureException("No protein reference found for ID: " + proteinReferenceID);
        }
        proteinByExperiment.setUniprotID(proteinReference.getUniprotID());
        proteinByExperiment.setDescription(proteinReference.getDescription());
        return proteinByExperiment;
    }
    
    public List<ProteinSequence> listProteinSequencesByProteinReference(String sessionToken,
            TechId proteinReferenceID) throws UserFailureException
    {
        IProteinQueryDAO proteinQueryDAO = specificDAOFactory.getProteinQueryDAO();
        DataSet<Sequence> sequences =
                proteinQueryDAO.listProteinSequencesByProteinReference(proteinReferenceID.getId());
        ArrayList<ProteinSequence> proteinSequences =
                new ArrayList<ProteinSequence>(sequences.size());
        int number = 0;
        for (Sequence sequence : sequences)
        {
            ProteinSequence proteinSequence = new ProteinSequence();
            proteinSequence.setId(new TechId(sequence.getId()));
            proteinSequence.setShortName(createShortName(number++));
            proteinSequence.setSequence(sequence.getSequence());
            proteinSequence.setDatabaseNameAndVersion(sequence.getDatabaseNameAndVersion());
            proteinSequences.add(proteinSequence);
        }
        sequences.close();
        return proteinSequences;
    }

    private String createShortName(int number)
    {
        StringBuilder builder = new StringBuilder();
        int n = number;
        while (n > 0)
        {
            builder.insert(0, "ABCEDEFGHIJKLMNOPQRSTUVWXYZ".charAt(n % 26));
            n /= 26;
        }
        return builder.toString();
    }

}
