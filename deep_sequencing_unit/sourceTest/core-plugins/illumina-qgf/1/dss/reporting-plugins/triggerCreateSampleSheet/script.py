def aggregate(parameters, tableBuilder):
  
  import subprocess
  sampleSheetBinary = "/links/application/dsu/createSampleSheet/createSampleSheet.sh"
  sampleSheetBinaryMiSeq = "/links/application/dsu/createSampleSheet/createSampleSheet_miseq.sh"

  miSeqPath = "/links/shared/dsu/runs/yoda"

  sampleId = parameters.get("sampleId")
  sendEmail = parameters.get("sendEmail")
  miSeqRun = parameters.get("miSeqRun")
  flowcellId = sampleId.split('/')[-1]

  if '-' in flowcellId:
    flowCellName = flowcellId.split('_')[3]
  else:
    flowCellName = flowcellId.split('_')[3][1:]

  sampleSheetFileName = "SampleSheet_" + flowCellName + ".csv"

  if miSeqRun:
    binary = sampleSheetBinaryMiSeq
  else:
    binary = sampleSheetBinary

  sampleSheet = subprocess.Popen([binary, "-f", flowcellId, "-v", "-o", miSeqPath, "=l", "win32"],  stdout=subprocess.PIPE)
  result = sampleSheet.stdout.read()
  ssList = result.split('\n')
  if sendEmail:
    mailService.createEmailSender().withSubject("Sample Sheet for flow cell: " + flowcellId).withAttachedText(result, sampleSheetFileName).send()

  tableBuilder.addHeader("CSV") 

  row = tableBuilder.addRow()
  row.setCell("CSV","Created Sample Sheet "+ sampleSheetFileName + " and stored here: " + "Y:/yoda")
  row = tableBuilder.addRow()
  row.setCell("CSV",200*"-")
  
  for line in ssList:
    row = tableBuilder.addRow()
    row.setCell("CSV",line)
  
  row = tableBuilder.addRow()
  row.setCell("CSV",200*"-")
