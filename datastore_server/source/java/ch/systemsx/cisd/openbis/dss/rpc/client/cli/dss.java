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

package ch.systemsx.cisd.openbis.dss.rpc.client.cli;

import ch.systemsx.cisd.openbis.dss.component.IDssComponent;
import ch.systemsx.cisd.openbis.dss.component.impl.DssComponent;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class dss
{
    static
    {
        // Disable any logging output.
        System.setProperty("org.apache.commons.logging.Log",
                "org.apache.commons.logging.impl.NoOpLog");
    }

    private final IDssComponent component;

    private dss()
    {
        this.component = new DssComponent("http://localhost:8888/openbis");
    }

    private void runWithArgs(String[] args)
    {
        component.login("test", "foo");
        System.out.println("" + component.getDataSet("20100318094819344-4"));
    }

    public static void main(String[] args)
    {
        dss newMe = new dss();
        newMe.runWithArgs(args);

    }

}
