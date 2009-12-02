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

package eu.basysbio.cisd.dss;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.filesystem.IOutputStream;
import ch.systemsx.cisd.etlserver.utils.Column;

/**
 * Helper class to write a table (i.e. a list of {@link Column} objects) onto an
 * {@link IOutputStream} in TAB-separated format.
 * 
 * @author Franz-Josef Elmer
 */
class TSVOutputWriter
{
    private static final class Printer
    {
        private final IOutputStream outputStream;

        public Printer(IOutputStream outputStream)
        {
            this.outputStream = outputStream;
        }
        
        public void println(Object object)
        {
            print(object + OSUtilities.LINE_SEPARATOR);
        }
        
        public void print(Object object)
        {
            outputStream.write(String.valueOf(object).getBytes());
        }
    }

    private final IOutputStream outputStream;

    TSVOutputWriter(IOutputStream outputStream)
    {
        this.outputStream = outputStream;
    } 
    
    /**
     * Writes specified columns.
     */
    void write(List<Column> columns)
    {
        Printer printer = new Printer(outputStream);
        List<List<String>> cols = new ArrayList<List<String>>();
        int numberOfRows = Integer.MAX_VALUE;
        String delim = "";
        for (Column column : columns)
        {
            printer.print(delim + column.getHeader());
            delim = "\t";
            List<String> values = column.getValues();
            numberOfRows = Math.min(numberOfRows, values.size());
            cols.add(values);
        }
        printer.println("");
        for (int i = 0; i < numberOfRows; i++)
        {
            delim = "";
            for (List<String> col : cols)
            {
                printer.print(delim + col.get(i));
                delim = "\t";
            }
            printer.println("");
        }
        outputStream.flush();
    }
    
    /**
     * Closes wrapped output stream.
     */
    void close()
    {
        outputStream.close();
    }
}
