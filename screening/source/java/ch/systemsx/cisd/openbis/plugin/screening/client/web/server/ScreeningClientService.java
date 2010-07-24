/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.server;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Component;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.mail.MailClientParameters;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.common.spring.IUncheckedMultipartFile;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.GenericTableRowColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericTableResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractClientService;
import ch.systemsx.cisd.openbis.generic.client.web.server.UploadedFilesBean;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.UserFailureExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericTableColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericTableRow;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.plugin.screening.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientService;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LibraryRegistrationInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialsSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;

/**
 * The {@link IScreeningClientService} implementation.
 * 
 * @author Tomasz Pylak
 */
@Component(value = ResourceNames.SCREENING_PLUGIN_SERVICE)
public final class ScreeningClientService extends AbstractClientService implements
        IScreeningClientService
{

    @Resource(name = ResourceNames.SCREENING_PLUGIN_SERVER)
    private IScreeningServer server;

    @Resource(name = ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames.GENERIC_PLUGIN_SERVER)
    private IGenericServer genericServer;

    @Resource(name = ResourceNames.MAIL_CLIENT_PARAMETERS)
    private MailClientParameters mailClientParameters;

    private static ThreadPoolExecutor executor =
            new ThreadPoolExecutor(1, 10, 360, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>());

    public ScreeningClientService()
    {
    }

    @Private
    ScreeningClientService(final IScreeningServer server,
            final IRequestContextProvider requestContextProvider)
    {
        super(requestContextProvider);
        this.server = server;
    }

    //
    // AbstractClientService
    //

    @Override
    protected final IServer getServer()
    {
        return server;
    }

    //
    // IScreeningClientService
    //

    @Override
    protected String getVersion()
    {
        return BuildAndEnvironmentInfo.INSTANCE.getFullVersion();
    }

    public final SampleParentWithDerived getSampleGenerationInfo(final TechId sampleId)
            throws UserFailureException
    {
        try
        {
            return server.getSampleInfo(getSessionToken(), sampleId);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public ExternalData getDataSetInfo(TechId datasetTechId)
    {
        try
        {
            return server.getDataSetInfo(getSessionToken(), datasetTechId);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public PlateContent getPlateContent(TechId plateId) throws UserFailureException
    {
        try
        {
            return server.getPlateContent(getSessionToken(), plateId);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public PlateImages getPlateContentForDataset(TechId datasetId)
    {
        try
        {
            return server.getPlateContentForDataset(getSessionToken(), datasetId);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public List<WellContent> getPlateLocations(TechId geneMaterialId,
            ExperimentIdentifier experimentIdentifier) throws UserFailureException
    {
        try
        {
            return server.getPlateLocations(getSessionToken(), geneMaterialId,
                    parseExperimentIdentifier(experimentIdentifier));
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public ResultSet<WellContent> listPlateLocations(
            DefaultResultSetConfig<String, WellContent> gridCriteria,
            final PlateMaterialsSearchCriteria materialCriteria)
    {
        try
        {
            return listEntities(gridCriteria, new IOriginalDataProvider<WellContent>()
                {
                    public List<WellContent> getOriginalData() throws UserFailureException
                    {
                        return server.listPlateLocations(getSessionToken(), materialCriteria);
                    }
                });
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public String prepareExportPlateLocations(TableExportCriteria<WellContent> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    private static ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier parseExperimentIdentifier(
            ExperimentIdentifier experimentIdentifier)
    {
        return new ExperimentIdentifierFactory(experimentIdentifier.getIdentifier())
                .createIdentifier();
    }

    public GenericTableResultSet listPlateMetadata(
            IResultSetConfig<String, GenericTableRow> criteria, TechId sampleId)
            throws UserFailureException
    {
        PlateMetadataProvider dataProvider =
                new PlateMetadataProvider(server, getSessionToken(), sampleId);
        // This is a different kind of query because the criteria does not define which columns
        // are available -- the provider does. Thus, inform the criteria which columns are
        // available.
        updateResultSetColumnsFromProvider(criteria, dataProvider);
        ResultSet<GenericTableRow> resultSet = listEntities(criteria, dataProvider);
        return new GenericTableResultSet(resultSet, dataProvider.getHeaders());
    }

    /**
     * In the plate metadata query, the result set does not define which columns are available --
     * the provider does. This method informs the result set about the columns available in the
     * provider.
     */
    private void updateResultSetColumnsFromProvider(
            IResultSetConfig<String, GenericTableRow> criteria, PlateMetadataProvider dataProvider)
    {
        Set<IColumnDefinition<GenericTableRow>> columns = criteria.getAvailableColumns();
        Set<String> availableColumnIdentifiers = extractColumnIdentifiers(columns);

        List<GenericTableColumnHeader> headers = dataProvider.getHeaders();
        for (GenericTableColumnHeader header : headers)
        {
            // the header's code is the same as the definition's identifier
            if (!availableColumnIdentifiers.contains(header.getCode()))
            {
                columns.add(new GenericTableRowColumnDefinition(header, header.getTitle()));
            }
        }
    }

    private static Set<String> extractColumnIdentifiers(
            Set<IColumnDefinition<GenericTableRow>> columns)
    {
        Set<String> availableColumnIdentifiers = new HashSet<String>();
        for (IColumnDefinition<GenericTableRow> colDef : columns)
        {
            availableColumnIdentifiers.add(colDef.getIdentifier());
        }
        return availableColumnIdentifiers;
    }

    public String prepareExportPlateMetadata(TableExportCriteria<GenericTableRow> criteria)
            throws UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    public void registerLibrary(LibraryRegistrationInfo details) throws UserFailureException

    {
        final String sessionToken = getSessionToken();
        HttpSession session = getHttpSession();
        UploadedFilesBean uploadedFiles = null;
        String sessionKey = details.getSessionKey();
        String experiment = details.getExperiment();
        try
        {
            String space =
                    new ExperimentIdentifierFactory(experiment).createIdentifier().getSpaceCode();
            uploadedFiles = getUploadedFiles(sessionKey, session);
            for (IUncheckedMultipartFile file : uploadedFiles.iterable())
            {
                LibraryExtractor extractor =
                        new LibraryExtractor(file.getInputStream(), experiment, space, details
                                .getPlateGeometry(), details.getScope());
                extractor.extract();
                executor.submit(new LibraryRegistrationTask(sessionToken, details.getUserEmail(),
                        extractor.getNewGenes(), extractor.getNewOligos(), extractor
                                .getNewSamplesWithType(), genericServer, mailClientParameters));
            }
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        } finally
        {
            cleanUploadedFiles(sessionKey, session, uploadedFiles);
        }

    }

    public Vocabulary getPlateGeometryVocabulary() throws UserFailureException
    {
        final String sessionToken = getSessionToken();
        return server.getVocabulary(sessionToken, ScreeningConstants.PLATE_GEOMETRY);
    }
}
