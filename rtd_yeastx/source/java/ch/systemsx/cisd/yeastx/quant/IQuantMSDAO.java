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

package ch.systemsx.cisd.yeastx.quant;

import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.Update;

import ch.systemsx.cisd.yeastx.db.generic.IDMGenericDAO;
import ch.systemsx.cisd.yeastx.quant.dto.MSConcentrationDTO;
import ch.systemsx.cisd.yeastx.quant.dto.MSQuantificationDTO;

/**
 * Creates MS Quantifications records.
 * 
 * @author Tomasz Pylak
 */
public interface IQuantMSDAO extends IDMGenericDAO
{
    @Select(sql = "insert into MS_QUANTIFICATIONS "
            + "(  EXPE_ID, DS_ID, SOURCE, VALID, COMMENT, REGISTRATOR, REGISTRATION_DATE ) "
            + "values (?{1}, ?{2}, ?{3.source}, ?{3.valid}, ?{3.comment}, "
            + "        ?{3.registrator}, ?{3.registrationDate} ) returning ID")
    public long addQuantification(long experimentId, long datasetId,
            MSQuantificationDTO quantification);

    @Select(sql = "insert into MS_QUANT_CONCENTRATIONS "
            + "( MS_QUANTIFICATION_ID, PARENT_DS_PERM_ID, AMOUNT, UNIT, VALID, COMMENT, "
            + "  RETENTION_TIME, Q1, Q3, INTERNAL_STANDARD ) "
            + "values (?{1}, ?{2.parentDatasetCode}, ?{2.amount}, ?{2.unit}, ?{2.valid}, ?{2.comment}, "
            + "        ?{2.retentionTime}, ?{2.q1}, ?{2.q3}, ?{2.internalStandard} ) returning ID")
    public long addConcentration(long quantMSId, MSConcentrationDTO concentration);

    @Update(sql = "insert into MS_QUANT_COMPOUNDS (MS_QUANT_CONCENTRATION_ID, COMPOUND_ID) "
            + "values (?{1}, ?{2})", batchUpdate = true)
    public void addCompoundIds(long concentrationId, Iterable<Long> compoundIds);
}
