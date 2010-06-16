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

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.bo.SampleHierarchyFiller;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleParentWithDerivedDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;
import ch.systemsx.cisd.openbis.plugin.generic.server.batch.BatchOperationExecutor;
import ch.systemsx.cisd.openbis.plugin.generic.server.batch.SampleBatchRegistration;
import ch.systemsx.cisd.openbis.plugin.generic.server.batch.SampleBatchUpdate;
import ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames;

/**
 * The <i>generic</i> slave server.
 * 
 * @author Christian Ribeaud
 */
@Component(ch.systemsx.cisd.openbis.generic.shared.ResourceNames.GENERIC_SAMPLE_TYPE_SLAVE_SERVER_PLUGIN)
public final class GenericSampleTypeSlaveServerPlugin implements ISampleTypeSlaveServerPlugin
{
    @Resource(name = ResourceNames.GENERIC_BUSINESS_OBJECT_FACTORY)
    private IGenericBusinessObjectFactory businessObjectFactory;

    @Resource(name = ComponentNames.DAO_FACTORY)
    private IDAOFactory daoFactory;

    private GenericSampleTypeSlaveServerPlugin()
    {
    }

    //
    // ISlaveServerPlugin
    //

    public final SampleParentWithDerivedDTO getSampleInfo(final Session session,
            final SamplePE sample) throws UserFailureException
    {
        assert session != null : "Unspecified session.";
        assert sample != null : "Unspecified sample.";

        HibernateUtils.initialize(sample.getProperties());
        SampleHierarchyFiller.enrichWithParentAndContainerHierarchy(sample);
        final List<SamplePE> generated =
                daoFactory.getSampleDAO().listSamplesByGeneratedFrom(sample);
        return new SampleParentWithDerivedDTO(sample, generated);
    }

    public final void registerSamples(final Session session, final List<NewSample> newSamples)
            throws UserFailureException
    {
        assert session != null : "Unspecified session.";
        assert newSamples != null && newSamples.size() > 0 : "Unspecified sample or empty samples.";

        new BatchOperationExecutor<NewSample>().executeInBatches(new SampleBatchRegistration(
                businessObjectFactory.createSampleTable(session), newSamples));
    }

    public void updateSamples(Session session, List<SampleBatchUpdatesDTO> updateSamples)
    {
        assert session != null : "Unspecified session.";
        assert updateSamples != null && updateSamples.size() > 0 : "Unspecified sample or empty samples.";

        new BatchOperationExecutor<SampleBatchUpdatesDTO>().executeInBatches(new SampleBatchUpdate(
                businessObjectFactory.createSampleTable(session), updateSamples));
    }
}
