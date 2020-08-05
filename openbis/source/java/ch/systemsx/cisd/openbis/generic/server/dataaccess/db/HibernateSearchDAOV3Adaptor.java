package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.*;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.ArchivingStatus;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.Complete;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.AbstractDataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.CompleteSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.StatusSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDmsAddressType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.search.ExternalDmsTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchObjectKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.AbstractSampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.*;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IHibernateSearchDAO;
import ch.systemsx.cisd.openbis.generic.shared.authorization.AuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.*;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.springframework.dao.DataAccessException;

import java.util.*;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.GlobalSearchCriteriaTranslator.IDENTIFIER_ALIAS;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PERSON_REGISTERER_COLUMN;

/*
 * The goal of this class is to replace HibernateSearchDAO
 * This should make possible to remove Hibernate Search without changing all the other business layers
 */
public class HibernateSearchDAOV3Adaptor implements IHibernateSearchDAO {

    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            HibernateSearchDAOV3Adaptor.class);

    private HibernateSearchDAO hibernateSearchDAO;

    // TODO : Remove HibernateSearchDAO when Full Text Search "searchEntitiesByTerm" is also implemented
    public HibernateSearchDAOV3Adaptor(HibernateSearchDAO hibernateSearchDAO) {
        this.hibernateSearchDAO = hibernateSearchDAO;
    }

    //
    // IHibernateSearchDAO interface
    //

    @Override
    public List<MatchingEntity> searchEntitiesByTerm(String userId, SearchableEntity searchableEntity, String searchTerm, HibernateSearchDataProvider dataProvider, boolean useWildcardSearchMode, int alreadyFoundEntities, int maxSize) throws DataAccessException {
        operationLog.info("TO ADAPT [FULL TEXT SEARCH] : " + searchableEntity + " [" + searchTerm + "] Wildcards: [" + useWildcardSearchMode + "]");

        if(StringUtils.isBlank(searchTerm)) {
            throw new AssertionError("searchTerm is empty");
        }
        if(searchableEntity == null) {
            throw new AssertionError("searchableEntity == null");
        }
        if(dataProvider == null) {
            throw new AssertionError("dataProvider == null");
        }
        if(useWildcardSearchMode) {
            operationLog.warn("TO ADAPT [FULL TEXT SEARCH] : useWildcardSearchMode not supported");
            throwUnsupportedOperationException("useWildcardSearchMode not supported");
        }

        if(maxSize != Integer.MAX_VALUE) {
            throwIllegalArgumentException("maxSize != Integer.MAX_VALUE");
        }

        // alreadyFoundEntities is an ignored parameter

        // Obtain PersonPE
        DAOFactory daoFactory = (DAOFactory) CommonServiceProvider.getApplicationContext().getBean(ComponentNames.DAO_FACTORY);
        PersonPE personPE = daoFactory.getPersonDAO().tryFindPersonByUserId(userId);

        if(personPE == null) {
            throwIllegalArgumentException("userId: '" + userId + "' not found on database.");
        }

        // Obtain global criteria

        GlobalSearchCriteria globalSearchCriteria = getCriteria(searchableEntity);
        if (searchTerm.startsWith("\"") && searchTerm.endsWith("\"")) {
            globalSearchCriteria.withText().thatContainsExactly(searchTerm.substring(1, searchTerm.length() - 1));
        } else {
            globalSearchCriteria.withText().thatContains(searchTerm);
        }

        operationLog.info("ADAPTED [FULL TEXT SEARCH] : " + searchableEntity + " [" + searchTerm + "] " + useWildcardSearchMode);

        // Obtain entity id results from search manager

        Set<Map<String, Object>> newResults = getGlobalSearchManager().searchForIDs(personPE.getId(),
                getAuthorisationInformation(personPE),
                globalSearchCriteria,
                ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN,
                getTableMapper(searchableEntity), true);

        Collection<MatchingEntity> matchingEntities = getGlobalSearchManager().map(newResults, true);

        // Set registrators for V1
        final Map<String, Long> registratorIdByRecordIdentifierMap = newResults.stream().collect(HashMap::new,
                (supplierMap, record) ->
                {
                    final Long registratorId = (Long) record.get(PERSON_REGISTERER_COLUMN);
                    if (registratorId != null)
                    {
                        supplierMap.put((String) record.get(IDENTIFIER_ALIAS), registratorId);
                    }
                },
                HashMap::putAll);

        List<PersonPE> registrators = daoFactory.getPersonDAO().getPersons(registratorIdByRecordIdentifierMap.values());
        Map<Long, PersonPE> registratorsById = new HashMap<>();
        for (PersonPE registrator:registrators) {
            registratorsById.put(registrator.getId(), registrator);
        }

        for (MatchingEntity matchingEntity:matchingEntities) {
            Long registratorId =registratorIdByRecordIdentifierMap.get(matchingEntity.getIdentifier());
            PersonPE registratorPersonPE = registratorsById.get(registratorId);
            Person registrator = new Person();
            registrator.setFirstName(registratorPersonPE.getFirstName());
            registrator.setLastName(registratorPersonPE.getLastName());
            registrator.setUserId(registratorPersonPE.getUserId());
            registrator.setEmail(registratorPersonPE.getEmail());
            registrator.setActive(registratorPersonPE.isActive());
            matchingEntity.setRegistrator(registrator);
        }

        // TODO: Remove when finish, old search to compare when debugging
        //List<MatchingEntity> oldResults = this.hibernateSearchDAO.searchEntitiesByTerm(userId, searchableEntity, searchTerm, dataProvider, useWildcardSearchMode, alreadyFoundEntities, maxSize);

        return new ArrayList<>(matchingEntities);
    }

    private GlobalSearchManager globalSearchManager;

    private IGlobalSearchManager getGlobalSearchManager() {
        if (globalSearchManager == null) {
            globalSearchManager = (GlobalSearchManager) CommonServiceProvider.getApplicationContext().getBean("global-search-manager");
        }
        return globalSearchManager;
    }

    private GlobalSearchCriteria getCriteria(SearchableEntity entityKind) {
        GlobalSearchCriteria globalSearchCriteria = new GlobalSearchCriteria();
        switch (entityKind) {
            case MATERIAL:
                globalSearchCriteria.withObjectKind().thatIn(GlobalSearchObjectKind.MATERIAL);
                break;
            case EXPERIMENT:
                globalSearchCriteria.withObjectKind().thatIn(GlobalSearchObjectKind.EXPERIMENT);
                break;
            case SAMPLE:
                globalSearchCriteria.withObjectKind().thatIn(GlobalSearchObjectKind.SAMPLE);
                break;
            case DATA_SET:
                globalSearchCriteria.withObjectKind().thatIn(GlobalSearchObjectKind.DATA_SET);
                break;
        }
        return globalSearchCriteria;
    }

    private TableMapper getTableMapper(SearchableEntity entityKind) {
        TableMapper tableMapper = null;
        switch (entityKind) {
            case MATERIAL:
                tableMapper = TableMapper.MATERIAL;
                break;
            case EXPERIMENT:
                tableMapper = TableMapper.EXPERIMENT;
                break;
            case SAMPLE:
                tableMapper = TableMapper.SAMPLE;
                break;
            case DATA_SET:
                tableMapper = TableMapper.DATA_SET;
                break;
        }
        return tableMapper;
    }

    @Override
    public int getResultSetSizeLimit() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void setProperties(Properties serviceProperties) {
        for (String propertyName:serviceProperties.stringPropertyNames()) {
            operationLog.warn("Configuration property ignored: " + propertyName);
        }
    }

    // TODO : Remove List<IAssociationCriteria> v1Associations from the interface and the code that build them from the core
    @Override
    public List<Long> searchForEntityIds(final String userId,
                                         final DetailedSearchCriteria mainV1Criteria,
                                         final EntityKind entityKind,
                                         final List<IAssociationCriteria> v1Associations)
    {
        operationLog.info("TO ADAPT [QUERY] : " + entityKind + " [" + mainV1Criteria + "] " + v1Associations);

        // Obtain PersonPE
        DAOFactory daoFactory = (DAOFactory) CommonServiceProvider.getApplicationContext().getBean(ComponentNames.DAO_FACTORY);
        PersonPE personPE = daoFactory.getPersonDAO().tryFindPersonByUserId(userId);

        if(personPE == null) {
            throwIllegalArgumentException("userId: '" + userId + "' not found on database.");
        }

        // Obtain entity criteria
        AbstractEntitySearchCriteria mainV3Criteria = getCriteria(entityKind);

        // Set the operator
        switch (mainV1Criteria.getConnection()) {
            case MATCH_ALL:
                mainV3Criteria.withAndOperator();
                break;
            case MATCH_ANY:
                mainV3Criteria.withOrOperator();
                break;
        }

        // Entity Criteria translation
        for (DetailedSearchCriterion mainV1Criterion:mainV1Criteria.getCriteria()) {
            adapt(mainV3Criteria, mainV1Criteria, mainV1Criterion);
        }

        // Nested Criteria translation
        for (DetailedSearchSubCriteria subV1CriteriaPointer:mainV1Criteria.getSubCriterias()) {
            DetailedSearchCriteria subV1Criteria = subV1CriteriaPointer.getCriteria();

            AbstractEntitySearchCriteria subV3Criteria = null;

            switch (subV1CriteriaPointer.getTargetEntityKind()) {
                case SAMPLE:
                    if (mainV3Criteria instanceof AbstractDataSetSearchCriteria) {
                        subV3Criteria = ((AbstractDataSetSearchCriteria) mainV3Criteria).withSample();
                    }
                    break;
                case EXPERIMENT:
                    if (mainV3Criteria instanceof AbstractSampleSearchCriteria) {
                        subV3Criteria = ((AbstractSampleSearchCriteria) mainV3Criteria).withExperiment();
                    } else if (mainV3Criteria instanceof AbstractDataSetSearchCriteria) {
                        subV3Criteria = ((AbstractDataSetSearchCriteria) mainV3Criteria).withExperiment();
                    }
                    break;
                case DATA_SET:
                    // TODO: V3 doesn't support withDataSet in Samples and Experiments
                    break;
                case DATA_SET_PARENT: // For Data Sets
                    if (mainV3Criteria instanceof DataSetSearchCriteria) {
                        subV3Criteria = ((DataSetSearchCriteria) mainV3Criteria).withParents();
                    }
                    break;
                case DATA_SET_CHILD: // For Data Sets
                    if (mainV3Criteria instanceof DataSetSearchCriteria) {
                        subV3Criteria = ((DataSetSearchCriteria) mainV3Criteria).withChildren();
                    }
                    break;
                case DATA_SET_CONTAINER: // For Data Sets
                    if (mainV3Criteria instanceof DataSetSearchCriteria) {
                        subV3Criteria = ((DataSetSearchCriteria) mainV3Criteria).withContainer();
                    }
                    break;
                case SAMPLE_CONTAINER: // For Samples
                    if (mainV3Criteria instanceof SampleSearchCriteria) {
                        subV3Criteria = ((SampleSearchCriteria) mainV3Criteria).withContainer();
                    }
                    break;
                case SAMPLE_CHILD: // For Samples
                    if (mainV3Criteria instanceof SampleSearchCriteria) {
                        subV3Criteria = ((SampleSearchCriteria) mainV3Criteria).withChildren();
                    }
                    break;
                case SAMPLE_PARENT: // For Samples
                    if (mainV3Criteria instanceof SampleSearchCriteria) {
                        subV3Criteria = ((SampleSearchCriteria) mainV3Criteria).withParents();
                    }
                    break;
                case MATERIAL:
                    // TODO: V3 doesn't support withMaterial criteria
                    break;
            }

            if (subV3Criteria == null) {
                throwUnsupportedOperationException("TargetEntityKind: " + subV1CriteriaPointer.getTargetEntityKind() + " For V3: " + mainV3Criteria.getClass().getName());
            }

            // Set the operator
            switch (subV1Criteria.getConnection()) {
                case MATCH_ALL:
                    subV3Criteria.withAndOperator();
                    break;
                case MATCH_ANY:
                    subV3Criteria.withOrOperator();
                    break;
            }

            for (DetailedSearchCriterion subV1Criterion : subV1Criteria.getCriteria()) {
                adapt(subV3Criteria, subV1Criteria, subV1Criterion);
            }
        }

        // Associations (Rules that indicate that something needs to be connected to)
        // They have 3 possible rules depending on the 3 classes
        // Null, Not Null, or Tech Id
        // V1 Handled sub criteria in managers and associations where used to make the link
        // If sub criteria are handled on the query, like they are implemented now, handling associations is not necessary.

//        boolean handleAsociations = false;
//        if (v1Associations != null && handleAsociations) {
//            for(IAssociationCriteria association:v1Associations) {
//                AbstractEntitySearchCriteria subAssV3Criteria = null;
//
//                switch (association.getEntityKind()) {
//                    case SAMPLE:
//                        if (mainV3Criteria instanceof AbstractDataSetSearchCriteria) {
//                            if (association instanceof DetailedSearchNotNullAssociationCriteria) {
//                                subAssV3Criteria = ((AbstractDataSetSearchCriteria) mainV3Criteria).withSample();
//                            } else if (association instanceof  DetailedSearchNullAssociationCriteria) {
//                                subAssV3Criteria = ((AbstractDataSetSearchCriteria) mainV3Criteria).withoutSample();
//                            } else { // This is DetailedSearchAssociationCriteria Tech ids
//                                // TODO: V3 doesn't support tech ids
////                                Collection<Long> ids = ((DetailedSearchAssociationCriteria) association).getIds();
////                                List<String> permIds = getPermIds(EntityKind.SAMPLE, ids);
////                                for (String permId:permIds) {
////                                    ((AbstractDataSetSearchCriteria) mainV3Criteria).withSample().withPermId().thatEquals(permId);
////                                }
//                            }
//                        }
//                        break;
//                    case EXPERIMENT:
//                        if (mainV3Criteria instanceof AbstractSampleSearchCriteria) {
//                            if (association instanceof DetailedSearchNotNullAssociationCriteria) {
//                                subAssV3Criteria = ((AbstractSampleSearchCriteria) mainV3Criteria).withExperiment();
//                            } else if (association instanceof  DetailedSearchNullAssociationCriteria) {
//                                subAssV3Criteria = ((AbstractSampleSearchCriteria) mainV3Criteria).withoutExperiment();
//                            } else { // This is DetailedSearchAssociationCriteria Tech ids
//                                // TODO: V3 doesn't support tech ids
////                                Collection<Long> ids = ((DetailedSearchAssociationCriteria) association).getIds();
////                                List<String> permIds = getPermIds(EntityKind.EXPERIMENT, ids);
////                                for (String permId:permIds) {
////                                    ((AbstractSampleSearchCriteria) mainV3Criteria).withExperiment().withPermId().thatEquals(permId);
////                                }
//                            }
//                        } else if(mainV3Criteria instanceof AbstractDataSetSearchCriteria) {
//                            if (association instanceof DetailedSearchNotNullAssociationCriteria) {
//                                subAssV3Criteria = ((AbstractDataSetSearchCriteria) mainV3Criteria).withExperiment();
//                            } else if (association instanceof  DetailedSearchNullAssociationCriteria) {
//                                subAssV3Criteria = ((AbstractDataSetSearchCriteria) mainV3Criteria).withoutExperiment();
//                            } else { // This is DetailedSearchAssociationCriteria Tech ids
//                                // TODO: V3 doesn't support tech ids
////                                Collection<Long> ids = ((DetailedSearchAssociationCriteria) association).getIds();
////                                List<String> permIds = getPermIds(EntityKind.EXPERIMENT, ids);
////                                for (String permId:permIds) {
////                                    ((AbstractDataSetSearchCriteria) mainV3Criteria).withExperiment().withPermId().thatEquals(permId);
////                                }
//                            }
//                        }
//                        break;
//                    case DATA_SET:
//                        // TODO: V3 doesn't support withDataSet in Samples and Experiments
//                        break;
//                    case DATA_SET_PARENT:
//                        if (mainV3Criteria instanceof DataSetSearchCriteria) {
//                            if (association instanceof DetailedSearchNotNullAssociationCriteria) {
//                                subAssV3Criteria = ((DataSetSearchCriteria) mainV3Criteria).withParents();
//                            } else if (association instanceof  DetailedSearchNullAssociationCriteria) {
//                                // TODO: V3 doesn't support withoutParents
//                            } else { // This is DetailedSearchAssociationCriteria Tech ids
//                                // TODO: V3 doesn't support tech ids
////                                Collection<Long> ids = ((DetailedSearchAssociationCriteria) association).getIds();
////                                List<String> permIds = getPermIds(EntityKind.DATA_SET, ids);
////                                for (String permId:permIds) {
////                                    ((DataSetSearchCriteria) mainV3Criteria).withParents().withPermId().thatEquals(permId);
////                                }
//                            }
//                        }
//                        break;
//                    case DATA_SET_CHILD:
//                        if (mainV3Criteria instanceof DataSetSearchCriteria) {
//                            if (association instanceof DetailedSearchNotNullAssociationCriteria) {
//                                subAssV3Criteria = ((DataSetSearchCriteria) mainV3Criteria).withChildren();
//                            } else if (association instanceof  DetailedSearchNullAssociationCriteria) {
//                                // TODO: V3 doesn't support withoutChildren
//                            } else { // This is DetailedSearchAssociationCriteria Tech ids
//                                // TODO: V3 doesn't support tech ids
////                                Collection<Long> ids = ((DetailedSearchAssociationCriteria) association).getIds();
////                                List<String> permIds = getPermIds(EntityKind.DATA_SET, ids);
////                                for (String permId:permIds) {
////                                    ((DataSetSearchCriteria) mainV3Criteria).withChildren().withPermId().thatEquals(permId);
////                                }
//                            }
//                        }
//                        break;
//                    case DATA_SET_CONTAINER:
//                        if (mainV3Criteria instanceof DataSetSearchCriteria) {
//                            if (association instanceof DetailedSearchNotNullAssociationCriteria) {
//                                subAssV3Criteria = ((DataSetSearchCriteria) mainV3Criteria).withContainer();
//                            } else if (association instanceof  DetailedSearchNullAssociationCriteria) {
//                                // TODO: V3 doesn't support withoutContainer
//                            } else { // This is DetailedSearchAssociationCriteria Tech ids
//                                // TODO: V3 doesn't support tech ids
////                                Collection<Long> ids = ((DetailedSearchAssociationCriteria) association).getIds();
////                                List<String> permIds = getPermIds(EntityKind.DATA_SET, ids);
////                                for (String permId:permIds) {
////                                    ((DataSetSearchCriteria) mainV3Criteria).withContainer().withPermId().thatEquals(permId);
////                                }
//                            }
//                        }
//                        break;
//                    case SAMPLE_CONTAINER:
//                        if (mainV3Criteria instanceof SampleSearchCriteria) {
//                            if (association instanceof DetailedSearchNotNullAssociationCriteria) {
//                                subAssV3Criteria = ((SampleSearchCriteria) mainV3Criteria).withContainer();
//                            } else if (association instanceof  DetailedSearchNullAssociationCriteria) {
//                                subAssV3Criteria = ((SampleSearchCriteria) mainV3Criteria).withoutContainer();
//                            } else { // This is DetailedSearchAssociationCriteria Tech ids
//                                // TODO: V3 doesn't support tech ids
////                                Collection<Long> ids = ((DetailedSearchAssociationCriteria) association).getIds();
////                                List<String> permIds = getPermIds(EntityKind.SAMPLE, ids);
////                                for (String permId:permIds) {
////                                    ((SampleSearchCriteria) mainV3Criteria).withContainer().withPermId().thatEquals(permId);
////                                }
//                            }
//                        }
//                        break;
//                    case SAMPLE_CHILD:
//                        if (mainV3Criteria instanceof SampleSearchCriteria) {
//                            if (association instanceof DetailedSearchNotNullAssociationCriteria) {
//                                subAssV3Criteria = ((SampleSearchCriteria) mainV3Criteria).withChildren();
//                            } else if (association instanceof  DetailedSearchNullAssociationCriteria) {
//                                // TODO: V3 doesn't support withoutChildren
//                            } else { // This is DetailedSearchAssociationCriteria Tech ids
//                                // TODO: V3 doesn't support tech ids
////                                Collection<Long> ids = ((DetailedSearchAssociationCriteria) association).getIds();
////                                List<String> permIds = getPermIds(EntityKind.SAMPLE, ids);
////                                for (String permId:permIds) {
////                                    ((SampleSearchCriteria) mainV3Criteria).withChildren().withPermId().thatEquals(permId);
////                                }
//                            }
//                        }
//                        break;
//                    case SAMPLE_PARENT:
//                        if (mainV3Criteria instanceof SampleSearchCriteria) {
//                            if (association instanceof DetailedSearchNotNullAssociationCriteria) {
//                                subAssV3Criteria = ((SampleSearchCriteria) mainV3Criteria).withParents();
//                            } else if (association instanceof  DetailedSearchNullAssociationCriteria) {
//                                // TODO: V3 doesn't support withoutParents
//                            } else { // This is DetailedSearchAssociationCriteria Tech ids
//                                // TODO: V3 doesn't support tech ids
////                                Collection<Long> ids = ((DetailedSearchAssociationCriteria) association).getIds();
////                                List<String> permIds = getPermIds(EntityKind.SAMPLE, ids);
////                                for (String permId:permIds) {
////                                    ((SampleSearchCriteria) mainV3Criteria).withParents().withPermId().thatEquals(permId);
////                                }
//                            }
//                        }
//                        break;
//                    case MATERIAL:
//                        // TODO: V3 doesn't support withMaterial criteria
//                        break;
//                }
//
//                if (subAssV3Criteria == null) {
//                    throwUnsupportedOperationException("TargetEntityKind: " + association.getEntityKind() + "with Search Pattern " + association.getSearchPatterns() + " For V3: " + mainV3Criteria.getClass().getName());
//                }
//            }
//        }

        operationLog.info("ADAPTED [QUERY] : " + mainV3Criteria);

        // Obtain entity id results from search manager
        Set<Long> results = getSearchManager(entityKind).searchForIDs(personPE.getId(),
                getAuthorisationInformation(personPE),
                mainV3Criteria,
                null,
                ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN);

        return new ArrayList<>(results);
    }

    private void adapt(AbstractEntitySearchCriteria v3Criteria,
                       DetailedSearchCriteria v1Criteria,
                       DetailedSearchCriterion v1Criterion) {
        // TODO: V3 doesn't support negated
        if(v1Criterion.isNegated()) {
            throwUnsupportedOperationException("negated");
        }

        for (String value:v1Criterion.getValues()) {
            ISearchCriteria criterionV3Criteria = null;
            switch (v1Criterion.getField().getKind()) {
                case ANY_FIELD:
                    criterionV3Criteria = v3Criteria.withAnyField();
                    break;
                case ANY_PROPERTY:
                    criterionV3Criteria = v3Criteria.withAnyProperty();
                    break;
                case PROPERTY:
                    if (v1Criterion.getField().getPropertyCode() == null) {
                        throwIllegalArgumentException("PROPERTY PropertyCode null");
                    }
                    if (isNumberProperty(v1Criterion.getField().getPropertyCode())) {
                        criterionV3Criteria = v3Criteria.withNumberProperty(v1Criterion.getField().getPropertyCode());
                    } else if(isDateProperty(v1Criterion.getField().getPropertyCode())) {
                        criterionV3Criteria = v3Criteria.withDateProperty(v1Criterion.getField().getPropertyCode());
                    } else {
                        criterionV3Criteria = v3Criteria.withProperty(v1Criterion.getField().getPropertyCode());
                    }
                    break;
                case ATTRIBUTE:
                    if (v1Criterion.getField().getAttributeCode() == null) {
                        throwIllegalArgumentException("ATTRIBUTE AttributeCode null");
                    }

                    EntityKind entityKind = getEntityKind(v3Criteria);

                    switch (entityKind) {
                        case MATERIAL:
                            criterionV3Criteria = withAttribute((MaterialSearchCriteria) v3Criteria, v1Criterion.getField().getAttributeCode());
                            break;
                        case EXPERIMENT:
                            criterionV3Criteria = withAttribute((ExperimentSearchCriteria) v3Criteria, v1Criterion.getField().getAttributeCode());
                            break;
                        case SAMPLE:
                            criterionV3Criteria = withAttribute((AbstractSampleSearchCriteria) v3Criteria, v1Criterion.getField().getAttributeCode());
                            break;
                        case DATA_SET:
                            criterionV3Criteria = withAttribute((AbstractDataSetSearchCriteria) v3Criteria, v1Criterion.getField().getAttributeCode());
                            break;
                    }

                    break;
                case REGISTRATOR:
                    criterionV3Criteria = v3Criteria.withRegistrator().withUserId();
                    break;
            }

            CompareType comparisonOperator = v1Criterion.getType();

            if (criterionV3Criteria instanceof StringFieldSearchCriteria) {
                StringFieldSearchCriteria stringFieldSearchCriteria = (StringFieldSearchCriteria) criterionV3Criteria;
                if (    comparisonOperator == null ||
                        comparisonOperator == CompareType.EQUALS) {
                    if (v1Criteria.isUseWildcardSearchMode()) {
                        // TODO: V3 doesn't support with or without wildcards, instead if a * or ? is found, they are used as wildcards
                    }
                    // Old Lucene behaviour was always word match, real equals should be even more restrictive/correct
                    stringFieldSearchCriteria.thatEquals(value);
                } else {
                    throwIllegalArgumentException(comparisonOperator + " compare type for StringFieldSearchCriteria");
                }
            } else if(criterionV3Criteria instanceof NumberPropertySearchCriteria) {
                NumberFieldSearchCriteria numberFieldSearchCriteria = (NumberFieldSearchCriteria) criterionV3Criteria;
                Number number = getNumber(value);
                switch (comparisonOperator) {
                    case LESS_THAN:
                        numberFieldSearchCriteria.thatIsLessThan(number);
                        break;
                    case LESS_THAN_OR_EQUAL:
                        numberFieldSearchCriteria.thatIsLessThanOrEqualTo(number);
                        break;
                    case EQUALS:
                        numberFieldSearchCriteria.thatEquals(number);
                        break;
                    case MORE_THAN_OR_EQUAL:
                        numberFieldSearchCriteria.thatIsGreaterThanOrEqualTo(number);
                        break;
                    case MORE_THAN:
                        numberFieldSearchCriteria.thatIsGreaterThan(number);
                        break;
                }
            } else if(criterionV3Criteria instanceof BooleanFieldSearchCriteria) {
                BooleanFieldSearchCriteria booleanFieldSearchCriteria = (BooleanFieldSearchCriteria) criterionV3Criteria;
                booleanFieldSearchCriteria.thatEquals(Boolean.parseBoolean(value));
            } else if(criterionV3Criteria instanceof DateFieldSearchCriteria) {
                DateFieldSearchCriteria dateFieldSearchCriteria = (DateFieldSearchCriteria) criterionV3Criteria;

                String timezone = v1Criterion.getTimeZone();
                if (timezone != null) {
                    int hourOffset = 0;
                    try {
                        hourOffset = Integer.parseInt(timezone);
                    } catch (Exception ex) {
                        throwUnsupportedOperationException("timezone format not supported for: '" + timezone + "'");
                    }
                    dateFieldSearchCriteria.withTimeZone(hourOffset);
                }

                switch (comparisonOperator) {
                    case LESS_THAN:
                        throwIllegalArgumentException("Date operator LESS_THAN");
                        break;
                    case LESS_THAN_OR_EQUAL:
                        dateFieldSearchCriteria.thatIsEarlierThanOrEqualTo(value);
                        break;
                    case EQUALS:
                        dateFieldSearchCriteria.thatEquals(value);
                        break;
                    case MORE_THAN_OR_EQUAL:
                        dateFieldSearchCriteria.thatIsLaterThanOrEqualTo(value);
                        break;
                    case MORE_THAN:
                        throwIllegalArgumentException("Date operator MORE_THAN");
                        break;
                }
                // Enums
            } else if(criterionV3Criteria instanceof CompleteSearchCriteria) {
                CompleteSearchCriteria enumFieldSearchCriteria = (CompleteSearchCriteria) criterionV3Criteria;
                enumFieldSearchCriteria.thatEquals(Complete.valueOf(value));
            } else if(criterionV3Criteria instanceof StatusSearchCriteria) {
                StatusSearchCriteria enumFieldSearchCriteria = (StatusSearchCriteria) criterionV3Criteria;
                enumFieldSearchCriteria.thatEquals(ArchivingStatus.valueOf(value));
            } else if(criterionV3Criteria instanceof ExternalDmsTypeSearchCriteria) {
                ExternalDmsTypeSearchCriteria enumFieldSearchCriteria = (ExternalDmsTypeSearchCriteria) criterionV3Criteria;
                enumFieldSearchCriteria.thatEquals(ExternalDmsAddressType.valueOf(value));
            }
            // Default
            else {
                throwUnsupportedOperationException("TargetV3Criteria: " + v3Criteria.getClass().getSimpleName() + " For V1 Criterion: " + v1Criterion);
            }
        }
    }

    //
    // Helper Methods - withAttribute conversions
    //

    private ISearchCriteria withAttribute(AbstractDataSetSearchCriteria v3Criteria, String attributeCode) {
        ISearchCriteria criterionV3Criteria = null;
        switch (DataSetAttributeSearchFieldKind.valueOf(attributeCode)) {
            case CODE:
                criterionV3Criteria = v3Criteria.withCode();
                break;
            case DATA_SET_TYPE:
                criterionV3Criteria = v3Criteria.withType().withCode();
                break;
            case METAPROJECT:
                criterionV3Criteria = v3Criteria.withTag().withCode();
                break;
            case REGISTRATION_DATE:
                criterionV3Criteria = v3Criteria.withRegistrationDate();
                break;
            case MODIFICATION_DATE:
                criterionV3Criteria = v3Criteria.withModificationDate();
                break;
            case REGISTRATION_DATE_FROM:
                // For this to work, the search Criteria should set CompareType.MORE_THAN_OR_EQUAL, this was found at SearchFieldDateCriterionFactory
                criterionV3Criteria = v3Criteria.withRegistrationDate();
                break;
            case MODIFICATION_DATE_FROM:
                // For this to work, the search Criteria should set CompareType.MORE_THAN_OR_EQUAL, this was found at SearchFieldDateCriterionFactory
                criterionV3Criteria = v3Criteria.withModificationDate();
                break;
            case REGISTRATION_DATE_UNTIL:
                // For this to work, the search Criteria should set CompareType.LESS_THAN_OR_EQUAL, this was found at SearchFieldDateCriterionFactory
                criterionV3Criteria = v3Criteria.withRegistrationDate();
                break;
            case MODIFICATION_DATE_UNTIL:
                // For this to work, the search Criteria should set CompareType.LESS_THAN_OR_EQUAL, this was found at SearchFieldDateCriterionFactory
                criterionV3Criteria = v3Criteria.withModificationDate();
                break;
            case REGISTRATOR_USER_ID:
                criterionV3Criteria = v3Criteria.withRegistrator().withUserId();
                break;
            case REGISTRATOR_FIRST_NAME:
                criterionV3Criteria = v3Criteria.withRegistrator().withFirstName();
                break;
            case REGISTRATOR_LAST_NAME:
                criterionV3Criteria = v3Criteria.withRegistrator().withLastName();
                break;
            case REGISTRATOR_EMAIL:
                criterionV3Criteria = v3Criteria.withRegistrator().withEmail();
                break;
            case MODIFIER_USER_ID:
                criterionV3Criteria = v3Criteria.withModifier().withUserId();
                break;
            case MODIFIER_FIRST_NAME:
                criterionV3Criteria = v3Criteria.withModifier().withFirstName();
                break;
            case MODIFIER_LAST_NAME:
                criterionV3Criteria = v3Criteria.withModifier().withLastName();
                break;
            case MODIFIER_EMAIL:
                criterionV3Criteria = v3Criteria.withModifier().withEmail();
                break;
            case LOCATOR_TYPE:
                criterionV3Criteria = v3Criteria.withPhysicalData().withLocatorType().withCode();
                break;
            case LOCATION:
                criterionV3Criteria = v3Criteria.withPhysicalData().withLocation();
                break;
            case SHARE_ID:
                criterionV3Criteria = v3Criteria.withPhysicalData().withShareId();
                break;
            case SIZE:
                criterionV3Criteria = v3Criteria.withPhysicalData().withSize();
                break;
            case STORAGE_FORMAT:
                criterionV3Criteria = v3Criteria.withPhysicalData().withStorageFormat().withCode();
                break;
            case FILE_TYPE:
                criterionV3Criteria = v3Criteria.withPhysicalData().withFileFormatType().withCode();
                break;
            case COMPLETE:
                criterionV3Criteria = v3Criteria.withPhysicalData().withComplete(); // Enum
                break;
            case STATUS:
                criterionV3Criteria = v3Criteria.withPhysicalData().withStatus(); // Enum
                break;
            case ARCHIVING_REQUESTED:
                criterionV3Criteria = v3Criteria.withPhysicalData().withArchivingRequested();
                break;
            case PRESENT_IN_ARCHIVE:
                criterionV3Criteria = v3Criteria.withPhysicalData().withPresentInArchive();
                break;
            case STORAGE_CONFIRMATION:
                criterionV3Criteria = v3Criteria.withPhysicalData().withStorageConfirmation();
                break;
            case SPEED_HINT:
                criterionV3Criteria = v3Criteria.withPhysicalData().withSpeedHint();
                break;
            case EXTERNAL_DMS_CODE:
                criterionV3Criteria = v3Criteria.withLinkedData().withExternalDms().withCode();
                break;
            case EXTERNAL_DMS_LABEL:
                criterionV3Criteria = v3Criteria.withLinkedData().withExternalDms().withLabel();
                break;
            case EXTERNAL_DMS_ADDRESS:
                criterionV3Criteria = v3Criteria.withLinkedData().withExternalDms().withAddress();
                break;
            case EXTERNAL_DMS_TYPE:
                criterionV3Criteria = v3Criteria.withLinkedData().withExternalDms().withType(); // Enum
                break;
            case EXTERNAL_CODE:
                criterionV3Criteria = v3Criteria.withLinkedData().withExternalCode();
                break;
            case PATH:
                criterionV3Criteria = v3Criteria.withLinkedData().withCopy().withPath();
                break;
            case COMMIT_HASH:
                criterionV3Criteria = v3Criteria.withLinkedData().withCopy().withGitCommitHash();
                break;
            case COMMIT_REPOSITORY_ID:
                criterionV3Criteria = v3Criteria.withLinkedData().withCopy().withGitRepositoryId();
                break;
        }
        return criterionV3Criteria;
    }

    private ISearchCriteria withAttribute(AbstractSampleSearchCriteria v3Criteria, String attributeCode) {
        ISearchCriteria criterionV3Criteria = null;
        switch (SampleAttributeSearchFieldKind.valueOf(attributeCode)) {
            case CODE:
                criterionV3Criteria = v3Criteria.withCode();
                break;
            case SAMPLE_TYPE:
                criterionV3Criteria = v3Criteria.withType().withCode();
                break;
            case PERM_ID:
                criterionV3Criteria = v3Criteria.withPermId();
                break;
            case IDENTIFIER:
                criterionV3Criteria = v3Criteria.withIdentifier();
                break;
            case SPACE:
                criterionV3Criteria = v3Criteria.withSpace().withCode();
                break;
            case PROJECT:
                criterionV3Criteria = v3Criteria.withProject().withCode();
                break;
            case PROJECT_PERM_ID:
                criterionV3Criteria = v3Criteria.withProject().withPermId();
                break;
            case PROJECT_SPACE:
                criterionV3Criteria = v3Criteria.withProject().withSpace().withCode();
                break;
            case METAPROJECT:
                criterionV3Criteria = v3Criteria.withTag().withCode();
                break;
            case REGISTRATION_DATE:
                criterionV3Criteria = v3Criteria.withRegistrationDate();
                break;
            case MODIFICATION_DATE:
                criterionV3Criteria = v3Criteria.withModificationDate();
                break;
            case REGISTRATION_DATE_FROM:
                // For this to work, the search Criteria should set CompareType.MORE_THAN_OR_EQUAL, this was found at SearchFieldDateCriterionFactory
                criterionV3Criteria = v3Criteria.withRegistrationDate();
                break;
            case MODIFICATION_DATE_FROM:
                // For this to work, the search Criteria should set CompareType.MORE_THAN_OR_EQUAL, this was found at SearchFieldDateCriterionFactory
                criterionV3Criteria = v3Criteria.withModificationDate();
                break;
            case REGISTRATION_DATE_UNTIL:
                // For this to work, the search Criteria should set CompareType.LESS_THAN_OR_EQUAL, this was found at SearchFieldDateCriterionFactory
                criterionV3Criteria = v3Criteria.withRegistrationDate();
                break;
            case MODIFICATION_DATE_UNTIL:
                // For this to work, the search Criteria should set CompareType.LESS_THAN_OR_EQUAL, this was found at SearchFieldDateCriterionFactory
                criterionV3Criteria = v3Criteria.withModificationDate();
                break;
            case REGISTRATOR_USER_ID:
                criterionV3Criteria = v3Criteria.withRegistrator().withUserId();
                break;
            case REGISTRATOR_FIRST_NAME:
                criterionV3Criteria = v3Criteria.withRegistrator().withFirstName();
                break;
            case REGISTRATOR_LAST_NAME:
                criterionV3Criteria = v3Criteria.withRegistrator().withLastName();
                break;
            case REGISTRATOR_EMAIL:
                criterionV3Criteria = v3Criteria.withRegistrator().withEmail();
                break;
            case MODIFIER_USER_ID:
                criterionV3Criteria = v3Criteria.withModifier().withUserId();
                break;
            case MODIFIER_FIRST_NAME:
                criterionV3Criteria = v3Criteria.withModifier().withFirstName();
                break;
            case MODIFIER_LAST_NAME:
                criterionV3Criteria = v3Criteria.withModifier().withLastName();
                break;
            case MODIFIER_EMAIL:
                criterionV3Criteria = v3Criteria.withModifier().withEmail();
                break;
        }
        return criterionV3Criteria;
    }

    private ISearchCriteria withAttribute(ExperimentSearchCriteria v3Criteria, String attributeCode) {
        ISearchCriteria criterionV3Criteria = null;
        switch (ExperimentAttributeSearchFieldKind.valueOf(attributeCode)) {
            case CODE:
                criterionV3Criteria = v3Criteria.withCode();
                break;
            case EXPERIMENT_TYPE:
                criterionV3Criteria = v3Criteria.withType().withCode();
                break;
            case PERM_ID:
                criterionV3Criteria = v3Criteria.withPermId();
                break;
            case IDENTIFIER:
                criterionV3Criteria = v3Criteria.withIdentifier();
                break;
            case PROJECT:
                criterionV3Criteria = v3Criteria.withProject().withCode();
                break;
            case PROJECT_PERM_ID:
                criterionV3Criteria = v3Criteria.withProject().withPermId();
                break;
            case PROJECT_SPACE:
                criterionV3Criteria = v3Criteria.withProject().withSpace().withCode();
                break;
            case METAPROJECT:
                criterionV3Criteria = v3Criteria.withTag().withCode();
                break;
            case REGISTRATION_DATE:
                criterionV3Criteria = v3Criteria.withRegistrationDate();
                break;
            case MODIFICATION_DATE:
                criterionV3Criteria = v3Criteria.withModificationDate();
                break;
            case REGISTRATION_DATE_FROM:
                // For this to work, the search Criteria should set CompareType.MORE_THAN_OR_EQUAL, this was found at SearchFieldDateCriterionFactory
                criterionV3Criteria = v3Criteria.withRegistrationDate();
                break;
            case MODIFICATION_DATE_FROM:
                // For this to work, the search Criteria should set CompareType.MORE_THAN_OR_EQUAL, this was found at SearchFieldDateCriterionFactory
                criterionV3Criteria = v3Criteria.withModificationDate();
                break;
            case REGISTRATION_DATE_UNTIL:
                // For this to work, the search Criteria should set CompareType.LESS_THAN_OR_EQUAL, this was found at SearchFieldDateCriterionFactory
                criterionV3Criteria = v3Criteria.withRegistrationDate();
                break;
            case MODIFICATION_DATE_UNTIL:
                // For this to work, the search Criteria should set CompareType.LESS_THAN_OR_EQUAL, this was found at SearchFieldDateCriterionFactory
                criterionV3Criteria = v3Criteria.withModificationDate();
                break;
            case REGISTRATOR_USER_ID:
                criterionV3Criteria = v3Criteria.withRegistrator().withUserId();
                break;
            case REGISTRATOR_FIRST_NAME:
                criterionV3Criteria = v3Criteria.withRegistrator().withFirstName();
                break;
            case REGISTRATOR_LAST_NAME:
                criterionV3Criteria = v3Criteria.withRegistrator().withLastName();
                break;
            case REGISTRATOR_EMAIL:
                criterionV3Criteria = v3Criteria.withRegistrator().withEmail();
                break;
            case MODIFIER_USER_ID:
                criterionV3Criteria = v3Criteria.withModifier().withUserId();
                break;
            case MODIFIER_FIRST_NAME:
                criterionV3Criteria = v3Criteria.withModifier().withFirstName();
                break;
            case MODIFIER_LAST_NAME:
                criterionV3Criteria = v3Criteria.withModifier().withLastName();
                break;
            case MODIFIER_EMAIL:
                criterionV3Criteria = v3Criteria.withModifier().withEmail();
                break;
        }
        return criterionV3Criteria;
    }

    private ISearchCriteria withAttribute(MaterialSearchCriteria v3Criteria, String attributeCode) {
        ISearchCriteria criterionV3Criteria = null;
        switch (MaterialAttributeSearchFieldKind.valueOf(attributeCode)) {
            case ID:
                // Not supported
                break;
            case CODE:
                criterionV3Criteria = v3Criteria.withCode();
                break;
            case PERM_ID:
                criterionV3Criteria = v3Criteria.withPermId();
                break;
            case MATERIAL_TYPE:
                criterionV3Criteria = v3Criteria.withType().withCode();
                break;
            case METAPROJECT:
                criterionV3Criteria = v3Criteria.withTag().withCode();
                break;
            case REGISTRATION_DATE:
                criterionV3Criteria = v3Criteria.withRegistrationDate();
                break;
            case MODIFICATION_DATE:
                criterionV3Criteria = v3Criteria.withModificationDate();
                break;
            case REGISTRATION_DATE_FROM:
                // For this to work, the search Criteria should set CompareType.MORE_THAN_OR_EQUAL, this was found at SearchFieldDateCriterionFactory
                criterionV3Criteria = v3Criteria.withRegistrationDate();
                break;
            case MODIFICATION_DATE_FROM:
                // For this to work, the search Criteria should set CompareType.MORE_THAN_OR_EQUAL, this was found at SearchFieldDateCriterionFactory
                criterionV3Criteria = v3Criteria.withModificationDate();
                break;
            case REGISTRATION_DATE_UNTIL:
                // For this to work, the search Criteria should set CompareType.LESS_THAN_OR_EQUAL, this was found at SearchFieldDateCriterionFactory
                criterionV3Criteria = v3Criteria.withRegistrationDate();
                break;
            case MODIFICATION_DATE_UNTIL:
                // For this to work, the search Criteria should set CompareType.LESS_THAN_OR_EQUAL, this was found at SearchFieldDateCriterionFactory
                criterionV3Criteria = v3Criteria.withModificationDate();
                break;
            case REGISTRATOR_USER_ID:
                criterionV3Criteria = v3Criteria.withRegistrator().withUserId();
                break;
            case REGISTRATOR_FIRST_NAME:
                criterionV3Criteria = v3Criteria.withRegistrator().withFirstName();
                break;
            case REGISTRATOR_LAST_NAME:
                criterionV3Criteria = v3Criteria.withRegistrator().withLastName();
                break;
            case REGISTRATOR_EMAIL:
                criterionV3Criteria = v3Criteria.withRegistrator().withEmail();
                break;
            case MODIFIER_USER_ID:
                criterionV3Criteria = v3Criteria.withModifier().withUserId();
                break;
            case MODIFIER_FIRST_NAME:
                criterionV3Criteria = v3Criteria.withModifier().withFirstName();
                break;
            case MODIFIER_LAST_NAME:
                criterionV3Criteria = v3Criteria.withModifier().withLastName();
                break;
            case MODIFIER_EMAIL:
                criterionV3Criteria = v3Criteria.withModifier().withEmail();
                break;
        }
        return criterionV3Criteria;
    }

    //
    // Helper Methods - Criteria build
    //

    private EntityKind getEntityKind(AbstractEntitySearchCriteria criteria) {
        EntityKind entityKind = null;
        if (criteria instanceof MaterialSearchCriteria) {
            entityKind = EntityKind.MATERIAL;
        }
        if (criteria instanceof ExperimentSearchCriteria) {
            entityKind = EntityKind.EXPERIMENT;
        }
        if (criteria instanceof AbstractSampleSearchCriteria) {
            entityKind = EntityKind.SAMPLE;
        }
        if (criteria instanceof AbstractDataSetSearchCriteria) {
            entityKind = EntityKind.DATA_SET;
        }
        return entityKind;
    }

    private AbstractEntitySearchCriteria getCriteria(EntityKind entityKind) {
        AbstractEntitySearchCriteria criteria = null;
        switch (entityKind) {
            case MATERIAL:
                criteria = new MaterialSearchCriteria();
                break;
            case EXPERIMENT:
                criteria = new ExperimentSearchCriteria();
                break;
            case SAMPLE:
                criteria = new SampleSearchCriteria();
                break;
            case DATA_SET:
                criteria = new DataSetSearchCriteria();
                break;
        }
        return criteria;
    }

    private MaterialSearchManager materialSearchManager;

    private MaterialSearchManager getMaterialSearchManager() {
        if (materialSearchManager == null) {
            materialSearchManager = (MaterialSearchManager) CommonServiceProvider.getApplicationContext().getBean("material-search-manager");
        }
        return materialSearchManager;
    }

    private ExperimentSearchManager experimentSearchManager;

    private ExperimentSearchManager getExperimentSearchManager() {
        if (experimentSearchManager == null) {
            experimentSearchManager = (ExperimentSearchManager) CommonServiceProvider.getApplicationContext().getBean("experiment-search-manager");
        }
        return experimentSearchManager;
    }

    private SampleSearchManager sampleSearchManager;

    private SampleSearchManager getSampleSearchManager() {
        if (sampleSearchManager == null) {
            sampleSearchManager = (SampleSearchManager) CommonServiceProvider.getApplicationContext().getBean("sample-search-manager");
        }
        return sampleSearchManager;
    }

    private DataSetSearchManager dataSetSearchManager;

    private DataSetSearchManager getDataSetSearchManager() {
        if (dataSetSearchManager == null) {
            dataSetSearchManager = (DataSetSearchManager) CommonServiceProvider.getApplicationContext().getBean("data-set-search-manager");
        }
        return dataSetSearchManager;
    }

    @SuppressWarnings("unchecked")
    private ILocalSearchManager<AbstractEntitySearchCriteria<?>, ?, Long> getSearchManager(EntityKind entityKind) {
        ILocalSearchManager<? extends AbstractEntitySearchCriteria<?>, ?, Long> manager = null;
        switch (entityKind) {
            case MATERIAL:
                manager = getMaterialSearchManager();
                break;
            case EXPERIMENT:
                manager = getExperimentSearchManager();
                break;
            case SAMPLE:
                manager = getSampleSearchManager();
                break;
            case DATA_SET:
                manager = getDataSetSearchManager();
                break;
        }
        return (ILocalSearchManager<AbstractEntitySearchCriteria<?>, ?, Long>) manager;
    }

    //
    // Helper Methods - Property Types
    //

    private Number getNumber(String number) {
        try {
            if (isInteger(number)) {
                return Integer.parseInt(number);
            } else {
                return Double.parseDouble(number);
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("NaN : " + number);
        }
    }

    private static boolean isInteger(String s) {
        return isInteger(s,10);
    }

    private static boolean isInteger(String s, int radix) {
        if(s.isEmpty()) {
            return false;
        }
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) {
                    return false;
                } else {
                    continue;
                }
            }
            if(Character.digit(s.charAt(i),radix) < 0) {
                return false;
            }
        }
        return true;
    }


    private static String IS_NUMBER = "SELECT COUNT(*) > 0 FROM property_types WHERE code = :code AND (daty_id = (SELECT id FROM data_types WHERE code = 'INTEGER') OR daty_id = (SELECT id FROM data_types WHERE code = 'REAL'))";

    private boolean isNumberProperty(String propertyCode) {
        DAOFactory daoFactory = (DAOFactory) CommonServiceProvider.getApplicationContext().getBean(ComponentNames.DAO_FACTORY);
        Session currentSession = daoFactory.getSessionFactory().getCurrentSession();
        NativeQuery nativeQuery = currentSession.createNativeQuery(IS_NUMBER);
        nativeQuery.setParameter("code", propertyCode);
        Boolean isNumber = (Boolean) nativeQuery.getSingleResult();
        return isNumber;
    }

    private static String IS_DATE = "SELECT COUNT(*) > 0 FROM property_types WHERE code = :code AND (daty_id = (SELECT id FROM data_types WHERE code = 'TIMESTAMP'))";

    private boolean isDateProperty(String propertyCode) {
        DAOFactory daoFactory = (DAOFactory) CommonServiceProvider.getApplicationContext().getBean(ComponentNames.DAO_FACTORY);
        Session currentSession = daoFactory.getSessionFactory().getCurrentSession();
        NativeQuery nativeQuery = currentSession.createNativeQuery(IS_DATE);
        nativeQuery.setParameter("code", propertyCode);
        Boolean isDate = (Boolean) nativeQuery.getSingleResult();
        return isDate;
    }

    //
    // Helper Methods - Tech Id to Perm Id
    //

//    private static String GET_SAMPLE_PERM_IDS = "SELECT perm_id FROM samples WHERE id IN (:ids)";
//    private static String GET_EXPERIMENT_PERM_IDS = "SELECT perm_id FROM experiments WHERE id IN (:ids)";
//    private static String GET_DATASETS_PERM_IDS = "SELECT code as perm_id FROM data WHERE id IN (:ids)";
//
//    private List<String> getPermIds(EntityKind entityKind, Collection<Long> ids) {
//        DAOFactory daoFactory = (DAOFactory) CommonServiceProvider.getApplicationContext().getBean(ComponentNames.DAO_FACTORY);
//        Session currentSession = daoFactory.getSessionFactory().getCurrentSession();
//        NativeQuery nativeQuery = null;
//
//        switch (entityKind) {
//            case MATERIAL:
//                throwUnsupportedOperationException("getPermIds for " + entityKind + "not implemented");
//                break;
//            case EXPERIMENT:
//                nativeQuery = currentSession.createNativeQuery(GET_EXPERIMENT_PERM_IDS);
//                break;
//            case SAMPLE:
//                nativeQuery = currentSession.createNativeQuery(GET_SAMPLE_PERM_IDS);
//                break;
//            case DATA_SET:
//                nativeQuery = currentSession.createNativeQuery(GET_DATASETS_PERM_IDS);
//                break;
//        }
//
//        nativeQuery.setParameterList("ids", ids);
//        List<String> permIds = nativeQuery.getResultList();
//        return permIds;
//    }

    //
    // Helper Methods - Authorisation
    //

    private AuthorizationConfig authorizationConfig;

    private AuthorizationConfig getAuthorizationConfig() {
        if (authorizationConfig == null) {
            authorizationConfig = (AuthorizationConfig) CommonServiceProvider.getApplicationContext().getBean("authorization-config");
        }
        return authorizationConfig;
    }

    private AuthorisationInformation getAuthorisationInformation(PersonPE personPE) {
        return AuthorisationInformation.getInstance(personPE, getAuthorizationConfig());
    }

    //
    // Helper Methods - Errors
    //

    private void throwUnsupportedOperationException(String feature) throws RuntimeException {
        throw new RuntimeException(new UnsupportedOperationException("HibernateSearchDAOV3Replacement - Feature not supported: " + feature));
    }

    private void throwIllegalArgumentException(String feature) throws RuntimeException {
        throw new IllegalArgumentException("HibernateSearchDAOV3Replacement - Illegal argument: " + feature);
    }

}
