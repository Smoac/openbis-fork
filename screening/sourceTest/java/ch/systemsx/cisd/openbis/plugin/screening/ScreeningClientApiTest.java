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

package ch.systemsx.cisd.openbis.plugin.screening;

import java.util.List;

import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.ScreeningOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.Plate;

/**
 * @author Tomasz Pylak
 */
public class ScreeningClientApiTest
{
    private static final String USER_ID = "tpylak";

    private static final String USER_PASSWORD = "x";

    private static final String SERVER_URL = "http://localhost:8888/openbis";

    public static void main(String[] args)
    {
        ScreeningOpenbisServiceFacade facade =
                ScreeningOpenbisServiceFacade.tryCreate(USER_ID, USER_PASSWORD, SERVER_URL);
        List<Plate> plates = facade.listPlates();
        System.out.println("Plates: " + plates);
        System.out.println("Image datasets: " + facade.listImageDatasets(plates));
        System.out.println("Feature vector datasets: " + facade.listFeatureVectorDatasets(plates));

    }
}
