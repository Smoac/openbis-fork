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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import java.util.HashSet;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.Component;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.SetUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;

/**
 * @author pkupczyk
 */
public class DisposableComposite implements IDisposableComponent
{

    private Component mainComponent;

    private IDisposableComponent[] subComponents;

    public DisposableComposite(Component mainComponent, IDisposableComponent... subComponents)
    {
        this.mainComponent = mainComponent;

        if (subComponents == null)
        {
            this.subComponents = new IDisposableComponent[0];
        } else
        {
            this.subComponents = subComponents;
        }

    }

    @Override
    public Component getComponent()
    {
        return mainComponent;
    }

    @Override
    public void dispose()
    {
        for (IDisposableComponent subComponent : subComponents)
        {
            subComponent.dispose();
        }
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        Set<DatabaseModificationKind> relevantModifications =
                new HashSet<DatabaseModificationKind>();

        for (IDisposableComponent subComponent : subComponents)
        {
            SetUtils.addAll(relevantModifications, subComponent.getRelevantModifications());
        }

        return relevantModifications.toArray(DatabaseModificationKind.EMPTY_ARRAY);
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        for (IDatabaseModificationObserver subComponent : subComponents)
        {
            if (SetUtils
                    .containsAny(observedModifications, subComponent.getRelevantModifications()))
            {
                subComponent.update(observedModifications);
            }
        }
    }

}
