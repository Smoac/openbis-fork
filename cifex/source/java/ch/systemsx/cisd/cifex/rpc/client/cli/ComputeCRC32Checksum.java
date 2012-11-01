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

package ch.systemsx.cisd.cifex.rpc.client.cli;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * A command that computes CRC32 checksums of files. 
 *
 * @author Bernd Rinn
 */
public class ComputeCRC32Checksum extends AbstractCommand
{

    private static final String NAME = "crc32";

    private static ComputeCRC32Checksum instance;

    /** Not to be instantiated outside of this class. */
    private ComputeCRC32Checksum()
    {
        super(NAME);
    }

    /** Returns the unique instance of this class. */
    public final static synchronized ComputeCRC32Checksum getInstance()
    {
        if (instance == null)
        {
            instance = new ComputeCRC32Checksum();
        }
        return instance;
    }

    @Override
    public int execute(String[] arguments) throws UserFailureException, EnvironmentFailureException
    {
        final MinimalParameters parameters = new MinimalParameters(arguments, NAME, "filename [...]");
        if (parameters.getArgs().isEmpty())
        {
            parameters.printHelp(false);
            System.exit(1);
        }
        for (String filename : parameters.getArgs())
        {
            final int crc32;
            try
            {
                crc32 = (int) FileUtils.checksumCRC32(new File(filename));
                System.out.printf("%s\t%x\n", filename, crc32);
            } catch (IOException ex)
            {
                System.err.println("Error reading file '" + filename + "'.");
                System.exit(2);
            }
        }
        return 0;
    }

}
