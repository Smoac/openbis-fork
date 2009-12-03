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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.lemnik.eodsql.DataSet;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IPhosphoNetXDAOFactory;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IProteinQueryDAO;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinSequence;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.Sequence;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class ProteinSequenceTable extends AbstractBusinessObject implements IProteinSequenceTable
{
    private List<ProteinSequence> proteinSequences;
    private Map<Long, String> databaseIDToShortNameMap;

    ProteinSequenceTable(IDAOFactory daoFactory, IPhosphoNetXDAOFactory specificDAOFactory,
            Session session)
    {
        super(daoFactory, specificDAOFactory, session);
    }

    public List<ProteinSequence> getSequences()
    {
        if (proteinSequences == null)
        {
            throw new IllegalStateException("Sequences not loaded.");
        }
        return proteinSequences;
    }
    
    public String getShortName(long databaseID)
    {
        if (databaseIDToShortNameMap == null)
        {
            throw new IllegalStateException("Sequences not loaded.");
        }
        String shortName = databaseIDToShortNameMap.get(databaseID);
        if (shortName == null)
        {
            throw new IllegalArgumentException("No sequence found for databaseID " + databaseID);
        }
        return shortName;
    }

    public void loadByReference(TechId proteinReferenceID)
    {
        final IProteinQueryDAO proteinQueryDAO = getSpecificDAOFactory().getProteinQueryDAOFromPool();
        try
        {
            final DataSet<Sequence> sequences =
                    proteinQueryDAO.listProteinSequencesByProteinReference(proteinReferenceID.getId());
            try
            {
                proteinSequences = new ArrayList<ProteinSequence>(sequences.size());
                databaseIDToShortNameMap = new HashMap<Long, String>();
                int number = 0;
                for (Sequence sequence : sequences)
                {
                    ProteinSequence proteinSequence = new ProteinSequence();
                    proteinSequence.setId(new TechId(sequence.getId()));
                    String shortName = createShortName(number++);
                    proteinSequence.setShortName(shortName);
                    proteinSequence.setSequence(sequence.getSequence());
                    long databaseID = sequence.getDatabaseID();
                    proteinSequence.setDatabaseID(new TechId(databaseID));
                    proteinSequence.setDatabaseNameAndVersion(sequence.getDatabaseNameAndVersion());
                    proteinSequences.add(proteinSequence);
                    databaseIDToShortNameMap.put(databaseID, shortName);
                }
            } finally
            {
                sequences.close();
            }
        } finally
        {
            getSpecificDAOFactory().returnProteinQueryDAOToPool(proteinQueryDAO);
        }
    }

    private String createShortName(int number)
    {
        StringBuilder builder = new StringBuilder();
        int n = number;
        while (n > 0 || builder.length() == 0)
        {
            builder.insert(0, "ABCEDEFGHIJKLMNOPQRSTUVWXYZ".charAt(n % 26));
            n /= 26;
        }
        return builder.toString();
    }

}
