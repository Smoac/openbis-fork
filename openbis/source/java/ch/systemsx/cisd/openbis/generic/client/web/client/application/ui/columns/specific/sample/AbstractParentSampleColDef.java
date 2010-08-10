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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.sample;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

public abstract class AbstractParentSampleColDef extends AbstractColumnDefinition<Sample> implements
        IsSerializable
{
    abstract public Sample tryGetParent(Sample sample);

    AbstractParentSampleColDef(String headerText)
    {
        super(headerText, AbstractColumnDefinitionKind.DEFAULT_COLUMN_WIDTH, false, false);
    }

    @Override
    protected String tryGetValue(Sample sample)
    {
        Sample parent = tryGetParent(sample);
        if (parent != null)
        {
            return getAsValue(parent);
        } else
        {
            return null;
        }
    }

    protected final String getAsValue(Sample sample)
    {
        return sample.getIdentifier();
    }

    @Override
    public String tryGetLink(Sample sample)
    {
        return LinkExtractor.tryExtract(tryGetParent(sample));
    }
}
