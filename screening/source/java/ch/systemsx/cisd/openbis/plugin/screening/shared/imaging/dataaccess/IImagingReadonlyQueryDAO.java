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

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.Select;

import ch.systemsx.cisd.bds.hcs.Location;

/**
 * Operations on imaging database which are read-only.<br>
 * It is recommended to create one instance of this interface, assign it to a static class field and
 * used everywhere in the application. In this way there will be no problem with closing the
 * database connection.
 * 
 * @author Tomasz Pylak
 */
public interface IImagingReadonlyQueryDAO extends BaseQuery
{
    public static final int FETCH_SIZE = 1000;

    public static final String SQL_IMAGE =
            "select i.* from CHANNEL_STACKS, SPOTS, ACQUIRED_IMAGES, IMAGES as i "
                    + "where                                                                "
                    + "ACQUIRED_IMAGES.CHANNEL_ID = ?{1} and CHANNEL_STACKS.DS_ID = ?{2} and "
                    + "CHANNEL_STACKS.x = ?{3.x} and CHANNEL_STACKS.y = ?{3.y} and "
                    + "SPOTS.x = ?{4.x} and SPOTS.y = ?{4.y} and "
                    // joins
                    + "ACQUIRED_IMAGES.CHANNEL_STACK_ID = CHANNEL_STACKS.ID and "
                    + "CHANNEL_STACKS.SPOT_ID = SPOTS.ID ";

    public static final String SQL_NO_MULTIDIMENTIONAL_DATA_COND =
            " order by CHANNEL_STACKS.T_in_SEC, CHANNEL_STACKS.Z_in_M limit 1";

    /**
     * @return an image for the specified chanel, well and tile. If many images for different
     *         timepoints or depths exist, the first one is returned.
     */
    @Select(SQL_IMAGE + " and ACQUIRED_IMAGES.IMG_ID = i.ID " + SQL_NO_MULTIDIMENTIONAL_DATA_COND)
    public ImgImageDTO tryGetImage(long channelId, long datasetId, Location tileLocation,
            Location wellLocation);

    /**
     * @return a thumbnail for the specified chanel, well and tile. If many images for different
     *         timepoints or depths exist, the first one is returned.
     */
    @Select(SQL_IMAGE + " and ACQUIRED_IMAGES.THUMBNAIL_ID = i.ID "
            + SQL_NO_MULTIDIMENTIONAL_DATA_COND)
    public ImgImageDTO tryGetThumbnail(long channelId, long datasetId, Location tileLocation,
            Location wellLocation);

    /** @return an image for the specified chanel and channel stack or null */
    @Select("select i.* from IMAGES as i "
            + "join ACQUIRED_IMAGES on ACQUIRED_IMAGES.IMG_ID = i.ID "
            + "join CHANNEL_STACKS on ACQUIRED_IMAGES.CHANNEL_STACK_ID = CHANNEL_STACKS.ID "
            + "where                                                                "
            + "ACQUIRED_IMAGES.CHANNEL_ID = ?{1} and CHANNEL_STACKS.ID = ?{2} and "
            + "CHANNEL_STACKS.DS_ID = ?{3}")
    // The condition on dataset equality is just to ensure that the stack channel belongs the right
    // dataset.
    public ImgImageDTO tryGetImage(long channelId, Long channelStackId, long datasetId);

    /** @return a thumbnail for the specified chanel and channel stack or null */
    @Select("select i.* from IMAGES as i "
            + "join ACQUIRED_IMAGES on ACQUIRED_IMAGES.THUMBNAIL_ID = i.ID "
            + "join CHANNEL_STACKS on ACQUIRED_IMAGES.CHANNEL_STACK_ID = CHANNEL_STACKS.ID "
            + "where                                                                "
            + "ACQUIRED_IMAGES.CHANNEL_ID = ?{1} and CHANNEL_STACKS.ID = ?{2} and "
            + "CHANNEL_STACKS.DS_ID = ?{3}")
    // The condition on dataset equality is just to ensure that the stack channel belongs the right
    // dataset.
    public ImgImageDTO tryGetThumbnail(long channelId, Long channelStackId, long datasetId);

    // simple getters

    @Select("select * from EXPERIMENTS where PERM_ID = ?{1}")
    public ImgExperimentDTO tryGetExperimentByPermId(String experimentPermId);

    @Select("select * from EXPERIMENTS where ID = ?{1}")
    public ImgExperimentDTO tryGetExperimentById(long experimentId);

    @Select("select ID from CONTAINERS where PERM_ID = ?{1}")
    public Long tryGetContainerIdPermId(String containerPermId);

    @Select("select * from DATA_SETS where PERM_ID = ?{1}")
    public ImgDatasetDTO tryGetDatasetByPermId(String datasetPermId);

    @Select("select * from CONTAINERS where ID = ?{1}")
    public ImgContainerDTO getContainerById(long containerId);

    // join with container is needed to use spots index
    @Select("select cs.* from CHANNEL_STACKS cs               "
            + "join SPOTS s on s.id = cs.spot_id              "
            + "join CONTAINERS c on c.id = s.cont_id          "
            + "where cs.ds_id = ?{1} and s.x = ?{2} and s.y = ?{3}")
    public List<ImgChannelStackDTO> listChannelStacks(long datasetId, int spotX, int spotY);

    @Select("select count(*) from CHANNELS where DS_ID = ?{1} or EXP_ID = ?{2}")
    public int countChannelByDatasetIdOrExperimentId(long datasetId, long experimentId);

    @Select("select * from CHANNELS where DS_ID = ?{1} or EXP_ID = ?{2} order by ID")
    public List<ImgChannelDTO> getChannelsByDatasetIdOrExperimentId(long datasetId,
            long experimentId);

    @Select(sql = "select * from CHANNELS where EXP_ID = ?{1} order by ID", fetchSize = FETCH_SIZE)
    public List<ImgChannelDTO> getChannelsByExperimentId(long experimentId);

    @Select("select * from SPOTS where cont_id = ?{1}")
    public List<ImgSpotDTO> listSpots(long contId);

    @Select("select * from FEATURE_DEFS where DS_ID = ?{1}")
    public List<ImgFeatureDefDTO> listFeatureDefsByDataSetId(long dataSetId);

    @Select("select t.* from FEATURE_VOCABULARY_TERMS t            "
            + "join FEATURE_DEFS fd on fd.id = t.fd_id             "
            + "where fd.DS_ID = ?{1}                               ")
    public List<ImgFeatureVocabularyTermDTO> listFeatureVocabularyTermsByDataSetId(long dataSetId);

    @Select(sql = "select * from FEATURE_VALUES where FD_ID = ?{1.id} order by T_in_SEC, Z_in_M", resultSetBinding = FeatureVectorDataObjectBinding.class)
    public List<ImgFeatureValuesDTO> getFeatureValues(ImgFeatureDefDTO featureDef);

    @Select("select * from CHANNELS where (DS_ID = ?{1} or EXP_ID = ?{2}) and CODE = upper(?{3})")
    public ImgChannelDTO tryGetChannelByChannelCodeDatasetIdOrExperimentId(long id,
            long experimentId, String chosenChannelCode);

    @Select("select * from channels where code = ?{2} and "
            + "exp_id in (select id from experiments where perm_id = ?{1})")
    public ImgChannelDTO tryGetChannelByChannelCodeAndExperimentPermId(String experimentPermId,
            String chosenChannelCode);

}
