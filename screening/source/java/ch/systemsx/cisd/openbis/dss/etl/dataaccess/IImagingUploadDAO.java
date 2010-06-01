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
import net.lemnik.eodsql.Update;

import ch.systemsx.cisd.bds.hcs.Location;

/**
 * @author Tomasz Pylak
 */
public interface IImagingUploadDAO extends TransactionQuery
{
    public static final int FETCH_SIZE = 1000;

    // select acquired_images.images.* from acquired_images
    @Select("select i.* from IMAGES i, ACQUIRED_IMAGES ai, CHANNEL_STACKS cs, SPOTS s "
            + "where                                                                "
            // where acquired_images.channel.id = ?{channelId}
            // and acquired_images.channel_stack.dataset.id = ?{datasetId}
            + "ai.CHANNEL_ID = ?{1} and cs.DS_ID = ?{2} and "
            // and acquired_images.channel_stack.x = tileX
            // and acquired_images.channel_stack.y = tileY
            // and acquired_images.channel_stack.spot.x = wellX
            // and acquired_images.channel_stack.spot.y = wellY
            + "cs.x = ?{3.x} and cs.y = ?{3.y} and s.x = ?{4.x} and s.y = ?{4.y} and "
            // joins
            + "ai.IMG_ID = i.ID and ai.CHANNEL_STACK_ID = cs.ID and cs.SPOT_ID = s.ID")
    public ImgImageDTO tryGetImage(long channelId, long datasetId, Location tileLocation,
            Location wellLocation);

    // simple getters

    @Select("select ID from EXPERIMENTS where PERM_ID = ?{1}")
    public Long tryGetExperimentIdByPermId(String experimentPermId);

    @Select("select ID from CONTAINERS where PERM_ID = ?{1}")
    public Long tryGetContainerIdPermId(String containerPermId);

    @Select("select * from DATA_SETS where PERM_ID = ?{1}")
    public ImgDatasetDTO tryGetDatasetByPermId(String datasetPermId);

    @Select("select * from CONTAINERS where ID = ?{1}")
    public ImgContainerDTO getContainerById(long containerId);

    @Select("select count(*) from CHANNELS where DS_ID = ?{1} or EXP_ID = ?{2}")
    public int countChannelByDatasetIdOrExperimentId(long datasetId, long experimentId);

    @Select(sql = "select id from CHANNELS where DS_ID = ?{1} or EXP_ID = ?{2} order by name", fetchSize = FETCH_SIZE)
    public long[] getChannelIdsByDatasetIdOrExperimentId(long datasetId, long experimentId);

    @Select(sql = "select * from CHANNELS where EXP_ID = ?{1} order by name", fetchSize = FETCH_SIZE)
    public List<ImgChannelDTO> getChannelsByExperimentId(long experimentId);

    @Select("select * from SPOTS where cont_id = ?{1}")
    public List<ImgSpotDTO> listSpots(long contId);

    // inserts

    @Select("insert into EXPERIMENTS (PERM_ID) values (?{1}) returning ID")
    public long addExperiment(String experimentPermId);

    @Select("insert into ACQUIRED_IMAGES (IMG_ID, THUMBNAIL_ID, CHANNEL_STACK_ID, CHANNEL_ID) values "
            + "(?{1.imageId}, ?{1.thumbnailId}, ?{1.channelStackId}, ?{1.channelId}) returning ID")
    public long addAcquiredImage(ImgAcquiredImageDTO acquiredImage);

    @Select("insert into CHANNELS (NAME, DESCRIPTION, WAVELENGTH, DS_ID, EXP_ID) values "
            + "(?{1.name}, ?{1.description}, ?{1.wavelength}, ?{1.datasetId}, ?{1.experimentId}) returning ID")
    public long addChannel(ImgChannelDTO channel);

    @Select("insert into CHANNEL_STACKS (X, Y, Z_in_M, T_in_SEC, DS_ID, SPOT_ID) values "
            + "(?{1.column}, ?{1.row}, ?{1.z}, ?{1.t}, ?{1.datasetId}, ?{1.spotId}) returning ID")
    public long addChannelStack(ImgChannelStackDTO channelStack);

    @Select("insert into CONTAINERS (PERM_ID, SPOTS_WIDTH, SPOTS_HEIGHT, EXPE_ID) values "
            + "(?{1.permId}, ?{1.numberOfColumns}, ?{1.numberOfRows}, ?{1.experimentId}) returning ID")
    public long addContainer(ImgContainerDTO container);

    @Select("insert into DATA_SETS (PERM_ID, IMAGES_DIRECTORY, FIELDS_WIDTH, FIELDS_HEIGHT, CONT_ID) values "
            + "(?{1.permId}, ?{1.imagesDirectoryPath}, ?{1.fieldNumberOfColumns}, "
            + "?{1.fieldNumberOfRows}, ?{1.containerId}) returning ID")
    public long addDataset(ImgDatasetDTO dataset);

    @Select("insert into IMAGES (PATH, PAGE, COLOR) values "
            + "(?{1.filePath}, ?{1.page}, ?{1.colorComponentAsString}) returning ID")
    public long addImage(ImgImageDTO image);

    @Select("insert into SPOTS (X, Y, CONT_ID) values "
            + "(?{1.column}, ?{1.row}, ?{1.containerId}) returning ID")
    public long addSpot(ImgSpotDTO spot);

    // updates

    @Update("update CHANNELS "
            + "set DESCRIPTION = ?{1.description}, WAVELENGTH = ?{1.wavelength} "
            + "where ID = ?{1.id}")
    public void updateChannel(ImgChannelDTO channel);

}
