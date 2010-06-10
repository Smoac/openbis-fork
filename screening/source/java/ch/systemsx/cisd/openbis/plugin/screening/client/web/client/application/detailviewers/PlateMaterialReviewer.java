/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import java.util.Arrays;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MultilineVarcharField;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;

/**
 * A grid with a list of material (e.g. gene) locations on the plate with a fast access to images.
 * 
 * @author Tomasz Pylak
 */
// TODO 2010-06-10, Tomasz Pylak: implement me 
public class PlateMaterialReviewer
{

    public static DatabaseModificationAwareComponent create(
            IViewContext<IScreeningClientServiceAsync> context,
            IEntityInformationHolderWithIdentifier experiment, String[] materialItemList)
    {
        MultilineVarcharField component = new MultilineVarcharField("", true);
        component.setValue(experiment + " " + Arrays.toString(materialItemList));
        return DatabaseModificationAwareComponent.wrapUnaware(component);
    }
}
