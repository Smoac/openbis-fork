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

            var testInteractiveSessionKey = "test-interactive-session-key"

            var testList = async function (assert, useTransaction) {
                const testFolder = "test-list"
                const testContent1 = "test-content-1-abc"
                const testContent2 = "test-content-2-abcd"
                const testContent3 = "test-content-3-abcde"
                const testContent4 = "test-content-4-abcdef"

                try {
                    var startDate = new Date()

                    var c = new common(assert, dtos)
                    c.start()

                    await c.login(facade)

                    await c.deleteFile(facade, testFolder, "")

                    await facade.getAfsServerFacade().write(testFolder, "test-file-1", 0, testContent1)
                    await facade.getAfsServerFacade().write(testFolder + "/test-folder-1", "test-file-2", 0, testContent2)
                    await facade.getAfsServerFacade().write(testFolder + "/test-folder-1", "test-file-3", 0, testContent3)
                    await facade.getAfsServerFacade().write(testFolder + "/test-folder-2", "test-file-4", 0, testContent4)

                    if (useTransaction) {
                        facade.setInteractiveSessionKey(testInteractiveSessionKey)
                        await facade.beginTransaction()
                    }

                    var list = await facade.getAfsServerFacade().list(testFolder, "", true)

                    if (useTransaction) {
                        await facade.commitTransaction()
                    }

                    list.sort((file1, file2) => {
                        return file1.getPath().localeCompare(file2.getPath())
                    })

                    c.assertEqual(list.length, 6, "Number of files")

                    c.assertFileEquals(list[0], {
                        path: "/test-file-1",
                        owner: testFolder,
                        name: "test-file-1",
                        size: testContent1.length,
                        directory: false,
                        creationTime: [startDate, new Date()],
                        lastModifiedTime: [startDate, new Date()],
                        lastAccessTime: [startDate, new Date()],
                    })
                    c.assertFileEquals(list[1], {
                        path: "/test-folder-1",
                        owner: testFolder,
                        name: "test-folder-1",
                        size: null,
                        directory: true,
                        creationTime: [startDate, new Date()],
                        lastModifiedTime: [startDate, new Date()],
                        lastAccessTime: [startDate, new Date()],
                    })
                    c.assertFileEquals(list[2], {
                        path: "/test-folder-1/test-file-2",
                        owner: testFolder,
                        name: "test-file-2",
                        size: testContent2.length,
                        directory: false,
                        creationTime: [startDate, new Date()],
                        lastModifiedTime: [startDate, new Date()],
                        lastAccessTime: [startDate, new Date()],
                    })
                    c.assertFileEquals(list[3], {
                        path: "/test-folder-1/test-file-3",
                        owner: testFolder,
                        name: "test-file-3",
                        size: testContent3.length,
                        directory: false,
                        creationTime: [startDate, new Date()],
                        lastModifiedTime: [startDate, new Date()],
                        lastAccessTime: [startDate, new Date()],
                    })
                    c.assertFileEquals(list[4], {
                        path: "/test-folder-2",
                        owner: testFolder,
                        name: "test-folder-2",
                        size: null,
                        directory: true,
                        creationTime: [startDate, new Date()],
                        lastModifiedTime: [startDate, new Date()],
                        lastAccessTime: [startDate, new Date()],
                    })
                    c.assertFileEquals(list[5], {
                        path: "/test-folder-2/test-file-4",
                        owner: testFolder,
                        name: "test-file-4",
                        size: testContent4.length,
                        directory: false,
                        creationTime: [startDate, new Date()],
                        lastModifiedTime: [startDate, new Date()],
                        lastAccessTime: [startDate, new Date()],
                    })

                    c.finish()
                } catch (error) {
                    c.fail(error)
                    c.finish()
                }
            }

            var testRead = async function (assert, useTransaction) {
                const testFolder = "test-read"
                const testFile = "test-file"
                const testContent = "test-content"

                try {
                    var c = new common(assert, dtos)
                    c.start()

                    await c.login(facade)

                    await c.deleteFile(facade, testFolder, "")

                    await facade.getAfsServerFacade().write(testFolder, testFile, 0, testContent)

                    if (useTransaction) {
                        facade.setInteractiveSessionKey(testInteractiveSessionKey)
                        await facade.beginTransaction()
                    }

                    var content = await facade.getAfsServerFacade().read(testFolder, testFile, 0, testContent.length)
                    c.assertEqual(await content.text(), testContent)

                    if (useTransaction) {
                        await facade.commitTransaction()
                    }

                    c.finish()
                } catch (error) {
                    c.fail(error)
                    c.finish()
                }
            }

            var testDelete = async function (assert, useTransaction) {
                const testFolder = "test-delete"
                const testFile = "test-file"
                const testContent = "test-content"

                try {
                    var c = new common(assert, dtos)
                    c.start()

                    await c.login(facade)

                    await c.deleteFile(facade, testFolder, "")

                    await facade.getAfsServerFacade().write(testFolder, testFile, 0, testContent)

                    var content = await facade.getAfsServerFacade().read(testFolder, testFile, 0, testContent.length)
                    c.assertEqual(await content.text(), testContent)

                    if (useTransaction) {
                        facade.setInteractiveSessionKey(testInteractiveSessionKey)
                        await facade.beginTransaction()
                    }

                    await facade.getAfsServerFacade().delete(testFolder, testFile)

                    if (useTransaction) {
                        await facade.commitTransaction()
                    }

                    await c.assertFileDoesNotExist(facade, testFolder, testFile)

                    c.finish()
                } catch (error) {
                    c.fail(error)
                    c.finish()
                }
            }

            var testCopy = async function (assert, useTransaction) {
                const testFolder = "test-copy"
                const testFileToCopy = "test-file-to-copy"
                const testFileCopied = "test-file-copied"
                const testContent = "test-content"

                try {
                    var c = new common(assert, dtos)
                    c.start()

                    await c.login(facade)

                    await c.deleteFile(facade, testFolder, "")

                    await facade.getAfsServerFacade().write(testFolder, testFileToCopy, 0, testContent)

                    if (useTransaction) {
                        facade.setInteractiveSessionKey(testInteractiveSessionKey)
                        await facade.beginTransaction()
                    }

                    await facade.getAfsServerFacade().copy(testFolder, testFileToCopy, testFolder, testFileCopied)

                    if (useTransaction) {
                        await facade.commitTransaction()
                    }

                    var contentToCopy = await facade.getAfsServerFacade().read(testFolder, testFileToCopy, 0, testContent.length)
                    c.assertEqual(await contentToCopy.text(), testContent)

                    var contentCopied = await facade.getAfsServerFacade().read(testFolder, testFileCopied, 0, testContent.length)
                    c.assertEqual(await contentCopied.text(), testContent)

                    c.finish()
                } catch (error) {
                    c.fail(error)
                    c.finish()
                }
            }

            var testMove = async function (assert, useTransaction) {
                const testFolder = "test-move"
                const testFileToMove = "test-file-to-move"
                const testFileMoved = "test-file-moved"
                const testContent = "test-content"

                try {
                    var c = new common(assert, dtos)
                    c.start()

                    await c.login(facade)

                    await c.deleteFile(facade, testFolder, "")

                    await facade.getAfsServerFacade().write(testFolder, testFileToMove, 0, testContent)

                    if (useTransaction) {
                        facade.setInteractiveSessionKey(testInteractiveSessionKey)
                        await facade.beginTransaction()
                    }

                    await facade.getAfsServerFacade().move(testFolder, testFileToMove, testFolder, testFileMoved)

                    if (useTransaction) {
                        await facade.commitTransaction()
                    }

                    await c.assertFileDoesNotExist(facade, testFolder, testFileToMove)

                    var content = await facade.getAfsServerFacade().read(testFolder, testFileMoved, 0, testContent.length)
                    c.assertEqual(await content.text(), testContent)

                    c.finish()
                } catch (error) {
                    c.fail(error)
                    c.finish()
                }
            }

            var testCreate = async function (assert, useTransaction) {
                const testFolder = "test-create"
                const testFile = "test-file"

                try {
                    var c = new common(assert, dtos)
                    c.start()

                    await c.login(facade)

                    await c.deleteFile(facade, testFolder, "")
                    await c.assertFileDoesNotExist(facade, testFolder, testFile)

                    if (useTransaction) {
                        facade.setInteractiveSessionKey(testInteractiveSessionKey)
                        await facade.beginTransaction()
                    }

                    await facade.getAfsServerFacade().create(testFolder, testFile, false)

                    if (useTransaction) {
                        await facade.commitTransaction()
                    }

                    await c.assertFileExists(facade, testFolder, testFile)

                    c.finish()
                } catch (error) {
                    c.fail(error)
                    c.finish()
                }
            }

            QUnit.test("list() without transaction", async function (assert) {
                await testList(assert, false)
            })

            QUnit.test("list() with transaction", async function (assert) {
                await testList(assert, true)
            })

            QUnit.test("read() without transaction", async function (assert) {
                await testRead(assert, false)
            })

            QUnit.test("read() with transaction", async function (assert) {
                await testRead(assert, true)
            })

            QUnit.test("delete() without transaction", async function (assert) {
                await testDelete(assert, false)
            })

            QUnit.test("delete() with transaction", async function (assert) {
                await testDelete(assert, true)
            })

            QUnit.test("copy() without transaction", async function (assert) {
                await testCopy(assert, false)
            })

            QUnit.test("copy() with transaction", async function (assert) {
                await testCopy(assert, true)
            })

            QUnit.test("move() without transaction", async function (assert) {
                await testMove(assert, false)
            })

            QUnit.test("move() with transaction", async function (assert) {
                await testMove(assert, true)
            })

            QUnit.test("create() without transaction", async function (assert) {
                await testCreate(assert, false)
            })

            QUnit.test("create() with transaction", async function (assert) {
                await testCreate(assert, true)
            })
        }

        resolve(function () {
            var afsServerUrl = "http://localhost:8085/data-store-server"
            executeModule("Afs tests (RequireJS)", new openbisRequireJS(null, afsServerUrl), dtos)
            executeModule("Afs tests (module VAR)", new window.openbis.openbis(null, afsServerUrl), window.openbis)
            executeModule("Afs tests (module ESM)", new window.openbisESM.openbis(null, afsServerUrl), window.openbisESM)
        })
    })
})
