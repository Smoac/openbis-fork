/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.sharedapi.v3.json;

import junit.framework.Assert;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonMappingException;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.server.sharedapi.v3.json.GenericObjectMapper;
import ch.systemsx.cisd.openbis.generic.shared.api.common.json.AbstractGenericObjectMapperTest;

/**
 * @author pkupczyk
 */
public class GenericObjectMapperTest extends AbstractGenericObjectMapperTest
{

    public GenericObjectMapperTest()
    {
        super(new GenericObjectMapper());
    }

    @Test(expectedExceptions = { JsonMappingException.class })
    public void testObjectWithNameThatExistsOnlyInV1IsNotDeserialized() throws Exception
    {
        deserialize("objectWithNameThatExistsOnlyInV1.json");
    }

    @Test
    public void testObjectWithNameThatExistsOnlyInV3IsDeserializedAsV3() throws Exception
    {
        SamplePermId permId = deserialize("objectWithNameThatExistsOnlyInV3.json");
        Assert.assertEquals("ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId", permId.getClass().getName());
        Assert.assertEquals("TEST_PERM_ID", permId.getPermId());
    }

    @Test(expectedExceptions = { JsonMappingException.class })
    public void testObjectWithLegacyClassAttributeThatIsSupportedOnlyInV1IsNotDeserialized() throws Exception
    {
        deserialize("objectWithLegacyClassAttributeThatIsSupportedOnlyInV1.json");
    }

}
