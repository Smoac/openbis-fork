//
// MetaDataExtraction.java
//

/*
 OME Bio-Formats package for reading and converting biological file formats.
 Copyright (C) 2005-@year@ UW-Madison LOCI and Glencoe Software, Inc.

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package ch.systemsx.cisd.imagereaders.bioformats;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.regex.Pattern;

import loci.common.DebugTools;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.common.xml.XMLTools;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.MetadataTools;
import loci.formats.MissingLibraryException;
import loci.formats.gui.BufferedImageReader;
import loci.formats.in.DefaultMetadataOptions;
import loci.formats.in.MetadataLevel;
import loci.formats.in.MetadataOptions;
import loci.formats.in.OMETiffReader;
import loci.formats.meta.MetadataRetrieve;
import loci.formats.meta.MetadataStore;
import loci.formats.services.OMEXMLService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class MetaDataExtraction
{

    // -- Constants --

    private static final Logger LOGGER = LoggerFactory.getLogger(MetaDataExtraction.class);

    // -- Fields --

    private String directory = null;

    private String filePattern = ".*";

    private String fileName = null;

    private String pathName = null;

    private boolean doOriginal = false;

    private boolean doGlobal = false;

    private boolean doCSV = false;

    private boolean doOMEXML = false;

    private String omexmlVersion = null;

    private Hashtable<String, Object> vCSVKeyToKeyMapping;

    private Hashtable<String, Object> vCSVMetaData;

    private String[] vCSVKeys;

    private String[] vCSVMetaKeys;

    private IFormatReader reader;

    private IFormatReader baseReader;

    // -- MetaDataExtraction methods --

    public boolean parseArgs(String[] args)
    {
        directory = null;
        filePattern = ".*";
        fileName = null;
        pathName = null;
        doOriginal = false;
        doGlobal = false;
        doCSV = false;
        doOMEXML = false;
        omexmlVersion = null;
        if (args == null)
        {
            return false;
        }
        for (int i = 0; i < args.length; i++)
        {
            if (args[i].length() == 0)
            {
                continue;
            }
            if (args[i].startsWith("-"))
            {
                if (args[i].equals("-csv"))
                {
                    doCSV = true;
                } else if (args[i].equals("-original"))
                {
                    doOriginal = true;
                } else if (args[i].equals("-global"))
                {
                    doGlobal = true;
                } else if (args[i].equals("-omexml"))
                {
                    doOMEXML = true;
                } else if (args[i].equals("-xmlversion"))
                {
                    omexmlVersion = args[++i];
                } else if (args[i].equals("-directory"))
                {
                    directory = args[++i];
                } else if (args[i].equals("-pattern"))
                {
                    filePattern = args[++i];
                } else if (args[i].equals("-help") || args[i].equals("--help"))
                {
                    printUsage();
                    return false;
                } else
                {
                    LOGGER.error("Found unknown command flag: '{}' exiting.", args[i]);
                    return false;
                }
            } else
            {
                LOGGER.error("Found unknown argument: '{}' exiting.", args[i]);
                return false;
            }
        }
        return true;
    }

    public void printUsage()
    {
        String[] s =
                    {
                            "To test read a file, run:",
                            "  MetaDataExtraction -directory <directory> -pattern <pattern> [-omexml] [-csv]",
                            "                     [-original] [-global]", "",
                            "    -directory: the directory with image file(s) to read",
                            "    -pattern:   regular expression for file names to read",
                            "    -original:  print original metadata (unformatted)",
                            "    -global:    print global metadata (unformatted)",
                            "    -csv:       print metadata in CSV format",
                            "    -omexml:    print metadata in OME-XML format", "" };
        for (int i = 0; i < s.length; i++)
        {
            LOGGER.error(s[i]);
        }
    }

    public void printGlobalMetadata()
    {
        Hashtable<String, Object> meta = reader.getGlobalMetadata();
        String[] vOriginalMetaKeys = MetadataTools.keys(meta);
        for (String key : vOriginalMetaKeys)
        {
            System.out.println(key + ": " + meta.get(key));
        }
    }

    public void printOriginalMetadata()
    {
        Hashtable<String, Object> meta = reader.getSeriesMetadata();
        String[] vOriginalMetaKeys = MetadataTools.keys(meta);
        for (int i = 0; i < vOriginalMetaKeys.length; i++)
        {
            System.out.println(vOriginalMetaKeys[i] + ": " + meta.get(vOriginalMetaKeys[i]));
        }
    }

    public void initCSVMetadata()
    {
        vCSVKeyToKeyMapping = new Hashtable<String, Object>();
        vCSVKeyToKeyMapping.put("ApplicationName", "ApplicationName");
        vCSVKeyToKeyMapping.put("ApplicationVersion", "ApplicationVersion");
        vCSVKeyToKeyMapping.put("Camera Bit Depth", "Camera Bit Depth");
        vCSVKeyToKeyMapping.put("Experiment base name", "Experiment base name");
        vCSVKeyToKeyMapping.put("Experiment set", "Experiment set");
        vCSVKeyToKeyMapping.put("Exposure", "ExposureTime");
        vCSVKeyToKeyMapping.put("Frames to Average", "Frames to Average");
        vCSVKeyToKeyMapping.put("Gain", "Gain");
        vCSVKeyToKeyMapping.put("ImageXpress Micro Filter Cube", "ImageXpress Micro Filter Cube");
        vCSVKeyToKeyMapping.put("ImageXpress Micro Objective", "ImageXpress Micro Objective");
        vCSVKeyToKeyMapping.put("Laser focus score", "Laser focus score");
        vCSVKeyToKeyMapping.put("Shading", "Shading");
        vCSVKeyToKeyMapping.put("Subtract", "Subtract");
        vCSVKeyToKeyMapping.put("Temperature", "Temperature");
        // vCSVKeyToKeyMapping.put("_IllumSetting_", "_IllumSetting_");
        // vCSVKeyToKeyMapping.put("_MagSetting_", "_MagSetting_");
        vCSVKeyToKeyMapping.put("acquisition-time-local", "AcquiredDate");
        vCSVKeyToKeyMapping.put("camera-binning-x", "camera-binning-x");
        vCSVKeyToKeyMapping.put("camera-binning-y", "camera-binning-y");
        vCSVKeyToKeyMapping.put("camera-chip-offset-x", "camera-chip-offset-x");
        vCSVKeyToKeyMapping.put("camera-chip-offset-y", "camera-chip-offset-y");
        vCSVKeyToKeyMapping.put("gamma", "gamma");
        vCSVKeyToKeyMapping.put("number-of-planes", "SizeZ");
        vCSVKeyToKeyMapping.put("pixel-size-x", "SizeX");
        vCSVKeyToKeyMapping.put("pixel-size-y", "SizeY");
        vCSVKeyToKeyMapping.put("spatial-calibration-state", "spatial-calibration-state");
        vCSVKeyToKeyMapping.put("spatial-calibration-units", "spatial-calibration-units");
        vCSVKeyToKeyMapping.put("spatial-calibration-x", "PhysicalSizeX");
        vCSVKeyToKeyMapping.put("spatial-calibration-y", "PhysicalSizeY");
        vCSVKeyToKeyMapping.put("stage-label", "stage-label");
        vCSVKeyToKeyMapping.put("stage-position-x", "PositionX");
        vCSVKeyToKeyMapping.put("stage-position-y", "PositionY");
        vCSVKeyToKeyMapping.put("z-position", "PositionZ");
        vCSVKeyToKeyMapping.put("wavelength", "wavelength");
        vCSVKeys = MetadataTools.keys(vCSVKeyToKeyMapping);

        vCSVMetaData = new Hashtable<String, Object>();
        for (int i = 0; i < vCSVKeys.length; i++)
        {
            String vCSVMappedKey = "" + vCSVKeyToKeyMapping.get(vCSVKeys[i]);
            String vOriginalMetaValue = "-";
            vCSVMetaData.put(vCSVMappedKey, vOriginalMetaValue);
        }
        vCSVMetaKeys = MetadataTools.keys(vCSVMetaData);
    }

    public void printCSVHeader()
    {
        System.out.print("\"" + "Filename" + "\",");
        for (int i = 0; i < vCSVMetaKeys.length; i++)
        {
            System.out.print("\"" + vCSVMetaKeys[i] + "\",");
        }
        System.out.println("");
    }

    public void printCSVMetadata()
    {
        Hashtable<String, Object> vOriginalMetaData = reader.getSeriesMetadata();
        // String[] vOriginalMetaKeys = MetadataTools.keys(vOriginalMetaData);

        vCSVMetaData = new Hashtable<String, Object>();
        for (int i = 0; i < vCSVKeys.length; i++)
        {
            String vCSVMappedKey = "" + vCSVKeyToKeyMapping.get(vCSVKeys[i]);
            String vOriginalMetaValue;
            if (vOriginalMetaData.containsKey(vCSVKeys[i]))
            {
                vOriginalMetaValue = "" + vOriginalMetaData.get(vCSVKeys[i]);
            } else
            {
                vOriginalMetaValue = "-";
            }
            vCSVMetaData.put(vCSVMappedKey, vOriginalMetaValue);
        }

        // Print the CSV output
        System.out.print("\"" + fileName + "\",");
        for (int i = 0; i < vCSVMetaKeys.length; i++)
        {
            System.out.print("\"" + vCSVMetaData.get(vCSVMetaKeys[i]) + "\",");
        }
        System.out.println("");
    }

    public void printOMEXML() throws MissingLibraryException, ServiceException
    {
        MetadataStore ms = reader.getMetadataStore();

        if (baseReader instanceof ImageReader)
        {
            baseReader = ((ImageReader) baseReader).getReader();
        }
        if (baseReader instanceof OMETiffReader)
        {
            ms = ((OMETiffReader) baseReader).getMetadataStoreForDisplay();
        }

        OMEXMLService service;
        try
        {
            ServiceFactory factory = new ServiceFactory();
            service = factory.getInstance(OMEXMLService.class);
        } catch (DependencyException de)
        {
            throw new MissingLibraryException(OMETiffReader.NO_OME_XML_MSG, de);
        }
        if (ms instanceof MetadataRetrieve)
        {
            String xml = service.getOMEXML((MetadataRetrieve) ms);
            if (service.validateOMEXML(xml))
            {
                System.out.print(XMLTools.indentXML(xml, true));
            } else
            {
                LOGGER.error("Could not validate OME-XML.");
            }
        } else
        {
            LOGGER.error("The metadata could not be converted to OME-XML.");
            if (omexmlVersion == null)
            {
                LOGGER.error("The OME-XML Java library is probably not available.");
            } else
            {
                LOGGER.error("{} is probably not a legal schema version.", omexmlVersion);
            }
        }
    }

    /**
     * A utility method for reading a file from the command line, and displaying the results in a
     * simple display.
     */
    public boolean testRead(String[] args) throws FormatException, ServiceException, IOException
    {
        // DebugTools.enableLogging("INFO");
        DebugTools.enableLogging("ERROR");
        boolean validArgs = parseArgs(args);
        if (validArgs == false)
        {
            return false;
        }
        if (directory == null)
        {
            printUsage();
            return false;
        }
        Pattern vPattern = Pattern.compile(filePattern, Pattern.CASE_INSENSITIVE);

        // initialize required structures
        boolean shownCSVHeader = false;
        if (doCSV)
        {
            initCSVMetadata();
        }

        // initialize the reader
        baseReader = new ImageReader();
        reader = new BufferedImageReader(baseReader);

        MetadataOptions metaOptions = new DefaultMetadataOptions(MetadataLevel.ALL);

        if (doOMEXML)
        {
            reader.setOriginalMetadataPopulated(true);
            try
            {
                ServiceFactory factory = new ServiceFactory();
                OMEXMLService service = factory.getInstance(OMEXMLService.class);
                reader.setMetadataStore(service.createOMEXMLMetadata(null, omexmlVersion));
            } catch (DependencyException de)
            {
                throw new MissingLibraryException(OMETiffReader.NO_OME_XML_MSG, de);
            } catch (ServiceException se)
            {
                throw new FormatException(se);
            }
        }

        // Get a sorted directory listing
        File directoryObject = new File(directory);
        pathName = directoryObject.getAbsolutePath();
        String[] directoryObjectEntries = directoryObject.list();
        Arrays.sort(directoryObjectEntries);

        // Iterate over the available files
        for (int vFileIdx = 0; vFileIdx < directoryObjectEntries.length; vFileIdx++)
        {
            File directoryObjectEntry = new File(directoryObjectEntries[vFileIdx]);
            if (directoryObjectEntry.isDirectory())
            {
                continue;
            }

            fileName = directoryObjectEntry.getName();

            boolean vMatches = vPattern.matcher(fileName).matches();
            if (vMatches == false)
            {
                continue;
            }

            // System.out.println("Found " + vFileIdx + ": " + FileName + ": " + vMatches);

            reader.close();
            reader.setMetadataOptions(metaOptions);

            // initialize reader
            try
            {
                // mapLocation();
                reader.setId(pathName + "/" + fileName);

                // read format-specific metadata table
                if (doOriginal)
                {
                    printOriginalMetadata();
                }
                if (doGlobal)
                {
                    printGlobalMetadata();
                }
                if (doCSV)
                {
                    if (shownCSVHeader == false)
                    {
                        printCSVHeader();
                        shownCSVHeader = true;
                    }
                    printCSVMetadata();
                }
                if (doOMEXML)
                {
                    printOMEXML();
                }
            } catch (FormatException e)
            {
            } catch (IOException e)
            {
            }
        }

        return true;
    }

    // -- Main method --

    public static void main(String[] args) throws Exception
    {
        if (new MetaDataExtraction().testRead(args) == false)
        {
            System.exit(1);
        }
    }

}
