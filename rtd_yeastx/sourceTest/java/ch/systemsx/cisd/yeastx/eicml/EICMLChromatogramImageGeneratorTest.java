/*
 * Copyright 2010 ETH Zuerich, CISD
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import ch.systemsx.cisd.yeastx.eicml.EICMLParser.IChromatogramObserver;

/**
 * @author Chandrasekhar Ramakrishnan
 */
@Test(groups = "broken")
public class EICMLChromatogramImageGeneratorTest extends AssertJUnit implements
        IChromatogramObserver
{
    Mockery context;

    ArrayList<ChromatogramDTO> chromatograms = new ArrayList<ChromatogramDTO>();

    @BeforeClass
    public final void initializeChromatogram()
    {
        // Get a chromatogram to work with.
        try
        {
            new EICMLParser("resource/examples/example.eicML", null, this);
        } catch (ParserConfigurationException ex)
        {
            fail(ex.getMessage());
        } catch (SAXException ex)
        {
            fail(ex.getMessage());
        } catch (IOException ex)
        {
            fail(ex.getMessage());
        }

        if (chromatograms.size() < 1)
        {
            fail("Could not create ChromatogramDTO instance for test.");
        }
    }

    @BeforeMethod
    public final void setUp()
    {
        context = new Mockery();
    }

    @AfterMethod
    public final void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testImageGeneration()
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EICMLChromatogramImageGenerator imageGenerator =
                new EICMLChromatogramImageGenerator(chromatograms.get(0), out, 300, 200);
        try
        {
            imageGenerator.generateImage();
        } catch (IOException ex)
        {
            fail(ex.getMessage());
        }
        if (out.size() < 1)
            fail("Image generation created an empty image");
    }

    @Test
    public void testImageStyles()
    {
        for (EICMLChromatogramImageGenerator.ChartType type : EICMLChromatogramImageGenerator.ChartType
                .values())
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            EICMLChromatogramImageGenerator imageGenerator =
                    new EICMLChromatogramImageGenerator(chromatograms.get(0), out, 300, 200, type);
            try
            {
                imageGenerator.generateImage();
            } catch (IOException ex)
            {
                fail(ex.getMessage());
            }
            if (out.size() < 1)
                fail("Image generation created an empty image");
        }
    }

    // CR : Not a real test, but it is useful to looking at the generated images, so I'm leaving it,
    // but commenting it out
    // @Test
    // public void testImageGenerationFile()
    // {
    // java.io.FileOutputStream out = null;
    // try
    // {
    // // Generate images with the default style for each of the datasets
    // int i = 0;
    // for (ChromatogramDTO chromatogram : chromatograms)
    // {
    // out = new java.io.FileOutputStream("chromatogram" + i + ".png");
    // EICMLChromatogramImageGenerator imageGenerator =
    // new EICMLChromatogramImageGenerator(chromatogram, out, 800, 600);
    // imageGenerator.generateImage();
    // ++i;
    // }
    // } catch (java.io.FileNotFoundException ex1)
    // {
    // ex1.printStackTrace();
    // } catch (IOException ex)
    // {
    // fail(ex.getMessage());
    // }
    //
    // try
    // {
    // // Generate each style for the first image
    // ChromatogramDTO chromatogram = chromatograms.get(0);
    // for (EICMLChromatogramImageGenerator.ChartType type :
    // EICMLChromatogramImageGenerator.ChartType
    // .values())
    // {
    // out = new java.io.FileOutputStream("chromatogram" + "-" + type + ".png");
    // EICMLChromatogramImageGenerator imageGenerator =
    // new EICMLChromatogramImageGenerator(chromatogram, out, 800, 600, type);
    // imageGenerator.generateImage();
    // }
    // } catch (java.io.FileNotFoundException ex1)
    // {
    // ex1.printStackTrace();
    // } catch (IOException ex)
    // {
    // fail(ex.getMessage());
    // }
    // }

    public void observe(ChromatogramDTO chromatogram)
    {
        chromatograms.add(chromatogram);
    }

}
