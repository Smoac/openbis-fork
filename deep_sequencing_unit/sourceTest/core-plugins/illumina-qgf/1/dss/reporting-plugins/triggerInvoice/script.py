def process(tr, parameters, tableBuilder):
  
  import subprocess
  import os
  from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria

  base = "/home/sbsuser/openbis/createInvoices/"
  createInvoiceBinary = base + "createInvoices.sh"
  outDir = base + "invoices/"
  magicString = "@Invoice@"

  invoiceSentList = []
  tmpIndexList = []

  sampleId = parameters.get("sampleId")
  sendEmail = parameters.get("sendEmail")
  flowcellId = sampleId.split('/')[-1]
  invoiceList = subprocess.Popen([createInvoiceBinary, "-f", flowcellId, "-o", outDir, "-d"],  stdout=subprocess.PIPE)
  result = invoiceList.stdout.read()
  
  ssList = result.split('\n')
  print ssList

  for idx, element in enumerate(ssList):
    if element.startswith(magicString):
      tmpIndexList.append(idx)
      invoiceSentList.append(element.split('@')[-1])

  tmpIndexList.sort(reverse=True)
  for idx in tmpIndexList:
    ssList.pop(idx)
      
  ssList = ssList[:-1]

  if sendEmail:
    mailService.createEmailSender().withSubject("Invoice for flow cell: " + flowcellId).withAttachedText(result, "Invoice_" + flowcellId + ".csv").send()

  # Search for the sample
  search_service = tr.getSearchService()
  sc = SearchCriteria()
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, flowcellId));
  myFC = search_service.searchForSamples(sc)
  try:
    assert myFC.size() == 1
  except AssertionError:
    print (str(myFC.size()) +  " " + myFC.getCode())

  def attachFile(fileToAttach):
    f = open(fileToAttach, 'r')
    print ("Attaching " + fileToAttach + " to " + myFC[0].getSampleIdentifier())
    updateableSample = tr.getSampleForUpdate(myFC[0].getSampleIdentifier())
    updateableSample.addAttachment(fileToAttach, "Invoice for Flow Cell " + flowcellId, "Invoice", f.read())

  column1 = "CSV"
  column2 = "invoiceFlag"

  tableBuilder.addHeader(column1) 
  tableBuilder.addHeader(column2) 

  for pI in invoiceSentList:
    print pI
 
  for line in ssList:
    XLXSfile = os.path.split(line)[-1]
    print ("___________________________")
    print line
    print "File: " + XLXSfile
    #line = '<a target="_self" href="' + filePath + XLXSfile + '">' + XLXSfile + '</a>' 
    attachFile(outDir +  XLXSfile)
    row = tableBuilder.addRow()
    row.setCell(column1,"Attached file as " + XLXSfile)
 
  for pI in invoiceSentList:
    row = tableBuilder.addRow()
    row.setCell(column2, pI)
