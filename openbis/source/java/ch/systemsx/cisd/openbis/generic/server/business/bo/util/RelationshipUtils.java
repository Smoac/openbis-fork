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

package ch.systemsx.cisd.openbis.generic.server.business.bo.util;

import java.util.Date;

import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.IModifierAndModificationDateBean;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * Utility function for relation ship.
 * 
 * @author Franz-Josef Elmer
 */
public class RelationshipUtils
{

    public static void setExperimentForDataSet(DataPE data, ExperimentPE experiment,
            IAuthSession session)
    {
        ExperimentPE currentExperiment = data.getExperiment();
        if (currentExperiment != null)
        {
            updateModificationDateAndModifier(currentExperiment, session);
        }
        data.setExperiment(experiment);
        updateModificationDateAndModifier(experiment, session);
    }

    public static void updateModificationDateAndModifier(
            IModifierAndModificationDateBean beanOrNull, IAuthSession session)
    {
        log(beanOrNull, session);
        if (beanOrNull == null)
        {
            return;
        }
        PersonPE person = session.tryGetPerson();
        if (person != null)
        {
            beanOrNull.setModifier(person);
        }
        beanOrNull.setModificationDate(new Date());
    }

    private static void log(IModifierAndModificationDateBean beanOrNull, IAuthSession session)
    {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace)
        {
            if (stackTraceElement.getClassName().contains("OptimisticLockingTest"))
            {
                System.err.println("===== UPDATE modification date and modifier ===== "
                        + stackTraceElement.getMethodName());
                System.err.println("bean: " + beanOrNull);
                System.err.println("user: " + session.tryGetPerson());
                System.err.println("\t" + stackTrace[3]);
                System.err.println("\t" + stackTrace[4]);
                System.err.println("\t" + stackTrace[5]);
                System.err.println("\t...");
                System.err.println("\t" + stackTraceElement);
                break;
            }
        }
    }

}
