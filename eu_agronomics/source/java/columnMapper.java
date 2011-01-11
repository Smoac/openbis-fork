import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;

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
 * @author walshs
 */
public class columnMapper
{

    // column headings index
    Integer inIndex;

    // in filename
    String inFile;

    // incoming Mapping File
    String inMapFile;

    // quick mapper specification
    String quickMapper;

    // hash Map of things to replace
    HashMap<String, String> hm = new HashMap<String, String>();

    // Common units
    Boolean commonUnits;

    // User supplied units
    String userUnits;
    
    // Upper case the heading
    Boolean upperCase;

    public columnMapper(int index, String infile, String q, Boolean c, String u, Boolean uc)
    {
        inIndex = index;
        inFile = infile;
        quickMapper = q;
        commonUnits = c;
        userUnits = u;
        upperCase = uc;

        createMapStruct();
        createMapStub();
    }

    private void createMapStruct()
    {

        if (quickMapper == null)
        {
            // do nothing
        } else
        {

            Scanner pairs = new Scanner(quickMapper).useDelimiter(",");
            while (pairs.hasNext())
            {
                String pair = pairs.next();
                String[] kv = StringUtils.split(pair, "=");
                hm.put(kv[0], kv[1]);
            }

        }

    }

    private void createMapStub()
    {
        try
        {
            BufferedReader input = new BufferedReader(new FileReader(inFile));
            String line = null;
            Integer lineNum = 0;
            StringBuffer units = new StringBuffer(" (");
            Boolean haveUnits = false;
            if (commonUnits)
            {
                units.append("mm^2 g µm^2 ");
                haveUnits = true;
            }

            if (userUnits == null)
            {
                // nothing to do
            } else
            {
                units.append(userUnits);
                haveUnits = true;
            }
            units.append(")");

            while ((line = input.readLine()) != null)
            {
                lineNum++;
                if (lineNum == inIndex)
                {
                    Scanner tokenize = new Scanner(line).useDelimiter("\t");
                    while (tokenize.hasNext())
                    {

                        String toke = tokenize.next();
                        
                        String toke2 = new String(toke);
                        if (upperCase){
                            toke2 = StringUtils.upperCase(toke2);
                        } 
              
                        toke2 = toke2.replace(" ", "_");
                        for (String s : hm.keySet())
                        {
                            toke2 = toke2.replace(s, hm.get(s));
                        }

                        System.out.print(toke + "\t" + toke2);

                        if (haveUnits)
                        {
                            System.out.print(units.toString());
                        }
                        System.out.print("\n");

                    }

                }
            }
            input.close();

        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
}
