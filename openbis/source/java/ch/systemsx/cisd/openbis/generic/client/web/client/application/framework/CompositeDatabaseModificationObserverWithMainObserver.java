/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.framework;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.SetUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;

/**
 * {@link CompositeDatabaseModificationObserver} extension with update of added observers stopped
 * until main observer update is finished. If main observer fails to update other observers will not
 * update at all.
 * 
 * @author Piotr Buczek
 */
public class CompositeDatabaseModificationObserverWithMainObserver extends
        CompositeDatabaseModificationObserver
{

    private IDatabaseModificationObserverWithCallback mainObserver;

    /** if this constructor is used main observer has to be added before other observers */
    public CompositeDatabaseModificationObserverWithMainObserver()
    {
        super();
    }

    public CompositeDatabaseModificationObserverWithMainObserver(
            IDatabaseModificationObserverWithCallback mainObserver)
    {
        super();
        this.addMainObserver(mainObserver);
    }

    public void addMainObserver(IDatabaseModificationObserverWithCallback observer)
    {
        assert observer != null : "main observer cannot be null";
        this.mainObserver = observer;
    }

    @Override
    public void addObserver(IDatabaseModificationObserver observerOrNull)
    {
        super.addObserver(observerOrNull);
    }

    @Override
    public void addObservers(List<? extends IDatabaseModificationObserver> observers)
    {
        assert mainObserver != null : "main observer cannot be null";
        super.addObservers(observers);
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        assert mainObserver != null : "main observer cannot be null";

        Set<DatabaseModificationKind> relevantModifications =
                new HashSet<DatabaseModificationKind>();
        SetUtils.addAll(relevantModifications, mainObserver.getRelevantModifications());
        SetUtils.addAll(relevantModifications, super.getRelevantModifications());
        return relevantModifications.toArray(DatabaseModificationKind.EMPTY_ARRAY);
    }

    @Override
    public void update(final Set<DatabaseModificationKind> observedModifications)
    {
        assert mainObserver != null : "main observer cannot be null";

        if (SetUtils.containsAny(observedModifications, mainObserver.getRelevantModifications()))
        {
            mainObserver.setSuccessfulUpdateCallback(new IDelegatedAction()
                {
                    public void execute()
                    {
                        updateOtherObservers(observedModifications);
                    }
                });
            mainObserver.update(observedModifications);
        } else
        {
            updateOtherObservers(observedModifications);
        }
    }

    private void updateOtherObservers(Set<DatabaseModificationKind> observedModifications)
    {
        super.update(observedModifications);
    }

}
