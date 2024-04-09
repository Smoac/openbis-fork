import jquery from "./types/jquery"
import underscore from "./types/underscore"
import common from "./types/common"
import openbis from "./types/openbis.esm"

exports.default = new Promise((resolve) => {
    require(["jquery", "underscore", "openbis", "test/common", "test/dtos"], function (
        $: jquery.JQueryStatic,
        _: underscore.UnderscoreStatic,
        openbisRequireJS,
        common: common.CommonConstructor,
        dtos
    ) {
        var executeModule = function (moduleName: string, facade: openbis.openbis, dtos: openbis.bundle) {
            QUnit.module(moduleName)

            QUnit.test("begin() and rollback()", async function (assert) {
                const testInteractiveSessionKey = "test-interactive-session-key"
                const testFolder = "test-begin-rollback"
                const testFile = "test-file"
                const testContent = "test-content"

                try {
                    var c = new common(assert, dtos)
                    c.start()

                    await c.login(facade)

                    await c.deleteFile(facade, testFolder, "")

                    facade.setInteractiveSessionKey(testInteractiveSessionKey)

                    await facade.beginTransaction()

                    const spaceCreation = new dtos.SpaceCreation()
                    spaceCreation.setCode(c.generateId("TRANSACTION_TEST_"))

                    const spaceIds = await facade.createSpaces([spaceCreation])

                    const spacesBeforeRollback = await facade.getSpaces(spaceIds, new dtos.SpaceFetchOptions())
                    c.assertEqual(Object.keys(spacesBeforeRollback).length, 1, "Space exists in the transaction")

                    const projectCreation = new dtos.ProjectCreation()
                    projectCreation.setCode(c.generateId("TRANSACTION_TEST_"))
                    projectCreation.setSpaceId(spaceIds[0])

                    const projectIds = await facade.createProjects([projectCreation])

                    const projectsBeforeRollback = await facade.getProjects(projectIds, new dtos.ProjectFetchOptions())
                    c.assertEqual(Object.keys(projectsBeforeRollback).length, 1, "Project exists in the transction")

                    await facade.getAfsServerFacade().write(testFolder, testFile, 0, testContent)

                    await facade.rollbackTransaction()

                    const spacesAfterRollback = await facade.getSpaces(spaceIds, new dtos.SpaceFetchOptions())
                    c.assertEqual(Object.keys(spacesAfterRollback).length, 0, "Space does not exist after a rollback")

                    const projectsAfterRollback = await facade.getProjects(projectIds, new dtos.ProjectFetchOptions())
                    c.assertEqual(Object.keys(projectsAfterRollback).length, 0, "Project does not exist after a rollback")

                    await c.assertFileDoesNotExist(facade, testFolder, testFile)

                    c.finish()
                } catch (error) {
                    c.fail(error)
                    c.finish()
                }
            })

            QUnit.test("begin() and commit()", async function (assert) {
                const testInteractiveSessionKey = "test-interactive-session-key"
                const testFolder = "test-begin-commit"
                const testFile = "test-file"
                const testContent = "test-content"

                try {
                    var c = new common(assert, dtos)
                    c.start()

                    await c.login(facade)

                    await c.deleteFile(facade, testFolder, "")

                    facade.setInteractiveSessionKey(testInteractiveSessionKey)

                    await facade.beginTransaction()

                    const spaceCreation = new dtos.SpaceCreation()
                    spaceCreation.setCode(c.generateId("TRANSACTION_TEST_"))

                    const spaceIds = await facade.createSpaces([spaceCreation])

                    const spacesBeforeCommit = await facade.getSpaces(spaceIds, new dtos.SpaceFetchOptions())
                    c.assertEqual(Object.keys(spacesBeforeCommit).length, 1, "Space exists in the transaction")

                    const projectCreation = new dtos.ProjectCreation()
                    projectCreation.setCode(c.generateId("TRANSACTION_TEST_"))
                    projectCreation.setSpaceId(spaceIds[0])

                    const projectIds = await facade.createProjects([projectCreation])

                    const projectsBeforeCommit = await facade.getProjects(projectIds, new dtos.ProjectFetchOptions())
                    c.assertEqual(Object.keys(projectsBeforeCommit).length, 1, "Project exists in the transction")

                    await facade.getAfsServerFacade().write(testFolder, testFile, 0, testContent)

                    await facade.commitTransaction()

                    const spacesAfterCommit = await facade.getSpaces(spaceIds, new dtos.SpaceFetchOptions())
                    c.assertEqual(Object.keys(spacesAfterCommit).length, 1, "Space exist after commit")

                    const projectsAfterCommit = await facade.getProjects(projectIds, new dtos.ProjectFetchOptions())
                    c.assertEqual(Object.keys(projectsAfterCommit).length, 1, "Project exists after commit")

                    await c.assertFileExists(facade, testFolder, testFile)

                    var filesAfterCommit = await facade.getAfsServerFacade().list(testFolder, "", false)

                    c.assertFileEquals(filesAfterCommit[0], {
                        path: "/" + testFile,
                        owner: testFolder,
                        name: testFile,
                        size: testContent.length,
                        directory: false,
                    })

                    var fileContentAfterCommit = await facade.getAfsServerFacade().read(testFolder, testFile, 0, testContent.length)
                    c.assertEqual(await fileContentAfterCommit.text(), testContent)

                    c.finish()
                } catch (error) {
                    c.fail(error)
                    c.finish()
                }
            })
        }

        resolve(function () {
            var afsServerUrl = "http://localhost:8085/data-store-server"
            executeModule("Transactions tests (RequireJS)", new openbisRequireJS(null, afsServerUrl), dtos)
            executeModule("Transactions tests (module VAR)", new window.openbis.openbis(null, afsServerUrl), window.openbis)
            executeModule("Transactions tests (module ESM)", new window.openbisESM.openbis(null, afsServerUrl), window.openbisESM)
        })
    })
})
