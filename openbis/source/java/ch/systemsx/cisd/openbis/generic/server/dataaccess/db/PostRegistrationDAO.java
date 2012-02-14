package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPostRegistrationDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PostRegistrationPE;

public class PostRegistrationDAO extends AbstractGenericEntityDAO<PostRegistrationPE> implements
        IPostRegistrationDAO
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            PostRegistrationDAO.class);

    protected PostRegistrationDAO(SessionFactory sessionFactory, DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance, PostRegistrationPE.class);
    }

    public void addDataSet(DataPE dataset)
    {
        PostRegistrationPE element = new PostRegistrationPE();

        element.setDataSet(dataset);
        HibernateTemplate template = getHibernateTemplate();
        template.persist(element);
        template.flush();
    }

    public void removeDataSet(DataPE dataSet)
    {
        PostRegistrationPE p = tryFindByDataSet(dataSet);

        if (p != null)
        {
            HibernateTemplate template = getHibernateTemplate();
            template.delete(p);
        }
    }

    public PostRegistrationPE tryFindByDataSet(final DataPE dataSet)
    {
        assert dataSet != null : "Unspecified dataset.";

        final Criteria criteria = getSession().createCriteria(getEntityClass());
        criteria.add(Restrictions.eq("dataSet", dataSet));
        final PostRegistrationPE pr = (PostRegistrationPE) criteria.uniqueResult();
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "Following experiment '%s' has been found for dataSet '%s'.", pr, dataSet));
        }
        return pr;
    }

    public Collection<Long> listDataSetsForPostRegistration()
    {
        final List<PostRegistrationPE> list =
                cast(getHibernateTemplate().loadAll(PostRegistrationPE.class));

        ArrayList<Long> idList = new ArrayList<Long>(list.size());

        for (PostRegistrationPE pr : list)
        {
            idList.add(pr.getDataSet().getId());
        }
        return idList;
    }

}
