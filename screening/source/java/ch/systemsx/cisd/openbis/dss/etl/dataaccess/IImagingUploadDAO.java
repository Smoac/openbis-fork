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

package ch.systemsx.cisd.openbis.dss.etl.dataaccess;

import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.TransactionQuery;

/**
 * @author Tomasz Pylak
 */
public interface IImagingUploadDAO extends TransactionQuery
{
    @Select("select ID from EXPERIMENTS where PERM_ID = ?{1}")
    public Long tryGetExperimentIdByPermId(String experimentPermId);

    @Select("insert into EXPERIMENTS (PERM_ID) values (?{1}) returning ID")
    public long addExperiment(String experimentPermId);

    @Select("select * from CONTAINERS where PERM_ID = ?{1}")
    public ImgContainerDTO tryGetContainerByPermId(String containerPermId);

}
