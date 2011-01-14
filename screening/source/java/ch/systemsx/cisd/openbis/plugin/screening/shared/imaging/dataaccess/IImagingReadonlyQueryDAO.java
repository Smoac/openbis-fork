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

    public static final String SQL_HCS_IMAGE =
            "select i.* from CHANNEL_STACKS, SPOTS, ACQUIRED_IMAGES, IMAGES as i "
                    + "where                                                                "
                    + "ACQUIRED_IMAGES.CHANNEL_ID = ?{1} and CHANNEL_STACKS.DS_ID = ?{2} and "
                    + "CHANNEL_STACKS.x = ?{3.x} and CHANNEL_STACKS.y = ?{3.y} and "
                    + "SPOTS.x = ?{4.x} and SPOTS.y = ?{4.y} and "
                    // joins
                    + "ACQUIRED_IMAGES.CHANNEL_STACK_ID = CHANNEL_STACKS.ID and "
                    + "CHANNEL_STACKS.SPOT_ID = SPOTS.ID ";

    public static final String SQL_MICROSCOPY_IMAGE =
            "select i.* from CHANNEL_STACKS, ACQUIRED_IMAGES, IMAGES as i "
                    + "where                                                                "
                    + "ACQUIRED_IMAGES.CHANNEL_ID = ?{1} and CHANNEL_STACKS.DS_ID = ?{2} and "
                    + "CHANNEL_STACKS.x = ?{3.x} and CHANNEL_STACKS.y = ?{3.y} and "
                    + "CHANNEL_STACKS.spot_id IS NULL and "
                    // joins
                    + "ACQUIRED_IMAGES.CHANNEL_STACK_ID = CHANNEL_STACKS.ID ";

    public static final String SQL_HCS_IMAGE_REPRESENTATIVE =
            "select i.* from CHANNEL_STACKS, SPOTS, ACQUIRED_IMAGES, IMAGES as i "
                    + "where                                        "
                    + "CHANNEL_STACKS.is_representative = 'T' and   "
                    + "CHANNEL_STACKS.DS_ID = ?{1} and              "
                    + "ACQUIRED_IMAGES.channel_id = ?{3} and        "
                    + "SPOTS.x = ?{2.x} and SPOTS.y = ?{2.y} and    "
                    // joins
                    + "ACQUIRED_IMAGES.CHANNEL_STACK_ID = CHANNEL_STACKS.ID and "
                    + "CHANNEL_STACKS.SPOT_ID = SPOTS.ID ";

    public static final String SQL_MICROSCOPY_IMAGE_REPRESENTATIVE =
            "select i.* from CHANNEL_STACKS, ACQUIRED_IMAGES, IMAGES as i "
                    + "where                                         "
                    + "CHANNEL_STACKS.is_representative = 'T' and    "
                    + "CHANNEL_STACKS.DS_ID = ?{1} and               "
                    + "ACQUIRED_IMAGES.channel_id = ?{2} and         "
                    // joins
                    + "ACQUIRED_IMAGES.CHANNEL_STACK_ID = CHANNEL_STACKS.ID ";

    // TODO 2010-12-10, Tomasz Pylak: uncomment when we are able to show a representative image
    public static final String SQL_NO_MULTIDIMENTIONAL_DATA_COND =
            " order by CHANNEL_STACKS.T_in_SEC, CHANNEL_STACKS.Z_in_M limit 1";

    // " and CHANNEL_STACKS.T_in_SEC IS NULL                        "
    // + " and CHANNEL_STACKS.Z_in_M IS NULL                ";

    // ---------------- HCS ---------------------------------

    /**
     * @return an HCS image for the specified chanel, well and tile. If many images (e.g. for
     *         different timepoints or depths) exist, null is returned.
     */
    @Select(SQL_HCS_IMAGE + " and ACQUIRED_IMAGES.IMG_ID = i.ID "
            + SQL_NO_MULTIDIMENTIONAL_DATA_COND)
    public ImgImageDTO tryGetHCSImage(long channelId, long datasetId, Location tileLocation,
            Location wellLocation);

    /**
     * @return a HCS thumbnail for the specified chanel, well and tile. If many images (e.g. for
     *         different timepoints or depths) exist, null is returned.
     */
    @Select(SQL_HCS_IMAGE + " and ACQUIRED_IMAGES.THUMBNAIL_ID = i.ID "
            + SQL_NO_MULTIDIMENTIONAL_DATA_COND)
    public ImgImageDTO tryGetHCSThumbnail(long channelId, long datasetId, Location tileLocation,
            Location wellLocation);

    /**
     * @return an representative HCS image for the specified dataset and well. Returns null if there
     *         are no images at all for the specified well.
     */
    @Select(SQL_HCS_IMAGE_REPRESENTATIVE + " and ACQUIRED_IMAGES.IMG_ID = i.ID ")
    public ImgImageDTO tryGetHCSRepresentativeImage(long datasetId, Location wellLocation,
            long channelId);

    /**
     * @return an representative HCS thumbnail for the specified dataset and well. Returns null if
     *         there are no images at all for the specified well.
     */
    @Select(SQL_HCS_IMAGE_REPRESENTATIVE + " and ACQUIRED_IMAGES.THUMBNAIL_ID = i.ID ")
    public ImgImageDTO tryGetHCSRepresentativeThumbnail(long datasetId, Location wellLocation,
            long channelId);

    // ---------------- Microscopy ---------------------------------

    /**
     * @return an microscopy image for the specified channel and tile. If many images (e.g. for
     *         different timepoints or depths) exist, null is returned.
     */
    @Select(SQL_MICROSCOPY_IMAGE + " and ACQUIRED_IMAGES.IMG_ID = i.ID "
            + SQL_NO_MULTIDIMENTIONAL_DATA_COND)
    public ImgImageDTO tryGetMicroscopyImage(long channelId, long datasetId, Location tileLocation);

    /**
     * @return a microscopy thumbnail for the specified channel and tile. If many images (e.g. for
     *         different timepoints or depths) exist, null is returned.
     */
    @Select(SQL_MICROSCOPY_IMAGE + " and ACQUIRED_IMAGES.THUMBNAIL_ID = i.ID "
            + SQL_NO_MULTIDIMENTIONAL_DATA_COND)
    public ImgImageDTO tryGetMicroscopyThumbnail(long channelId, long datasetId,
            Location tileLocation);

    /**
     * @return an representative microscopy image for the specified dataset.
     */
    @Select(SQL_MICROSCOPY_IMAGE_REPRESENTATIVE + " and ACQUIRED_IMAGES.IMG_ID = i.ID ")
    public ImgImageDTO tryGetMicroscopyRepresentativeImage(long datasetId, long channelId);

    /**
     * @return an representative microscopy thumbnail for the specified dataset. Can be null if
     *         thumbnail has not been generated.
     */
    @Select(SQL_MICROSCOPY_IMAGE_REPRESENTATIVE + " and ACQUIRED_IMAGES.THUMBNAIL_ID = i.ID ")
    public ImgImageDTO tryGetMicroscopyRepresentativeThumbnail(long datasetId, long channelId);

    // ---------------- Microscopy - channels ---------------------------------

    @Select("select cs.* from CHANNEL_STACKS cs               "
            + "where cs.ds_id = ?{1} and cs.spot_id is NULL")
    public List<ImgChannelStackDTO> listSpotlessChannelStacks(long datasetId);

    @Select("select * from CHANNELS where DS_ID = ?{1} order by ID")
    public List<ImgChannelDTO> getChannelsByDatasetId(long datasetId);

    @Select("select * from CHANNELS where (DS_ID = ?{1}) and CODE = upper(?{2})")
    public ImgChannelDTO tryGetChannelForDataset(long datasetId, String chosenChannelCode);

    @Select("select count(*) > 0 from CHANNELS ch "
            + "join DATA_SETS d on ch.ds_id = d.id where d.PERM_ID = ?{1}")
    public boolean hasDatasetChannels(String datasetPermId);

    // ---------------- Generic ---------------------------------

    /** @return an image for the specified channel and channel stack or null */
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

    @Select("select * from DATA_SETS where PERM_ID = ?{1}")
    public ImgDatasetDTO tryGetDatasetByPermId(String datasetPermId);

    // ---------------- HCS - experiments, containers, channels ---------------------------------

    @Select("select * from EXPERIMENTS where PERM_ID = ?{1}")
    public ImgExperimentDTO tryGetExperimentByPermId(String experimentPermId);

    @Select("select * from EXPERIMENTS where ID = ?{1}")
    public ImgExperimentDTO tryGetExperimentById(long experimentId);

    @Select("select ID from CONTAINERS where PERM_ID = ?{1}")
    public Long tryGetContainerIdPermId(String containerPermId);

    @Select("select * from CONTAINERS where ID = ?{1}")
    public ImgContainerDTO getContainerById(long containerId);

    // join with container is needed to use spots index
    @Select("select cs.* from CHANNEL_STACKS cs               "
            + "join SPOTS s on s.id = cs.spot_id              "
            + "join CONTAINERS c on c.id = s.cont_id          "
            + "where cs.ds_id = ?{1} and s.x = ?{2} and s.y = ?{3}")
    public List<ImgChannelStackDTO> listChannelStacks(long datasetId, int spotX, int spotY);

    @Select(sql = "select * from CHANNELS where EXP_ID = ?{1} order by ID", fetchSize = FETCH_SIZE)
    public List<ImgChannelDTO> getChannelsByExperimentId(long experimentId);

    @Select("select * from SPOTS where cont_id = ?{1}")
    public List<ImgSpotDTO> listSpots(long contId);

    @Select("select * from CHANNELS where (EXP_ID = ?{1}) and CODE = upper(?{2})")
    public ImgChannelDTO tryGetChannelForExperiment(long experimentId, String chosenChannelCode);

    @Select("select * from channels where code = ?{2} and "
            + "exp_id in (select id from experiments where perm_id = ?{1})")
    public ImgChannelDTO tryGetChannelForExperimentPermId(String experimentPermId,
            String chosenChannelCode);

    // ---------------- HCS - feature vectors ---------------------------------

    @Select("select * from FEATURE_DEFS where DS_ID = ?{1}")
    public List<ImgFeatureDefDTO> listFeatureDefsByDataSetId(long dataSetId);

    @Select("select t.* from FEATURE_VOCABULARY_TERMS t            "
            + "join FEATURE_DEFS fd on fd.id = t.fd_id             "
            + "where fd.DS_ID = ?{1}                               ")
    public List<ImgFeatureVocabularyTermDTO> listFeatureVocabularyTermsByDataSetId(long dataSetId);

    @Select(sql = "select * from FEATURE_VALUES where FD_ID = ?{1.id} order by T_in_SEC, Z_in_M", resultSetBinding = FeatureVectorDataObjectBinding.class)
    public List<ImgFeatureValuesDTO> getFeatureValues(ImgFeatureDefDTO featureDef);

}
