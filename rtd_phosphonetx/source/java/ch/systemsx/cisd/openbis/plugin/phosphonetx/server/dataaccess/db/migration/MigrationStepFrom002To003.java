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

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;

import sun.print.PSPrinterJob.EPSPrinter;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.dbmigration.java.MigrationStepAdapter;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.Occurrence;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.OccurrenceUtil;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReferenceWithProbability;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class MigrationStepFrom002To003 extends MigrationStepAdapter
{
    private static final long MB = 1024 * 1024;
    private static final Logger operationLog =
        LogFactory.getLogger(LogCategory.OPERATION, MigrationStepFrom002To003.class);
    
    @Override
    public void performPostMigration(SimpleJdbcTemplate simpleJdbcTemplate)
            throws DataAccessException
    {
        List<Object[]> coverageValues = calculateCoverageValues(simpleJdbcTemplate);
        operationLog.info("update " + coverageValues.size() + " identified proteins");
        simpleJdbcTemplate.batchUpdate("update identified_proteins set coverage = ? where id = ?", coverageValues);
        createProteinView(simpleJdbcTemplate);
    }
    
    private List<Object[]> calculateCoverageValues(SimpleJdbcTemplate simpleJdbcTemplate)
    {
        logMemory();
        JdbcOperations jdbcOperations = simpleJdbcTemplate.getJdbcOperations();
        final Map<Long, List<String>> peptides = getPeptides(jdbcOperations);
        logMemory();
        final List<Object[]> values = new ArrayList<Object[]>();
        jdbcOperations.query(
                "select ip.id, ip.prot_id, s.amino_acid_sequence " +
                "from identified_proteins as ip " +
                "join sequences as s on ip.sequ_id = s.id where coverage is null",
                new ResultSetExtractor()
                {
                    public Object extractData(ResultSet rs) throws SQLException,
                    DataAccessException
                    {
                        while (rs.next())
                        {
                            long id = rs.getLong(1);
                            long proteinID = rs.getLong(2);
                            String sequence = rs.getString(3);
                            List<String> peptideSequences = peptides.get(proteinID);
                            double coverage = calculateCoverage(sequence, peptideSequences);
                            values.add(new Object[] { coverage, id });
                        }
                        return null;
                    }
                });
        logMemory();
        return values;
    }
    
    private void createProteinView(SimpleJdbcTemplate simpleJdbcTemplate)
    {
        List<String> experiments =
                simpleJdbcTemplate.query("select perm_id from experiments",
                        new ParameterizedRowMapper<String>()
                            {
                                public String mapRow(ResultSet rs, int rowNum) throws SQLException
                                {
                                    return rs.getString(1);
                                }
                            });
        operationLog.info("create protein views for " + experiments.size() + " experiments");
        logMemory();
        JdbcOperations jdbcOperations = simpleJdbcTemplate.getJdbcOperations();
        Object[] arguments = new Object[1];
        for (final String experiment : experiments)
        {
            arguments[0] = experiment;
            List<ProteinReferenceWithProbability> rows = simpleJdbcTemplate.query(
                    "select pr.id, pr.accession_number, pr.description, d.id, p.probability, "
                            + "ip.coverage, a.value, samples.perm_id "
                            + "from identified_proteins as ip "
                            + "left join proteins as p on ip.prot_id = p.id "
                            + "left join data_sets as d on p.dase_id = d.id "
                            + "left join experiments as e on d.expe_id = e.id "
                            + "left join sequences as s on ip.sequ_id = s.id "
                            + "left join protein_references as pr on s.prre_id = pr.id "
                            + "left join abundances as a on p.id = a.prot_id "
                            + "left join samples on a.samp_id = samples.id "
                            + "where e.perm_id = ?", new ParameterizedRowMapper<ProteinReferenceWithProbability>()
                                {

                                    public ProteinReferenceWithProbability mapRow(ResultSet rs,
                                            int rowNum) throws SQLException
                                    {
                                        ProteinReferenceWithProbability protein = new ProteinReferenceWithProbability();
                                        protein.setId(rs.getLong(1));
                                        protein.setAccessionNumber(rs.getString(2));
                                        protein.setDescription(rs.getString(3));
                                        protein.setDataSetID(rs.getLong(4));
                                        protein.setProbability(rs.getDouble(5));
                                        protein.setCoverage(rs.getDouble(6));
                                        protein.setAbundance(getDoubleOrNull(rs, 7));
                                        protein.setSamplePermID(rs.getString(8));
                                        return protein;
                                    }
                                }, experiment);
            operationLog.info("insert " + rows.size() + " rows into protein_view_cache for experiment "
                    + experiment);
            byte[] serializedRows = SerializationUtils.serialize((Serializable) rows);
            simpleJdbcTemplate.update("insert into protein_view_cache (experiment_perm_id, blob) "
                    + "values(?, ?)", experiment, serializedRows);
            logMemory();
        }
    }
    private Double getDoubleOrNull(ResultSet rs, int index) throws SQLException
    {
        double result = rs.getDouble(index);
        return rs.wasNull() ? null : result;
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
                            ArrayList<String> list = null;
                            while (rs.next())
                            {
                                long proteinID = rs.getLong(1);
                                if (list == null || proteinID != currentProteinID)
                                {
                                    if (list != null)
                                    {
                                        list.trimToSize();
                                    }
                                    currentProteinID = proteinID;
                                    list = new ArrayList<String>();
                                    peptides.put(proteinID, list);
                                }
                                list.add(rs.getString(2));
                            }
                            return null;
                        }
                    });
        return peptides;
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

    private void logMemory()
    {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / MB;
        operationLog.info(usedMemory + " MB used, " + runtime.totalMemory() / MB + " MB total");
    }
}

