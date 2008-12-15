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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;

/**
 * Defines the sample parents columns for sample grid/table.
 * <p>
 * Currently these columns are not sortable.
 * </p>
 * 
 * @author Izabela Adamczyk
 * @author Tomasz Pylak
 */
public final class ParentColumnsConfig extends AbstractColumnsConfig
{
    private final IMessageProvider messageProvider;

    public ParentColumnsConfig(final IMessageProvider messageProvider)
    {
        assert messageProvider != null : "Unspecified message provider.";
        this.messageProvider = messageProvider;
    }

    public final void define(final SampleType type)
    {
        defineColumns(SampleModel.createParentColumnsSchema(messageProvider, type), false);
    }
}