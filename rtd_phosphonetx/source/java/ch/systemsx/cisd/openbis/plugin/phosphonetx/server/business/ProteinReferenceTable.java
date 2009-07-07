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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.lemnik.eodsql.DataSet;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IPhosphoNetXDAOFactory;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReference;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReferenceWithProbability;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class ProteinReferenceTable extends AbstractBusinessObject implements IProteinReferenceTable
{
    private List<ProteinReference> proteins;

    public ProteinReferenceTable(IDAOFactory daoFactory,
            IPhosphoNetXDAOFactory specificDAOFactory, Session session)
    {
        super(daoFactory, specificDAOFactory, session);
    }

    public List<ProteinReference> getProteinReferences()
    {
        if (proteins == null)
        {
            throw new IllegalStateException("No proteins loaded.");
        }
        return proteins;
    }

    public void load(String experimentPermID, double falseDiscoveryRate)
    {
        proteins = new ArrayList<ProteinReference>();
        DataSet<ProteinReferenceWithProbability> resultSet =
            getSpecificDAOFactory().getProteinQueryDAO().listProteinsByExperiment(experimentPermID);
        ErrorModel errorModel = new ErrorModel(getSpecificDAOFactory());
        Set<String> idsOfPassedProteins = new HashSet<String>();
        for (ProteinReferenceWithProbability protein : resultSet)
        {
            if (errorModel.passProtein(protein, falseDiscoveryRate))
            {
                String uniprotID = protein.getUniprotID();
                if (idsOfPassedProteins.contains(uniprotID) == false)
                {
                    idsOfPassedProteins.add(uniprotID);
                    proteins.add(protein);
                }
            }
        }
    }

}
