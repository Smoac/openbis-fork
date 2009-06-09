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

import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ModificationType;
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
    public Sequence tryToGetBySequence(String sequence);
    
    @Select("insert into sequences (amino_acid_sequence, checksum) "
            + "values (?{1.sequence}, ?{1.checksum}) returning id")
    public long createSequence(Sequence sequence);
    
    @Select("insert into proteins (data_set_code) values (?{1}) returning id")
    public long createProtein(String dataSetCode);
    
    @Select("insert into peptides (prot_id, sequ_id) values (?{1}, ?{2}) returning id")
    public long createPeptide(long proteinID, long sequenceID);
    
    @Select("insert into modified_peptides (pept_id, charge) values (?{1}, ?{2}) returning id")
    public long createModifiedPeptide(long peptideID, int charge);
    
    @Update("insert into modifications (mope_id, moty_id, position, mass) "
            + "values (?{1}, ?{2}, ?{3}, ?{4})")
    public void createModification(long modifiedPeptideID, long modificationTypeID, int position,
            double mass);
}
