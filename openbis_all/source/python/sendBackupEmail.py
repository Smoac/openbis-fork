'''
  @copyright: 2012 ETH Zuerich, CISD
  
  @license: 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
       http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.


  @author: Manuel Kohler
'''

#!/usr/bin/python

import smtplib
import socket
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
from optparse import OptionParser

smtpServer = 'smtp0.ethz.ch'
fromMail = 'manuel.kohler@bsse.ethz.ch'
COMMASPACE = ', '
hostname = socket.gethostname()

def parseOptions():
  parser = OptionParser()
  parser.add_option("-f", "--file", dest = "file",
                  help = "file containing mail addresses", metavar = "FILE")
  parser.add_option("-r", "--return-value", dest = "returnvalue",
                  help = "returnvalue from a postgres backup", metavar = "RETURN")
  parser.add_option("-t", "--text", dest = "text", help = "additinal text included in email")
  parser.add_option("-s", "--subject", dest = "subject", help = "subject of email")

  (options, args) = parser.parse_args()
  return options

def parseEmailFile(myfile = ''):
  emailList = []
  with open(myfile) as f:
    for line in f:
      emailList.append(line.strip())
  return (emailList)
        
def buildMail(listOfEmails, myoptions):
  mm = MIMEMultipart()
  mm['From'] = fromMail
  mm['To'] = COMMASPACE.join(listOfEmails)
  mm['Subject'] = myoptions.subject
  mm.add_header("X-CISD-Backup", "yes")
  text = "PostgreSQL Backup on\nHost: " + hostname + "\n" +  myoptions.text + "\n"

  # if backup failed and gives a return value other than '0'
  if myoptions.returnvalue != '0':
    mm.add_header("X-Broken-Backup", "yes")
    mm['Subject'] = mm['Subject'] + ' FAILED!'
    text = "FAILED " + text
  
  part1 = MIMEText(text, 'plain')
  mm.attach(part1)
  return mm

def sendMail(mymail, listOfEmails):
  s = smtplib.SMTP(smtpServer)
  s.sendmail(fromMail, listOfEmails, mymail.as_string())
  s.quit()

def main():
  myoptions = parseOptions()
  listOfEmails = parseEmailFile(myoptions.file)
  mymail = buildMail(listOfEmails, myoptions)
  sendMail(mymail, listOfEmails)

if __name__ == "__main__":
  main()
