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

package ch.systemsx.cisd.yeastx.eicml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

/**
 * A converter from eicML files to tab files.
 * 
 * @author Bernd Rinn
 */
public class EICML2TabConverter
{

    public static void main(String[] args) throws IOException, ParserConfigurationException,
            SAXException
    {
        final String dir = args[0];
        for (String f : new File(dir).list(new EICMLFilenameFilter()))
        {
            final String[] msRunName = new String[1];
            new EICMLParser(dir + "/" + f, null, new EICMLParser.IMSRunObserver()
                {
                    public void observe(MSRunDTO run)
                    {
                        String runName = run.rawDataFileName;
                        if (runName.endsWith(".RAW"))
                        {
                            runName = runName.substring(0, runName.length() - 4);
                        }
                        String runDir = dir + "/" + runName;
                        new File(runDir).mkdir();
                        msRunName[0] = runDir;
                        PrintStream out;
                        try
                        {
                            out =
                                    new PrintStream(new File(runDir + "/msrun_" + runName
                                            + ".tsv"));
                            if (StringUtils.isNotBlank(run.rawDataFilePath))
                            {
                                out.println("filePath\t" + run.rawDataFilePath);
                            }
                            if (StringUtils.isNotBlank(run.rawDataFileName))
                            {
                                out.println("fileName\t" + run.rawDataFileName);
                            }
                            if (StringUtils.isNotBlank(run.instrumentType))
                            {
                                out.println("instrumentType\t" + run.instrumentType);
                            }
                            if (StringUtils.isNotBlank(run.instrumentManufacturer))
                            {
                                out.println("instrumentManufacturer\t"
                                        + run.instrumentManufacturer);
                            }
                            if (StringUtils.isNotBlank(run.instrumentModel))
                            {
                                out.println("instrumentModel\t" + run.instrumentModel);
                            }
                            if (StringUtils.isNotBlank(run.methodIonisation))
                            {
                                out.println("methodIonisation\t" + run.methodIonisation);
                            }
                            if (StringUtils.isNotBlank(run.methodSeparation))
                            {
                                out.println("methodSeparation\t" + run.methodSeparation);
                            }
                            if (StringUtils.isNotBlank(run.acquisitionDate))
                            {
                                out.println("acquisitionDate\t" + run.acquisitionDate);
                            }
                            if (run.chromCount >= 0)
                            {
                                out.println("chromCount\t" + run.chromCount);
                            }
                            if (Float.isNaN(run.startTime) == false)
                            {
                                out.println("startTime\t" + run.startTime);
                            }
                            if (Float.isNaN(run.endTime) == false)
                            {
                                out.println("endTime\t" + run.endTime);
                            }
                            if (run.msRunId >= 0)
                            {
                                out.println("msRunId\t" + run.msRunId);
                            }
                            out.close();
                        } catch (FileNotFoundException ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                }, new EICMLParser.IChromatogramObserver()
                {
                    public void observe(ChromatogramDTO chromatogram)
                    {
                        PrintStream out;
                        try
                        {
                            out =
                                    new PrintStream(new File(msRunName[0] + "/"
                                            + chromatogram.label + ".tsv"));
                            if (Float.isNaN(chromatogram.q1Mz) == false)
                            {
                                out.println("#\tQ1Mz\t" + chromatogram.q1Mz);
                            }
                            if (Float.isNaN(chromatogram.q3LowMz) == false)
                            {
                                out.println("#\tQ3LowMz\t" + chromatogram.q3LowMz);
                            }
                            if (Float.isNaN(chromatogram.q3HighMz) == false)
                            {
                                out.println("#\tQ3HighMz\t" + chromatogram.q3HighMz);
                            }
                            if (StringUtils.isNotBlank(chromatogram.label))
                            {
                                out.println("#\tLabel\t" + chromatogram.label);
                            }
                            if (chromatogram.polarity != '\0')
                            {
                                out.println("#\tPolarity\t" + chromatogram.polarity);
                            }
                            for (int i = 0; i < chromatogram.runTimes.length; ++i)
                            {
                                out.println(chromatogram.runTimes[i] + "\t"
                                        + chromatogram.intensities[i]);
                            }
                            out.close();
                        } catch (FileNotFoundException ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                });
        }
    }

}
