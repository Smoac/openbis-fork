/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.server;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMaterialBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister.IMaterialLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.AbstractPluginBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.IScreeningDAOFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellFeatureVectorReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader.WellFeatureCollection;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.HCSDatasetLoader;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.IHCSDatasetLoader;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.IHCSFeatureVectorLoader;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;

/**
 * The unique {@link IScreeningBusinessObjectFactory} implementation.
 * 
 * @author Tomasz Pylak
 */
@Component(ResourceNames.SCREENING_BUSINESS_OBJECT_FACTORY)
public final class ScreeningBusinessObjectFactory extends AbstractPluginBusinessObjectFactory
        implements IScreeningBusinessObjectFactory
{

    @Resource(name = ResourceNames.SCREENING_DAO_FACTORY)
    private IScreeningDAOFactory specificDAOFactory;

    public ScreeningBusinessObjectFactory()
    {
    }

    public IHCSDatasetLoader createHCSDatasetLoader(String datasetCode, String datastoreCode)
    {
        return new HCSDatasetLoader(specificDAOFactory.getImagingQueryDAO(datastoreCode),
                datasetCode);
    }

    public IHCSFeatureVectorLoader createHCSFeatureVectorLoader(String datastoreCode)
    {
        final IImagingReadonlyQueryDAO dao = specificDAOFactory.getImagingQueryDAO(datastoreCode);
        return new IHCSFeatureVectorLoader()
            {
                public WellFeatureCollection<FeatureVectorValues> fetchWellFeatureValuesIfPossible(
                        List<WellFeatureVectorReference> references)
                {
                    return FeatureVectorLoader.fetchWellFeatureValuesIfPossible(references, dao);
                }

                public WellFeatureCollection<FeatureVectorValues> fetchDatasetFeatureValues(
                        String dataSetCode)
                {
                    return FeatureVectorLoader.fetchDatasetFeatures(dataSetCode, dao);
                }
            };
    }

    public final ISampleBO createSampleBO(final Session session)
    {
        return getCommonBusinessObjectFactory().createSampleBO(session);
    }

    public ISampleLister createSampleLister(Session session)
    {
        return getCommonBusinessObjectFactory().createSampleLister(session);
    }

    public IMaterialLister createMaterialLister(Session session)
    {
        return getCommonBusinessObjectFactory().createMaterialLister(session);
    }

    public IExternalDataTable createExternalDataTable(Session session)
    {
        return getCommonBusinessObjectFactory().createExternalDataTable(session);
    }

    public IExperimentBO createExperimentBO(Session session)
    {
        return getCommonBusinessObjectFactory().createExperimentBO(session);
    }

    public IMaterialBO createMaterialBO(Session session)
    {
        return getCommonBusinessObjectFactory().createMaterialBO(session);
    }

    public IExternalDataBO createExternalDataBO(Session session)
    {
        return getCommonBusinessObjectFactory().createExternalDataBO(session);
    }

    public IDatasetLister createDatasetLister(Session session)
    {
        return getCommonBusinessObjectFactory().createDatasetLister(session);
    }
}
