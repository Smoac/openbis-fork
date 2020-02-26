var UserTests = new function() {

    this.startUserTests = function() {
        testChain = Promise.resolve();
                 //5. User Manager (end of test)
        testChain.then(() => TestUtil.deleteCookies("suitename"))
                 .then(() => TestUtil.login("testId", "pass"))
                 .then(() => this.inventorySpaceForTestUser())
                 //6. Sample Form - Creation
                 .then(() => this.creationSampleForm())
                 //13. Inventory Table - Imports for Create - Automatic Codes
                 .then(() => this.importsAutomaticCodes())
                 //14. Inventory Table - Imports for Create - Given Codes
                 .then(() => this.importsGivenCodes())
                 //15. Sample Form - Storage
                 .then(() => this.storageTest())
                 //16. Storage Manager - Moving Box
                 .then(() => this.movingBoxTest())
                 //17. Storage Manager - Moving Sample
                 .then(() => this.movingSampleTest())
                 //18. Create Protocol
                 .then(() => this.createProtocol())
                 //19. Project Form - Create/Update
                 .then(() => this.createProject())
                 //20. Experiment Form - Create/Update
                 .then(() => this.createExperiment())
                 //21. Experiment Step Form - Create/Update
                 .then(() => this.createExperimentStep())
                 //22. is now disabled
                 //23. Experiment Step Form - Dataset Uploader and Viewer
                 .then(() => this.datasetUploader())
                 //24. Experiment Step Form - Children Generator (not exist)
                 //24. Project  Form - Show in project overview
                 .then(() => this.showInProjectOverview())
                 .catch(error => { console.log(error) });
    }


    this.inventorySpaceForTestUser = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            var ids = ["LAB_NOTEBOOK",
                       "TESTID",
                       "MATERIALS",
                       "_MATERIALS_BACTERIA_BACTERIA_COLLECTION",
                       "_MATERIALS_CELL_LINES_CELL_LINE_COLLECTION",
                       "_MATERIALS_FLIES_FLY_COLLECTION",
                       "_MATERIALS_PLANTS_PLANT_COLLECTION",
                       "_MATERIALS_PLASMIDS_PLASMID_COLLECTION",
                       "_MATERIALS_POLYNUCLEOTIDES_OLIGO_COLLECTION",
                       "_MATERIALS_POLYNUCLEOTIDES_RNA_COLLECTION",
                       "_MATERIALS_REAGENTS_ANTIBODY_COLLECTION",
                       "_MATERIALS_REAGENTS_CHEMICAL_COLLECTION",
                       "_MATERIALS_REAGENTS_ENZYME_COLLECTION",
                       "_MATERIALS_REAGENTS_MEDIA_COLLECTION",
                       "_MATERIALS_REAGENTS_SOLUTION_BUFFER_COLLECTION",
                       "_MATERIALS_YEASTS_YEAST_COLLECTION",
                       "METHODS",
                       "_METHODS_PROTOCOLS_GENERAL_PROTOCOLS",
                       "_METHODS_PROTOCOLS_PCR_PROTOCOLS",
                       "_METHODS_PROTOCOLS_WESTERN_BLOTTING_PROTOCOLS"];

            Promise.resolve().then(() => TestUtil.verifyInventory(ids))
                             .then(() => e.verifyExistence("USER_MANAGER", false))
                             .then(() => resolve());
        });
    }

    this.creationSampleForm = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            Promise.resolve().then(() => UserTests.createBacteria("BAC1", "Aurantimonas"))
                             .then(() => UserTests.createBacteria("BAC2", "Burantimonas"))
                             .then(() => UserTests.createBacteria("BAC3", "Curantimonas"))
                             .then(() => UserTests.createBacteria("BAC4", "Durantimonas"))
                             .then(() => resolve());
        });
    }

    this.createBacteria = function(code, name) {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;
            var testChain = Promise.resolve();

            var richText = '<p><span style="color:#000080;"><strong>F-&nbsp;tonA21 thi-1 thr-1 leuB6 lacY1</strong></span><strong>&nbsp;</strong><span style="color:#008000;"><i><strong>glnV44 rfbC1 fhuA1 ?? mcrB e14-(mcrA-)</strong></i></span><i><strong>&nbsp;</strong></i><span style="color:#cc99ff;"><strong><u>hsdR(rK&nbsp;-mK&nbsp;+) λ-</u></strong></span></p>';

            testChain.then(() => e.waitForId("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                     .then(() => e.click("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                     .then(() => e.waitForId("create-btn"))
                     .then(() => e.click("create-btn"))
                     .then(() => e.waitForId("sampleFormTitle"))
                     .then(() => e.equalTo("sampleFormTitle", "New Bacteria", true, false));

            if (code === "BAC1") {
                // Show Code
                testChain.then(() => e.waitForId("options-menu-btn-sample-view-bacteria"))
                         .then(() => e.click("options-menu-btn-sample-view-bacteria"))
                         .then(() => e.waitForId("options-menu-btn-identification-info"))
                         .then(() => e.click("options-menu-btn-identification-info"));
            }

            testChain.then(() => e.waitForId("codeId"))
                     .then(() => e.waitForFill("codeId"))
                     .then(() => e.equalTo("codeId", code, true, false))
                     .then(() => e.waitForId("NAME"))
                     .then(() => e.change("NAME", name, false))
                     //Paste from Word
                     .then(() => e.waitForCkeditor("BACTERIA.GENOTYPE"))
                     .then(() => TestUtil.ckeditorSetData("BACTERIA.GENOTYPE", richText))
                     .then(() => e.waitForId("save-btn"))
                     .then(() => e.click("save-btn"))
                     //Check saving results
                     .then(() => e.waitForId("edit-btn"))
                     .then(() => e.waitForId("NAME"))
                     .then(() => e.equalTo("NAME", name, true, false))
                     .then(() => TestUtil.ckeditorTestData("BACTERIA.GENOTYPE", richText))
                     .then(() => resolve());
        });
    }

    this.importsAutomaticCodes = function() {
        var baseURL = location.protocol + '//' + location.host + location.pathname;
        var pathToResource = "js/test/resources/bacteria_for_test_without_identifier.tsv";

        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;
            Promise.resolve().then(() => UserTests.importBacteriasFromFile(baseURL + pathToResource))
                             // check that bacterias was created
                             .then(() => e.waitForId("bac5-column-id"))
                             .then(() => e.waitForId("bac6-column-id"))
                             .then(() => e.waitForId("bac7-column-id"))
                             .then(() => e.waitForId("bac8-column-id"))
                             .then(() => resolve());
        });
    }

    this.importsGivenCodes = function() {
        var baseURL = location.protocol + '//' + location.host + location.pathname;
        var pathToResource = "js/test/resources/bacteria_for_test_with_identifier.tsv";

        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;
            Promise.resolve().then(() => UserTests.importBacteriasFromFile(baseURL + pathToResource))
                             // check that bacterias was created
                             .then(() => e.waitForId("bac10-column-id"))
                             .then(() => e.waitForId("bac11-column-id"))
                             .then(() => e.waitForId("next-page-id"))
                             .then(() => e.click("next-page-id"))
                             .then(() => e.waitForId("bac12-column-id"))
                             .then(() => e.waitForId("bac13-column-id"))
                             .then(() => resolve());
        });
    }

    this.importBacteriasFromFile = function(file) {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            Promise.resolve().then(() => e.waitForId("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.click("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.waitForId("sample-options-menu-btn"))
                             .then(() => e.click("sample-options-menu-btn"))
                             .then(() => e.waitForId("register-object-btn"))
                             .then(() => e.click("register-object-btn"))
                             .then(() => e.waitForId("choose-type-btn"))
                             .then(() => e.change("choose-type-btn", "BACTERIA", false))
                             .then(() => TestUtil.setFile("name", file, "text"))
                             .then(() => e.waitForId("accept-type-file"))
                             .then(() => e.click("accept-type-file"))
                             .then(() => resolve());
        });
    }

    this.storageTest = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            Promise.resolve().then(() => e.waitForId("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.click("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.waitForId("bac1-column-id"))
                             .then(() => e.click("bac1-column-id"))
                             .then(() => e.waitForId("edit-btn"))
                             .then(() => e.click("edit-btn"))
                             // we wait for the save-button, cause page contains add-storage-btn
                             // even when page can't be edit. So we wait when page be reloaded.
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.sleep(2000))
                             .then(() => e.waitForId("add-storage-btn"))
                             .then(() => e.click("add-storage-btn"))
                             .then(() => e.waitForId("storage-drop-down-id"))
                             .then(() => e.change("storage-drop-down-id", "DEFAULT_STORAGE", false))
                             .then(() => e.waitForId("storage-drop-down-id-1-2"))
                             .then(() => e.click("storage-drop-down-id-1-2"))
                             .then(() => e.waitForId("box-name-id"))
                             .then(() => e.write("box-name-id", "Test Box", false))
                             .then(() => e.waitForId("box-size-drop-down-id"))
                             .then(() => e.change("box-size-drop-down-id", "4X4", false))
                             .then(() => e.waitForId("storage-drop-down-id-C-2"))
                             .then(() => e.click("storage-drop-down-id-C-2"))
                             .then(() => e.click("storage-accept"))
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.click("save-btn"))
                             // check that new storage was created
                             .then(() => e.waitForId("testbox-c2-id"))
                             .then(() => resolve());
        });
    }

    this.movingBoxTest = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            Promise.resolve().then(() => e.waitForId("STORAGE_MANAGER"))
                             .then(() => e.click("STORAGE_MANAGER"))
                             .then(() => e.waitForId("storage-drop-down-id-a"))
                             .then(() => e.change("storage-drop-down-id-a", "DEFAULT_STORAGE", false))
                             .then(() => e.waitForId("toggle-storage-b-id"))
                             .then(() => e.click("toggle-storage-b-id"))
                             .then(() => e.waitForId("storage-drop-down-id-b"))
                             .then(() => e.change("storage-drop-down-id-b", "BENCH", false))
                             .then(() => e.waitForId("storage-drop-down-id-a-1-2-storage-box"))
                             .then(() => e.waitForId("storage-drop-down-id-b-1-1"))
                             .then(() => e.dragAndDrop("storage-drop-down-id-a-1-2-storage-box", "storage-drop-down-id-b-1-1", false))
                             .then(() => e.equalTo("change-log-container-id", "None", false, false))
                             .then(() => e.click("save-changes-btn"))
                             .then(() => e.sleep(3000))
                             .then(() => resolve());
        });
    }

    this.movingSampleTest = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            Promise.resolve().then(() => e.waitForId("STORAGE_MANAGER"))
                             .then(() => e.click("STORAGE_MANAGER"))
                             .then(() => e.waitForId("storage-drop-down-id-a"))
                             .then(() => e.change("storage-drop-down-id-a", "BENCH", false))
                             .then(() => e.waitForId("storage-drop-down-id-a-1-1"))
                             .then(() => e.waitForId("storage-drop-down-id-a-1-1-storage-box"))
                             .then(() => e.click("storage-drop-down-id-a-1-1-storage-box"))
                             .then(() => e.waitForId("storage-drop-down-id-a-C-2-storage-box"))
                             .then(() => e.dragAndDrop("storage-drop-down-id-a-C-2-storage-box", "storage-drop-down-id-a-A-3", false))
                             .then(() => e.equalTo("change-log-container-id", "None", false, false))
                             .then(() => e.click("save-changes-btn"))
                             // Open object BAC1 and verify storage.
                             .then(() => e.waitForId("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.click("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.waitForId("bac1-column-id"))
                             .then(() => e.click("bac1-column-id"))
                             .then(() => e.waitForId("testbox-a3-id"))
                             .then(() => e.equalTo("testbox-a3-id", "Test Box - A3", true, false))
                             .then(() => e.sleep(3000))
                             .then(() => resolve());
        });
    }

    this.createProtocol = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            Promise.resolve().then(() => e.waitForId("_METHODS_PROTOCOLS_GENERAL_PROTOCOLS"))
                             .then(() => e.click("_METHODS_PROTOCOLS_GENERAL_PROTOCOLS"))
                             .then(() => e.waitForId("create-btn"))
                             .then(() => e.click("create-btn"))
                             .then(() => e.waitForId("options-menu-btn-sample-view-general_protocol"))
                             .then(() => e.click("options-menu-btn-sample-view-general_protocol"))
                             .then(() => e.waitForId("options-menu-btn-identification-info"))
                             .then(() => e.click("options-menu-btn-identification-info"))
                             .then(() => e.waitForId("codeId"))
                             .then(() => e.waitForFill("codeId"))
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.click("save-btn"))
                             .then(() => e.waitForId("edit-btn"))
                             .then(() => resolve());
        });
    }

    this.createProject = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            Promise.resolve().then(() => e.waitForId("TESTID"))
                             .then(() => e.click("TESTID"))
                             .then(() => e.waitForId("create-btn"))
                             .then(() => e.click("create-btn"))
                             .then(() => e.waitForId("project-code-id"))
                             .then(() => e.write("project-code-id", "PROJECT_101", false))
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.click("save-btn"))
                             .then(() => e.waitForId("edit-btn"))
                             .then(() => e.click("edit-btn"))
                             .then(() => e.waitForId("options-menu-btn-project-view"))
                             .then(() => e.click("options-menu-btn-project-view"))
                             .then(() => e.waitForId("options-menu-btn-description"))
                             .then(() => e.click("options-menu-btn-description"))
                             .then(() => e.waitForCkeditor("description-id"))
                             .then(() => TestUtil.ckeditorSetData("description-id", "Test Description 101"))
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.click("save-btn"))
                             .then(() => e.waitForId("edit-btn"))
                             .then(() => resolve());
        });
    }

    this.createExperiment = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            var yesterday = Util.getFormatedDate(new Date(new Date().setDate(new Date().getDate() - 1)));
            var tomorrow = Util.getFormatedDate(new Date(new Date().setDate(new Date().getDate() + 1)));

            Promise.resolve().then(() => e.waitForId("options-menu-btn"))
                             .then(() => e.click("options-menu-btn"))
                             // Create Default Experiment
                             .then(() => e.waitForId("default-experiment"))
                             .then(() => e.click("default-experiment"))
                             .then(() => e.waitForId("codeId"))
                             .then(() => e.waitForFill("codeId"))
                             // add Name
                             .then(() => e.waitForId("NAME"))
                             .then(() => e.change("NAME", "Experiment 101", false))
                             // show in project overview checked
                             .then(() => e.waitForId("SHOW_IN_PROJECT_OVERVIEW"))
                             .then(() => e.checked("SHOW_IN_PROJECT_OVERVIEW", true))
                             .then(() => e.change("SHOW_IN_PROJECT_OVERVIEW", true))
                             // add first comment
                             .then(() => e.waitForId("add-comment-btn"))
                             .then(() => e.click("add-comment-btn"))
                             .then(() => e.waitForId("comment-0-box"))
                             .then(() => e.write("comment-0-box", "My first comment", false))
                             .then(() => e.waitForId("save-comment-0-btn"))
                             .then(() => e.click("save-comment-0-btn"))
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.click("save-btn"))
                             // Update date and name for Experiment
                             .then(() => e.waitForId("edit-btn"))
                             .then(() => e.click("edit-btn"))
                             .then(() => e.waitForId("save-btn"))
                             // edit name
                             .then(() => e.waitForId("NAME"))
                             .then(() => e.change("NAME", "Experiment 101 Bis", false))
                             // set start date
                             .then(() => e.waitForId("START_DATE"))
                             .then(() => e.change("START_DATE", tomorrow, false))
                             // set end date
                             .then(() => e.waitForId("END_DATE"))
                             .then(() => e.change("END_DATE", yesterday, false))
                             // add second comment
                             .then(() => e.waitForId("add-comment-btn"))
                             .then(() => e.click("add-comment-btn"))
                             .then(() => e.waitForId("comment-0-box"))
                             .then(() => e.write("comment-0-box", "My second comment", false))
                             .then(() => e.waitForId("save-comment-0-btn"))
                             .then(() => e.click("save-comment-0-btn"))
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.click("save-btn"))
                             //You should see the error
                             .then(() => e.waitForId("jNotifyDismiss"))
                             .then(() => e.click("jNotifyDismiss"))
                             // fix the error (remove end date) and save experiment
                             .then(() => e.change("END_DATE", "", false))
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.click("save-btn"))
                             .then(() => resolve());
        });
    }

    this.createExperimentStep = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            var tomorrow = Util.getFormatedDate(new Date(new Date().setDate(new Date().getDate() + 1)));

            Promise.resolve().then(() => e.waitForId("options-menu-btn"))
                             .then(() => e.click("options-menu-btn"))
                             // add Experimental Step
                             .then(() => e.waitForId("experimental-step"))
                             .then(() => e.click("experimental-step"))
                             .then(() => e.waitForId("options-menu-btn-sample-view-experimental_step"))
                             .then(() => e.click("options-menu-btn-sample-view-experimental_step"))
                             .then(() => e.waitForId("codeId"))
                             .then(() => e.click("codeId"))
                             // add name
                             .then(() => e.waitForId("NAME"))
                             .then(() => e.change("NAME", "Step 101", false))
                             // show in project overview checked
                             .then(() => e.waitForId("SHOW_IN_PROJECT_OVERVIEW"))
                             .then(() => e.checked("SHOW_IN_PROJECT_OVERVIEW", true))
                             .then(() => e.change("SHOW_IN_PROJECT_OVERVIEW", true))
                             // set start date
                             .then(() => e.waitForId("START_DATE"))
                             .then(() => e.change("START_DATE", tomorrow, false))
                             // add protocol
                             .then(() => e.waitForId("plus-btn-general_protocol"))
                             .then(() => e.click("plus-btn-general_protocol"))
                             .then(() => e.waitForId("gen1-column-id"))
                             .then(() => e.click("gen1-column-id"))
                             // Operations
                             .then(() => e.waitForId("gen1-operations-column-id"))
                             .then(() => e.click("gen1-operations-column-id"))
                             .then(() => e.waitForId("gen1-operations-column-id-use-as-template"))
                             .then(() => e.click("gen1-operations-column-id-use-as-template"))
                             .then(() => e.waitForId("newSampleCodeForCopy"))
                             .then(() => e.write("newSampleCodeForCopy", "CODE1", false))
                             .then(() => e.waitForId("copyAccept"))
                             .then(() => e.click("copyAccept"))
                             // add first comment
                             .then(() => e.waitForId("add-comment-btn"))
                             .then(() => e.click("add-comment-btn"))
                             .then(() => e.waitForId("comment-0-box"))
                             .then(() => e.write("comment-0-box", "My first comment", false))
                             .then(() => e.waitForId("save-comment-0-btn"))
                             .then(() => e.click("save-comment-0-btn"))
                             .then(() => e.waitForId("code1-column-id"))
                             // save
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.click("save-btn"))
                             // edit
                             .then(() => e.waitForId("edit-btn"))
                             .then(() => e.click("edit-btn"))
                             .then(() => e.waitForId("save-btn"))
                             // edit name
                             .then(() => e.waitForId("NAME"))
                             .then(() => e.change("NAME", "Step 101 Bis", false))
                             // save
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.click("save-btn"))
                             .then(() => resolve());
        });
    }

    this.datasetUploader = function() {
        var baseURL = location.protocol + '//' + location.host + location.pathname;
        var pathToResource = "js/test/resources/test-image.png";

        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            Promise.resolve().then(() => e.waitForId("upload-btn"))
                             .then(() => e.click("upload-btn"))
                             // choose type
                             .then(() => e.waitForId("DATASET_TYPE"))
                             .then(() => e.changeSelect2("DATASET_TYPE", "ELN_PREVIEW", false))
                             // add first comment
                             .then(() => e.waitForId("add-comment-btn"))
                             .then(() => e.click("add-comment-btn"))
                             .then(() => e.waitForId("comment-0-box"))
                             .then(() => e.write("comment-0-box", "My first comment", false))
                             .then(() => e.waitForId("save-comment-0-btn"))
                             .then(() => e.click("save-comment-0-btn"))
                             // upload image
                             .then(() => e.dropFile("test-image.png", baseURL + pathToResource, "filedrop", false))
                             .then(() => e.waitForClass("progressbar.ready"))
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.click("save-btn"))
                             // open data set and edit it
                             .then(() => e.waitForId("dataSetPosInTree-0"))
                             .then(() => e.click("dataSetPosInTree-0"))
                             .then(() => e.waitForId("dataset-edit-btn"))
                             .then(() => e.click("dataset-edit-btn"))
                             .then(() => e.waitForId("save-btn"))
                             // change Name
                             .then(() => e.waitForId("NAME"))
                             .then(() => e.change("NAME", "New Name", false))
                             .then(() => e.click("save-btn"))
                             .then(() => e.waitForId("dataset-edit-btn"))
                             .then(() => resolve());
        });
    }

    this.showInProjectOverview = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;
                             // go to project 101
            Promise.resolve().then(() => e.waitForId("PATH_TESTID_PROJECT_101"))
                             .then(() => e.click("PATH_TESTID_PROJECT_101"))
                             // click "Show Experiments"
                             .then(() => e.waitForId("options-menu-btn-project-view"))
                             .then(() => e.waitForId("project-experiments"))
                             .then(() => e.waitForStyle("project-experiments", "display", "none", false))
                             .then(() => e.click("options-menu-btn-project-view"))
                             .then(() => e.waitForId("options-menu-btn-experiments"))
                             .then(() => e.click("options-menu-btn-experiments"))
                             .then(() => e.waitForId("project-experiments"))
                             .then(() => e.waitForStyle("project-experiments", "display", "", false))
                             // click "Show Objects"
                             .then(() => e.waitForId("options-menu-btn-project-view"))
                             .then(() => e.waitForId("project-samples"))
                             .then(() => e.waitForStyle("project-samples", "display", "none", false))
                             .then(() => e.click("options-menu-btn-project-view"))
                             .then(() => e.waitForId("options-menu-btn-objects"))
                             .then(() => e.click("options-menu-btn-objects"))
                             .then(() => e.waitForId("project-samples"))
                             .then(() => e.waitForStyle("project-samples", "display", "", false))
                             .then(() => resolve());
        });
    }
}