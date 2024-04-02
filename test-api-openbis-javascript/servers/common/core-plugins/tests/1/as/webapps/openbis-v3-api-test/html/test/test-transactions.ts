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
                try {
                    var c = new common(assert, dtos)
                    c.start()

                    await c.login(facade)

                    facade.setInteractiveSessionKey("test-interactive-session-key")

                    await facade.beginTransaction()

                    await facade.rollbackTransaction()
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
