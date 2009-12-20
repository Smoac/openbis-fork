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

/**
 * Utilities to convert an integer value to a byte array and vice versa.
 *
 * @author Bernd Rinn
 */
public class IntConversionUtils
{
    private final static int MASK = 0xff;
    
    public static void intToBytes(int value, byte[] arr, int off)
    {
        int work = value;
        arr[off] = (byte) (work & MASK);
        work >>= 8;
        arr[off + 1] = (byte) (work & MASK);
        work >>= 8;
        arr[off + 2] = (byte) (work & MASK);
        work >>= 8;
        arr[off + 3] = (byte) (work & MASK);
    }
    
    public static int bytesToInt(byte[] value)
    {
        int result = (value[3] & MASK);
        result <<= 8;
        result |= (value[2] & MASK);
        result <<= 8;
        result |= (value[1] & MASK);
        result <<= 8;
        result |= (value[0] & MASK);
        return result;
    }
    
}
