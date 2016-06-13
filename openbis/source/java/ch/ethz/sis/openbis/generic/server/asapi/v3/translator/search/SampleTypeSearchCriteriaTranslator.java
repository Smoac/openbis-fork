package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.ListableSampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SimpleAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;

public class SampleTypeSearchCriteriaTranslator extends EntityTypeSearchCriteriaTranslator
{
    public SampleTypeSearchCriteriaTranslator(IDAOFactory daoFactory, IObjectAttributeProviderFactory entityAttributeProviderFactory)
    {
        super(daoFactory, entityAttributeProviderFactory);
    }

    @Override
    protected boolean doAccepts(ISearchCriteria criteria)
    {
        return criteria instanceof SampleTypeSearchCriteria
                || criteria instanceof ListableSampleTypeSearchCriteria;
    }

    @Override
    protected SearchCriteriaTranslationResult doTranslate(SearchTranslationContext context, ISearchCriteria criteria)
    {
        if (criteria instanceof ListableSampleTypeSearchCriteria)
        {
            ListableSampleTypeSearchCriteria lstsc = (ListableSampleTypeSearchCriteria) criteria;

            // the field will be translated later
            DetailedSearchCriterion criterion = new DetailedSearchCriterion(null, String.valueOf(lstsc.isListable()));

            return new SearchCriteriaTranslationResult(criterion);
        } else
        {
            return super.doTranslate(context, criteria);
        }
    }

    @Override
    protected DetailedSearchField doTranslateSearchField(SearchTranslationContext context, ISearchCriteria criteria, ISearchCriteria subCriteria)
    {
        SearchObjectKind objectKind = context.peekObjectKind();

        if (SearchObjectKind.SAMPLE.equals(objectKind))
        {
            if (subCriteria instanceof ListableSampleTypeSearchCriteria)
            {
                SimpleAttributeSearchFieldKind attributeFieldKind =
                        new SimpleAttributeSearchFieldKind(SearchFieldConstants.PREFIX_ENTITY_TYPE + SearchFieldConstants.IS_LISTABLE, "listable");
                return DetailedSearchField.createAttributeField(attributeFieldKind);
            } else
            {
                return DetailedSearchField.createAttributeField(SampleAttributeSearchFieldKind.SAMPLE_TYPE);
            }
        } else
        {
            throw new IllegalArgumentException("Unknown object kind: " + objectKind);
        }
    }
}
