SPACE_CODE = "MOVE_COPY_TEST"
PROJECT_ID = "/%s/P1" % SPACE_CODE
EXPERIMENT_ID = "%s/E1" % PROJECT_ID

def process(transaction):
    space = transaction.createNewSpace(SPACE_CODE, None)
    project = transaction.createNewProject(PROJECT_ID)
    experiment = transaction.createNewExperiment(EXPERIMENT_ID, 'SIRNA_HCS')
    experiment.setPropertyValue('DESCRIPTION', 'test')
    
    ds1 = transaction.createNewDataSet('UNKNOWN', 'MOVE_COPY_FILE_TO_MOVE')
    ds1.setExperiment(experiment)
    transaction.copyFile('my-data/greetings.txt', ds1, 'somewhere/file_copy/greetings.txt', False)
    transaction.copyFile('my-data/greetings.txt', ds1, 'somewhere/file_copy_hard_link/greetings1.txt', True)
    transaction.copyFile('my-data/greetings.txt', ds1, 'somewhere/file_copy_hard_link/greetings2.txt', True)
    transaction.moveFile('my-data/greetings.txt', ds1, 'somewhere/file_move/greetings.txt')
    transaction.copyFile('my-data/folder_to_copy', ds1, 'somewhere/folder_copy', False)
    transaction.copyFile('my-data/folder_to_copy', ds1, 'somewhere/folder_copy_hard_link1', True)
    transaction.copyFile('my-data/folder_to_copy', ds1, 'somewhere/folder_copy_hard_link2', True)
    transaction.moveFile('my-data/folder_to_move', ds1, 'somewhere/folder_move')
