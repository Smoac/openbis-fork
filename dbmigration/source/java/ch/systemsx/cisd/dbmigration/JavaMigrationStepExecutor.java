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

package ch.systemsx.cisd.dbmigration;

import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import ch.systemsx.cisd.common.Script;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
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
public class JavaMigrationStepExecutor extends JdbcDaoSupport implements IJavaMigrationStepExecutor
{
    private static final String JAVA_MIGRATION_STEP_PREFIX = "--JAVA";

    public JavaMigrationStepExecutor(final DataSource dataSource)
    {
        setDataSource(dataSource);
    }

    private IMigrationStep tryExtractMigrationStep(final Script sqlScript)

    {
        if (sqlScript == null || StringUtils.isBlank(sqlScript.getCode()))
        {
            return null;
        }
        final String code = sqlScript.getCode();
        final List<String> lines = Arrays.asList(code.split("\n"));
        IMigrationStep extratedMigrationStepOrNull = null;
        boolean nonEmptyLineFound = false;
        for (final String line : lines)
        {
            // blank lines are allowed at the beginning of the script
            if (StringUtils.isBlank(line))
            {
                continue;
            }
            // only the first non-blank line is supposed to contain Java Migration Step
            if (nonEmptyLineFound == false)
            {
                extratedMigrationStepOrNull = tryExtractMigrationStepFromLine(line);
                nonEmptyLineFound = true;
            } else
            {
                checkIfCurrentLineConsistentWithAlredyProcessed(sqlScript,
                        extratedMigrationStepOrNull, line);
            }
        }
        return extratedMigrationStepOrNull;
    }

    private void checkIfCurrentLineConsistentWithAlredyProcessed(final Script sqlScript,
            final IMigrationStep extratedMigrationStepOrNull, final String line)
    {
        if (tryExtractMigrationStepFromLine(line) != null)
        {
            final String msg;
            if (extratedMigrationStepOrNull != null)
            {

                msg =
                        String.format("Migration script '%s' contains more "
                                + "than one Java Migration Steps.", sqlScript.getName());
            } else
            {
                msg =
                        String.format("Java Migration Step should be defined in the first "
                                + "non-blank line of the migration script '%s'.", sqlScript
                                .getName());
            }
            throw new EnvironmentFailureException(msg);
        }
    }

    private final IMigrationStep tryExtractMigrationStepFromLine(final String lineToProcess)
    {
        final String line = StringUtils.deleteWhitespace(lineToProcess);
        if (line != null && line.startsWith(JAVA_MIGRATION_STEP_PREFIX))
        {
            final String className = StringUtils.removeStart(line, JAVA_MIGRATION_STEP_PREFIX);
            try
            {
                return (IMigrationStep) ClassUtils.createInstance(Class.forName(className));
            } catch (final ClassNotFoundException ex)
            {
                throw new EnvironmentFailureException(String.format("Class '%s' not found.",
                        className));
            } catch (final RuntimeException ex)
            {
                throw new EnvironmentFailureException(ex.getMessage());
            }
        } else
        {
            return null;
        }
    }

    /**
     * Returns null if MigrationStep has not been found and status returned by
     * {@link  IMigrationStep#performPreMigration(org.springframework.jdbc.core.JdbcTemplate)}
     * otherwise.
     */
    public Status tryPerformPreMigration(final Script sqlScript)

    {
        final IMigrationStep migrationStep = tryExtractMigrationStep(sqlScript);
        if (migrationStep != null)
        {
            final Status preMigrationStatus = migrationStep.performPreMigration(getJdbcTemplate());
            return preMigrationStatus;
        } else
        {
            return null;
        }
    }

    /**
     * Returns null if MigrationStep has not been found and status returned by
     * {@link  IMigrationStep#performPostMigration(org.springframework.jdbc.core.JdbcTemplate)}
     * otherwise.
     */
    public Status tryPerformPostMigration(final Script sqlScript)
    {
        final IMigrationStep migrationStep = tryExtractMigrationStep(sqlScript);
        if (migrationStep != null)
        {
            final Status postMigrationStatus =
                    migrationStep.performPostMigration(getJdbcTemplate());
            return postMigrationStatus;
        } else
        {
            return null;
        }
    }

}
