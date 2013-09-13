/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.api.v1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.common.spring.IUncheckedMultipartFile;
import ch.systemsx.cisd.openbis.generic.client.web.server.UploadedFilesBean;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.UserFailureExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.exception.SampleUniqueCodeViolationExceptionAbstract;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseCreateOrDeleteModification;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MetaprojectAssignmentsIds;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.NewVocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.WebAppSettings;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject.IMetaprojectId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewEntitiesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.parser.NamedInputStream;
import ch.systemsx.cisd.openbis.generic.shared.parser.SampleUploadSectionsParser;
import ch.systemsx.cisd.openbis.generic.shared.parser.SampleUploadSectionsParser.BatchSamplesOperation;
import ch.systemsx.cisd.openbis.generic.shared.parser.SampleUploadSectionsParser.SampleCodeGenerator;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

/**
 * @author Franz-Josef Elmer
 */
@Component(ResourceNames.GENERAL_INFORMATION_CHANGING_SERVICE_SERVER)
public class GeneralInformationChangingService extends
        AbstractServer<IGeneralInformationChangingService> implements
        IGeneralInformationChangingService
{
    public static final int MINOR_VERSION = 4;

    @Resource(name = ch.systemsx.cisd.openbis.generic.shared.ResourceNames.COMMON_SERVER)
    private ICommonServer server;

    // Default constructor needed by Spring
    public GeneralInformationChangingService()
    {
    }

    GeneralInformationChangingService(IOpenBisSessionManager sessionManager,
            IDAOFactory daoFactory, IPropertiesBatchManager propertiesBatchManager,
            ICommonServer server)
    {
        super(sessionManager, daoFactory, propertiesBatchManager);
        this.server = server;
    }

    @Override
    public IGeneralInformationChangingService createLogger(IInvocationLoggerContext context)
    {
        return new GeneralInformationChangingServiceLogger(sessionManager, context);
    }

    @Override
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    public void updateSampleProperties(String sessionToken, long sampleID,
            Map<String, String> properties)
    {
        checkSession(sessionToken);

        EntityHelper.updateSampleProperties(server, sessionToken, new TechId(sampleID), properties);
    }

    @Override
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_POWER_USER)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.VOCABULARY_TERM)
    public void addUnofficialVocabularyTerm(String sessionToken, TechId vocabularyId, String code,
            String label, String description, Long previousTermOrdinal)
    {
        server.addUnofficialVocabularyTerm(sessionToken, vocabularyId, code, label, description,
                previousTermOrdinal);
    }

    @Override
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_POWER_USER)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.VOCABULARY_TERM)
    public void addUnofficialVocabularyTerm(String sessionToken, Long vocabularyId,
            NewVocabularyTerm term)
    {
        TechId vocabularyTechId = new TechId(vocabularyId);
        server.addUnofficialVocabularyTerm(sessionToken, vocabularyTechId, term.getCode(),
                term.getLabel(), term.getDescription(), term.getPreviousTermOrdinal());
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @SuppressWarnings("deprecation")
    public WebAppSettings getWebAppSettings(String sessionToken, String webAppId)
    {
        final Session session = getSession(sessionToken);
        return new WebAppSettings(webAppId, displaySettingsProvider.getWebAppSettings(
                session.getPerson(), webAppId));
    }

    @Override
    @Transactional(readOnly = false)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public void setWebAppSettings(String sessionToken, WebAppSettings webAppSettings)
    {
        try
        {
            final Session session = getSession(sessionToken);
            PersonPE person = session.tryGetPerson();
            if (person != null)
            {
                synchronized (displaySettingsProvider)
                {
                    displaySettingsProvider.replaceWebAppSettings(person, webAppSettings);
                    getDAOFactory().getPersonDAO().updatePerson(person);
                }
            }
        } catch (InvalidSessionException e)
        {
            // ignore the situation when session is not available
        }
    }

    @Override
    @Transactional(readOnly = false)
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    public Metaproject createMetaproject(String sessionToken, String name, String descriptionOrNull)
    {
        Metaproject registration = new Metaproject();
        registration.setName(name);
        registration.setDescription(descriptionOrNull);
        return server.registerMetaproject(sessionToken, registration);
    }

    @Override
    @Transactional(readOnly = false)
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    public Metaproject updateMetaproject(String sessionToken, IMetaprojectId metaprojectId,
            String name, String descriptionOrNull)
    {
        Metaproject update = new Metaproject();
        update.setName(name);
        update.setDescription(descriptionOrNull);
        return server.updateMetaproject(sessionToken, metaprojectId, update);
    }

    @Override
    @Transactional(readOnly = false)
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    public void deleteMetaproject(String sessionToken, IMetaprojectId metaprojectId)
    {
        server.deleteMetaproject(sessionToken, metaprojectId, null);
    }

    @Override
    @Transactional(readOnly = false)
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    public void addToMetaproject(String sessionToken, IMetaprojectId metaprojectId,
            MetaprojectAssignmentsIds assignmentsToAdd)
    {
        server.addToMetaproject(sessionToken, metaprojectId, assignmentsToAdd);
    }

    @Override
    @Transactional(readOnly = false)
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    public void removeFromMetaproject(String sessionToken, IMetaprojectId metaprojectId,
            MetaprojectAssignmentsIds assignmentsToRemove)
    {
        server.removeFromMetaproject(sessionToken, metaprojectId, assignmentsToRemove);
    }

    @Override
    public int getMajorVersion()
    {
        return 1;
    }

    @Override
    public int getMinorVersion()
    {
        return MINOR_VERSION;
    }

    //
    // <TEST IMPLEMENTATION For registerSamples>
    //
    @Resource(name = ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames.GENERIC_PLUGIN_SERVER)
    private IGenericServer genericServer;

    @Resource(name = "request-context-provider")
    @Private
    public IRequestContextProvider requestContextProvider;

    @Override
    @Transactional(readOnly = false)
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    public final boolean registerSamples(
            final String sessionToken,
            final String sampleTypeCode,
            final String sessionKey,
            final String defaultGroupIdentifier)
    {
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType> sampleTypes = server.listSampleTypes(sessionToken);
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType sampleType = null;
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType auxSampleType : sampleTypes)
        {
            if (auxSampleType.getCode().equals(sampleTypeCode))
            {
                sampleType = auxSampleType;
                break;
            }
        }

        boolean isAutogenerateCodes = defaultGroupIdentifier != null;

        BatchOperationKind operationKind = BatchOperationKind.REGISTRATION;
        BatchSamplesOperation info =
                parseSamples(sessionToken, sampleType, sessionKey, defaultGroupIdentifier, isAutogenerateCodes, true, null, operationKind);
        try
        {
            genericServer.registerOrUpdateSamples(sessionToken, info.getSamples());
            return true; // info.getResultList();
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            if (e.getCause() instanceof SampleUniqueCodeViolationExceptionAbstract)
            {
                SampleUniqueCodeViolationExceptionAbstract codeException = (SampleUniqueCodeViolationExceptionAbstract) e.getCause();

                if (isAutogenerateCodes)
                {
                    throw new UserFailureException(
                            String.format(
                                    "Import failed because the autogenerated codes are no longer unique. Somebody has created a sample (code: %s) that matched one of the autogenerated codes. Please run the import once again.",
                                    codeException.getSampleCode()));
                } else
                {
                    throw new UserFailureException(String.format(
                            "Import failed because sample (code: %s) already exists.",
                            codeException.getSampleCode()));
                }
            } else
            {
                throw UserFailureExceptionTranslator.translate(e);
            }
        }
    }

    private BatchSamplesOperation parseSamples(
            final String sessionToken,
            final ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType sampleType,
            final String sessionKey,
            String defaultGroupIdentifier,
            final boolean isAutoGenerateCodes,
            final boolean allowExperiments,
            String excelSheetName,
            BatchOperationKind operationKind)
    {
        HttpSession httpSession = getHttpSession();
        UploadedFilesBean uploadedFiles = null;
        try
        {
            uploadedFiles = getUploadedFiles(sessionKey, httpSession);
            return parseSamples(sessionToken, sampleType, httpSession, uploadedFiles, defaultGroupIdentifier,
                    isAutoGenerateCodes, allowExperiments, excelSheetName, operationKind);
        } finally
        {
            cleanUploadedFiles(sessionKey, httpSession, uploadedFiles);
        }
    }

    private BatchSamplesOperation parseSamples(
            final String sessionToken,
            final ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType sampleType,
            HttpSession httpSession,
            UploadedFilesBean uploadedFiles,
            String defaultGroupIdentifier,
            final boolean isAutoGenerateCodes,
            final boolean allowExperiments,
            String excelSheetName,
            BatchOperationKind operationKind)
    {
        boolean updateExisting = (operationKind == BatchOperationKind.UPDATE);
        SampleCodeGenerator sampleCodeGeneratorOrNull =
                tryGetSampleCodeGenerator(sessionToken, isAutoGenerateCodes, sampleType.getGeneratedCodePrefix());
        Collection<NamedInputStream> files = new ArrayList<NamedInputStream>(uploadedFiles.size());
        for (IUncheckedMultipartFile f : uploadedFiles.iterable())
        {
            files.add(new NamedInputStream(f.getInputStream(), f.getOriginalFilename()));
        }
        BatchSamplesOperation batchSamplesOperation =
                SampleUploadSectionsParser.prepareSamples(sampleType, files,
                        defaultGroupIdentifier, sampleCodeGeneratorOrNull, allowExperiments,
                        excelSheetName, operationKind);
        setUpdatePossibility(batchSamplesOperation.getSamples(), updateExisting);
        return batchSamplesOperation;

    }

    protected static UploadedFilesBean getUploadedFiles(String sessionKey, HttpSession session)
    {
        if (session.getAttribute(sessionKey) == null
                || session.getAttribute(sessionKey) instanceof UploadedFilesBean == false)
        {
            throw new IllegalStateException(String.format(
                    "No UploadedFilesBean object as session attribute '%s' found.", sessionKey));
        }
        return (UploadedFilesBean) session.getAttribute(sessionKey);
    }

    protected static void cleanUploadedFiles(final String sessionKey, HttpSession session,
            UploadedFilesBean uploadedFiles)
    {
        if (uploadedFiles != null)
        {
            uploadedFiles.deleteTransferredFiles();
        }
        if (session != null)
        {
            session.removeAttribute(sessionKey);
        }
    }

    private SampleCodeGenerator tryGetSampleCodeGenerator(final String sessionToken, boolean isAutoGenerateCodes,
            final String codePrefix)
    {
        if (isAutoGenerateCodes)
        {
            return new SampleCodeGenerator()
                {
                    @Override
                    public List<String> generateCodes(int size)
                    {
                        return genericServer.generateCodes(sessionToken, codePrefix,
                                EntityKind.SAMPLE, size);
                    }
                };
        } else
        {
            return null;
        }
    }

    private static void setUpdatePossibility(
            List<? extends NewEntitiesWithTypes<?, ?>> batchOperation, boolean updateExisting)
    {
        for (NewEntitiesWithTypes<?, ?> entitiesWithTypes : batchOperation)
        {
            entitiesWithTypes.setAllowUpdateIfExist(updateExisting);
        }
    }

    protected final HttpSession getHttpSession()
    {
        return getOrCreateHttpSession(false);
    }

    private final HttpSession getOrCreateHttpSession(final boolean create)
    {
        return requestContextProvider.getHttpServletRequest().getSession(create);
    }
}
