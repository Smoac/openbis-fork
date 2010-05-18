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

    @Select("insert into ACQUIRED_IMAGES (IMG_ID, THUMBNAIL_ID, CHANNEL_STACK_ID, CHANNEL_ID) values "
            + "(?{1.imageId}, ?{1.thumbnailId}, ?{1.channelStackId}, ?{1.channelId}) returning ID")
    public long addAcquiredImage(ImgAcquiredImageDTO acquiredImage);

    @Select("insert into CHANNELS (NAME, DESCRIPTION, WAVELENGTH, DS_ID, EXP_ID) values "
            + "(?{1.name}, ?{1.description}, ?{1.wavelength}, ?{1.datasetId}, ?{1.experimentId}) returning ID")
    public long addChannel(ImgChannelDTO channel);

    @Select("insert into CHANNEL_STACKS (X, Y, Z_in_M, T_in_SEC, DS_ID, SPOT_ID) values "
            + "(?{1.x}, ?{1.y}, ?{1.z}, ?{1.t}, ?{1.datasetId}, ?{1.spotId}) returning ID")
    public long addChannelStack(ImgChannelStackDTO channelStack);

    @Select("insert into CONTAINERS (PERM_ID, SPOTS_WIDTH, SPOTS_HEIGHT, EXPE_ID) values "
            + "(?{1.permId}, ?{1.spotWidth}, ?{1.spotHeight}, ?{1.experimentId}) returning ID")
    public long addContainer(ImgContainerDTO container);

    @Select("insert into DATA_SETS (PERM_ID, FIELDS_WIDTH, FIELDS_HEIGHT, CONT_ID) values "
            + "(?{1.permId}, ?{1.fieldsWidth}, ?{1.fieldsHeight}, ?{1.containerId}) returning ID")
    public long addDataset(ImgDatasetDTO dataset);

    @Select("insert into IMAGES (PATH, PAGE, COLOR) values "
            + "(?{1.filePath}, ?{1.page}, ?{1.colorComponentAsString}) returning ID")
    public long addImage(ImgImageDTO image);

    @Select("insert into SPOTS (PERM_ID, X, Y, CONT_ID) values "
            + "(?{1.permId}, ?{1.x}, ?{1.y}, ?{1.containerId}) returning ID")
    public long addSpot(ImgSpotDTO spot);

}
