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

package ch.systemsx.cisd.cina.dss.info;

/**
 * A registry for the different entity types CINA uses
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class EntityTypes
{
    /**
     * CINA carries out the following kinds of experiment: electron microscopy, light microscopy,
     * mass spectrometry, tomography, and a non-specific CINA experiment type.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static enum ExperimentTypes
    {
        ELECTRON_MICROSCOPY, LIGHT_MICROSCOPY, MASS_SPEC, TOMOGRAPHY, CINA_EXP_TYPE
    }

    /**
     * CINA utilizes the following sample types: original experimental sample, samples that result
     * from biochemical manipulation of the original, grid peperation, and non-specific CINA sample
     * type.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static enum SampleTypes
    {
        ORIGINAL, BIOCHEMISTRY, GRID_PREP, CINA_SAMPLE_TYPE
    }

    /**
     * Results and by products of experiments in CINA are data sets types: electron microscope
     * images, light microscope images, mass spectrograms, tomograms, µ-fluidics parameters, lab
     * notebooks, and a non-specific data set type.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static enum DataSetTypes
    {
        EM_IMAGE, LM_IMAGE, MASS_SPECTROGRAM, TOMOGRAM, U_FLUIDICS_PARAMETERS, LAB_NOTEBOOK,
        UNKNOWN
    }
}
