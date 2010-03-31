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

import it.unimi.dsi.fastutil.longs.LongSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import net.lemnik.eodsql.DataSet;

import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.support.lob.LobHandler;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IPhosphoNetXDAOFactory;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IProteinQueryDAO;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.IdentifiedPeptide;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.IdentifiedProtein;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProbabilityFDRMapping;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinAbundance;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReference;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReferenceWithProbability;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReferenceWithProbabilityAndPeptide;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReferenceWithProtein;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.SampleAbundance;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.Sequence;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class DAOFactoryWithCache implements IPhosphoNetXDAOFactory
{
    private static final class ProteinQueryDAO implements IProteinQueryDAO
    {
        private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, ProteinQueryDAO.class);
        
        private static final class DataSetProxy<T> implements DataSet<T>
        {
            private List<T> list;
            DataSetProxy(List<T> list)
            {
                this.list = list;
            }
            public void add(int index, T element)
            {
                list.add(index, element);
            }
            public boolean add(T o)
            {
                return list.add(o);
            }
            public boolean addAll(Collection<? extends T> c)
            {
                return list.addAll(c);
            }
            public boolean addAll(int index, Collection<? extends T> c)
            {
                return list.addAll(index, c);
            }
            public void clear()
            {
                list.clear();
            }
            public boolean contains(Object o)
            {
                return list.contains(o);
            }
            public boolean containsAll(Collection<?> c)
            {
                return list.containsAll(c);
            }
            @Override
            public boolean equals(Object o)
            {
                return list.equals(o);
            }
            public T get(int index)
            {
                return list.get(index);
            }
            @Override
            public int hashCode()
            {
                return list.hashCode();
            }
            public int indexOf(Object o)
            {
                return list.indexOf(o);
            }
            public boolean isEmpty()
            {
                return list.isEmpty();
            }
            public Iterator<T> iterator()
            {
                return list.iterator();
            }
            public int lastIndexOf(Object o)
            {
                return list.lastIndexOf(o);
            }
            public ListIterator<T> listIterator()
            {
                return list.listIterator();
            }
            public ListIterator<T> listIterator(int index)
            {
                return list.listIterator(index);
            }
            public T remove(int index)
            {
                return list.remove(index);
            }
            public boolean remove(Object o)
            {
                return list.remove(o);
            }
            public boolean removeAll(Collection<?> c)
            {
                return list.removeAll(c);
            }
            public boolean retainAll(Collection<?> c)
            {
                return list.retainAll(c);
            }
            public T set(int index, T element)
            {
                return list.set(index, element);
            }
            public int size()
            {
                return list.size();
            }
            public List<T> subList(int fromIndex, int toIndex)
            {
                return list.subList(fromIndex, toIndex);
            }
            public Object[] toArray()
            {
                return list.toArray();
            }
            public <X> X[] toArray(X[] a)
            {
                return list.toArray(a);
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
        
        private final IProteinQueryDAO dao;

        private final DatabaseConfigurationContext context;

        private final SimpleJdbcTemplate template;

        private NamedParameterJdbcTemplate namedTemplate;

        ProteinQueryDAO(final IProteinQueryDAO dao, DatabaseConfigurationContext context)
        {
            this.dao = dao;
            this.context = context;
            template = new SimpleJdbcTemplate(context.getDataSource());
            namedTemplate = new NamedParameterJdbcTemplate(context.getDataSource());
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
            long time = System.currentTimeMillis();
            final LobHandler lobHandler = context.getLobHandler();
            List<ProteinReferenceWithProbability> resultSet =
                    template.queryForObject(
                            "select blob from protein_view_cache where experiment_perm_id = ?",
                            new ParameterizedRowMapper<List<ProteinReferenceWithProbability>>()
                                {

                                    public List<ProteinReferenceWithProbability> mapRow(ResultSet rs,
                                            int rowNum) throws SQLException
                                    {
                                        return (List<ProteinReferenceWithProbability>) SerializationUtils
                                                .deserialize(lobHandler.getBlobAsBinaryStream(rs, 1));
                                    }
                                }, experimentPermID);
            operationLog.info("(" + (System.currentTimeMillis() - time) + "ms) listProteinsByExperiment");
            return new DataSetProxy<ProteinReferenceWithProbability>(resultSet);
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

        public DataSet<ProteinReferenceWithProbabilityAndPeptide> listProteinsWithProbabilityAndPeptidesByExperiment(
                String experimentPermID)
        {
            return dao.listProteinsWithProbabilityAndPeptidesByExperiment(experimentPermID);
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

        public byte[] tryToGetCachedProteinView(String experimentPermID)
        {
            return dao.tryToGetCachedProteinView(experimentPermID);
        }

        public DataSet<ProteinReferenceWithProtein> listProteinReferencesByExperiment(
                String experimentPermID)
        {
            long time = System.currentTimeMillis();
            try
            {
                List<ProteinReferenceWithProtein> list = template.query("select d.id , p.id , probability, coverage, "
                        + "pr.id, accession_number, description "
                        + "from protein_references as pr "
                        + "left join sequences as s on s.prre_id = pr.id "
                        + "left join identified_proteins as ip on ip.sequ_id = s.id "
                        + "left join proteins as p on ip.prot_id = p.id "
                        + "left join data_sets as d on p.dase_id = d.id "
                        + "left join experiments as e on d.expe_id = e.id where e.perm_id = ?",
                        new ParameterizedRowMapper<ProteinReferenceWithProtein>()
                            {

                                public ProteinReferenceWithProtein mapRow(ResultSet rs, int rowNum)
                                        throws SQLException
                                {
                                    ProteinReferenceWithProtein protein =
                                            new ProteinReferenceWithProtein();
                                    protein.setDataSetID(rs.getLong(1));
                                    protein.setProteinID(rs.getLong(2));
                                    protein.setProbability(rs.getDouble(3));
                                    protein.setCoverage(rs.getDouble(4));
                                    protein.setId(rs.getLong(5));
                                    protein.setAccessionNumber(rs.getString(6));
                                    protein.setDescription(rs.getString(7));
                                    return protein;
                                }
                            }, experimentPermID);
                return new DataSetProxy<ProteinReferenceWithProtein>(list);
//                return dao.listProteinReferencesByExperiment(experimentPermID);
            } finally
            {
                operationLog.info("(" + (System.currentTimeMillis() - time )+ "ms) listProteinReferenceByExperiment");
            }
        }

        public DataSet<ProteinAbundance> listProteinWithAbundanceByExperiment(
                LongSet proteinIDs)
        {
            long time = System.currentTimeMillis();
            try
            {
                List<ProteinAbundance> list = namedTemplate.query("select p.id, a.value, s.perm_id "
                        + "from proteins as p join abundances as a on p.id = a.prot_id "
                        + "left join samples as s on a.samp_id = s.id "
                        + "where p.id in (:ids)", Collections.singletonMap("ids", proteinIDs), new RowMapper()
                    {
                        
                        public Object mapRow(ResultSet rs, int rowNum) throws SQLException
                        {
                            ProteinAbundance proteinAbundance = new ProteinAbundance();
                            proteinAbundance.setId(rs.getLong(1));
                            proteinAbundance.setAbundance(rs.getDouble(2));
                            proteinAbundance.setSampleID(rs.getString(3));
                            return proteinAbundance;
                        }
                    });
                return new DataSetProxy<ProteinAbundance>(list);
            } finally
            {
                operationLog.info("(" + (System.currentTimeMillis() - time)
                        + "ms) listProteinWithAbundanceByExperiment");
            }
        }
    }
    
    private IProteinQueryDAO proteinQueryDAO;
    private DatabaseConfigurationContext context;

    DAOFactoryWithCache(IPhosphoNetXDAOFactory daoFactory, String typeOfCaching)
    {
        proteinQueryDAO = new ProteinQueryDAO(daoFactory.getProteinQueryDAO(), daoFactory.getContext());
        context = daoFactory.getContext();
    }

    public IProteinQueryDAO getProteinQueryDAO()
    {
        return proteinQueryDAO;
    }

    public DatabaseConfigurationContext getContext()
    {
        return context;
    }

}
