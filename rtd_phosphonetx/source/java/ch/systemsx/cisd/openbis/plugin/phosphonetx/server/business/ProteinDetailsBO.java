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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.lemnik.eodsql.DataSet;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IPhosphoNetXDAOFactory;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IProteinQueryDAO;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.IndistinguishableProteinInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.Peptide;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.PeptideModification;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinDetails;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.IdentifiedProtein;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.IndistinguishableProtein;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.PeptideWithModification;

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
                details.setCoverage(100 * protein.getCoverage());
                details.setFalseDiscoveryRate(protein.getFalseDiscoveryRate());
                String dataSetPermID = protein.getDataSetPermID();
                details.setDataSetPermID(dataSetPermID);
                DataPE ds = getDaoFactory().getDataDAO().tryToFindDataSetByCode(dataSetPermID);
                if (ds != null)
                {
                    details.setDataSetTechID(ds.getId());
                    details.setDataSetTypeCode(ds.getDataSetType().getCode());
                }
                details.setPeptides(loadPeptides(protein));
                long proteinID = protein.getProteinID();
                details.setProteinID(new TechId(proteinID));
                details.setIndistinguishableProteinInfos(loadIndistinguishableProteinInfos(proteinID));
            }
        } finally
        {
            proteins.close();
        }
    }

    private List<IndistinguishableProteinInfo> loadIndistinguishableProteinInfos(long proteinID)
    {
        IProteinQueryDAO proteinQueryDAO = getSpecificDAOFactory().getProteinQueryDAO();
        DataSet<IndistinguishableProtein> proteins =
                proteinQueryDAO.listIndistinguishableProteinsByProteinID(proteinID);
        try
        {
            List<IndistinguishableProteinInfo> infos =
                    new ArrayList<IndistinguishableProteinInfo>();
            for (IndistinguishableProtein protein : proteins)
            {
                IndistinguishableProteinInfo info = new IndistinguishableProteinInfo();
                AccessionNumberBuilder builder =
                        new AccessionNumberBuilder(protein.getAccessionNumber());
                info.setAccessionNumber(builder.getAccessionNumber());
                info.setAccessionNumberType(builder.getTypeOrNull());
                info.setDescription(protein.getDescription());
                info.setSequence(protein.getSequence());
                info.setCoverage(100 * protein.getCoverage());
                infos.add(info);
            }
            return infos;
        } finally
        {
            proteins.close();
        }
    }

    private List<Peptide> loadPeptides(IdentifiedProtein protein)
    {
        IProteinQueryDAO proteinQueryDAO = getSpecificDAOFactory().getProteinQueryDAO();
        DataSet<PeptideWithModification> identifiedPeptides =
                proteinQueryDAO.listIdentifiedPeptidesByProtein(protein.getProteinID());
        try
        {
            Map<Long, Peptide> peps = new HashMap<Long, Peptide>();
            for (PeptideWithModification peptidWithModification : identifiedPeptides)
            {
                long id = peptidWithModification.getId();
                Peptide peptide = peps.get(id);
                if (peptide == null)
                {
                    peptide = new Peptide();
                    peptide.setSequence(peptidWithModification.getSequence());
                    peps.put(id, peptide);
                }
                Integer position = peptidWithModification.getPosition();
                Double mass = peptidWithModification.getMass();
                if (position != null && mass != null)
                {
                    PeptideModification peptideModification = new PeptideModification();
                    peptideModification.setPosition(position);
                    peptideModification.setMass(mass);
                    peptide.getModifications().add(peptideModification);
                }
            }
            List<Peptide> result = new ArrayList<Peptide>(peps.values());
            Collections.sort(result, new Comparator<Peptide>()
                {
                    public int compare(Peptide p1, Peptide p2)
                    {
                        return p1.getSequence().compareTo(p2.getSequence());
                    }
                });
            return result;
        } finally
        {
            identifiedPeptides.close();
        }
    }

    private String getExperimentPermIDFor(TechId experimentId)
    {
        ExperimentPE experiment = getDaoFactory().getExperimentDAO().getByTechId(experimentId);
        return experiment.getPermId();
    }
}
