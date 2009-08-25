import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
public class headerReplacer
{

    String inFile;

    Integer headerIndex;

    String mapFile;

    String outFile;

    HashMap<String, String> hm = new HashMap<String, String>();

    public headerReplacer(String file, Integer i, String m, String o)
    {

        inFile = file;
        headerIndex = i;
        mapFile = m;
        outFile = o;

        try
        {
            createHashMap();
        } catch (Exception ex)
        {
            System.err.println(ex.getMessage());
            ex.printStackTrace();
            System.exit(0);
        }

        try
        {
            createOutFile();
        } catch (Exception ex)
        {
            System.err.println(ex.getMessage());
        }
    }

    private void createHashMap() throws Exception
    {

        try
        {
            BufferedReader input = new BufferedReader(new FileReader(mapFile));
            String line = null;

            int lineNum = 0;
            while ((line = input.readLine()) != null)
            {
                lineNum++;
                String[] j = line.split("\t");
                if (j.length != 2)
                {
                    throw new Exception(
                            "Expecting just 2 values separated by a tab-character in "
                                    + mapFile
                                    + " at line "
                                    + lineNum
                                    + "\nLine is : \""
                                    + line
                                    + "\"\nLine contains "
                                    + j.length
                                    + " value(s) some of which may be empty strings or hiddden characters\n");
                } else
                {
                    if (hm.containsKey(j[0]))
                    {
                        throw new Exception("Duplicate key " + j[0] + " in map file " + mapFile);
                    } else
                    {
                        hm.put(j[0], j[1]);
                    }
                }

            }
            input.close();

        } catch (IOException ex)
        {
            ex.printStackTrace();
        }

    }

    private void createOutFile() throws Exception
    {

        try
        {
            BufferedReader input = new BufferedReader(new FileReader(inFile));
            String line = null;
            Integer lineNum = 0;
            File f = new File("./" + outFile);
            System.out.println("Attempting to write the file " + f.toString());
            //f.mkdirs();
            if (f.exists()){
                System.out.println("File exists " + f.toString());
                throw new Exception("Output file already exists on the file system. Delete the existing file and re-run. File is :\n" + f.toString());
            }
            f.createNewFile();
                
            BufferedWriter fOut = new BufferedWriter(new FileWriter(f));
       
                
            while ((line = input.readLine()) != null)
            {
                lineNum++;
                if (lineNum == headerIndex)
                {
                    Scanner tokenize = new Scanner(line).useDelimiter("\t");

                    ArrayList<String> headings = new ArrayList<String>();
                    while (tokenize.hasNext())
                    {

                        String toke = tokenize.next();

                        if (hm.containsKey(toke))
                        {
                            headings.add(hm.get(toke));
                        } else
                        {
                            throw new Exception("The column heading \"" + toke + "\" in " + inFile
                                    + " does not exist in mapFile " + mapFile);
                        }

                        

                    }
                    String headrow = StringUtils.join(headings.toArray(), "\t") + "\n";
                    fOut.write(headrow);
             
                } else
                {
                    fOut.write(line + "\n");
                }
            }
            input.close();
            fOut.close();
            System.out.println("Done writing.");
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }

    }

}
