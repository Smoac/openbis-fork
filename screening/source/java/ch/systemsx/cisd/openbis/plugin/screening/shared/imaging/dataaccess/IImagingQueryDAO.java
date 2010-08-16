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

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess;

import java.util.List;

import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.TransactionQuery;
import net.lemnik.eodsql.Update;

import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.ByteArrayMapper;

/**
 * @author Tomasz Pylak
 */
public interface IImagingQueryDAO extends TransactionQuery
{
    public static final int FETCH_SIZE = 1000;

    public static final String SQL_IMAGE =
            ", CHANNEL_STACKS cs, SPOTS s "
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
                    + "ai.CHANNEL_STACK_ID = cs.ID and cs.SPOT_ID = s.ID "
                    // TODO 2010-07-27, Tomasz Pylak: select the first image if there are many time
                    // points or depth scans.
                    // Should be deleted when support for time points will be added!
                    + "order by cs.T_in_SEC, cs.Z_in_M limit 1";

    // select acquired_images.images.* from acquired_images
    @Select("select i.* " + "from ACQUIRED_IMAGES as ai join IMAGES as i on ai.IMG_ID = i.ID "
            + SQL_IMAGE)
    public ImgImageDTO tryGetImage(long channelId, long datasetId, Location tileLocation,
            Location wellLocation);

    // select acquired_images.thumbnail.* from acquired_images
    @Select("select i.* "
            + "from ACQUIRED_IMAGES as ai join IMAGES as i on ai.THUMBNAIL_ID = i.ID " + SQL_IMAGE)
    public ImgImageDTO tryGetThumbnail(long channelId, long datasetId, Location tileLocation,
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

    @Select("select name from CHANNELS where DS_ID = ?{1} or EXP_ID = ?{2} order by NAME")
    public String[] getChannelNamesByDatasetIdOrExperimentId(long datasetId, long experimentId);

    @Select(sql = "select id from CHANNELS where DS_ID = ?{1} or EXP_ID = ?{2} order by NAME", fetchSize = FETCH_SIZE)
    public long[] getChannelIdsByDatasetIdOrExperimentId(long datasetId, long experimentId);

    @Select(sql = "select * from CHANNELS where EXP_ID = ?{1} order by name", fetchSize = FETCH_SIZE)
    public List<ImgChannelDTO> getChannelsByExperimentId(long experimentId);

    @Select("select * from SPOTS where cont_id = ?{1}")
    public List<ImgSpotDTO> listSpots(long contId);

    @Select("select * from FEATURE_DEFS where DS_ID = ?{1}")
    public List<ImgFeatureDefDTO> listFeatureDefsByDataSetId(long dataSetId);

    @Select(sql = "select * from FEATURE_VALUES where FD_ID = ?{1.id} order by T_in_SEC, Z_in_M", resultSetBinding = FeatureVectorDataObjectBinding.class)
    public List<ImgFeatureValuesDTO> getFeatureValues(ImgFeatureDefDTO featureDef);

    // generate ids

    @Select("select nextval('images_id_seq')")
    public long createImageId();

    @Select("select nextval('channel_stacks_id_seq')")
    public long createChannelStackId();

    // batch updates

    @Update(sql = "insert into CHANNEL_STACKS (ID, X, Y, Z_in_M, T_in_SEC, DS_ID, SPOT_ID) values "
            + "(?{1.id}, ?{1.column}, ?{1.row}, ?{1.z}, ?{1.t}, ?{1.datasetId}, ?{1.spotId})", batchUpdate = true)
    public void addChannelStacks(List<ImgChannelStackDTO> channelStacks);

    @Update(sql = "insert into IMAGES (ID, PATH, PAGE, COLOR) values "
            + "(?{1.id}, ?{1.filePath}, ?{1.page}, ?{1.colorComponentAsString})", batchUpdate = true)
    public void addImages(List<ImgImageDTO> images);

    @Update(sql = "insert into ACQUIRED_IMAGES (IMG_ID, THUMBNAIL_ID, CHANNEL_STACK_ID, CHANNEL_ID) values "
            + "(?{1.imageId}, ?{1.thumbnailId}, ?{1.channelStackId}, ?{1.channelId})", batchUpdate = true)
    public void addAcquiredImages(List<ImgAcquiredImageDTO> acquiredImages);

    // inserts

    @Select("insert into EXPERIMENTS (PERM_ID) values (?{1}) returning ID")
    public long addExperiment(String experimentPermId);

    @Select("insert into CHANNELS (NAME, DESCRIPTION, WAVELENGTH, DS_ID, EXP_ID) values "
            + "(?{1.name}, ?{1.description}, ?{1.wavelength}, ?{1.datasetId}, ?{1.experimentId}) returning ID")
    public long addChannel(ImgChannelDTO channel);

    @Select("insert into CONTAINERS (PERM_ID, SPOTS_WIDTH, SPOTS_HEIGHT, EXPE_ID) values "
            + "(?{1.permId}, ?{1.numberOfColumns}, ?{1.numberOfRows}, ?{1.experimentId}) returning ID")
    public long addContainer(ImgContainerDTO container);

    @Select("insert into DATA_SETS (PERM_ID, FIELDS_WIDTH, FIELDS_HEIGHT, CONT_ID, IS_MULTIDIMENSIONAL) values "
            + "(?{1.permId}, ?{1.fieldNumberOfColumns}, "
            + "?{1.fieldNumberOfRows}, ?{1.containerId}, ?{1.isMultidimensional}) returning ID")
    public long addDataset(ImgDatasetDTO dataset);

    @Select("insert into SPOTS (X, Y, CONT_ID, PERM_ID) values "
            + "(?{1.column}, ?{1.row}, ?{1.containerId}, ?{1.permId}) returning ID")
    public long addSpot(ImgSpotDTO spot);

    @Select("insert into FEATURE_DEFS (NAME, DESCRIPTION, DS_ID) values "
            + "(?{1.name}, ?{1.description}, ?{1.dataSetId}) RETURNING ID")
    public long addFeatureDef(ImgFeatureDefDTO featureDef);

    @Select(sql = "insert into FEATURE_VALUES (VALUES, Z_in_M, T_in_SEC, FD_ID) values "
            + "(?{1.byteArray}, ?{1.z}, ?{1.t}, ?{1.featureDefId}) RETURNING ID", parameterBindings =
        { ByteArrayMapper.class })
    public long addFeatureValues(ImgFeatureValuesDTO featureValues);

    // updates

    @Update("update CHANNELS "
            + "set DESCRIPTION = ?{1.description}, WAVELENGTH = ?{1.wavelength} "
            + "where ID = ?{1.id}")
    public void updateChannel(ImgChannelDTO channel);

    @Select("select ID from CHANNELS where (DS_ID = ?{1} or EXP_ID = ?{2}) and NAME = upper(?{3})")
    public Long tryGetChannelIdByChannelNameDatasetIdOrExperimentId(long id, long experimentId,
            String chosenChannel);

}
