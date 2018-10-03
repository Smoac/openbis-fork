/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.systemsx.cisd.etlserver.postregistration;

import static org.testng.Assert.assertEquals;

import java.util.List;
import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;

/**
 * @author Franz-Josef Elmer
 */
public class RequestArchivingPostRegistrationTaskTest
{
    private static final String SESSION_TOKEN = "session-token";

    private Mockery context;

    private IEncapsulatedOpenBISService service;

    private IApplicationServerApi v3api;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        v3api = context.mock(IApplicationServerApi.class);
    }

    @Test
    public void test()
    {
        // Given
        Properties properties = new Properties();
        IPostRegistrationTask task = createTask(properties);
        RecordingMatcher<List<DataSetUpdate>> recordedUpdates = new RecordingMatcher<List<DataSetUpdate>>();
        context.checking(new Expectations()
            {
                {
                    one(service).getSessionToken();
                    will(returnValue(SESSION_TOKEN));
                    one(v3api).updateDataSets(with(SESSION_TOKEN), with(recordedUpdates));
                }
            });

        // When
        task.createExecutor("ds1", false).execute();

        // Then
        List<DataSetUpdate> updates = recordedUpdates.recordedObject();
        DataSetUpdate update = updates.get(0);
        assertEquals(update.getDataSetId().toString(), "DS1");
        assertEquals(update.getPhysicalData().getValue().isArchivingRequested().isModified(), true);
        assertEquals(update.getPhysicalData().getValue().isArchivingRequested().getValue(), Boolean.TRUE);
        assertEquals(updates.size(), 1);
        context.assertIsSatisfied();
    }

    private IPostRegistrationTask createTask(Properties properties)
    {
        return new RequestArchivingPostRegistrationTask(properties, service)
            {
                @Override
                protected IApplicationServerApi getV3api()
                {
                    return v3api;
                }
            };
    }

}
