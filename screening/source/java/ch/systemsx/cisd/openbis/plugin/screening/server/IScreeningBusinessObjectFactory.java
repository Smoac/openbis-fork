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

import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMaterialBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister.IMaterialLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.IHCSDatasetLoader;

/**
 * A <i>screening</i> plugin specific business object factory.
 * 
 * @author Tomasz Pylak
 */
public interface IScreeningBusinessObjectFactory
{
    public IHCSDatasetLoader createHCSDatasetLoader(final String datasetPermId);

    public ISampleBO createSampleBO(final Session session);

    public IExternalDataTable createExternalDataTable(final Session session);

    public IExperimentBO createExperimentBO(Session session);

    public IMaterialBO createMaterialBO(Session session);

    public ISampleLister createSampleLister(Session session);

    public IMaterialLister createMaterialLister(Session session);

    public IExternalDataBO createExternalDataBO(Session session);

    public IDatasetLister createDatasetLister(Session session, String defaultDataStoreBaseURL);
}
