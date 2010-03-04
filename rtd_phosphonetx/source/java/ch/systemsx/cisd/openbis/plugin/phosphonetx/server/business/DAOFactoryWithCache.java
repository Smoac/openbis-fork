/*
 * Copyright 2010 ETH Zuerich, CISD
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.lemnik.eodsql.DataSet;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IPhosphoNetXDAOFactory;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IProteinQueryDAO;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.IdentifiedPeptide;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.IdentifiedProtein;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProbabilityFDRMapping;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReference;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReferenceWithPeptideSequence;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReferenceWithProbability;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReferenceWithProbabilityAndPeptide;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.SampleAbundance;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.Sequence;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class DAOFactoryWithCache implements IPhosphoNetXDAOFactory
{
    private IProteinQueryDAO proteinQueryDAO;

    private static final class ProteinQueryDAO implements IProteinQueryDAO
    {
        private static final class ListBasedDataSet<T> extends ArrayList<T> implements DataSet<T>
        {
            private static final long serialVersionUID = 1L;
            
            ListBasedDataSet(DataSet<T> dataSet)
            {
                try
                {
                    for (T row : dataSet)
                    {
                        add(row);
                    }
                } finally
                {
                    dataSet.close();
                }
            }

            public void close()
            {
            }

            public void disconnect()
            {
            }

            public boolean isConnected()
            {
                return false;
            }
        }
        
        private static interface IDataSetLoader<T>
        {
            public DataSet<T> load(String experimentPermID);
        }
        
        private static final class FileBasedDataSetLoader<T> implements IDataSetLoader<T>
        {
            private final File store;

            FileBasedDataSetLoader(File store)
            {
                this.store = store;
                store.mkdirs();
            }
            
            void save(String experimentPermID, DataSet<T> dataSet)
            {
                File file = new File(store, experimentPermID);
                ObjectOutputStream outputStream = null;
                try
                {
                    outputStream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file), 1024*1024));
                    outputStream.writeObject(dataSet);
                } catch (IOException ex)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                } finally
                {
                    IOUtils.closeQuietly(outputStream);
                }
            }

            @SuppressWarnings("unchecked")
            public DataSet<T> load(String experimentPermID)
            {
                File file = new File(store, experimentPermID);
                if (file.exists() == false)
                {
                    return null;
                }
                ObjectInputStream inputStream = null;
                try
                {
                    inputStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file), 1024*1024));
                    return (DataSet<T>) inputStream.readObject();
                } catch (Exception ex)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                } finally
                {
                    IOUtils.closeQuietly(inputStream);
                }
            }
            
        }

        private static final class ResultSetCache<T> 
        {
            private enum CachingType { MEMORY, FILE_SYSTEM }
            
            private final String name;
            private final IDataSetLoader<T> dataSetLoader;
            private final Map<String, DataSet<T>> cache;
            private final FileBasedDataSetLoader<T> fileBasedDataSetLoader;
            private final CachingType cachingType;
            
            ResultSetCache(String name, String typeOfCaching, IDataSetLoader<T> dataSetLoader)
            {
                this.name = name;
                cachingType = resolve(typeOfCaching);
                fileBasedDataSetLoader = new FileBasedDataSetLoader<T>(new File(name));
                this.dataSetLoader = dataSetLoader;
                cache = new HashMap<String, DataSet<T>>();
            }

            private CachingType resolve(String typeOfCaching)
            {
                CachingType[] values = CachingType.values();
                for (CachingType type : values)
                {
                    if (type.toString().equalsIgnoreCase(typeOfCaching))
                    {
                        return type;
                    }
                }
                return null;
            }
            
            DataSet<T> getDataSet(String experimentPermID)
            {
                long time = System.currentTimeMillis();
                DataSet<T> dataSet;
                if (cachingType == CachingType.MEMORY)
                {
                    dataSet = cache.get(experimentPermID);
                    if (dataSet == null)
                    {
                        DataSet<T> resultSet = dataSetLoader.load(experimentPermID);
                        dataSet = new ListBasedDataSet<T>(resultSet);
                        cache.put(experimentPermID, dataSet);
                    }
                } else if (cachingType == CachingType.FILE_SYSTEM)
                {
                    dataSet = fileBasedDataSetLoader.load(experimentPermID);
                    if (dataSet == null)
                    {
                        DataSet<T> resultSet = dataSetLoader.load(experimentPermID);
                        dataSet = new ListBasedDataSet<T>(resultSet);
                        fileBasedDataSetLoader.save(experimentPermID, dataSet);
                    }
                } else
                {
                    dataSet = new ListBasedDataSet<T>(dataSetLoader.load(experimentPermID));
                }
                System.out.println(System.currentTimeMillis() - time + "msec for " + name);
                return dataSet;
            }
        }
        
        private final IProteinQueryDAO dao;

        private final ResultSetCache<ProteinReferenceWithProbability> listProteinsByExperimentCache;

        private final ResultSetCache<ProteinReferenceWithPeptideSequence> listProteinsWithSequencesByExperimentCache;
        
        private final ResultSetCache<ProteinReferenceWithPeptideSequence> listProteinsWithPeptidesByExperiment;

        ProteinQueryDAO(final IProteinQueryDAO dao, String typeOfCaching)
        {
            System.getProperties().list(System.out);
            this.dao = dao;
            listProteinsByExperimentCache =
                    new ResultSetCache<ProteinReferenceWithProbability>("listProteinsByExperiment", typeOfCaching, 
                            new IDataSetLoader<ProteinReferenceWithProbability>()
                                {

                                    public DataSet<ProteinReferenceWithProbability> load(
                                            String experimentPermID)
                                    {
                                        return dao.listProteinsByExperiment(experimentPermID);
                                    }
                                });
            listProteinsWithSequencesByExperimentCache =
                    new ResultSetCache<ProteinReferenceWithPeptideSequence>(
                            "listProteinsWithSequencesByExperiment", typeOfCaching, 
                            new IDataSetLoader<ProteinReferenceWithPeptideSequence>()
                                {

                                    public DataSet<ProteinReferenceWithPeptideSequence> load(
                                            String experimentPermID)
                                    {
                                        return dao
                                                .listProteinsWithSequencesByExperiment(experimentPermID);
                                    }
                                });
            listProteinsWithPeptidesByExperiment =
                    new ResultSetCache<ProteinReferenceWithPeptideSequence>(
                            "listProteinsWithPeptidesByExperiment", typeOfCaching, 
                            new IDataSetLoader<ProteinReferenceWithPeptideSequence>()
                                {

                                    public DataSet<ProteinReferenceWithPeptideSequence> load(
                                            String experimentPermID)
                                    {
                                        return dao
                                                .listProteinsWithPeptidesByExperiment(experimentPermID);
                                    }
                                });
        }

        public void close()
        {
            dao.close();
        }

        public DataSet<ProbabilityFDRMapping> getProbabilityFDRMapping(long dataSetID)
        {
            return dao.getProbabilityFDRMapping(dataSetID);
        }

        public boolean isClosed()
        {
            return dao.isClosed();
        }

        public DataSet<String> listAbundanceRelatedSamplePermIDsByExperiment(String experimentPermID)
        {
            return dao.listAbundanceRelatedSamplePermIDsByExperiment(experimentPermID);
        }

        public DataSet<IdentifiedPeptide> listIdentifiedPeptidesByProtein(long proteinID)
        {
            return dao.listIdentifiedPeptidesByProtein(proteinID);
        }

        public DataSet<ProteinReferenceWithProbability> listProteinsByExperiment(
                String experimentPermID)
        {
            return listProteinsByExperimentCache.getDataSet(experimentPermID);
        }
        
        public DataSet<IdentifiedProtein> listProteinsByProteinReferenceAndExperiment(
                String experimentPermID, long proteinReferenceID)
        {
            return dao.listProteinsByProteinReferenceAndExperiment(experimentPermID,
                    proteinReferenceID);
        }

        public DataSet<Sequence> listProteinSequencesByProteinReference(long proteinReferenceID)
        {
            return dao.listProteinSequencesByProteinReference(proteinReferenceID);
        }

        public DataSet<ProteinReferenceWithPeptideSequence> listProteinsWithPeptidesByExperiment(
                String experimentPermID)
        {
            return listProteinsWithPeptidesByExperiment.getDataSet(experimentPermID);
        }

        public DataSet<ProteinReferenceWithProbabilityAndPeptide> listProteinsWithProbabilityAndPeptidesByExperiment(
                String experimentPermID)
        {
            return dao.listProteinsWithProbabilityAndPeptidesByExperiment(experimentPermID);
        }

        public DataSet<ProteinReferenceWithPeptideSequence> listProteinsWithSequencesByExperiment(
                String experimentPermID)
        {
            return listProteinsWithSequencesByExperimentCache.getDataSet(experimentPermID);
        }

        public DataSet<SampleAbundance> listSampleAbundanceByProtein(String experimentPermID,
                long proteinReferenceID)
        {
            return dao.listSampleAbundanceByProtein(experimentPermID, proteinReferenceID);
        }

        public ProteinReference tryToGetProteinReference(long proteinReferenceID)
        {
            return dao.tryToGetProteinReference(proteinReferenceID);
        }
    }
    
    DAOFactoryWithCache(IPhosphoNetXDAOFactory daoFactory, String typeOfCaching)
    {
        proteinQueryDAO = new ProteinQueryDAO(daoFactory.getProteinQueryDAO(), typeOfCaching);
    }

    public IProteinQueryDAO getProteinQueryDAO()
    {
        return proteinQueryDAO;
    }

}
