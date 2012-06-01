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

package ch.systemsx.cisd.openbis.plugin.proteomics.server.business;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.lemnik.eodsql.DataSet;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.dataaccess.IPhosphoNetXDAOFactory;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.dataaccess.IProteinQueryDAO;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.AbundanceColumnDefinition;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.AggregateFunction;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinInfo;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.dto.ProteinAbundance;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.dto.ProteinReferenceWithProtein;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.dto.ProteinWithAbundances;

/**
 * Implementation based of {@link IDAOFactory} and {@link IPhosphoNetXDAOFactory}.
 * 
 * @author Franz-Josef Elmer
 */
class ProteinInfoTable extends AbstractBusinessObject implements IProteinInfoTable
{
    protected static final Logger operationLog =
        LogFactory.getLogger(LogCategory.OPERATION, ProteinInfoTable.class);
    
    private List<ProteinInfo> infos;
    private final ISampleProvider sampleProvider;

    ProteinInfoTable(IDAOFactory daoFactory, IPhosphoNetXDAOFactory specificDAOFactory,
            Session session, ISampleProvider sampleProvider)
    {
        super(daoFactory, specificDAOFactory, session);
        this.sampleProvider = sampleProvider;
    }

    @Override
    public List<ProteinInfo> getProteinInfos()
    {
        if (infos == null)
        {
            throw new IllegalStateException("No proteins loaded.");
        }
        return infos;
    }

    @Override
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

                @Override
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
        IPhosphoNetXDAOFactory specificDAOFactory = getSpecificDAOFactory();
        IProteinQueryDAO dao = specificDAOFactory.getProteinQueryDAO(experimentPermID);
        long time = System.currentTimeMillis();
        DataSet<ProteinReferenceWithProtein> dataSet =
                dao.listProteinReferencesByExperiment(experimentPermID);
        List<ProteinReferenceWithProtein> proteins = new ArrayList<ProteinReferenceWithProtein>();
        LongOpenHashSet proteinIDs = new LongOpenHashSet();
        try
        {
            for (ProteinReferenceWithProtein protein : dataSet)
            {
                proteins.add(protein);
                proteinIDs.add(protein.getProteinID());
            }
        } finally
        {
            dataSet.close();
        }
        operationLog.info("(" + (System.currentTimeMillis() - time) + "ms) for listProteinReferencesByExperiment");
        Map<Long, List<ProteinAbundance>> abundancesPerProtein = getAbudancesPerProtein(dao, proteinIDs);
        ErrorModel errorModel = new ErrorModel(dao);
        for (ProteinReferenceWithProtein protein : proteins)
        {
            if (errorModel.passProtein(protein, falseDiscoveryRate))
            {
                List<ProteinAbundance> list = abundancesPerProtein.get(protein.getProteinID());
                abundanceManager.handle(protein, list);
            }
        }
        return abundanceManager;
    }

    private Map<Long, List<ProteinAbundance>> getAbudancesPerProtein(IProteinQueryDAO dao, LongOpenHashSet proteinIDs)
    {
        long time = System.currentTimeMillis();
        DataSet<ProteinAbundance> dataSet = dao.listProteinWithAbundanceByExperiment(proteinIDs);
        List<ProteinAbundance> proteinAbundances = new ArrayList<ProteinAbundance>();
        try
        {
            for (ProteinAbundance proteinAbundance : dataSet)
            {
                proteinAbundances.add(proteinAbundance);
            }
        } finally
        {
            dataSet.close();
        }
        operationLog.info("(" + (System.currentTimeMillis() - time) + "ms) for listProteinWithAbundanceByExperiment");
        Map<Long, List<ProteinAbundance>> abundancesPerProtein =
                new HashMap<Long, List<ProteinAbundance>>();
        for (ProteinAbundance proteinAbundance : proteinAbundances)
        {
            long proteinID = proteinAbundance.getId();
            List<ProteinAbundance> list = abundancesPerProtein.get(proteinID);
            if (list == null)
            {
                list = new ArrayList<ProteinAbundance>();
                abundancesPerProtein.put(proteinID, list);
            }
            list.add(proteinAbundance);
        }
        return abundancesPerProtein;
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
