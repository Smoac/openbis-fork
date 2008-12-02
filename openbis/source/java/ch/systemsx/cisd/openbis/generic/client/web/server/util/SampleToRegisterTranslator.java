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

package ch.systemsx.cisd.openbis.generic.client.web.server.util;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleToRegister;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleToRegisterDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * A {@link SampleToRegisterDTO} &lt;---&gt; {@link SampleToRegister} translator.
 * 
 * @author Izabela Adamczyk
 */
public final class SampleToRegisterTranslator
{

    private SampleToRegisterTranslator()
    {
        // Can not be instantiated.
    }

    /**
     * Translates a given {@link SampleToRegister} into a {@link SampleToRegisterDTO}.
     */
    public final static SampleToRegisterDTO translate(final SampleToRegister sample)
    {
        final SampleToRegisterDTO result = new SampleToRegisterDTO();
        result.setSampleIdentifier(SampleIdentifierFactory.parse(sample.getSampleIdentifier()));
        final String container = sample.getContainer();
        if (StringUtils.isBlank(container) == false)
        {
            result.setContainer(SampleIdentifierFactory.parse(container));
        }
        final String parent = sample.getParent();
        if (StringUtils.isBlank(parent) == false)
        {
            result.setParent(SampleIdentifierFactory.parse(parent));
        }
        result.setProperties(sample.getProperties().toArray(SampleProperty.EMPTY_ARRAY));
        result.setSampleTypeCode(sample.getSampleTypeCode());
        return result;
    }
}
