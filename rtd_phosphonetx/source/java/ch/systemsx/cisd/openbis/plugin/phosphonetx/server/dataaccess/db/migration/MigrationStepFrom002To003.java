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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.db.migration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.dbmigration.java.MigrationStepAdapter;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.Occurrence;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.OccurrenceUtil;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class MigrationStepFrom002To003 extends MigrationStepAdapter
{
    private static final Logger operationLog =
        LogFactory.getLogger(LogCategory.OPERATION, MigrationStepFrom002To003.class);
    
    @Override
    public void performPostMigration(SimpleJdbcTemplate simpleJdbcTemplate)
            throws DataAccessException
    {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        JdbcOperations jdbcOperations = simpleJdbcTemplate.getJdbcOperations();
        Map<Long, String> sequences = getSequences(jdbcOperations);
        Map<Long, List<String>> peptides = getPeptides(jdbcOperations);
        List<Object[]> coverageValues =
                calculateCoverageValues(jdbcOperations, sequences, peptides);
        operationLog.info("update " + coverageValues.size() + " identified proteins ("
                + (runtime.totalMemory() - runtime.freeMemory() - usedMemory) / 1024 / 1024 + " MB)");
        simpleJdbcTemplate.batchUpdate("update identified_proteins set coverage = ? where id = ?", coverageValues);
    }

    private List<Object[]> calculateCoverageValues(JdbcOperations jdbcOperations,
            final Map<Long, String> sequences, final Map<Long, List<String>> peptides)
    {
        final List<Object[]> result = new ArrayList<Object[]>();
        jdbcOperations.query(
                "select id, prot_id, sequ_id from identified_proteins where coverage is null",
                new ResultSetExtractor()
                    {
                        public Object extractData(ResultSet rs) throws SQLException,
                                DataAccessException
                        {
                            while (rs.next())
                            {
                                long id = rs.getLong(1);
                                long proteinID = rs.getLong(2);
                                long sequenceID = rs.getLong(3);
                                String sequence = sequences.get(sequenceID);
                                List<String> peptideSequences = peptides.get(proteinID);
                                double coverage = calculateCoverage(sequence, peptideSequences);
                                result.add(new Object[] { coverage, id });
                            }
                            return null;
                        }
                    });
        return result;
    }

    private Map<Long, List<String>> getPeptides(JdbcOperations jdbcOperations)
    {
        final Map<Long, List<String>> peptides = new HashMap<Long, List<String>>();
        jdbcOperations.query("select prot_id, sequence from peptides order by prot_id",
                new ResultSetExtractor()
                    {
                        public Object extractData(ResultSet rs) throws SQLException,
                                DataAccessException
                        {
                            long currentProteinID = -1;
                            List<String> set = null;
                            while (rs.next())
                            {
                                long proteinID = rs.getLong(1);
                                if (set == null || proteinID != currentProteinID)
                                {
                                    currentProteinID = proteinID;
                                    set = new ArrayList<String>();
                                    peptides.put(proteinID, set);
                                }
                                set.add(rs.getString(2));
                            }
                            return null;
                        }
                    });
        return peptides;
    }

    private Map<Long, String> getSequences(JdbcOperations jdbcOperations)
    {
        final Map<Long, String> sequences = new HashMap<Long, String>();
        jdbcOperations.query("select id, amino_acid_sequence from sequences",
                new ResultSetExtractor()
                    {
                        public Object extractData(ResultSet rs) throws SQLException,
                                DataAccessException
                        {
                            while (rs.next())
                            {
                                sequences.put(rs.getLong(1), rs.getString(2));
                            }
                            return null;
                        }
                    });
        return sequences;
    }

    private double calculateCoverage(String sequence, List<String> peptides)
    {
        Set<String> distinctPeptides = new HashSet<String>(peptides);
        List<Occurrence> list = OccurrenceUtil.getCoverage(sequence, distinctPeptides);
        int sumPeptides = 0;
        for (Occurrence occurrence : list)
        {
            sumPeptides += occurrence.getWord().length();
        }
        return sumPeptides / (double) sequence.length();
    }

}
