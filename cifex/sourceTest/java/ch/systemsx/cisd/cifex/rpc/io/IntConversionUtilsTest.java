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

package ch.systemsx.cisd.cifex.rpc.io;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Test cases for {@link IntConversionUtils}
 * 
 * @author Bernd Rinn
 */
public class IntConversionUtilsTest
{

    @DataProvider(name = "data")
    public Object[][] getValues()
    {
        return new Object[][]
            {
                { 1 },
                { 256 },
                { 255 },
                { 65536 },
                { 65535 },
                { 16777216 },
                { 16777215 },
                { -1 },
                { -127 },
                { -32767 },
                { -8388607 },
                { 17 },
                { -88 },
                { 1 << 31 },
                { Integer.MAX_VALUE },
                { Integer.MAX_VALUE - 1 },
                { Integer.MIN_VALUE },
                { Integer.MIN_VALUE + 1 }, 
            };
    }

    @Test(dataProvider = "data")
    public void testRoundtrip(Integer value)
    {
        final byte[] barr = new byte[4];
        IntConversionUtils.intToBytes(value, barr, 0);
        final int value2 = IntConversionUtils.bytesToInt(barr);
        assertEquals(value.intValue(), value2);
    }
}
