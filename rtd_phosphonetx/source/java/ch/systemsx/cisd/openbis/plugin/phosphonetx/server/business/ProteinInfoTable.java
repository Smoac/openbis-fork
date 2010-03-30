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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.lemnik.eodsql.DataSet;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IPhosphoNetXDAOFactory;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.AbundanceColumnDefinition;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.AggregateFunction;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReferenceWithProbability;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinWithAbundances;

/**
 * Implementation based of {@link IDAOFactory} and {@link IPhosphoNetXDAOFactory}.
 * 
 * @author Franz-Josef Elmer
 */
class ProteinInfoTable extends AbstractBusinessObject implements IProteinInfoTable
{
    private List<ProteinInfo> infos;
    private final ISampleProvider sampleProvider;

    ProteinInfoTable(IDAOFactory daoFactory, IPhosphoNetXDAOFactory specificDAOFactory,
            Session session, ISampleProvider sampleProvider)
    {
        super(daoFactory, specificDAOFactory, session);
        this.sampleProvider = sampleProvider;
    }

    public List<ProteinInfo> getProteinInfos()
    {
        if (infos == null)
        {
            throw new IllegalStateException("No proteins loaded.");
        }
        return infos;
    }

    public void load(List<AbundanceColumnDefinition> definitions, TechId experimentID,
            double falseDiscoveryRate, AggregateFunction function, boolean aggregateOnOriginal)
    {
        IExperimentDAO experimentDAO = getDaoFactory().getExperimentDAO();
        String permID = experimentDAO.getByTechId(experimentID).getPermId();
        AbundanceManager abundanceManager = setUpAbundanceManager(permID, falseDiscoveryRate);
        Collection<ProteinWithAbundances> proteins = abundanceManager.getProteinsWithAbundances();
        infos = new ArrayList<ProteinInfo>(proteins.size());
        for (ProteinWithAbundances protein : proteins)
        {
            ProteinInfo proteinInfo = new ProteinInfo();
            proteinInfo.setId(new TechId(protein.getId()));
            AccessionNumberBuilder builder = new AccessionNumberBuilder(protein.getAccessionNumber());
            proteinInfo.setCoverage(100 * protein.getCoverage());
            proteinInfo.setAccessionNumber(builder.getAccessionNumber());
            proteinInfo.setDescription(protein.getDescription());
            proteinInfo.setExperimentID(experimentID);
            Map<Long, Double> abundances = new HashMap<Long, Double>();
            for (AbundanceColumnDefinition abundanceColumnDefinition : definitions)
            {
                double[] abundanceValues = new double[0];
                List<Long> ids = abundanceColumnDefinition.getSampleIDs();
                for (Long sampleID : ids)
                {
                    double[] values = protein.getAbundancesForSample(sampleID);
                    if (values != null && values.length > 0 && aggregateOnOriginal == false)
                    {
                        values = new double[] {function.aggregate(values)};
                    }
                    abundanceValues = concatenate(abundanceValues, values);
                }
                if (abundanceValues.length > 0)
                {
                    double aggregatedAbundance = function.aggregate(abundanceValues);
                    abundances.put(abundanceColumnDefinition.getID(), aggregatedAbundance);
                }
            }
            proteinInfo.setAbundances(abundances);
            infos.add(proteinInfo);
        }
        Collections.sort(infos, new Comparator<ProteinInfo>()
            {

                public int compare(ProteinInfo p1, ProteinInfo p2)
                {
                    String an1 = p1.getAccessionNumber();
                    String an2 = p2.getAccessionNumber();
                    return an1 == null ? -1 : (an2 == null ? 1 : an1.compareToIgnoreCase(an2));
                }
            });
    }

    private AbundanceManager setUpAbundanceManager(String experimentPermID,
            double falseDiscoveryRate)
    {
        AbundanceManager abundanceManager = new AbundanceManager(sampleProvider);
        IPhosphoNetXDAOFactory daoFactory = getSpecificDAOFactory();
        ErrorModel errorModel = new ErrorModel(daoFactory);
        DataSet<ProteinReferenceWithProbability> resultSet =
                daoFactory.getProteinQueryDAO().listProteinsByExperiment(experimentPermID);
        try
        {
            for (ProteinReferenceWithProbability protein : resultSet)
            {
                if (errorModel.passProtein(protein, falseDiscoveryRate))
                {
                    abundanceManager.handle(protein);
                }
            }
        } finally
        {
            resultSet.close();
        }
        return abundanceManager;
    }

    private static double[] concatenate(double[] array1OrNull, double[] array2OrNull)
    {
        if (array1OrNull == null || array1OrNull.length == 0)
        {
            return array2OrNull;
        }
        if (array2OrNull == null || array2OrNull.length == 0)
        {
            return array1OrNull;
        }
        double[] newArray = new double[array1OrNull.length + array2OrNull.length];
        System.arraycopy(array1OrNull, 0, newArray, 0, array1OrNull.length);
        System.arraycopy(array2OrNull, 0, newArray, array1OrNull.length, array2OrNull.length);
        return newArray;
    }

}
