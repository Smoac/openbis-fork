package ch.systemsx.cisd.openbis.dss.generic.server.ftp.v3.plugins;

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.common.jython.JythonUtils;
import ch.systemsx.cisd.common.jython.evaluator.Evaluator;
import ch.systemsx.cisd.common.jython.evaluator.IJythonEvaluator;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverContext;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.v3.V3ResolverPlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.v3.file.V3FtpFile;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.v3.file.V3FtpNonExistingFile;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;

public class JythonResolver implements V3ResolverPlugin
{

    private static Map<String, IJythonEvaluator> interpreters = new HashMap<>();

    private static final String RESOLVE_FUNCTION_NAME = "resolve";

    private IJythonEvaluator interpreter;

    private String code;

    private String pluginName;

    @Override
    public V3FtpFile resolve(String fullPath, String[] pathItems, FtpPathResolverContext resolverContext)
    {
        if (fullPath.startsWith("/" + code))
        {
            String shortPath = fullPath.substring(code.length() + 1);
            if (shortPath.startsWith("/"))
            {
                shortPath = shortPath.substring(1);
            }
            Object result = interpreter.evalFunction(RESOLVE_FUNCTION_NAME, shortPath, fullPath, resolverContext);
            return (V3FtpFile) result;
        } else
        {
            return new V3FtpNonExistingFile(fullPath, "invalid request to plugin " + pluginName + ": " + fullPath);
        }
    }

    @Override
    public void initialize(String pluginName, String code)
    {
        this.pluginName = pluginName;
        this.code = code;

        if (interpreters.containsKey(pluginName) == false)
        {
            String scriptPath = PropertyUtils.getMandatoryProperty(DssPropertyParametersUtil.loadServiceProperties(), pluginName + ".script-file");
            String scriptString = JythonUtils.extractScriptFromPath(scriptPath);
            String[] pythonPath = JythonUtils.getScriptDirectoryPythonPath(scriptPath);
            interpreters.put(pluginName, Evaluator.getFactory().create("", pythonPath, scriptPath, null, scriptString, false));
        }
        interpreter = interpreters.get(pluginName);
    }

}
