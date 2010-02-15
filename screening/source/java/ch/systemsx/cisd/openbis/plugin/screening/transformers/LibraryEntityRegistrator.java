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

package ch.systemsx.cisd.openbis.plugin.screening.transformers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Creates files to register genes, oligos and plate with wells.
 * <p>
 * This registrator works with the assumption that the entities which should be registered do not
 * already exist in openBIS. If it is the case we should extend the implementation to fetch existing
 * entities, LIBRARY_ID property can be used to recognize that.
 * </p>
 * 
 * @author Tomasz Pylak
 */
public class LibraryEntityRegistrator
{
    private static final String GENES_FILE_NAME = "genes.txt";

    private static final String OLIGOS_FILE_NAME = "oligos.txt";

    private static final String PLATES_FILE_NAME = "plates.txt";

    private final GeneRegistrator geneRegistrator;

    private final OligoRegistrator oligoRegistrator;

    private final PlateRegistrator plateRegistrator;

    public LibraryEntityRegistrator(IScreeningLibraryColumnExtractor extractor,
            String experimentIdentifier, String plateGeometry, String groupCode) throws IOException
    {
        this.geneRegistrator = new GeneRegistrator(new File(GENES_FILE_NAME));
        this.oligoRegistrator =
                new OligoRegistrator(new File(OLIGOS_FILE_NAME), extractor
                        .getAdditionalOligoPropertyNames());
        this.plateRegistrator =
                new PlateRegistrator(new File(PLATES_FILE_NAME), experimentIdentifier,
                        plateGeometry, groupCode);
    }

    public void register(IScreeningLibraryColumnExtractor extractor, String[] row)
            throws IOException
    {
        String geneId = geneRegistrator.register(extractor, row);
        String oligoId = oligoRegistrator.register(extractor, row, geneId);
        String plateId = plateRegistrator.registerPlate(extractor, row);
        plateRegistrator.registerWell(extractor, row, plateId, oligoId);
    }

    abstract static protected class AbstractMetadataRegistrator
    {
        private static final String TAB = "\t";

        private final OutputStream stream;

        protected AbstractMetadataRegistrator(File file) throws FileNotFoundException
        {
            this.stream = new FileOutputStream(file);
        }

        protected void writeLine(String... tokens) throws IOException
        {
            writeLine(join(tokens));
        }

        // joins token into one line adding separators in between
        public static String join(String... tokens)
        {
            return StringUtils.join(tokens, TAB);
        }

        private void writeLine(String line) throws IOException
        {
            IOUtils.writeLines(Arrays.asList(line), "\n", stream);
        }
    }

    private static class PlateRegistrator extends AbstractMetadataRegistrator
    {
        private static final String HEADER_PLATES =
                "[PLATE]\n" + join("identifier", "experiment", "$PLATE_GEOMETRY");

        private static final String HEADER_OLIGOS =
                "[OLIGO_WELL]\n" + join("identifier", "container", "OLIGO");

        private final Set<String/* plate code */> registeredPlates;

        private final String experimentIdentifier;

        private final String plateGeometry;

        private final String groupCode;

        // we register wells and plates in the same file. This flag tells us in which section we
        // are, the one for plates or one for wells
        private boolean lastRegisteredWasWell;

        public PlateRegistrator(File outputFile, String experimentIdentifier, String plateGeometry,
                String groupCode) throws IOException
        {
            super(outputFile);
            this.experimentIdentifier = experimentIdentifier;
            this.plateGeometry = plateGeometry;
            this.groupCode = groupCode;
            this.registeredPlates = new HashSet<String>();
            lastRegisteredWasWell = false;
            writeLine(HEADER_PLATES);
        }

        /** @return sampleIdentifier */
        public String registerPlate(IScreeningLibraryColumnExtractor extractor, String[] row)
                throws IOException
        {
            String plateCode = extractor.getPlateCode(row);
            String sampleIdentifier = getSampleIdentifier(plateCode);
            if (registeredPlates.contains(plateCode) == false)
            {
                if (lastRegisteredWasWell)
                {
                    lastRegisteredWasWell = false;
                    writeLine(HEADER_PLATES);
                }
                writeLine(sampleIdentifier, experimentIdentifier, plateGeometry);
                registeredPlates.add(plateCode);
            }
            return sampleIdentifier;
        }

        private String getSampleIdentifier(String plateCode)
        {
            return "/" + groupCode + "/" + plateCode;
        }

        public void registerWell(IScreeningLibraryColumnExtractor extractor, String[] row,
                String plateId, String oligoId) throws IOException
        {
            if (lastRegisteredWasWell == false)
            {
                lastRegisteredWasWell = true;
                writeLine(HEADER_OLIGOS);
            }
            String wellCode = extractor.getWellCode(row);
            String wellIdentifier = plateId + ":" + wellCode;
            String oligoMaterialProperty = oligoId + " (OLIGO)";
            writeLine(wellIdentifier, plateId, oligoMaterialProperty);
        }

    }

    private static class GeneRegistrator extends AbstractMetadataRegistrator
    {
        private static final String HEADER = join("CODE", "DESCRIPTION", "LIBRARY_ID");

        private final Set<String/* gene code */> registeredGenes;

        public GeneRegistrator(File genesFile) throws IOException
        {
            super(genesFile);
            this.registeredGenes = new HashSet<String>();
            writeLine(HEADER);
        }

        // / returns gene id
        public String register(IScreeningLibraryColumnExtractor extractor, String[] row)
                throws IOException
        {
            String geneSymbol = extractor.getGeneCode(row);
            if (registeredGenes.contains(geneSymbol) == false)
            {
                String desc = extractor.getGeneDescription(row);
                String libraryId = extractor.getGeneId(row);
                writeLine(geneSymbol, desc, libraryId);
                registeredGenes.add(geneSymbol);
            }
            return geneSymbol;
        }
    }

    private static class OligoRegistrator extends AbstractMetadataRegistrator
    {
        private static final String HEADER =
                join("CODE", "NUCLEOTIDE_SEQUENCE", "INHIBITOR_OF", "LIBRARY_ID");

        private final Set<String/* code */> registeredOligos;

        private final List<String> additionalPropertyNames;

        public OligoRegistrator(File file, List<String> additionalPropertyNames) throws IOException
        {
            super(file);
            this.registeredOligos = new HashSet<String>();
            this.additionalPropertyNames = additionalPropertyNames;
            writeLine(createHeader(additionalPropertyNames));
        }

        private static String createHeader(List<String> additionalPropertyNames)
        {
            String header = HEADER;
            for (String propertyName : additionalPropertyNames)
            {
                header = join(header, propertyName);
            }
            return header;
        }

        // / returns openbis id
        public String register(IScreeningLibraryColumnExtractor extractor, String[] row,
                String inhibitedGeneCode) throws IOException
        {
            String geneSymbol = extractor.getGeneCode(row);
            String oligoId = extractor.getOligoId(row);
            String openbisOligoId = geneSymbol + "_" + oligoId;
            if (containsCaseInsensitive(registeredOligos, openbisOligoId) == false)
            {
                String seq = extractor.getRNASequence(row);
                String geneMaterialProperty = inhibitedGeneCode + " (GENE)";
                String line = join(openbisOligoId, seq, geneMaterialProperty, oligoId);
                // add additional properties
                List<String> propertyValues =
                        extractor.getAdditionalOligoPropertyValues(row, additionalPropertyNames);
                for (int i = 0; i < propertyValues.size(); i++)
                {
                    line = join(line, propertyValues.get(i));
                }

                writeLine(line);
                addCaseInsensitive(registeredOligos, openbisOligoId);
            }
            return openbisOligoId;
        }

        private void addCaseInsensitive(Set<String> set, String value)
        {
            set.add(value.toLowerCase());
        }

        private boolean containsCaseInsensitive(Set<String> set, String value)
        {
            return set.contains(value.toLowerCase());
        }
    }
}
