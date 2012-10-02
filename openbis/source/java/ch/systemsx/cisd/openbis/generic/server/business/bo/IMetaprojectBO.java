/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

/**
 * @author Pawel Glyzewski
 */
public interface IMetaprojectBO extends IEntityBusinessObject
{
    void addExperiments(List<TechId> experiments);

    void addSamples(List<TechId> samples);

    void addDataSets(List<TechId> dataSets);

    void addMaterials(List<TechId> materials);

    void removeExperiments(List<TechId> experiments);

    void removeSamples(List<TechId> samples);

    void removeDataSets(List<TechId> dataSets);

    void removeMaterials(List<TechId> materials);

    void deleteByTechId(TechId metaprojectId) throws UserFailureException;
}
