/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer;

import java.util.Date;

import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.SyncEntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;

class IncomingSample extends IncomingEntity<NewSample>
{
    public NewSample getSample()
    {
        return (NewSample) getEntity();
    }

    IncomingSample(NewSample sample, Date lastModDate)
    {
        super(sample, SyncEntityKind.SAMPLE, lastModDate);
    }
}