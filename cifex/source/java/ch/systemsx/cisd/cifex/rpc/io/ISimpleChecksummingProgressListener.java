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

package ch.systemsx.cisd.cifex.rpc.io;

import java.io.IOException;

/**
 * A simple progress listener for processing a byte stream (with CRC32 checksum).
 *
 * @author Bernd Rinn
 */
public interface ISimpleChecksummingProgressListener
{
    /**
     * Indicates that <var>bytesProcessed</var> bytes have been processed and that the content
     * of all bytes processed up to now have a ckecksum of <var>crc32Value</var>.
     */
    void update(long bytesProcessed, int crc32Value);
    
    /**
     * Indicates that the exception <var>e</var> was thrown during processing.
     */
    void exceptionThrown(IOException e);
}