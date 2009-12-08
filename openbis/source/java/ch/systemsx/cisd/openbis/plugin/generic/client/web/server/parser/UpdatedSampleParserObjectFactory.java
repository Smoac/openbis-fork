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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.server.parser;

import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedSample;

/**
 * A {@link NewSampleParserObjectFactory} extension for creating {@link NewSample} for batch update.
 * 
 * @author Piotr Buczek
 */
final class UpdatedSampleParserObjectFactory extends NewSampleParserObjectFactory
{
    private final SampleBatchUpdateDetails batchUpdateDetails;

    UpdatedSampleParserObjectFactory(final SampleType sampleType,
            final IPropertyMapper propertyMapper, boolean identifierExpectedInFile,
            boolean allowExperiments)
    {
        super(sampleType, propertyMapper, identifierExpectedInFile, allowExperiments);
        this.batchUpdateDetails = createBatchUpdateDetails();
    }

    private SampleBatchUpdateDetails createBatchUpdateDetails()
    {
        boolean updateExperiment = isColumnAvailable(UpdatedSample.EXPERIMENT);
        boolean updateParent = isColumnAvailable(UpdatedSample.PARENT);
        boolean updateContainer = isColumnAvailable(UpdatedSample.CONTAINER);
        return new SampleBatchUpdateDetails(updateExperiment, updateParent, updateContainer,
                getUnmatchedProperties());
    }

    private boolean isColumnAvailable(String columnName)
    {
        return tryGetPropertyModel(columnName) != null;
    }

    //
    // AbstractParserObjectFactory
    //

    @Override
    public NewSample createObject(final String[] lineTokens) throws ParserException
    {
        final NewSample newSample = super.createObject(lineTokens);
        return new UpdatedSample(newSample, batchUpdateDetails);
    }
}
