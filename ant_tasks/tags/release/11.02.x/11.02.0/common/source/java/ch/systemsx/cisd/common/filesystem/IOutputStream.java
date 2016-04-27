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

package ch.systemsx.cisd.common.filesystem;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;

/**
 * An interface for {@link java.io.OutputStream}.
 * 
 * @author Bernd Rinn
 */
public interface IOutputStream extends ICloseable, ISynchronizable
{
    /**
     * @see java.io.OutputStream#write(int)
     */
    public void write(int b) throws IOExceptionUnchecked;

    /**
     * @see java.io.OutputStream#write(byte[])
     */
    public void write(byte b[]) throws IOExceptionUnchecked;

    /**
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    public void write(byte b[], int off, int len) throws IOExceptionUnchecked;

    /**
     * @see java.io.OutputStream#flush()
     */
    public void flush() throws IOExceptionUnchecked;

    /**
     * @see java.io.OutputStream#flush()
     */
    public void synchronize() throws IOExceptionUnchecked;

    /**
     * @see java.io.OutputStream#close()
     */
    public void close() throws IOExceptionUnchecked;

}
