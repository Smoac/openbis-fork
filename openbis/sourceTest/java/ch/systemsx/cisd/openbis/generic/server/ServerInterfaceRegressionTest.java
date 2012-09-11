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

package ch.systemsx.cisd.openbis.generic.server;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.util.AnnotationAppliedTestCase;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.ITrackingServer;

/**
 * @author Franz-Josef Elmer
 */
public class ServerInterfaceRegressionTest extends AnnotationAppliedTestCase
{

    @Test
    public void testICommonServer()
    {
        assertMandatoryMethodAnnotations(ICommonServer.class, CommonServer.class,
                "getLastModificationState: Transactional\n");
    }

    @Test
    public void testIETLLIMSService()
    {
        assertMandatoryMethodAnnotations(IETLLIMSService.class, ETLService.class);
    }

    @Test
    public void testITrackingServer()
    {
        assertMandatoryMethodAnnotations(ITrackingServer.class, TrackingServer.class);
    }
}
