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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.IGenericClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Person;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGeneration;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.DtoConverters;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.GroupTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.PersonTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.PropertyTypeTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.RoleAssignmentTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.SamplePropertyTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.SampleTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.SampleTypeTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.UserFailureExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSampleCriteriaDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleGenerationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;
import ch.systemsx.cisd.openbis.plugin.AbstractClientService;
import ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames;

/**
 * The {@link IGenericClientService} implementation.
 * 
 * @author Franz-Josef Elmer
 */
@Component(value = ResourceNames.GENERIC_SERVICE)
public final class GenericClientService extends AbstractClientService implements
        IGenericClientService
{

    @Resource(name = ResourceNames.GENERIC_SERVER)
    private IGenericServer genericServer;

    private final static RoleCode translateRoleSetCode(final String code)
    {
        if ("INSTANCE_ADMIN".compareTo(code) == 0)
        {
            return RoleCode.ADMIN;
        } else if ("GROUP_ADMIN".compareTo(code) == 0)
        {
            return RoleCode.ADMIN;
        } else if ("USER".compareTo(code) == 0)
        {
            return RoleCode.USER;
        } else if ("OBSERVER".compareTo(code) == 0)
        {
            return RoleCode.OBSERVER;
        } else
        {
            throw new IllegalArgumentException("Unknown role set");
        }
    }

    //
    // AbstractClientService
    //

    @Override
    protected final IServer getServer()
    {
        return genericServer;
    }

    //
    // IGenericClientService
    //

    public final List<Group> listGroups(final String databaseInstanceCode)
    {
        try
        {
            final DatabaseInstanceIdentifier identifier =
                    new DatabaseInstanceIdentifier(databaseInstanceCode);
            final List<Group> result = new ArrayList<Group>();
            final List<GroupPE> groups = genericServer.listGroups(getSessionToken(), identifier);
            for (final GroupPE group : groups)
            {
                result.add(GroupTranslator.translate(group));
            }
            return result;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void registerGroup(final String groupCode, final String descriptionOrNull,
            final String groupLeaderOrNull)
    {
        try
        {
            final String sessionToken = getSessionToken();
            genericServer.registerGroup(sessionToken, groupCode, descriptionOrNull,
                    groupLeaderOrNull);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final List<Person> listPersons()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {

        try
        {
            final List<Person> result = new ArrayList<Person>();
            final List<PersonPE> persons = genericServer.listPersons(getSessionToken());
            for (final PersonPE person : persons)
            {
                result.add(PersonTranslator.translate(person));
            }
            return result;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void registerPerson(final String code)
    {
        try
        {
            final String sessionToken = getSessionToken();
            genericServer.registerPerson(sessionToken, code);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final List<RoleAssignment> listRoles()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final List<RoleAssignment> result = new ArrayList<RoleAssignment>();
            final List<RoleAssignmentPE> roles = genericServer.listRoles(getSessionToken());
            for (final RoleAssignmentPE role : roles)
            {
                result.add(RoleAssignmentTranslator.translate(role));
            }
            return result;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void registerGroupRole(final String roleSetCode, final String group,
            final String person)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final GroupIdentifier groupIdentifier =
                    new GroupIdentifier(DatabaseInstanceIdentifier.HOME, group);
            final String sessionToken = getSessionToken();
            genericServer.registerGroupRole(sessionToken, translateRoleSetCode(roleSetCode),
                    groupIdentifier, person);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void registerInstanceRole(final String roleSetCode, final String person)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            genericServer.registerInstanceRole(sessionToken, translateRoleSetCode(roleSetCode),
                    person);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void deleteGroupRole(final String roleSetCode, final String group,
            final String person)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final GroupIdentifier groupIdentifier =
                    new GroupIdentifier(DatabaseInstanceIdentifier.HOME, group);
            final String sessionToken = getSessionToken();
            genericServer.deleteGroupRole(sessionToken, translateRoleSetCode(roleSetCode),
                    groupIdentifier, person);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }

    }

    public final void deleteInstanceRole(final String roleSetCode, final String person)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            genericServer.deleteInstanceRole(sessionToken, translateRoleSetCode(roleSetCode),
                    person);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }

    }

    public final List<SampleType> listSampleTypes()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final List<SampleTypePE> sampleTypes = genericServer.listSampleTypes(getSessionToken());
            final List<SampleType> result = new ArrayList<SampleType>();
            for (final SampleTypePE sampleTypePE : sampleTypes)
            {
                result.add(SampleTypeTranslator.translate(sampleTypePE));
            }
            return result;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public List<Sample> listSamples(final ListSampleCriteria listCriteria,
            final List<PropertyType> propertyCodes)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final ListSampleCriteriaDTO criteria = createCriteriaDTO(listCriteria);
            final List<SamplePE> samplePEs = genericServer.listSamples(getSessionToken(), criteria);
            final List<SamplePropertyPE> propertiesMap =
                    genericServer.listSamplesProperties(getSessionToken(), criteria,
                            PropertyTypeTranslator.translate(propertyCodes));
            final List<Sample> result = new ArrayList<Sample>();
            for (final SamplePE sample : samplePEs)
            {
                result.add(SampleTranslator.translate(sample));
            }
            setSampleProperties(result, propertiesMap);
            return result;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    private static void setSampleProperties(final List<Sample> samples,
            final List<SamplePropertyPE> properties)
    {
        final Map<Long, List<SampleProperty>> propertiesMap = splitForSamples(properties);
        for (final Sample sample : samples)
        {
            final long sampleId = sample.getId();
            List<SampleProperty> props = sample.getProperties();
            if (props == null)
            {
                props = new ArrayList<SampleProperty>();
            }
            final List<SampleProperty> list = propertiesMap.get(sampleId);
            if (list != null)
            {
                props.addAll(list);
            }
            sample.setProperties(props);
        }
    }

    private static ListSampleCriteriaDTO createCriteriaDTO(final ListSampleCriteria listCriteria)
    {
        final ListSampleCriteriaDTO criteria = new ListSampleCriteriaDTO();
        final String containerIdentifier = listCriteria.getContainerIdentifier();
        if (containerIdentifier != null)
        {
            criteria.setContainerIdentifier(SampleIdentifierFactory.parse(containerIdentifier));
        } else
        {
            criteria.setOwnerIdentifiers(createOwnerIdentifiers(listCriteria));
            criteria.setSampleType(SampleTypeTranslator.translate(listCriteria.getSampleType()));
        }
        return criteria;
    }

    private static List<SampleOwnerIdentifier> createOwnerIdentifiers(
            final ListSampleCriteria listCriteria)
    {
        final List<SampleOwnerIdentifier> ownerIdentifiers = new ArrayList<SampleOwnerIdentifier>();
        final DatabaseInstanceIdentifier databaseIdentifier = getDatabaseIdentifier(listCriteria);
        if (listCriteria.isIncludeGroup())

        {
            ownerIdentifiers.add(new SampleOwnerIdentifier(new GroupIdentifier(databaseIdentifier,
                    listCriteria.getGroupCode())));
        }
        if (listCriteria.isIncludeInstance())
        {
            ownerIdentifiers.add(new SampleOwnerIdentifier(databaseIdentifier));
        }
        return ownerIdentifiers;
    }

    private static DatabaseInstanceIdentifier getDatabaseIdentifier(
            final ListSampleCriteria listCriteria)
    {
        final DatabaseInstance databaseInstance =
                listCriteria.getSampleType().getDatabaseInstance();
        return new DatabaseInstanceIdentifier(databaseInstance.getCode());
    }

    public Map<Long, List<SampleProperty>> listSamplesProperties(
            final ListSampleCriteria listCriteria, final List<PropertyType> propertyCodes)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final List<SamplePropertyPE> allProperties =
                genericServer.listSamplesProperties(getSessionToken(),
                        createCriteriaDTO(listCriteria), PropertyTypeTranslator
                                .translate(propertyCodes));

        final Map<Long, List<SampleProperty>> propertiesMap = splitForSamples(allProperties);
        return propertiesMap;
    }

    private static Map<Long, List<SampleProperty>> splitForSamples(
            final List<SamplePropertyPE> properties)
    {
        final Map<Long, List<SampleProperty>> propertiesMap =
                new HashMap<Long, List<SampleProperty>>();

        for (final SamplePropertyPE prop : properties)
        {
            final long sampleId = prop.getSample().getId();
            List<SampleProperty> sampleProps = propertiesMap.get(sampleId);
            if (sampleProps == null)
            {
                sampleProps = new ArrayList<SampleProperty>();
            }
            sampleProps.add(SamplePropertyTranslator.translate(prop));
            propertiesMap.put(sampleId, sampleProps);
        }
        return propertiesMap;
    }

    public final SampleGeneration getSampleInfo(final String sampleIdentifier)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final SampleIdentifier identifier = SampleIdentifierFactory.parse(sampleIdentifier);
            final SampleGenerationDTO sampleGeneration =
                    genericServer.getSampleInfo(getSessionToken(), identifier);
            return BeanUtils.createBean(SampleGeneration.class, sampleGeneration, DtoConverters
                    .getSampleConverter());
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final List<ExternalData> listExternalData(final String sampleIdentifier)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final SampleIdentifier identifier = SampleIdentifierFactory.parse(sampleIdentifier);
            final List<ExternalDataPE> externalData =
                    genericServer.listExternalData(getSessionToken(), identifier);
            return BeanUtils.createBeanList(ExternalData.class, externalData);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final List<SearchableEntity> listSearchableEntities()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            return BeanUtils.createBeanList(SearchableEntity.class, Arrays
                    .asList(ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity.values()));
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final List<MatchingEntity> listMatchingEntities(
            final SearchableEntity searchableEntityOrNull, final String queryText)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity[] searchableEntities;
            if (searchableEntityOrNull == null)
            {
                searchableEntities =
                        ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity.values();
            } else
            {
                searchableEntities =
                        new ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity[]
                            { ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity
                                    .valueOf(searchableEntityOrNull.getName()) };
            }
            return BeanUtils.createBeanList(MatchingEntity.class, genericServer
                    .listMatchingEntities(getSessionToken(), searchableEntities, queryText));
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }
}
