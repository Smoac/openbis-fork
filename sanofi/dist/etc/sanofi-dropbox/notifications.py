import config

from ch.systemsx.cisd.openbis.generic.shared.basic.dto.api import ValidationException

from org.apache.commons.lang.exception import ExceptionUtils
from ch.systemsx.cisd.common.mail import From

reload(config)

class EmailUtils:
    state = None
    incoming = None
    experiment = None
    plateCode = None
    
    def __init__(self, state, incoming, experiment, plateCode):
        self.state = state
        self.incoming = incoming
        self.experiment = experiment
        self.plateCode = plateCode

    # ------------ public methods ------------ 

    def sendRollbackError(self, ex):
        if self.isUserError(ex):
            shortErrorMessage = ex.getMessage()
            if not shortErrorMessage:
                shortErrorMessage = ex.value.getMessage()
            self.sendUserError(shortErrorMessage, self.getAllEmailRecipients())
        else:
            fullErrorMessage = ExceptionUtils.getFullStackTrace(ex)
            self.sendAdminError(fullErrorMessage, self.getAdminEmails())
            self.sendSystemErrorNotificationToUser(self.getUserEmails())
        
    def sendSuccessConfirmation(self):
        plateLink = self.createPlateLink(config.OPENBIS_URL, self.plateCode)
        incomingFileName = self.incoming.getName()
        self.sendEmail("openBIS: New data registered for %s" % (self.plateCode), 
"""Dear openBIS user,
New data from folder '%(incomingFileName)s' has been successfully registered for plate %(plateLink)s.
       
This email has been generated automatically.
      
Have a nice day!
Administrator""" % vars(), self.getAllEmailRecipients())

    # ------------ private methods ------------ 
    
    def isUserError(self, ex):
        if hasattr(ex, "value") and ex.value and hasattr(ex.value, "getClass"):
            return (ex.value.getClass() == ValidationException("").getClass())
        
        return False
    
    def getAdminEmails(self):
        admins = self.state.getOpenBisService().listAdministrators()
        adminEmails = [ admin.getEmail() for admin in admins if admin.getEmail() ]
        return adminEmails
    
    def getUserEmails(self):
        recipients = []
        
        if self.experiment:
            recipientsProp = self.experiment.getPropertyValue(config.EXPERIMENT_RECIPIENTS_PROPCODE)
            if recipientsProp:
               recipients = [ email.strip() for email in recipientsProp.split(",") ]
            
        return recipients
    
    def getAllEmailRecipients(self):
        return self.getAdminEmails() + self.getUserEmails()

    def sendEmail(self, title, content, recipients):
        if not recipients:
            if self.experiment:
                experimentMsg = ("Please, fill in e-mail recipients list in the property '%s' of experiment '%s'." % (config.EXPERIMENT_RECIPIENTS_PROPCODE, self.experiment.getExperimentIdentifier()))
            else:
                experimentMsg = "" 
            self.state.operationLog.error("Failed to detect e-mail recipients for incoming folder '%s'.%s"
                                     " No e-mails will be sent." 
                                     "\nEmail title: %s" 
                                     "\nEmail content: %s" % 
                                     (self.incoming.getName(), experimentMsg, title, content))
            return
        
        fromAddress = From("openbis@sanofi-aventis.com")
        replyTo = None
        self.state.mailClient.sendMessage(title, content, replyTo, fromAddress, recipients)
    
    def createPlateLink(self, openbisUrl, code):
        return "%(openbisUrl)s#entity=SAMPLE&sample_type=PLATE&action=SEARCH&code=%(code)s" % vars()
    
    def sendUserError(self, errorDetails, recipients):
        incomingFileName = self.incoming.getName()   
        localPlateCode = self.plateCode
        
        if localPlateCode:
            self.sendEmail("openBIS: Data registration failed for %s" % (localPlateCode), 
"""Dear openBIS user,
Registering new data for plate %(localPlateCode)s has failed with error '%(errorDetails)s'.
The name of the incoming folder '%(incomingFileName)s' was added to '.faulty_paths'. Please,
repair the problem and remove the entry from '.faulty_paths' to retry registration.
       
This email has been generated automatically.
      
Administrator""" % vars(), recipients)
        else:
            self.sendEmail("openBIS: Data registration failed for folder '%s'" % (incomingFileName), 
"""Dear openBIS user,
openBIS was unable to understand the name of an incoming folder. 
Detailed error message was '%(errorDetails)s'.
      
This email has been generated automatically.
      
Administrator""" % vars(), recipients)
    
    def sendAdminError(self, errorDetails, recipients):        
        incomingFileName = self.incoming.getName()
            
        self.sendEmail("openBIS System Error: Data registration failed for %s" % (incomingFileName), 
"""Dear openBIS Administrator,
    
The registration of data sets from incoming folder '%(incomingFileName)s' has failed
in an unexpected way. The most probable cause is a misconfiguration of the system. It may be also a bug. 
Here is a full description of the encountered error:
      
%(errorDetails)s
      
Please, repair the problem and remove the entry from '.faulty_paths' to retry registration.
      
openBIS""" % vars(), recipients)
        
    def sendSystemErrorNotificationToUser(self, recipients):        
        incomingFileName = self.incoming.getName()
            
        self.sendEmail("openBIS: Data registration failed for folder '%s'" % (incomingFileName), 
"""Dear openBIS user,
Registering new data from incoming folder '%(incomingFileName)s' has failed due to a system error.
      
openBIS has sent a notification to the responsible system administrators and they should be 
fixing the problem as soon as possible. 
      
We are sorry for any inconveniences this may have caused. 
      
openBIS Administrators""" % vars(), recipients)
