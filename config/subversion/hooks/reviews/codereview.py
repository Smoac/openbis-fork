#!/usr/bin/python
import smtplib
import syslog
import sys
from email.MIMEMultipart import MIMEMultipart
from email.MIMEText import MIMEText
from email.MIMEMessage import MIMEMessage


class CodeReview:
  def __init__(self, settings, sender, subject_prefix):
    self.smtp_server = settings.smtp_server
    self.recipients = settings.recipients
    self.hostname = settings.hostname
    self.path = settings.path
    self.subject_prefix = subject_prefix
    self.sendmail = settings.sendmail
    self.message = MIMEText('')
    self.message['From'] = sender
    self.message['To'] = ', '.join(settings.recipients)


  def attachText(self, text):
    """Adds text version of code review e-mail"""
    self.message.set_payload(text)


  def setSubjectInfo(self, subject_info):
    """Sets subject for code-review e-mail"""
    self.message['Subject'] = self.subject_prefix + subject_info;


  def send(self, sender):
    """Sends e-mail"""
    if not self.sendmail:
      return
    session = smtplib.SMTP(self.smtp_server, "25")

    syslog.syslog('sending message to' + self.message['To'])

    smtpresult = session.sendmail(self.message['From'], self.recipients, 
        self.message.as_string())
    
    if smtpresult:
      errstr = ""
      for recip in smtpresult.keys():
        errstr = """Could not deliver mail to: %s \n Server said: %s \n %s \n  %s""" \
             % (recip, smtpresult[recip][0], smtpresult[recip][1], errstr)
      raise smtplib.SMTPException, errstr
