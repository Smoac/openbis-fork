package ch.systemsx.cisd.openbis.generic;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.ITrackingServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

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

/**
 * Demo of the openBIS client which uses HTTPInvoker. This test is not run automatically.
 * 
 * @author Tomasz Pylak
 * @author Piotr Buczek
 */
public class OpenbisClientTest
{
    private static final String USER_ID = "test";

    private static final String USER_PASSWORD = "test";

    private static final String SERVER_URL = "http://localhost:8888/openbis";

    // CommonServiceServer
    private static final String COMMON_SERVICE_PATH = SERVER_URL + "/rmi-common";

    // GenericServiceServer
    private static final String GENERIC_SERVICE_PATH = SERVER_URL + "/rmi-plugin-generic";

    // TrackingServiceServer
    private static final String TRACKING_SERVICE_PATH = SERVER_URL + "/rmi-tracking";

    public static void main(String[] args)
    {
        testCommonServerService(COMMON_SERVICE_PATH);
        testGenericServerService(GENERIC_SERVICE_PATH);
        testTrackingServerService(TRACKING_SERVICE_PATH);
    }

    private static void testCommonServerService(String serviceURL)
    {
        System.out.println("TEST CommonServerService: " + serviceURL);
        ICommonServer commonServer =
                HttpInvokerUtils.createServiceStub(ICommonServer.class, serviceURL, 5);
        SessionContextDTO session = commonServer.tryToAuthenticate(USER_ID, USER_PASSWORD);

        List<Person> persons = commonServer.listPersons(session.getSessionToken());
        for (Person p : persons)
        {
            System.out.println(p.getUserId());
        }
    }

    private static void testGenericServerService(String serviceURL)
    {
        System.out.println("\nTEST GenericServerService: " + serviceURL);
        IGenericServer genericServer =
                HttpInvokerUtils.createServiceStub(IGenericServer.class, serviceURL, 5);
        SessionContextDTO session = genericServer.tryToAuthenticate(USER_ID, USER_PASSWORD);

        SampleParentWithDerived sampleInfo =
                genericServer.getSampleInfo(session.getSessionToken(), new TechId(1L));
        System.out.println(sampleInfo.getParent().getCode());
    }

    private static void testTrackingServerService(String serviceURL)
    {
        System.out.println("\nTEST TrackingServerService: " + serviceURL);
        ITrackingServer trackingServer =
                HttpInvokerUtils.createServiceStub(ITrackingServer.class, serviceURL, 5);
        SessionContextDTO session = trackingServer.tryToAuthenticate(USER_ID, USER_PASSWORD);

        final String sampleTypeCode = "CELL_PLATE";
        final int lastSeenSampleId = 0;
        final int lastSeenDataSetId = 0;

        final TrackingSampleCriteria sampleCriteria =
                new TrackingSampleCriteria(sampleTypeCode, lastSeenSampleId);
        final List<Sample> samples =
                trackingServer.listSamples(session.getSessionToken(), sampleCriteria);
        outputFoundEntities(samples, EntityKind.SAMPLE);

        final TrackingDataSetCriteria dataSetCriteria =
                new TrackingDataSetCriteria(sampleTypeCode, lastSeenDataSetId);
        final List<ExternalData> dataSets =
                trackingServer.listDataSets(session.getSessionToken(), dataSetCriteria);
        outputFoundEntities(dataSets, EntityKind.DATA_SET);
    }

    private static void outputFoundEntities(List<? extends IEntityInformationHolder> entities,
            EntityKind entityKind)
    {
        if (entities == null || entities.size() == 0)
        {
            System.out.println(String.format("No %ss found.", entityKind.getDescription()));
        } else
        {
            List<String> codes = new ArrayList<String>(entities.size());
            for (IEntityInformationHolder entity : entities)
            {
                codes.add(entity.getCode());
            }
            System.out.println(String.format("Found %d %ss: %s", codes.size(), entityKind
                    .getDescription(), StringUtils.join(codes, ", ")));
        }
    }
}
