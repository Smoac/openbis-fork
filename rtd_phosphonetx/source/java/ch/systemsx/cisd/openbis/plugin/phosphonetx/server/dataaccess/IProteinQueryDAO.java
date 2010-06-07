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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess;


import it.unimi.dsi.fastutil.longs.LongSet;
import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.DataSet;
import net.lemnik.eodsql.Select;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.LongSetMapper;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.IdentifiedPeptide;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.IdentifiedProtein;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.IndistinguishableProtein;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProbabilityFDRMapping;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinAbundance;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReference;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReferenceWithProbabilityAndPeptide;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReferenceWithProtein;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.SampleAbundance;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.Sequence;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public interface IProteinQueryDAO extends BaseQuery
{
    @Select(sql = "select * from probability_fdr_mappings where dase_id = ?{1}", disconnected = true)
    public DataSet<ProbabilityFDRMapping> getProbabilityFDRMapping(long dataSetID);
    
    @Select("select d.id as data_set_id, p.id as protein_id, probability, coverage, "
            + "pr.id, accession_number, description from protein_references as pr "
            + "left join sequences as s on s.prre_id = pr.id "
            + "left join identified_proteins as ip on ip.sequ_id = s.id "
            + "left join proteins as p on ip.prot_id = p.id "
            + "left join data_sets as d on p.dase_id = d.id "
            + "left join experiments as e on d.expe_id = e.id where e.perm_id = ?{1} and ip.is_primary = 't'")
    public DataSet<ProteinReferenceWithProtein> listProteinReferencesByExperiment(
            String experimentPermID);
    
    @Select(sql = "select p.id, a.value, s.perm_id "
            + "from proteins as p join abundances as a on p.id = a.prot_id "
            + "left join samples as s on a.samp_id = s.id "
            + "where p.id = any (?{1})", parameterBindings = { LongSetMapper.class })
    public DataSet<ProteinAbundance> listProteinWithAbundanceByExperiment(LongSet proteinIDs);
    
    @Select(sql = "select p.dase_id as data_set_id, p.probability, s.prre_id as id, pe.sequence "
            + "from identified_proteins as ip left join sequences as s on ip.sequ_id = s.id "
            + "left join proteins as p on ip.prot_id = p.id "
            + "left join peptides as pe on pe.prot_id = p.id "
            + "left join data_sets as d on p.dase_id = d.id "
            + "left join experiments as e on d.expe_id = e.id " 
            + "where e.perm_id = ?{1} and ip.is_primary = 't'")
    public DataSet<ProteinReferenceWithProbabilityAndPeptide> listProteinsWithProbabilityAndPeptidesByExperiment(
            String experimentPermID);
    
    @Select("select distinct s.perm_id "
            + "from abundances as a left join proteins as p on a.prot_id = p.id "
            + "                     left join data_sets as d on p.dase_id = d.id "
            + "                     left join experiments as e on d.expe_id = e.id "
            + "                     left join samples as s on a.samp_id = s.id "
            + "where e.perm_id = ?{1} order by s.perm_id")
    public DataSet<String> listAbundanceRelatedSamplePermIDsByExperiment(String experimentPermID);
    
    @Select("select * from protein_references where id = ?{1}")
    public ProteinReference tryToGetProteinReference(long proteinReferenceID);
    
    @Select("select s.id, db_id, amino_acid_sequence, name_and_version "
            + "from sequences as s join databases as d on s.db_id = d.id "
            + "where s.prre_id = ?{1} order by name_and_version")
    public DataSet<Sequence> listProteinSequencesByProteinReference(long proteinReferenceID);
    
    @Select("select ds.id as data_set_id, ds.perm_id as data_set_perm_id, p.id as protein_id, "
            + "probability, coverage, count(pe.id) as peptide_count, amino_acid_sequence, s.db_id, name_and_version "
            + "from data_sets as ds join experiments as e on ds.expe_id = e.id "
            + "                     join proteins as p on p.dase_id = ds.id "
            + "                     join identified_proteins as ip on ip.prot_id = p.id "
            + "                     join sequences as s on ip.sequ_id = s.id "
            + "                     join databases as db on s.db_id = db.id "
            + "                     left join peptides as pe on pe.prot_id = p.id "
            + "where s.prre_id = ?{2} and e.perm_id = ?{1} and ip.is_primary = 't' "
            + "group by data_set_id, data_set_perm_id, protein_id, probability, coverage, "
            + "         amino_acid_sequence, s.db_id, name_and_version order by data_set_perm_id")
    public DataSet<IdentifiedProtein> listProteinsByProteinReferenceAndExperiment(
            String experimentPermID, long proteinReferenceID);
    
    @Select("select * from peptides where prot_id = ?{1}")
    public DataSet<IdentifiedPeptide> listIdentifiedPeptidesByProtein(long proteinID);
    
    @Select("select accession_number, description, amino_acid_sequence, coverage "
            + "from identified_proteins as ip join sequences as s on ip.sequ_id = s.id "
            + "                               join protein_references as pr on s.prre_id = pr.id "
            + "where ip.prot_id = ?{1} and ip.is_primary = 'f'")
    public DataSet<IndistinguishableProtein> listIndistinguishableProteinsByProteinID(long proteinID);
    
    @Select("select distinct a.id, samples.perm_id, value "
            + "from abundances as a left join proteins as p on a.prot_id = p.id "
            + "                     left join data_sets as d on p.dase_id = d.id "
            + "                     left join experiments as e on d.expe_id = e.id "
            + "                     left join identified_proteins as ip on ip.prot_id = p.id "
            + "                     left join sequences as s on ip.sequ_id = s.id "
            + "                     left join samples on a.samp_id = samples.id "
            + "where e.perm_id = ?{1} and s.prre_id = ?{2} and ip.is_primary = 't'")
    public DataSet<SampleAbundance> listSampleAbundanceByProtein(String experimentPermID,
            long proteinReferenceID);
    
}
