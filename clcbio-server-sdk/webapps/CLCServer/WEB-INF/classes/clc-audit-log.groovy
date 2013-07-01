import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.encoder.LayoutWrappingEncoder
import com.clcbio.server.log.SimpleAuditlogLayout
import com.clcbio.server.configuration.ServerConfigurationHelper

import static ch.qos.logback.classic.Level.ALL

appender("AUDIT LOG FILE", RollingFileAppender) {
    encoder(LayoutWrappingEncoder) {
        layout(SimpleAuditlogLayout) {
       /*
			%d    Date and time
			%l    Log leve
			%o    Operation: Login, Logout, Command queued, Command done, Command executing, Change server configuration, Server lifecycle. More may be added and existing may be changed or removed.
			%u    User
			%i    IP
			%pn   Process name (when operation is one of the Command values) or description of server lifecycle (when operation is Server lifecycle)
			%pi   Process identifier - can be used to differentiate several processes of the same type.
		*/
            pattern =  "%d\t%l\t%o\t%u\t%i\t%pn\t%pi"
            elementNotFound = "-"
        }
    }
    file = ServerConfigurationHelper.getCLCServerHomeConfFilePath()+"/audit.log"
    rollingPolicy("ch.qos.logback.core.rolling.TimeBasedRollingPolicy") {
        fileNamePattern = ServerConfigurationHelper.getCLCServerHomeConfFilePath()+"/audit.%d{yyyy-MM-dd}.log"
        maxHistory = 31
    }
}

logger("com.clcbio.server.log.audit", ALL, ["AUDIT LOG FILE"], false)