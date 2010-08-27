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

import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.ByteArrayMapper;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgAcquiredImageDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgChannelDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgChannelStackDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgContainerDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureValuesDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgSpotDTO;

/**
 * Operations on imaging database which are non-read-only.
 * 
 * @author Tomasz Pylak
 */
public interface IImagingQueryDAO extends TransactionQuery, IImagingReadonlyQueryDAO
{
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

    @Select("insert into CHANNELS (LABEL, CODE, DESCRIPTION, WAVELENGTH, DS_ID, EXP_ID) values "
            + "(?{1.label}, ?{1.code}, ?{1.description}, ?{1.wavelength}, ?{1.datasetId}, ?{1.experimentId}) returning ID")
    public long addChannel(ImgChannelDTO channel);

    @Select("insert into CONTAINERS (PERM_ID, SPOTS_WIDTH, SPOTS_HEIGHT, EXPE_ID) values "
            + "(?{1.permId}, ?{1.numberOfColumns}, ?{1.numberOfRows}, ?{1.experimentId}) returning ID")
    public long addContainer(ImgContainerDTO container);

    @Select("insert into DATA_SETS (PERM_ID, FIELDS_WIDTH, FIELDS_HEIGHT, CONT_ID, IS_MULTIDIMENSIONAL) values "
            + "(?{1.permId}, ?{1.fieldNumberOfColumns}, "
            + "?{1.fieldNumberOfRows}, ?{1.containerId}, ?{1.isMultidimensional}) returning ID")
    public long addDataset(ImgDatasetDTO dataset);

    @Select("insert into SPOTS (X, Y, CONT_ID) values "
            + "(?{1.column}, ?{1.row}, ?{1.containerId}) returning ID")
    public long addSpot(ImgSpotDTO spot);

    @Select("insert into FEATURE_DEFS (LABEL, CODE, DESCRIPTION, DS_ID) values "
            + "(?{1.label}, ?{1.code}, ?{1.description}, ?{1.dataSetId}) RETURNING ID")
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
}
