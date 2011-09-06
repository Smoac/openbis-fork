#! /usr/bin/env python
# Jython dropbox which is not used by iBrain2.
# It is suitable to upload any uninterpreted datasets with the API.

transaction = service.transaction()
dataset = transaction.createNewDataSet()
transaction.moveFile(incoming.getPath(), dataset)
