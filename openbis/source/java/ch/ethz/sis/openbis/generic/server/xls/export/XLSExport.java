package ch.ethz.sis.openbis.generic.server.xls.export;

import static ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.DATA_SET;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.EXPERIMENT;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.SAMPLE;
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.MASTER_DATA_EXPORTABLE_KINDS;
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.VOCABULARY;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertyAssignmentsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.server.xls.export.helper.IXLSExportHelper;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.ISessionWorkspaceProvider;

public class XLSExport
{

    private static final String XLSX_EXTENSION = ".xlsx";

    private static final String ZIP_EXTENSION = ".zip";

    public static ExportResult export(final String filePrefix, final IApplicationServerApi api, final String sessionToken,
            final Collection<ExportablePermId> exportablePermIds, final boolean exportReferred,
            final Map<String, Map<String, Collection<String>>> exportProperties,
            final TextFormatting textFormatting) throws IOException
    {
        final PrepareWorkbookResult exportResult = prepareWorkbook(api, sessionToken, exportablePermIds,
                exportReferred, exportProperties, textFormatting);
        final Map<String, String> scripts = exportResult.getScripts();
        final ISessionWorkspaceProvider sessionWorkspaceProvider = CommonServiceProvider.getSessionWorkspaceProvider();
        final ByteArrayOutputStream baos = getByteArrayOutputStream(filePrefix, exportResult, scripts);
        try (final PipedInputStream pis = new PipedInputStream())
        {
            new Thread(() ->
            {
                try (final PipedOutputStream pos = new PipedOutputStream(pis))
                {
                    baos.writeTo(pos);
                } catch (final IOException e)
                {
                    throw new RuntimeException(e);
                }
            }).start();

            final String fullFileName = filePrefix + "." +
                    new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(new Date()) +
                    (scripts.isEmpty() ? XLSX_EXTENSION : ZIP_EXTENSION);
            sessionWorkspaceProvider.write(sessionToken, fullFileName, pis);
            return new ExportResult(fullFileName, exportResult.getWarnings());
        }
    }

    private static ByteArrayOutputStream getByteArrayOutputStream(final String outputFileName,
            final PrepareWorkbookResult exportResult, final Map<String, String> scripts) throws IOException
    {
        if (scripts.isEmpty())
        {
            try
            (
                    final Workbook wb = exportResult.getWorkbook();
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    final BufferedOutputStream bos = new BufferedOutputStream(baos)
            )
            {
                wb.write(bos);
                return baos;
            }
        } else
        {
            try
            (
                    final Workbook wb = exportResult.getWorkbook();
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    final ZipOutputStream zos = new ZipOutputStream(baos);
                    final BufferedOutputStream bos = new BufferedOutputStream(zos)
            )
            {
                for (final Map.Entry<String, String> script : scripts.entrySet())
                {
                    zos.putNextEntry(new ZipEntry(String.format("scripts/%s.py", script.getKey())));
                    bos.write(script.getValue().getBytes());
                    bos.flush();
                    zos.closeEntry();
                }

                zos.putNextEntry(new ZipEntry(outputFileName + XLSX_EXTENSION));
                wb.write(bos);
                return baos;
            }
        }
    }

    static PrepareWorkbookResult prepareWorkbook(final IApplicationServerApi api, final String sessionToken,
            Collection<ExportablePermId> exportablePermIds, final boolean exportReferred,
            final Map<String, Map<String, Collection<String>>> exportProperties, final TextFormatting textFormatting)
    {
        if (!isValid(exportablePermIds))
        {
            throw new IllegalArgumentException();
        }

        final Workbook wb = new XSSFWorkbook();
        wb.createSheet();

        final ExportHelperFactory exportHelperFactory = new ExportHelperFactory(wb);

        if (exportReferred)
        {
            exportablePermIds = expandReference(api, sessionToken, exportablePermIds, exportHelperFactory);
        }

        final Collection<Collection<ExportablePermId>> groupedExportablePermIds =
                putVocabulariesFirst(group(exportablePermIds));

        int rowNumber = 0;
        final Map<String, String> scripts = new HashMap<>();
        final Collection<String> warnings = new ArrayList<>();

        for (final Collection<ExportablePermId> exportablePermIdGroup : groupedExportablePermIds)
        {
            final ExportablePermId exportablePermId = exportablePermIdGroup.iterator().next();
            final IXLSExportHelper helper = exportHelperFactory.getHelper(exportablePermId.getExportableKind());
            final List<String> permIds = exportablePermIdGroup.stream()
                    .map(permId -> permId.getPermId().getPermId()).collect(Collectors.toList());
            final Map<String, Collection<String>> entityTypeExportPropertiesMap = exportProperties == null
                    ? null
                    : exportProperties.get(exportablePermId.getExportableKind().toString());
            final IXLSExportHelper.AdditionResult additionResult = helper.add(api, sessionToken, wb, permIds, rowNumber,
                    entityTypeExportPropertiesMap, textFormatting);
            rowNumber = additionResult.getRowNumber();
            warnings.addAll(additionResult.getWarnings());

            final IEntityType entityType = helper.getEntityType(api, sessionToken,
                    exportablePermId.getPermId().getPermId());

            if (entityType != null)
            {
                final Plugin validationPlugin = entityType.getValidationPlugin();
                if (validationPlugin != null && validationPlugin.getScript() != null)
                {
                    scripts.put(validationPlugin.getName(), validationPlugin.getScript());
                }

                final Map<String, String> propertyAssignmentPluginScripts = entityType.getPropertyAssignments().stream()
                        .filter(propertyAssignment -> propertyAssignment.getPlugin() != null
                                && propertyAssignment.getPlugin().getScript() != null)
                        .map(PropertyAssignment::getPlugin)
                        .collect(Collectors.toMap(Plugin::getName, Plugin::getScript));

                scripts.putAll(propertyAssignmentPluginScripts);
            }
        }

        return new PrepareWorkbookResult(wb, scripts, warnings);
    }

    private static Collection<ExportablePermId> expandReference(final IApplicationServerApi api,
            final String sessionToken, final Collection<ExportablePermId> exportablePermIds,
            final ExportHelperFactory exportHelperFactory)
    {
        return exportablePermIds.stream().flatMap(exportablePermId ->
        {
            final Stream<ExportablePermId> expandedExportablePermIds = getExpandedExportablePermIds(api, sessionToken,
                    exportablePermId, new HashSet<>(Collections.singletonList(exportablePermId)), exportHelperFactory);
            return Stream.concat(expandedExportablePermIds, Stream.of(exportablePermId));
        }).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static Stream<ExportablePermId> getExpandedExportablePermIds(final IApplicationServerApi api,
            final String sessionToken, final ExportablePermId exportablePermId,
            final Set<ExportablePermId> processedIds, final ExportHelperFactory exportHelperFactory)
    {
        final IXLSExportHelper helper = exportHelperFactory.getHelper(exportablePermId.getExportableKind());
        if (helper != null)
        {
            final IPropertyAssignmentsHolder propertyAssignmentsHolder = helper
                    .getEntityType(api, sessionToken, exportablePermId.getPermId().getPermId());

            if (propertyAssignmentsHolder != null)
            {
                return propertyAssignmentsHolder.getPropertyAssignments().stream().flatMap(propertyAssignment ->
                        {
                            final PropertyType propertyType = propertyAssignment.getPropertyType();
                            switch (propertyType.getDataType())
                            {
                                case CONTROLLEDVOCABULARY:
                                {
                                    return Stream.of(new ExportablePermId(ExportableKind.VOCABULARY,
                                            propertyType.getVocabulary().getPermId()));
                                }
                                case SAMPLE:
                                {
                                    final SampleType sampleType = propertyType.getSampleType();
                                    final ExportablePermId samplePropertyExportablePermId =
                                            new ExportablePermId(ExportableKind.SAMPLE_TYPE,
                                                    new EntityTypePermId(sampleType.getCode(),
                                                            SAMPLE));

                                    if (processedIds.contains(samplePropertyExportablePermId))
                                    {
                                        return Stream.empty();
                                    } else
                                    {
                                        processedIds.add(samplePropertyExportablePermId);

                                        final Stream<ExportablePermId> samplePropertyExpandedExportablePermIds =
                                                getExpandedExportablePermIds(api, sessionToken,
                                                        samplePropertyExportablePermId, processedIds,
                                                        exportHelperFactory);

                                        return Stream.concat(samplePropertyExpandedExportablePermIds,
                                                Stream.of(samplePropertyExportablePermId));
                                    }
                                }
                                default:
                                {
                                    return Stream.empty();
                                }
                            }
                        });
            }
        }

        return Stream.empty();
    }

    static Collection<Collection<ExportablePermId>> group(final Collection<ExportablePermId> exportablePermIds)
    {
        final Map<ExportableKind, Collection<ExportablePermId>> groupMap = new EnumMap<>(ExportableKind.class);
        final Collection<Collection<ExportablePermId>> result = new ArrayList<>(exportablePermIds.size());
        for (final ExportablePermId permId : exportablePermIds)
        {
            final ExportableKind exportableKind = permId.getExportableKind();
            if (MASTER_DATA_EXPORTABLE_KINDS.contains(exportableKind))
            {
                result.add(Collections.singletonList(permId));
            } else
            {
                final Collection<ExportablePermId> foundGroup = groupMap.get(exportableKind);
                final Collection<ExportablePermId> group;

                if (foundGroup == null)
                {
                    group = new ArrayList<>();
                    groupMap.put(exportableKind, group);
                } else
                {
                    group = foundGroup;
                }

                group.add(permId);
            }
        }

        result.addAll(groupMap.values());

        return result;
    }

    static Collection<Collection<ExportablePermId>> putVocabulariesFirst(
            final Collection<Collection<ExportablePermId>> exportablePermIds)
    {
        final List<Collection<ExportablePermId>> result = new ArrayList<>(exportablePermIds.size());

        // Adding vocabularies first
        for (final Collection<ExportablePermId> group : exportablePermIds)
        {
            if (group.iterator().next().getExportableKind() == VOCABULARY)
            {
                result.add(group);
            }
        }

        // Adding other items
        for (final Collection<ExportablePermId> group : exportablePermIds)
        {
            if (group.iterator().next().getExportableKind() != VOCABULARY)
            {
                result.add(group);
            }
        }

        return result;
    }

    private static boolean isValid(final Collection<ExportablePermId> exportablePermIds)
    {
        boolean isValid = true;
        for (final ExportablePermId exportablePermId : exportablePermIds)
        {
            switch (exportablePermId.getExportableKind())
            {
                case SAMPLE_TYPE:
                {
                    isValid = exportablePermId.getPermId() instanceof EntityTypePermId &&
                            ((EntityTypePermId) exportablePermId.getPermId()).getEntityKind() == SAMPLE;
                    break;
                }
                case EXPERIMENT_TYPE:
                {
                    isValid = exportablePermId.getPermId() instanceof EntityTypePermId &&
                            ((EntityTypePermId) exportablePermId.getPermId()).getEntityKind() == EXPERIMENT;
                    break;
                }
                case DATASET_TYPE:
                {
                    isValid = exportablePermId.getPermId() instanceof EntityTypePermId &&
                            ((EntityTypePermId) exportablePermId.getPermId()).getEntityKind() == DATA_SET;
                    break;
                }
                case VOCABULARY:
                {
                    isValid = exportablePermId.getPermId() instanceof VocabularyPermId;
                    break;
                }
                case SPACE:
                {
                    isValid = exportablePermId.getPermId() instanceof SpacePermId;
                    break;
                }
            }

            if (isValid == false)
            {
                break;
            }
        }

        return isValid;
    }

    public static class PrepareWorkbookResult
    {

        private final Workbook workbook;

        private final Map<String, String> scripts;

        final Collection<String> warnings;

        public PrepareWorkbookResult(final Workbook workbook, final Map<String, String> scripts,
                final Collection<String> warnings)
        {
            this.workbook = workbook;
            this.scripts = scripts;
            this.warnings = warnings;
        }

        public Workbook getWorkbook()
        {
            return workbook;
        }

        public Map<String, String> getScripts()
        {
            return scripts;
        }

        public Collection<String> getWarnings()
        {
            return warnings;
        }

    }

    public enum TextFormatting
    {
        PLAIN, RICH
    }

    public static class ExportResult
    {

        final String fileName;

        final Collection<String> warnings;

        public ExportResult(final String fileName, final Collection<String> warnings)
        {
            this.fileName = fileName;
            this.warnings = warnings;
        }

        public String getFileName()
        {
            return fileName;
        }

        public Collection<String> getWarnings()
        {
            return warnings;
        }

    }

}
