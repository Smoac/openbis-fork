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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Interface of an image selection criterion based on {@link IImageSetMetaData}.
 *
 * @author Franz-Josef Elmer
 */
public interface IImageSetSelectionCriterion extends Serializable
{
    /**
     * Returns all {@link IImageSetMetaData} objects from the specified list which fulfill this
     * criterion.
     */
    public List<IImageSetMetaData> getMatching(List<IImageSetMetaData> imageMetaData);
}
