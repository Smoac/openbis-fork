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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

public abstract class AbstractParentSampleColDef extends AbstractColumnDefinition<Sample> implements
        IsSerializable
{
    abstract protected Sample tryGetParent(Sample sample);

    abstract protected String getIdentifierPrefix();

    private int/* the level which should be shown */level;

    AbstractParentSampleColDef(int level, String headerText)
    {
        super(headerText, AbstractColumnDefinitionKind.DEFAULT_COLUMN_WIDTH, false, false);
        this.level = level;
    }

    @Override
    protected String tryGetValue(Sample sample)
    {
        Sample parent = tryGetParentSample(sample);
        if (parent != null)
        {
            return parent.getIdentifier();
        } else
        {
            return null;
        }
    }

    public String getIdentifier()
    {
        return getIdentifierPrefix() + level;
    }

    public final Sample tryGetParentSample(final Sample sample)
    {
        Sample parent = sample;
        int depth = level;
        while (depth > 0 && parent != null)
        {
            parent = tryGetParent(parent);
            depth--;
        }
        return parent;
    }

}
