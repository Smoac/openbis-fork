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

import org.apache.commons.lang.StringUtils;
import ch.systemsx.cisd.args4j.CmdLineParser;
import ch.systemsx.cisd.args4j.Option;
import ch.systemsx.cisd.args4j.CmdLineException;

/**
 * @author walshs
 */

public class mapColumns
{

    @Option(name = "-f", longName = "tsvFile", metaVar = "TSV File ", 
            usage = "The Tab Separated Value file to manipulate", required = true)
    private static String file = null;

    @Option(name = "-i", longName = "headingsIndex", metaVar = "Row number",
            usage = "Row number of the column headings. 1 is the first.", required = true)
    private static Integer i = -1;
    
    @Option(name = "-uc", longName = "upperCase", metaVar = "Upper case the headings",
            usage = "By default headings are upper-cased", required = false)
    private static Boolean uc = true;

    @Option(name = "-q", longName = "quickMapper", metaVar = "Replace characters",
            usage = "Quick mappers specify what to replace as key value pairs", required = false)
    private static String q = "";
    
    @Option(name = "-c", longName = "commonUnits", metaVar = "Include some common units for editing",
            usage = "", required = true)
    private static Boolean c ;
        
    @Option(name = "-u", longName = "userUnits", metaVar = "User supplied units",
            usage = "User supplied units are added verbatamin", required = false)
    private static String u ;
    
    
    public void doArgs(String[] args)
    {
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(150);
        try
        {
                 
            parser.parseArgument(args);
            // some checking
            if (i < 0)
            {
                throw new CmdLineException("Problem with headings index which was set to " + i);
            }
            

        } catch (CmdLineException e)
        {

            System.err.println(e.getMessage());
            System.err.println("Options were \"" + StringUtils.join(args," ") + "\"\n");          
            System.out.println("Usage is :\n\njava mapColumns [ options...]" + "\n");
            parser.printUsage(System.out);
            System.out.println();

            return;
        }
    }
    
    
       public static void main(String[] args)
    {
        new mapColumns().doArgs(args);
          
        columnMapper cm = new columnMapper(i, file, q, c, u, uc);
             
    }

}
