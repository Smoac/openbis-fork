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

package ch.systemsx.cisd.openbis.dss.etl.featurevector;

import java.util.List;

import ch.systemsx.cisd.openbis.dss.etl.ScreeningContainerDatasetInfo;
import ch.systemsx.cisd.openbis.dss.etl.ScreeningContainerDatasetInfoHelper;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureValuesDTO;

/**
 * Helper class for uploading feature vectors from the file system into the data base.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class FeatureVectorUploader
{
    private final IImagingQueryDAO dao;

    private final ScreeningContainerDatasetInfo info;

    public FeatureVectorUploader(IImagingQueryDAO imagingDao, ScreeningContainerDatasetInfo info)
    {
        this.dao = imagingDao;
        this.info = info;
    }

    /**
     * Creates a dataset and uploads feature vectors, creates experiment and container if needed.
     * Commit on the dao is NOT performed.
     */
    public void uploadFeatureVectors(List<CanonicalFeatureVector> fvecs)
    {
        ScreeningContainerDatasetInfoHelper helper = new ScreeningContainerDatasetInfoHelper(dao);
        long contId = helper.getOrCreateExperimentAndContainer(info).getContainerId();
        long dataSetId = helper.createFeatureVectorDataset(contId, info);

        uploadFeatureVectors(dao, fvecs, dataSetId);
    }

    /** Uploads feature vectors for a given dataset id. Commit on the dao is NOT performed. */
    public static void uploadFeatureVectors(IImagingQueryDAO dao,
            List<CanonicalFeatureVector> fvecs, long dataSetId)
    {
        for (CanonicalFeatureVector fvec : fvecs)
        {
            FeatureVectorUploaderHelper fvecUploader =
                    new FeatureVectorUploaderHelper(dao, dataSetId, fvec);
            fvecUploader.createFeatureDef();
            fvecUploader.createFeatureValues();
        }
    }

    private static class FeatureVectorUploaderHelper
    {
        private final IImagingQueryDAO dao;

        private final long dataSetId;

        private final CanonicalFeatureVector fvec;

        FeatureVectorUploaderHelper(IImagingQueryDAO dao, long dataSetId,
                CanonicalFeatureVector fvec)
        {
            this.dao = dao;
            this.dataSetId = dataSetId;
            this.fvec = fvec;
        }

        void createFeatureDef()
        {
            // The PK and FK of the feature def are not yet valid. Patch them up.
            ImgFeatureDefDTO featureDef = fvec.getFeatureDef();
            featureDef.setDataSetId(dataSetId);
            long defId = dao.addFeatureDef(featureDef);
            featureDef.setId(defId);
        }

        void createFeatureValues()
        {
            ImgFeatureDefDTO featureDef = fvec.getFeatureDef();

            // The PK and FK of the feature def are not yet valid. Patch them up.
            for (ImgFeatureValuesDTO featureValues : fvec.getValues())
            {
                featureValues.setFeatureDefId(featureDef.getId());
                long valuesId = dao.addFeatureValues(featureValues);
                featureValues.setId(valuesId);
            }
        }
    }
}
