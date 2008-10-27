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

package ch.systemsx.cisd.openbis.plugin.generic.server;

import java.util.List;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.openbis.generic.server.business.bo.SampleHierarchyFiller;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleGenerationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames;

/**
 * The <i>generic</i> slave server.
 * 
 * @author Christian Ribeaud
 */
@Component(ResourceNames.GENERIC_SAMPLE_TYPE_SLAVE_SERVER_PLUGIN)
public final class GenericSampleTypeSlaveServerPlugin implements ISampleTypeSlaveServerPlugin
{

    //
    // ISlaveServerPlugin
    //

    public final SampleGenerationDTO getSampleInfo(final IDAOFactory daoFactory,
            final Session session, final SamplePE sample)
    {
        assert sample != null : "Unspecified sample.";
        sample.ensurePropertiesAreLoaded();
        SampleHierarchyFiller.enrichWithFullHierarchy(sample);
        final List<SamplePE> generated =
                daoFactory.getSampleDAO().listSampleByGeneratedFrom(sample);
        final SampleGenerationDTO sampleGenerationDTO = new SampleGenerationDTO(sample, generated);
        return sampleGenerationDTO;
    }
}
