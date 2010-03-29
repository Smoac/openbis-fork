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

import java.util.List;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.Update;

import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Database;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Experiment;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinReference;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Sample;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Sequence;

/**
 * @author Franz-Josef Elmer
 */
public interface IProtDAO extends BaseQuery
{
    @Select("select * from experiments where perm_id = ?{1}")
    public Experiment tryToGetExperimentByPermID(String permID);

    @Select("insert into experiments (perm_id) values (?{1}) returning id")
    public long createExperiment(String experimentPermID);

    @Select("select * from samples where perm_id = ?{1}")
    public Sample tryToGetSampleByPermID(String permID);

    @Select("insert into samples (expe_id, perm_id) values (?{1}, ?{2}) returning id")
    public long createSample(long experimentID, String samplePermID);

    @Select("select * from databases where name_and_version = ?{1}")
    public Database tryToGetDatabaseByName(String name);

    @Select("insert into databases (name_and_version) values (?{1}) returning id")
    public long createDatabase(String databaseName);

    @Select("select * from data_sets where perm_id = ?{1}")
    public ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.DataSet tryToGetDataSetByPermID(
            String permID);

    @Select("insert into data_sets (expe_id, perm_id, db_id) values (?{1}, ?{2}, ?{3}) returning id")
    public long createDataSet(long experimentID, String dataSetPermID,
            long databaseID);

    @Update("insert into probability_fdr_mappings (dase_id, probability, false_discovery_rate) "
            + "values (?{1}, ?{2}, ?{3})")
    public void createProbabilityToFDRMapping(long dataSetID, double probability,
            double falseDiscoveryRate);

    @Select("insert into proteins (dase_id, probability) values (?{1}, ?{2}) returning id")
    public long createProtein(long dataSetID, double probability);

    @Select("insert into peptides (prot_id, sequence, charge) values (?{1}, ?{2}, ?{3}) returning id")
    public long createPeptide(long proteinID, String sequence, int charge);

    @Select("insert into modified_peptides (pept_id, nterm_mass, cterm_mass) values (?{1}, ?{2}, ?{3}) returning id")
    public long createModifiedPeptide(long peptideID, double nTermMass, double cTermMass);

    @Update("insert into modifications (mope_id, pos, mass) values (?{1}, ?{2}, ?{3})")
    public void createModification(long modPeptideID, int position, double mass);

    @Select("select * from protein_references where accession_number = ?{1}")
    public ProteinReference tryToGetProteinReference(String accessionNumber);

    @Select("insert into protein_references (accession_number, description) values (?{1}, ?{2}) returning id")
    public long createProteinReference(String accessionNumber, String description);

    @Update("update protein_references set description = ?{2} where id = ?{1}")
    public void updateProteinReferenceDescription(long proteinReferenceID, String description);

    @Select("select * from sequences where prre_id = ?{1} and db_id = ?{2}")
    public List<Sequence> tryToGetSequencesByReferenceAndDatabase(long referenceID, long databaseID);

    @Select("insert into sequences (db_id, prre_id, amino_acid_sequence, checksum) "
            + "values (?{1.databaseID}, ?{1.proteinReferenceID}, ?{1.sequence}, ?{1.checksum}) "
            + "returning id")
    public long createSequence(Sequence sequence);

    @Update("insert into identified_proteins (prot_id, sequ_id, coverage) values (?{1}, ?{2}, ?{3})")
    public void createIdentifiedProtein(long proteinID, long sequenceID, double coverage);

    @Update("insert into abundances (prot_id, samp_id, value) values (?{1}, ?{2}, ?{3})")
    public void createAbundance(long proteinID, long sampleID, double value);

}
