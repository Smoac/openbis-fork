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

import static ch.systemsx.cisd.base.utilities.OSUtilities.LINE_SEPARATOR;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.OutputStreamAdapter;
import ch.systemsx.cisd.etlserver.utils.Column;
import ch.systemsx.cisd.etlserver.utils.TabSeparatedValueTable;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class TSVOutputWriterTest extends AssertJUnit
{
    @Test
    public void test()
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        TSVOutputWriter writer = new TSVOutputWriter(new OutputStreamAdapter(outputStream));
        String content = "a\tb\n11\t12\n\n";
        TabSeparatedValueTable table = new TabSeparatedValueTable(new StringReader(content), content, true);
        List<Column> columns = table.getColumns();
        
        writer.write(columns);
        
        assertEquals("a\tb" + LINE_SEPARATOR + "11\t12" + LINE_SEPARATOR + "\t" + LINE_SEPARATOR, outputStream.toString());
    }
}
