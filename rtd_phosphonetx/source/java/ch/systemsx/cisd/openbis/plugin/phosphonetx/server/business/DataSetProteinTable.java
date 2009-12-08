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
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IPhosphoNetXDAOFactory;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IProteinQueryDAO;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.DataSetProtein;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.IdentifiedProtein;

/**
 * @author Franz-Josef Elmer
 */
class DataSetProteinTable extends AbstractBusinessObject implements IDataSetProteinTable
{
    private List<DataSetProtein> dataSetProteins;

    DataSetProteinTable(IDAOFactory daoFactory, IPhosphoNetXDAOFactory specificDAOFactory,
            Session session)
    {
        super(daoFactory, specificDAOFactory, session);
    }

    public List<DataSetProtein> getDataSetProteins()
    {
        return dataSetProteins;
    }

    public void load(String experimentPermID, TechId proteinReferenceID,
            IProteinSequenceTable sequenceTable)
    {
        IProteinQueryDAO proteinQueryDAO = getSpecificDAOFactory().getProteinQueryDAO();
        ErrorModel errorModel = new ErrorModel(getSpecificDAOFactory());
        DataSet<IdentifiedProtein> proteins =
                proteinQueryDAO.listProteinsByProteinReferenceAndExperiment(experimentPermID,
                        proteinReferenceID.getId());
        dataSetProteins = new ArrayList<DataSetProtein>();
        for (IdentifiedProtein protein : proteins)
        {
            errorModel.setFalseDiscoveryRateFor(protein);
            DataSetProtein dataSetProtein = new DataSetProtein();
            dataSetProtein.setDataSetID(new TechId(protein.getDataSetID()));
            dataSetProtein.setDataSetPermID(protein.getDataSetPermID());
            dataSetProtein.setFalseDiscoveryRate(protein.getFalseDiscoveryRate());
            dataSetProtein.setPeptideCount(protein.getPeptideCount());
            dataSetProtein.setProteinID(new TechId(protein.getProteinID()));
            dataSetProtein.setSequenceName(sequenceTable.getShortName(protein.getDatabaseID()));
            dataSetProteins.add(dataSetProtein);
        }
        proteins.close();
    }

}
