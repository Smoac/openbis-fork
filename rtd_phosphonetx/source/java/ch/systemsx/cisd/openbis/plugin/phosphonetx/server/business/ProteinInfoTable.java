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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import net.lemnik.eodsql.DataSet;

import ch.systemsx.cisd.base.namedthread.NamingThreadPoolExecutor;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IPhosphoNetXDAOFactory;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IProteinQueryDAO;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.AbundanceColumnDefinition;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.AggregateFunction;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReferenceWithPeptideSequence;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReferenceWithProbability;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinWithAbundances;

/**
 * Implementation based of {@link IDAOFactory} and {@link IPhosphoNetXDAOFactory}.
 * 
 * @author Franz-Josef Elmer
 */
class ProteinInfoTable extends AbstractBusinessObject implements IProteinInfoTable
{
    private static final ExecutorService executor =
            new NamingThreadPoolExecutor("ProteinInfoTableQueries").corePoolSize(2).daemonize();

    private List<ProteinInfo> infos;

    ProteinInfoTable(IDAOFactory daoFactory, IPhosphoNetXDAOFactory specificDAOFactory,
            Session session)
    {
        super(daoFactory, specificDAOFactory, session);
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
        final IExperimentDAO experimentDAO = getDaoFactory().getExperimentDAO();
        final String permID = experimentDAO.getByTechId(experimentID).getPermId();
        final IProteinQueryDAO dao1 = getSpecificDAOFactory().getProteinQueryDAOFromPool();
        final AbundanceManager abundanceManager;
        final CoverageCalculator coverageCalculator;
        final Collection<ProteinWithAbundances> proteins;
        try
        {
            final Future<DataSet<ProteinReferenceWithPeptideSequence>> proteinReferencesWithPeptidesFuture =
                    getProteinReferencesWithPeptideAsynchronously(dao1, permID);
            final IProteinQueryDAO dao2 = getSpecificDAOFactory().getProteinQueryDAOFromPool();
            try
            {
                final Future<DataSet<ProteinReferenceWithProbability>> proteinReferencesWithProbabilityFuture =
                        getProteinReferencesWithProbabilityAsynchronously(dao2, permID);
                abundanceManager =
                        setUpAbundanceManager(ConcurrencyUtilities.tryGetResult(
                                proteinReferencesWithProbabilityFuture,
                                ConcurrencyUtilities.NO_TIMEOUT), falseDiscoveryRate);
            } finally
            {
                getSpecificDAOFactory().returnProteinQueryDAOToPool(dao2);
            }
            proteins = abundanceManager.getProteinsWithAbundances();
            infos = new ArrayList<ProteinInfo>(proteins.size());
            coverageCalculator =
                    setUpCoverageCalculator(ConcurrencyUtilities.tryGetResult(
                            proteinReferencesWithPeptidesFuture, ConcurrencyUtilities.NO_TIMEOUT));
        } finally
        {
            getSpecificDAOFactory().returnProteinQueryDAOToPool(dao1);
        }
        for (ProteinWithAbundances protein : proteins)
        {
            ProteinInfo proteinInfo = new ProteinInfo();
            proteinInfo.setId(new TechId(protein.getId()));
            AccessionNumberBuilder builder =
                    new AccessionNumberBuilder(protein.getAccessionNumber());
            proteinInfo.setCoverage(coverageCalculator.calculateCoverageFor(protein.getId()));
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
                        values = new double[]
                            { function.aggregate(values) };
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
    }

    private Future<DataSet<ProteinReferenceWithProbability>> getProteinReferencesWithProbabilityAsynchronously(
            final IProteinQueryDAO dao, final String experimentPermID)
    {
        return executor.submit(new Callable<DataSet<ProteinReferenceWithProbability>>()
            {
                public DataSet<ProteinReferenceWithProbability> call() throws Exception
                {
                    return dao.listProteinsByExperiment(experimentPermID);
                }
            });
    }

    private AbundanceManager setUpAbundanceManager(
            DataSet<ProteinReferenceWithProbability> proteinReferences, double falseDiscoveryRate)
    {
        AbundanceManager abundanceManager = new AbundanceManager(getDaoFactory().getSampleDAO());
        ErrorModel errorModel = new ErrorModel(getSpecificDAOFactory());
        try
        {
            for (ProteinReferenceWithProbability protein : proteinReferences)
            {
                if (errorModel.passProtein(protein, falseDiscoveryRate))
                {
                    abundanceManager.handle(protein);
                }
            }
        } finally
        {
            proteinReferences.close();
        }
        return abundanceManager;
    }

    private Future<DataSet<ProteinReferenceWithPeptideSequence>> getProteinReferencesWithPeptideAsynchronously(
            final IProteinQueryDAO dao, final String experimentPermID)
    {
        return executor.submit(new Callable<DataSet<ProteinReferenceWithPeptideSequence>>()
            {
                public DataSet<ProteinReferenceWithPeptideSequence> call() throws Exception
                {
                    return dao.listProteinsWithPeptidesByExperiment(experimentPermID);
                }
            });
    }

    private CoverageCalculator setUpCoverageCalculator(
            DataSet<ProteinReferenceWithPeptideSequence> proteinReferences)
    {
        try
        {
            return new CoverageCalculator(proteinReferences);
        } finally
        {
            proteinReferences.close();
        }
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
