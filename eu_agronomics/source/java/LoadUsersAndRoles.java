import org.apache.commons.lang.StringUtils;
import ch.systemsx.cisd.args4j.CmdLineException;
import ch.systemsx.cisd.args4j.CmdLineParser;
import ch.systemsx.cisd.args4j.Option;

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

/**
 * A test of HTTP Invoker 
 * 
 * @author Sean Walsh
 */
public class LoadUsersAndRoles
{

    @Option(name = "-p", longName = "password", metaVar = "The password of the test user ", usage = " ", required = true)
    private static String pass = null;

    @Option(name = "-su", longName = "serverURL", metaVar = "The URL to the server ", usage = "the path to RMI services ie. where do we find rmi-common and rmi-plugin-generic e.g. https://agronomics.ethz.ch:8443/openbis/openbis/", required = true)
    private static String url = null;

    @Option(name = "-f", longName = "usersFile", metaVar = "The file of users ", usage = "", required = false)
    private static String file = "";

    public void doArgs(String[] args)
    {
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(150);
        try
        {
            parser.parseArgument(args);
           

        } catch (CmdLineException e)
        {

            System.err.println(e.getMessage());
            System.err.println("Options were \"" + StringUtils.join(args, " ") + "\"\n");
            System.out.println("Usage is :\n\njava LoadUsersAndRoles [ options...]" + "\n");
            parser.printUsage(System.out);
            System.out.println();

            return;
        }
    }

    public static void main(String[] args)

    {
        new LoadUsersAndRoles().doArgs(args);

        BisUtil bu = new BisUtil(url, pass);
        
        if (file.equals("")){
              bu.dumpUsersAndRoles();
              
              bu.listUsersAndRoles();
              
        } else {
             bu.enableUsers();
        }
       

    }
}