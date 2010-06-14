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

import java.util.List;

import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.TransactionQuery;

import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.ByteArrayMapper;

/**
 * DAO for interacting with feature vector tables.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IFeatureVectorDAO extends TransactionQuery
{
    public static final int FETCH_SIZE = 1000;

    @Select("SELECT * from FEATURE_DEFS where PERM_ID = ?{1}")
    public ImgImageDTO tryGetFeatureDef(long channelId, long datasetId, Location tileLocation,
            Location wellLocation);

    // Simple Getters
    @Select("SELECT * from FEATURE_DEFS where DS_ID = ?{1}")
    public List<ImgFeatureDefDTO> listFeatureDefsByDataSetId(long dataSetId);

    @Select("SELECT * from FEATURE_VALUES where FD_ID = ?{1.id}")
    public List<ImgFeatureValuesDTO> getFeatureValues(ImgFeatureDefDTO featureDev);

    // Inserts
    @Select("INSERT into FEATURE_DEFS (NAME, DESCRIPTION, DS_ID) values "
            + "(?{1.name}, ?{1.description}, ?{1.dataSetId}) RETURNING ID")
    public long addFeatureDef(ImgFeatureDefDTO featureVectorDef);

    @Select(value = "INSERT into FEATURE_VALUES (BYTEA, Z_in_M, T_in_SEC, FD_ID, DS_ID) values "
            + "(?{1.values}, ?{1.z}, ?{1.t}, ?{1.featureDefId}, ?{1.dataSetId}) RETURNING ID", parameterBindings =
        { ByteArrayMapper.class })
    public long addFeatureValues(ImgFeatureValuesDTO featureVectorDef);
}
