package ch.ethz.bsse.cisd.dsu.properties;

import java.util.HashMap;

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

/**
 * @author Manuel Kohler
 */

public class GenomeMap
{

    private static String PATH_TO_GENOMES = "/array0/Genomes/";

    private static HashMap<String, String> genomePaths = new HashMap<String, String>();
    static
    {
        genomePaths.put("PHIX", PATH_TO_GENOMES + "PhiX");
        genomePaths.put("MUS_MUSCULUS", PATH_TO_GENOMES + "MusMus/Ncbi37_Ensembl49");
        genomePaths.put("HOMO_SAPIENS", PATH_TO_GENOMES + "HomSap/Release36.50");
        genomePaths.put("DROSOPHILA_MELANOGASTER", PATH_TO_GENOMES + "DrosMel/Release5");
        genomePaths.put("CAENORHABDITIS_ELEGANS", PATH_TO_GENOMES + "Celegans/Release112708");
        genomePaths.put("ESCHERICHIA_COLI", PATH_TO_GENOMES + "EColi");
        genomePaths.put("RATTUS_NORVEGICUS", PATH_TO_GENOMES + "");
    }

    public static String getGenomePath(String genome)
    {
        return genomePaths.get(genome);
    }

    private static HashMap<String, String> endType = new HashMap<String, String>();
    static
    {
        endType.put("SINGLE_READ", "eland_extended");
        endType.put("PAIRED_END", "eland_pair");
    }

    public static String getEndType(String openbisEndType)
    {
        return endType.get(openbisEndType);
    }

    private static HashMap<String, String> bowtieIndex = new HashMap<String, String>();
    static
    {
        bowtieIndex.put("PHIX", "phiX");
        bowtieIndex.put("MUS_MUSCULUS", "ncbi37_mm9");
        bowtieIndex.put("HOMO_SAPIENS", "h_sapiens_37_asm");
        bowtieIndex.put("DROSOPHILA_MELANOGASTER", "d_melanogaster_fb5_22");
    }

    public static String getBowtieIndex(String genome)
    {
        return bowtieIndex.get(genome);
    }
}
