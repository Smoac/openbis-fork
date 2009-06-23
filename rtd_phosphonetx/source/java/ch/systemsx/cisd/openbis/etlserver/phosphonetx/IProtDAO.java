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

package ch.systemsx.cisd.openbis.etlserver.phosphonetx;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.DataSet;
import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.Update;

import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Experiment;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ModificationType;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinAnnotation;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Sample;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Sequence;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public interface IProtDAO extends BaseQuery
{
    @Select("select * from modification_types")
    public DataSet<ModificationType> listModificationTypes();
    
    @Select("select * from sequences where amino_acid_sequence = ?{1}")
    public Sequence tryToGetSequenceBySequenceString(String sequence);
    
    @Select("insert into sequences (amino_acid_sequence, checksum) "
            + "values (?{1.sequence}, ?{1.checksum}) returning id")
    public long createSequence(Sequence sequence);
    
    @Select("select * from experiments where perm_id = ?{1}")
    public Experiment tryToGetExperimentByPermID(String permID);
    
    @Select("insert into experiments (perm_id) values (?{1}) returning id")
    public long createExperiment(String experimentPermID);
    
    @Select("select * from samples where perm_id = ?{1}")
    public Sample tryToGetSampleByPermID(String permID);
    
    @Select("insert into samples (expe_id, perm_id) values (?{1}, ?{2}) returning id")
    public long createSample(long experimentID, String samplePermID);
    
    @Select("select * from data_sets where perm_id = ?{1}")
    public ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.DataSet tryToGetDataSetByPermID(String permID);
    
    @Select("insert into data_sets (expe_id, samp_id, perm_id) values (?{1}, ?{2}, ?{3}) returning id")
    public long createDataSet(long experimentID, Long sampleID, String dataSetPermID);
    
    @Select("insert into proteins (dase_id) values (?{1}) returning id")
    public long createProtein(long dataSetID);
    
    @Select("insert into peptides (prot_id, sequ_id, charge) values (?{1}, ?{2}, ?{3}) returning id")
    public long createPeptide(long proteinID, long sequenceID, int charge);
    
    @Update("insert into modifications (pept_id, moty_id, position, mass) "
            + "values (?{1}, ?{2}, ?{3}, ?{4})")
    public void createModification(long peptideID, long modificationTypeID, int position,
            double mass);

    @Update("insert into annotations (prot_id, protein_description, ipi_name, refseq_name, "
            + "swissprot_name, ensembl_name, trembl_name, locus_link_name, flybase) "
            + "values (?{1}, ?{2.description}, ?{2.ipiName}, ?{2.refseqName}, ?{2.swissprotName}, "
            + "?{2.ensemblName}, ?{2.tremblName}, ?{2.locusLinkName}, ?{2.flybase})")
    public void createAnnotion(long proteinID, ProteinAnnotation annotation);
}
