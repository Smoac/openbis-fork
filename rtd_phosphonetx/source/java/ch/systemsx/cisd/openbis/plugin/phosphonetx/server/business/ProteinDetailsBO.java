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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business;

import java.util.ArrayList;
import java.util.List;

import net.lemnik.eodsql.DataSet;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IPhosphoNetXDAOFactory;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IProteinQueryDAO;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.Peptide;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinDetails;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.IdentifiedPeptide;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.IdentifiedProtein;

/**
 * @author Franz-Josef Elmer
 */
class ProteinDetailsBO extends AbstractBusinessObject implements IProteinDetailsBO
{
    private ProteinDetails details;

    ProteinDetailsBO(IDAOFactory daoFactory, IPhosphoNetXDAOFactory specificDAOFactory,
            Session session)
    {
        super(daoFactory, specificDAOFactory, session);
    }

    public ProteinDetails getDetailsOrNull()
    {
        return details;
    }

    public void loadByExperimentAndReference(TechId experimentID, TechId proteinReferenceID)
    {
        String experimentPermID = getExperimentPermIDFor(experimentID);
        IProteinQueryDAO proteinQueryDAO = getSpecificDAOFactory().getProteinQueryDAO();
        DataSet<IdentifiedProtein> proteins =
                proteinQueryDAO.listProteinsByProteinReferenceAndExperiment(experimentPermID,
                        proteinReferenceID.getId());
        try
        {
            if (proteins.size() == 1)
            {
                ErrorModel errorModel = new ErrorModel(getSpecificDAOFactory());
                IdentifiedProtein protein = proteins.get(0);
                errorModel.setFalseDiscoveryRateFor(protein);
                details = new ProteinDetails();
                details.setSequence(protein.getSequence());
                details.setDatabaseNameAndVersion(protein.getDatabaseNameAndVersion());
                details.setProbability(protein.getProbability());
                details.setFalseDiscoveryRate(protein.getFalseDiscoveryRate());
                String dataSetPermID = protein.getDataSetPermID();
                details.setDataSetPermID(dataSetPermID);
                DataPE ds = getDaoFactory().getExternalDataDAO().tryToFindDataSetByCode(dataSetPermID);
                if (ds != null)
                {
                    details.setDataSetTechID(ds.getId());
                    details.setDataSetTypeCode(ds.getDataSetType().getCode());
                }
                details.setPeptides(loadPeptides(protein));
                details.setProteinID(new TechId(protein.getProteinID()));
            }
        } finally
        {
            proteins.close();
        }
    }

    private List<Peptide> loadPeptides(IdentifiedProtein protein)
    {
        IProteinQueryDAO proteinQueryDAO = getSpecificDAOFactory().getProteinQueryDAO();
        DataSet<IdentifiedPeptide> identifiedPeptides =
                proteinQueryDAO.listIdentifiedPeptidesByProtein(protein.getProteinID());
        List<Peptide> peptides = new ArrayList<Peptide>();
        for (IdentifiedPeptide identifiedPeptide : identifiedPeptides)
        {
            Peptide peptide = new Peptide();
            peptide.setSequence(identifiedPeptide.getSequence());
            peptide.setCharge(identifiedPeptide.getCharge());
            peptides.add(peptide);
        }
        identifiedPeptides.close();
        return peptides;
    }

    private String getExperimentPermIDFor(TechId experimentId)
    {
        ExperimentPE experiment = getDaoFactory().getExperimentDAO().getByTechId(experimentId);
        return experiment.getPermId();
    }
}
