/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins;

import java.io.File;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.openbis.dss.etl.IContentRepository;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageEnrichedDTO;

/**
 * @author Tomasz Pylak
 */
public class RevertImageLevelTransformationProcessingPlugin extends
        AbstractSpotImagesTransformerProcessingPlugin
{
    private static final long serialVersionUID = 1L;

    public RevertImageLevelTransformationProcessingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
    }

    @Override
    protected IImageTransformerFactoryProvider getTransformationProvider(
            List<ImgImageEnrichedDTO> spotImages, IContentRepository contentRepository)
    {
        return new IImageTransformerFactoryProvider()
            {
                public IImageTransformerFactory getTransformationFactory(ImgImageEnrichedDTO image)
                {
                    return null;
                }
            };
    }
}
