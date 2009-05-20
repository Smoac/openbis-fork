/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.dbmigration.java;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.Script;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.parser.Line;
import ch.systemsx.cisd.common.parser.ParserUtilities;
import ch.systemsx.cisd.common.parser.filter.NonEmptyLineFilter;
import ch.systemsx.cisd.common.utilities.ClassUtils;

/**
 * Allows to extract {@link IMigrationStep} class from migration script and run the <i>pre</i>- and
 * <i>post</i>- migration java steps.<br>
 * Example of the script containing Java Migration Step definition:
 * 
 * <pre>
 * -- JAVA ch.systemsx.cisd.openbis.db.migration.MigrationStepFrom022To023
 * </pre>
 * 
 * @author Izabela Adamczyk
 */
public class MigrationStepExecutor extends SimpleJdbcDaoSupport implements IMigrationStepExecutor
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, MigrationStepExecutor.class);

    private static final String JAVA_MIGRATION_STEP_PREFIX = "--JAVA";

    private static final String JAVA_ADMIN_MIGRATION_STEP_PREFIX = "--JAVA_ADMIN";

    private final boolean isAdmin;

    private IMigrationStep migrationStep;

    private boolean inited;

    public MigrationStepExecutor(final DataSource dataSource, boolean isAdmin)
    {
        setDataSource(dataSource);
        this.isAdmin = isAdmin;
    }

    private final static IMigrationStep tryExtractMigrationStep(final Script sqlScript,
            final boolean isAdmin)

    {
        assert sqlScript != null : "SQL script not provided";
        final String content = sqlScript.getContent();
        if (StringUtils.isBlank(content))
        {
            return null;
        }
        final Line firstNonEmptyLineOrNull =
                ParserUtilities.tryGetFirstAcceptedLine(content, NonEmptyLineFilter.INSTANCE);
        if (firstNonEmptyLineOrNull != null)
        {
            return tryExtractMigrationStepFromLine(firstNonEmptyLineOrNull.getText(), isAdmin);
        }
        return null;
    }

    private final static IMigrationStep tryExtractMigrationStepFromLine(final String lineToProcess,
            final boolean isAdmin)
    {
        final String line = StringUtils.deleteWhitespace(lineToProcess);
        final String prefix =
                isAdmin ? JAVA_ADMIN_MIGRATION_STEP_PREFIX : JAVA_MIGRATION_STEP_PREFIX;
        if (line != null && line.startsWith(prefix))
        {
            final String className = StringUtils.removeStart(line, prefix);
            try
            {
                return (IMigrationStep) ClassUtils.createInstance(Class.forName(className));
            } catch (final ClassNotFoundException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        } else
        {
            return null;
        }
    }

    //
    // IMigrationStepExecutor
    //

    public final void init(final Script migrationScript)
    {
        migrationStep = tryExtractMigrationStep(migrationScript, isAdmin);
        if (migrationStep != null)
        {
            operationLog.info(String.format(
                    "Migration step class '%s' found for migration script '%s'.", migrationStep
                            .getClass().getSimpleName(), migrationScript.getName()));
        } else
        {
            operationLog.debug(String.format(
                    "No migration step class found for migration script '%s'.", migrationScript
                            .getName()));
        }
        inited = true;
    }

    public final void performPreMigration()
    {
        assert inited : "Executor not initialized.";
        if (migrationStep != null)
        {
            migrationStep.performPreMigration(getSimpleJdbcTemplate());
        }
    }

    public final void performPostMigration()
    {
        assert inited : "Executor not initialized.";
        if (migrationStep != null)
        {
            migrationStep.performPostMigration(getSimpleJdbcTemplate());
        }
    }

    public final void finish()
    {
        inited = false;
        migrationStep = null;
    }
}
