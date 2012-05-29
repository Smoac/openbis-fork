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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * Abstract super class of {@link IImageRepresentationFormatSelectionCriterion} which accepting
 * matching {@link ImageRepresentationFormat} instance individually.
 * 
 * @author Franz-Josef Elmer
 */
@JsonTypeName("AbstractFormatSelectionCriterion")
@JsonSubTypes(value = {@JsonSubTypes.Type(ColorDepthCriterion.class), 
        @JsonSubTypes.Type(FileTypeCriterion.class), @JsonSubTypes.Type(OriginalCriterion.class)})
public abstract class AbstractFormatSelectionCriterion implements 
        IImageRepresentationFormatSelectionCriterion
{
    private static final long serialVersionUID = 1L;

    public List<ImageRepresentationFormat> getMatching(
            List<ImageRepresentationFormat> imageRepresentationFormats)
    {
        List<ImageRepresentationFormat> filteredMetaData =
                new ArrayList<ImageRepresentationFormat>();
        for (ImageRepresentationFormat format : imageRepresentationFormats)
        {
            if (accept(format))
            {
                filteredMetaData.add(format);
            }
        }
        return filteredMetaData;
    }

    /**
     * Returns <code>true</code> if the specified {@link ImageRepresentationFormat} instance is
     * accepted by this criterion.
     */
    protected abstract boolean accept(ImageRepresentationFormat format);

}
