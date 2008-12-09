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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.server;

import ch.systemsx.cisd.common.parser.AbstractParserObjectFactory;
import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;

/**
 * A {@link AbstractParserObjectFactory} extension for creating {@link NewSample}.
 * 
 * @author Christian Ribeaud
 */
final class NewSampleParserObjectFactory extends AbstractParserObjectFactory<NewSample>
{
    private final SampleType sampleType;

    NewSampleParserObjectFactory(final SampleType sampleType, final IPropertyMapper propertyMapper)
    {
        super(NewSample.class, propertyMapper);
        this.sampleType = sampleType;
    }

    //
    // AbstractParserObjectFactory
    //

    @Override
    public final NewSample createObject(final String[] lineTokens) throws ParserException
    {
        final NewSample newSample = super.createObject(lineTokens);
        newSample.setSampleType(sampleType);
        return newSample;
    }
}
