/*
 * Copyright 2015 ETH Zuerich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function HistoryView(controller, model) {
    this._model = model
    this._controller = controller
    this._container = $("<div>", { class: "history-view" })
    this._dataGrid

    this.repaint = function (views) {
        var _this = this

        var $containerColumn = $("<form>", {
            role: "form",
            action: "javascript:void(0);",
            onsubmit: "",
        })
        $containerColumn.append(this._container)
        views.content.append($containerColumn)

        views.header.append($("<h1>").append("History of " + Util.getDisplayNameForEntity(this._model.entity)))

        this._showHistory()
    }

    this._showHistory = function () {
        var _this = this

        var columns = []

        columns.push(
            {
                label: "Version",
                property: "version",
                getValue: function (params) {
                    return params.row.version
                },
                showByDefault: true,
            },
            {
                label: "Author",
                property: "author",
                getValue: function (params) {
                    return params.row.changes.author
                },
                showByDefault: true,
            },
            {
                label: "Changes",
                property: "changes",
                getValue: function (params) {
                    if (params.row._changes) {
                        return params.row._changes
                    } else {
                        var $changes = _this._renderChanges(params.row)
                        params.row._changes = $changes ? $changes.text() : null
                        return params.row._changes
                    }
                },
                render: this._renderChanges,
                showByDefault: true,
            },
            {
                label: "Full Document",
                property: "fullDocument",
                getValue: function (params) {
                    if (params.row._fullDocument) {
                        return params.row._fullDocument
                    } else {
                        var $fullDocument = _this._renderFullDocument(params.row)
                        params.row._fullDocument = $fullDocument ? $fullDocument.text() : null
                        return params.row._fullDocument
                    }
                },
                render: this._renderFullDocument,
                showByDefault: true,
                sortable: false,
            },
            {
                label: "Timestamp",
                property: "timestamp",
                getValue: function (params) {
                    var timestamp = params.row.changes.timestamp
                    return Util.getFormatedDate(new Date(timestamp))
                },
                showByDefault: true,
            }
        )

        var getDataList = function (callback) {
            var data = _this._model.getData()
            callback(data)
        }

        this._dataGrid = new DataGridController(
            null,
            columns,
            [],
            null,
            getDataList,
            null,
            false,
            this._model.entity["@type"] + "_HISTORY",
            true
        )

        this._dataGrid.init(this._container, [
            {
                name: "Compare",
                action: function (selected) {
                    if (selected.length !== 2) {
                        alert("Please select 2 versions to compare")
                        return
                    }
                },
            },
        ])

        this._container.prepend($("<legend>").append("History"))
    }

    this._renderChanges = function (row) {
        var $container = $("<div>", { class: "changes-list" })

        function abbreviate(array) {
            var limit = 100
            var str = ""

            for (var index = 0; index < Math.min(array.length, limit); index++) {
                if (index > 0) {
                    str += ", "
                }
                str += array[index]
            }

            if (array.length > limit) {
                str += ", ... and " + (array.length - limit) + " more"
            }

            return str
        }

        var relations = row.changes.relations
        if (!_.isEmpty(relations)) {
            var $relations = $("<ul>")
            Object.keys(relations)
                .sort(function (r1, r2) {
                    var sortings = {
                        SPACE: 1,
                        PROJECT: 2,
                        EXPERIMENT: 3,
                        SAMPLE: 4,
                        DATA_SET: 5,
                        PARENT: 6,
                        CHILD: 7,
                        CONTAINER: 8,
                        COMPONENT: 9,
                        UNKNOWN: 10,
                    }
                    return sortings[r1] - sortings[r2]
                })
                .forEach(function (relationType) {
                    var relation = relations[relationType]

                    var $relation = $("<li>")
                    $relation.append($("<span>", { class: "relation-type" }).text(relationType))

                    if (!_.isEmpty(relation.removed)) {
                        $relation.append(" relation(s) removed: ")
                        $relation.append(
                            $("<div>", { class: "relation-value-removed" }).text(abbreviate(relation.removed))
                        )
                    }
                    if (!_.isEmpty(relation.added)) {
                        $relation.append(" relation(s) added: ")
                        $relation.append($("<div>", { class: "relation-value-added" }).text(abbreviate(relation.added)))
                    }
                    if (relation.oldValue !== undefined || relation.newValue !== undefined) {
                        $relation.append(" relation changed: ")

                        $diff = $("<div>", { class: "relation-diff" })
                        $diff.append($("<div>", { class: "relation-old-value" }).text(relation.oldValue))
                        $diff.append($("<div>", { class: "relation-new-value" }).text(relation.newValue))

                        $relation.append($diff)
                    }

                    $relations.append($relation)
                })
            $container.append($relations)
        }

        var properties = row.changes.properties
        if (!_.isEmpty(properties)) {
            var $properties = $("<ul>")
            Object.keys(properties)
                .sort()
                .forEach(function (propertyName) {
                    var property = properties[propertyName]

                    var $property = $("<li>")
                    $property.append($("<span>", { class: "property-name" }).text(property.label))
                    $property.append(" [" + property.code + "]")
                    $property.append(" property changed: ")

                    $diff = $("<div>", { class: "property-diff" })
                    $diff.append(
                        $("<div>", { class: "property-old-value" }).html(DOMPurify.sanitize(property.oldValue))
                    )
                    $diff.append(
                        $("<div>", { class: "property-new-value" }).html(DOMPurify.sanitize(property.newValue))
                    )

                    $property.append($diff)
                    $properties.append($property)
                })
            $container.append($properties)
        }

        return $container
    }

    this._renderFullDocument = function (row) {
        var visible = false

        var $json = $("<pre>", { class: "full-document" })
        $json.text(JSON.stringify(row.fullDocument, null, 4))
        $json.hide()

        var $showHide = $("<a>").text("show")
        $showHide.click(function () {
            if (visible) {
                $showHide.text("show")
                $json.slideUp()
            } else {
                $showHide.text("hide")
                $json.slideDown()
            }
            visible = !visible
        })

        return $("<div>").append($showHide).append($json)
    }
}
