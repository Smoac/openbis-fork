/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.AggregateFunction;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.AbundanceColumnDefinition;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinInfo;

/**
 * Business object for loading proteins together with their abundances.
 *
 * @author Franz-Josef Elmer
 */
public interface IProteinInfoTable
{
    /**
     * Loads proteins of all data sets registered for the specified experiment. All proteins are
     * filtered out if their false discovery rate is larger then the specified one.
     */
    public void load(List<AbundanceColumnDefinition> definitions, TechId experimentId,
            double falseDiscoveryRate, AggregateFunction function, String treatmentTypeCode,
            boolean aggregateOnOriginal);

    public List<ProteinInfo> getProteinInfos();

}
