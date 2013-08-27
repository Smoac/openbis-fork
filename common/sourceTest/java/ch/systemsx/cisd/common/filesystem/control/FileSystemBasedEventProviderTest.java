/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.filesystem.control;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author anttil
 */
public class FileSystemBasedEventProviderTest
{
    File controlDir;

    private IEventProvider provider;

    @BeforeMethod
    public void fixture()
    {
        controlDir = new File("/tmp/" + UUID.randomUUID().toString());
        controlDir.mkdir();
        provider = new FileSystemBasedEventProvider(controlDir);
    }

    @Test
    public void eventsAreReturnedOnlyOnce() throws Exception
    {
        new File(controlDir, "parameter-x").createNewFile();

        assertThat(provider.getNewEvents(Arrays.asList("parameter")).isEmpty(), is(false));
        assertThat(provider.getNewEvents(Arrays.asList("parameter")).isEmpty(), is(true));
    }

    @Test
    public void filesAreCleaned() throws Exception
    {
        File event = new File(controlDir, "parameter-x");
        event.createNewFile();

        provider.getNewEvents(Arrays.asList("parameter"));

        assertThat(event.exists(), is(false));
    }

    @Test
    public void unregisteredfilesAreNotCleaned() throws Exception
    {
        File event = new File(controlDir, "other_parameter-x");
        event.createNewFile();

        Map<String, String> events = provider.getNewEvents(Arrays.asList("parameter"));

        assertThat(events.isEmpty(), is(true));
        assertThat(event.exists(), is(true));
    }

    @Test
    public void keyAndValueAreParsedCorrectlyFromTheFileName() throws Exception
    {
        new File(controlDir, "parameter-x").createNewFile();

        Map<String, String> events = provider.getNewEvents(Arrays.asList("parameter"));

        assertThat(events.get("parameter"), is("x"));
    }

    @Test
    public void emptyValueWorks() throws Exception
    {
        new File(controlDir, "parameter-").createNewFile();

        Map<String, String> events = provider.getNewEvents(Arrays.asList("parameter"));

        assertThat(events.get("parameter"), is(""));
    }
}
