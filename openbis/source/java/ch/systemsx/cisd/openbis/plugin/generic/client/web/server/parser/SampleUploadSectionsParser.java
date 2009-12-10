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

import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.io.DelegatedReader;
import ch.systemsx.cisd.common.parser.IParserObjectFactory;
import ch.systemsx.cisd.common.parser.IParserObjectFactoryFactory;
import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.common.spring.IUncheckedMultipartFile;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.client.web.server.BisTabFileLoader;
import ch.systemsx.cisd.openbis.generic.client.web.server.UploadedFilesBean;
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

        private Object operationDetailsOrNull;

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

        public Object getOperationDetailsOrNull()
        {
            return operationDetailsOrNull;
        }

        public void setOperationDetailsOrNull(Object operationDetailsOrNull)
        {
            this.operationDetailsOrNull = operationDetailsOrNull;
        }
    }

    public interface SampleCodeGenerator
    {
        /** generates 'size' codes for new samples */
        List<String> generateCodes(int size);
    }

    public static BatchSamplesOperation prepareSamples(final SampleType sampleType,
            final UploadedFilesBean uploadedFiles, String defaultGroupIdentifier,
            final SampleCodeGenerator sampleCodeGeneratorOrNull, final boolean allowExperiments,
            BatchOperationKind operationKind)
    {
        final List<NewSamplesWithTypes> newSamples = new ArrayList<NewSamplesWithTypes>();
        boolean isAutoGenerateCodes = (sampleCodeGeneratorOrNull != null);
        final List<BatchRegistrationResult> results =
                loadSamplesFromFiles(uploadedFiles, sampleType, isAutoGenerateCodes, newSamples,
                        allowExperiments, operationKind);
        generateIdentifiersIfNecessary(defaultGroupIdentifier, sampleCodeGeneratorOrNull,
                isAutoGenerateCodes, newSamples);
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

    private static List<FileSection> extractSections(IUncheckedMultipartFile multipartFile)
    {
        List<FileSection> sections = new ArrayList<FileSection>();
        InputStreamReader reader = new InputStreamReader(multipartFile.getInputStream());
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
            UploadedFilesBean uploadedFiles, SampleType sampleType, boolean isAutoGenerateCodes,
            final List<NewSamplesWithTypes> newSamples, boolean allowExperiments,
            BatchOperationKind operationKind)
    {

        final List<BatchRegistrationResult> results =
                new ArrayList<BatchRegistrationResult>(uploadedFiles.size());

        for (final IUncheckedMultipartFile multipartFile : uploadedFiles.iterable())
        {
            List<FileSection> sampleSections = new ArrayList<FileSection>();
            if (sampleType.isDefinedInFileSampleTypeCode())
            {
                sampleSections.addAll(extractSections(multipartFile));
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

    private static void generateIdentifiersIfNecessary(String defaultGroupIdentifier,
            SampleCodeGenerator sampleCodeGeneratorOrNull, boolean isAutoGenerateCodes,
            List<NewSamplesWithTypes> newSamples)
    {
        if (sampleCodeGeneratorOrNull != null)
        {
            for (NewSamplesWithTypes st : newSamples)
            {
                generateIdentifiers(defaultGroupIdentifier, sampleCodeGeneratorOrNull, st
                        .getNewSamples());
            }
        }
    }

    private static void generateIdentifiers(String defaultGroupIdentifier,
            final SampleCodeGenerator sampleCodeGenerator, final List<NewSample> newSamples)
    {
        List<String> codes = sampleCodeGenerator.generateCodes(newSamples.size());
        for (int i = 0; i < newSamples.size(); i++)
        {
            newSamples.get(i).setIdentifier(defaultGroupIdentifier + "/" + codes.get(i));
        }
    }

}
