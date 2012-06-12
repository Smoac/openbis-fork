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

package ch.systemsx.cisd.etlserver.registrator.monitor;

import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

/**
 * The helper class for checking if all conditions necessary for the succesfull registration are
 * met. That includes the availability of application server, availability of the necessary
 * filesystems and the remaining disk space.
 * 
 * @author jakubs
 */
public class DssRegistrationHealthMonitor
{
    private IEncapsulatedOpenBISService openBisService;

    public boolean isApplicationServerAlive()
    {
        try
        {
            openBisService.didEntityOperationsSucceed(new TechId(1));
        } catch (Exception e)
        {
            return false;
        }
        return true;
    }
    
//    public boolean 
}
