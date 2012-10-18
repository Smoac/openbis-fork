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

package ch.systemsx.cisd.openbis.uitest.gui;

import ch.systemsx.cisd.openbis.uitest.dsl.Executor;
import ch.systemsx.cisd.openbis.uitest.layout.RegisterScriptLocation;
import ch.systemsx.cisd.openbis.uitest.page.RegisterScript;
import ch.systemsx.cisd.openbis.uitest.request.CreateScript;
import ch.systemsx.cisd.openbis.uitest.type.Script;

/**
 * @author anttil
 */
public class CreateScriptGui extends Executor<CreateScript, Script>
{

    @Override
    public Script run(CreateScript request)
    {
        Script script = request.getScript();
        RegisterScript register = goTo(new RegisterScriptLocation());
        register.fillWith(script);
        register.save();
        return script;
    }

}
