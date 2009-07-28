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

import java.util.List;

import javax.annotation.Resource;

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
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business.IDataSetProteinTable;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business.IProteinDetailsBO;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business.IProteinReferenceTable;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business.IProteinSequenceTable;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business.ISampleTable;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IPhosphoNetXDAOFactory;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IProteinQueryDAO;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IPhosphoNetXServer;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.DataSetProtein;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinByExperiment;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinSequence;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.SampleWithPropertiesAndAbundance;
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

    public IPhosphoNetXServer createLogger(boolean invocationSuccessful, long elapsedTime)
    {
        return new PhosphoNetXServerLogger(getSessionManager(), invocationSuccessful, elapsedTime);
    }

    public List<ProteinReference> listProteinsByExperiment(String sessionToken,
            TechId experimentId, double falseDiscoveryRate) throws UserFailureException
    {
        final Session session = getSessionManager().getSession(sessionToken);
        IProteinReferenceTable table = specificBOFactory.createProteinReferenceTable(session);
        String experimentPermId = getExperimentPermIDFor(experimentId);
        table.load(experimentPermId, falseDiscoveryRate);
        return table.getProteinReferences();
    }

    public ProteinByExperiment getProteinByExperiment(String sessionToken, TechId experimentID,
            TechId proteinReferenceID) throws UserFailureException
    {
        Session session = getSessionManager().getSession(sessionToken);
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
        IProteinDetailsBO proteinDetailsBO = specificBOFactory.createProteinDetailsBO(session);
        proteinDetailsBO.loadByExperimentAndReference(experimentID, proteinReferenceID);
        proteinByExperiment.setDetails(proteinDetailsBO.getDetailsOrNull());
        return proteinByExperiment;
    }
    
    public List<ProteinSequence> listProteinSequencesByProteinReference(String sessionToken,
            TechId proteinReferenceID) throws UserFailureException
    {
        final Session session = getSessionManager().getSession(sessionToken);
        IProteinSequenceTable sequenceTable = specificBOFactory.createProteinSequenceTable(session);
        sequenceTable.loadByReference(proteinReferenceID);
        return sequenceTable.getSequences();
    }

    public List<DataSetProtein> listProteinsByExperimentAndReference(String sessionToken,
            TechId experimentId, TechId proteinReferenceID) throws UserFailureException
    {
        final Session session = getSessionManager().getSession(sessionToken);
        IProteinSequenceTable sequenceTable = specificBOFactory.createProteinSequenceTable(session);
        sequenceTable.loadByReference(proteinReferenceID);
        IDataSetProteinTable dataSetProteinTable = specificBOFactory.createDataSetProteinTable(session);
        dataSetProteinTable.load(getExperimentPermIDFor(experimentId), proteinReferenceID, sequenceTable);
        return dataSetProteinTable.getDataSetProteins();
    }

    public List<SampleWithPropertiesAndAbundance> listSamplesWithAbundanceByProtein(
            String sessionToken, TechId proteinID) throws UserFailureException
    {
        final Session session = getSessionManager().getSession(sessionToken);
        ISampleTable sampleTable = specificBOFactory.createSampleTable(session);
        sampleTable.loadSamplesWithAbundance(proteinID);
        return sampleTable.getSamples();
    }

    private String getExperimentPermIDFor(TechId experimentId)
    {
        ExperimentPE experiment = getDAOFactory().getExperimentDAO().getByTechId(experimentId);
        String experimentPermId = experiment.getPermId();
        return experimentPermId;
    }
    
}
