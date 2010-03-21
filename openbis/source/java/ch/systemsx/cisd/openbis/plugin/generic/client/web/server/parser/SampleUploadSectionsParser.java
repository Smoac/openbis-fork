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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.server.parser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.io.DelegatedReader;
import ch.systemsx.cisd.common.parser.IParserObjectFactory;
import ch.systemsx.cisd.common.parser.IParserObjectFactoryFactory;
import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.client.web.server.BisTabFileLoader;
import ch.systemsx.cisd.openbis.generic.client.web.server.NamedInputStream;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * Helps uploading samples of multiple types from one file. Parses the file.
 * 
 * @author Izabela Adamczyk
 * @author Tomasz Pylak
 */
public class SampleUploadSectionsParser
{

    public static class BatchSamplesOperation
    {
        private final List<NewSamplesWithTypes> samples;

        private final List<BatchRegistrationResult> resultList;

        private final String[] sampleCodes;

        public BatchSamplesOperation(List<NewSamplesWithTypes> samples,
                List<BatchRegistrationResult> resultList, String[] sampleCodes)
        {
            this.samples = samples;
            this.resultList = resultList;
            this.sampleCodes = sampleCodes;
        }

        public List<NewSamplesWithTypes> getSamples()
        {
            return samples;
        }

        public List<BatchRegistrationResult> getResultList()
        {
            return resultList;
        }

        public String[] getCodes()
        {
            return sampleCodes;
        }
    }

    public interface SampleCodeGenerator
    {
        /** generates 'size' codes for new samples */
        List<String> generateCodes(int size);
    }

    public static BatchSamplesOperation prepareSamples(final SampleType sampleType,
            final Collection<NamedInputStream> files, String defaultGroupIdentifier,
            final SampleCodeGenerator sampleCodeGeneratorOrNull, final boolean allowExperiments,
            BatchOperationKind operationKind)
    {
        final List<NewSamplesWithTypes> newSamples = new ArrayList<NewSamplesWithTypes>();
        boolean isAutoGenerateCodes = (sampleCodeGeneratorOrNull != null);
        final List<BatchRegistrationResult> results =
                loadSamplesFromFiles(files, sampleType, isAutoGenerateCodes, newSamples,
                        allowExperiments, operationKind);
        if (defaultGroupIdentifier != null)
        {
            switch (operationKind)
            {
                case REGISTRATION:
                    if (isAutoGenerateCodes)
                    {
                        generateIdentifiers(defaultGroupIdentifier, sampleCodeGeneratorOrNull,
                                isAutoGenerateCodes, newSamples);
                    }
                    break;
                case UPDATE:
                    fillIdentifiers(defaultGroupIdentifier, newSamples);
                    break;
            }
        }
        return new BatchSamplesOperation(newSamples, results, parseCodes(newSamples));
    }

    private static String[] parseCodes(final List<NewSamplesWithTypes> newSamples)
    {
        List<String> codes = new ArrayList<String>();
        for (NewSamplesWithTypes st : newSamples)
        {
            for (NewSample s : st.getNewSamples())
            {
                codes.add(SampleIdentifierFactory.parse(s.getIdentifier()).getSampleCode());
            }
        }
        return codes.toArray(new String[0]);
    }

    private static BisTabFileLoader<NewSample> createSampleLoader(final SampleType sampleType,
            final boolean isAutoGenerateCodes, final boolean allowExperiments,
            final BatchOperationKind operationKind)
    {
        final BisTabFileLoader<NewSample> tabFileLoader =
                new BisTabFileLoader<NewSample>(new IParserObjectFactoryFactory<NewSample>()
                    {
                        public final IParserObjectFactory<NewSample> createFactory(
                                final IPropertyMapper propertyMapper) throws ParserException
                        {
                            switch (operationKind)
                            {
                                case REGISTRATION:
                                    return new NewSampleParserObjectFactory(sampleType,
                                            propertyMapper, isAutoGenerateCodes == false,
                                            allowExperiments);
                                case UPDATE:
                                    return new UpdatedSampleParserObjectFactory(sampleType,
                                            propertyMapper, isAutoGenerateCodes == false,
                                            allowExperiments);
                            }
                            throw new UnsupportedOperationException(operationKind
                                    + " is not supported");
                        }
                    }, true);
        return tabFileLoader;
    }

    static class FileSection
    {
        private final String content;

        private final String sectionName;

        public FileSection(String content, String sectionName)
        {
            this.sectionName = sectionName;
            this.content = content;
        }

        public String getContent()
        {
            return content;
        }

        public String getSectionName()
        {
            return sectionName;
        }
    }

    private static List<FileSection> extractSections(InputStream stream)
    {
        List<FileSection> sections = new ArrayList<FileSection>();
        InputStreamReader reader = new InputStreamReader(stream);
        try
        {
            LineIterator it = IOUtils.lineIterator(reader);
            StringBuilder sb = null;
            String sectionName = null;
            while (it.hasNext())
            {
                String line = it.nextLine();
                String newSectionName = tryGetSectionName(line);
                if (newSectionName != null)
                {
                    if (sectionName != null && sb != null)
                    {
                        sections.add(new FileSection(sb.toString(), sectionName));
                    }
                    sectionName = newSectionName;
                    sb = new StringBuilder();
                } else if (sectionName == null || sb == null)
                {
                    throw new UserFailureException("Discovered the unnamed section in the file");
                } else
                {
                    if (sb.length() != 0)
                    {
                        sb.append("\n");
                    }
                    sb.append(line);
                }
                if (it.hasNext() == false)
                {
                    sections.add(new FileSection(sb.toString(), sectionName));
                }
            }
        } finally
        {
            IOUtils.closeQuietly(reader);
        }
        return sections;
    }

    private static String tryGetSectionName(String line)
    {
        final String beginSection = "[";
        final String endSection = "]";
        if (line == null)
        {
            return null;
        }
        String trimmedLine = line.trim();
        if (trimmedLine.startsWith(beginSection) && trimmedLine.endsWith(endSection))
        {
            return trimmedLine.substring(1, trimmedLine.length() - 1);
        } else
        {
            return null;
        }
    }

    private static List<BatchRegistrationResult> loadSamplesFromFiles(
            Collection<NamedInputStream> uploadedFiles, SampleType sampleType,
            boolean isAutoGenerateCodes, final List<NewSamplesWithTypes> newSamples,
            boolean allowExperiments, BatchOperationKind operationKind)
    {
        final List<BatchRegistrationResult> results =
                new ArrayList<BatchRegistrationResult>(uploadedFiles.size());
        for (final NamedInputStream multipartFile : uploadedFiles)
        {
            List<FileSection> sampleSections = new ArrayList<FileSection>();
            if (sampleType.isDefinedInFileSampleTypeCode())
            {
                sampleSections.addAll(extractSections(multipartFile.getInputStream()));
            } else
            {
                sampleSections.add(new FileSection(new String(multipartFile.getBytes()), sampleType
                        .getCode()));
            }
            int sampleCounter = 0;
            for (FileSection fs : sampleSections)
            {
                final StringReader stringReader = new StringReader(fs.getContent());
                SampleType typeFromSection = new SampleType();
                typeFromSection.setCode(fs.getSectionName());
                final BisTabFileLoader<NewSample> tabFileLoader =
                        createSampleLoader(typeFromSection, isAutoGenerateCodes, allowExperiments,
                                operationKind);
                String sectionInFile =
                        sampleSections.size() == 1 ? "" : " (section:" + fs.getSectionName() + ")";
                final List<NewSample> loadedSamples =
                        tabFileLoader.load(new DelegatedReader(stringReader, multipartFile
                                .getOriginalFilename()
                                + sectionInFile));
                if (loadedSamples.size() > 0)
                {
                    newSamples.add(new NewSamplesWithTypes(typeFromSection, loadedSamples));
                    sampleCounter += loadedSamples.size();
                }
            }
            results.add(new BatchRegistrationResult(multipartFile.getOriginalFilename(), String
                    .format("%s of %d sample(s) is complete.", operationKind.getDescription(),
                            sampleCounter)));
        }
        return results;
    }

    private static void generateIdentifiers(String defaultGroupIdentifier,
            SampleCodeGenerator sampleCodeGenerator, boolean isAutoGenerateCodes,
            List<NewSamplesWithTypes> newSamplesWithTypes)
    {
        assert sampleCodeGenerator != null;
        assert isAutoGenerateCodes == true;
        for (NewSamplesWithTypes st : newSamplesWithTypes)
        {
            final List<NewSample> newSamples = st.getNewSamples();
            List<String> codes = sampleCodeGenerator.generateCodes(newSamples.size());
            for (int i = 0; i < newSamples.size(); i++)
            {
                newSamples.get(i).setIdentifier(defaultGroupIdentifier + "/" + codes.get(i));
            }
        }
    }

    private static void fillIdentifiers(String defaultGroupIdentifier,
            List<NewSamplesWithTypes> newSamplesWithTypes)
    {
        for (NewSamplesWithTypes st : newSamplesWithTypes)
        {
            final List<NewSample> newSamples = st.getNewSamples();
            for (int i = 0; i < newSamples.size(); i++)
            {
                final String identifierFromFile = newSamples.get(i).getIdentifier();
                // Leave identifier specified in the file if it contains information about group,
                // otherwise fill default group.
                if (identifierFromFile.contains("/"))
                {
                    continue;
                } else
                {
                    newSamples.get(i).setIdentifier(
                            defaultGroupIdentifier + "/" + identifierFromFile);
                }
            }
        }
    }

}
