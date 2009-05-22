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
                    public void observe(EICMSRunDTO run)
                    {
                        String runName = run.getRawDataFileName();
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
                            out = new PrintStream(new File(runDir + "/msrun_" + runName + ".tsv"));
                            if (StringUtils.isNotBlank(run.getRawDataFilePath()))
                            {
                                out.println("filePath\t" + run.getRawDataFilePath());
                            }
                            if (StringUtils.isNotBlank(run.getRawDataFileName()))
                            {
                                out.println("fileName\t" + run.getRawDataFileName());
                            }
                            if (StringUtils.isNotBlank(run.getInstrumentType()))
                            {
                                out.println("instrumentType\t" + run.getInstrumentType());
                            }
                            if (StringUtils.isNotBlank(run.getInstrumentManufacturer()))
                            {
                                out.println("instrumentManufacturer\t"
                                        + run.getInstrumentManufacturer());
                            }
                            if (StringUtils.isNotBlank(run.getInstrumentModel()))
                            {
                                out.println("instrumentModel\t" + run.getInstrumentModel());
                            }
                            if (StringUtils.isNotBlank(run.getMethodIonisation()))
                            {
                                out.println("methodIonisation\t" + run.getMethodIonisation());
                            }
                            if (StringUtils.isNotBlank(run.getMethodSeparation()))
                            {
                                out.println("methodSeparation\t" + run.getMethodSeparation());
                            }
                            if (run.getAcquisitionDate() != null)
                            {
                                out.println("acquisitionDate\t"
                                        + EICMLParser.getDateFormat().format(
                                                run.getAcquisitionDate()));
                            }
                            if (run.getChromCount() >= 0)
                            {
                                out.println("chromCount\t" + run.getChromCount());
                            }
                            if (Float.isNaN(run.getStartTime()) == false)
                            {
                                out.println("startTime\t" + run.getStartTime());
                            }
                            if (Float.isNaN(run.getEndTime()) == false)
                            {
                                out.println("endTime\t" + run.getEndTime());
                            }
                            if (run.getEicMsRunId() >= 0)
                            {
                                out.println("msRunId\t" + run.getEicMsRunId());
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
                                            + chromatogram.getLabel() + ".tsv"));
                            if (Float.isNaN(chromatogram.getQ1Mz()) == false)
                            {
                                out.println("#\tQ1Mz\t" + chromatogram.getQ1Mz());
                            }
                            if (Float.isNaN(chromatogram.getQ3LowMz()) == false)
                            {
                                out.println("#\tQ3LowMz\t" + chromatogram.getQ3LowMz());
                            }
                            if (Float.isNaN(chromatogram.getQ3HighMz()) == false)
                            {
                                out.println("#\tQ3HighMz\t" + chromatogram.getQ3HighMz());
                            }
                            if (StringUtils.isNotBlank(chromatogram.getLabel()))
                            {
                                out.println("#\tLabel\t" + chromatogram.getLabel());
                            }
                            if (chromatogram.getPolarity() != '\0')
                            {
                                out.println("#\tPolarity\t" + chromatogram.getPolarity());
                            }
                            for (int i = 0; i < chromatogram.getRunTimes().length; ++i)
                            {
                                out.println(chromatogram.getRunTimes()[i] + "\t"
                                        + chromatogram.getIntensities()[i]);
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
