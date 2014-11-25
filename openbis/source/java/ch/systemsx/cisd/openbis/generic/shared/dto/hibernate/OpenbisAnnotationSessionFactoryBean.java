/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto.hibernate;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;

/**
 * Overrides {@code org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean} to modify {@code access_timestamp} column definition.
 * 
 * @author Jakub Straszewski
 */
/*
 * The solution designed here is a bit ugly, but the best one I could find. It requires the users using access timestamp in 13.04 release to set a
 * java option for openbis script. This is a bit inconvenient, but at the moment when this code is executed there is no DataDAO yet, so we can't read
 * database. And DAOFactory depends in spring configuration on hibernate so we cannot reverse the order of initialisation.
 */
/*
 * The way we handle the case when there is no access timestamp is also not the most elegant. We just replace the access_timestamp column definition
 * with modification timestamp. It would be more desirable to have the annotation completely removed, but removing the single column from table does
 * not work. It would be better to have no real annotation on the field and create it from here, but it is not as simple as adding one line, as the
 * AnnotationConfiguration is not a simple annotated configuration, but it operates on some complex objects, which are not so easy to
 * create/manipulate. The Table or Column objects also do not give us the option to disable or remove the column definition. The only thing we have to
 * worry is if there are no issues that when there is no access timestamp, then modification timestamp refers to two fields, but that seems to be not
 * an issue.
 */
@SuppressWarnings("deprecation")
public class OpenbisAnnotationSessionFactoryBean extends AnnotationSessionFactoryBean
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, OpenbisAnnotationSessionFactoryBean.class);

    @Override
    protected void postProcessAnnotationConfiguration(AnnotationConfiguration config)
            throws HibernateException
    {
        String isAccessTimestampEnabled = System.getProperty("access-timestamp-enabled", "");

        if (isAccessTimestampEnabled.equals("true"))
        {
            operationLog
                    .info("Enabling access timestamp column annotations");
        } else
        {
            removeAccessTimestampColumn(config);
            operationLog
                    .info("Access timestamp column is not enabled. In order to use access_timestamp "
                            + "column you should apply the special database patch and set the java runtime property.\n"
                            + "in file servers/openBIS-server/jetty/etc/openbis.conf amend JAVA_OPTS with -Daccess-timestamp-enabled=true");
        }
    }

    private void removeAccessTimestampColumn(AnnotationConfiguration config)
    {
        Table table = config.getClassMapping(DataPE.class.getCanonicalName()).getRootClass().getTable();
        Iterator<?> it = table.getColumnIterator();

        while (it.hasNext())
        {
            Column column = (Column) it.next();
            if (column.getName() == ColumnNames.ACCESS_TIMESTAMP)
            {
                column.setName(ColumnNames.MODIFICATION_TIMESTAMP_COLUMN);
            }
        }
    }
}
