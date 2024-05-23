# Imports of openBIS exports

It is possible to export metadata and data from one openBIS instance to another.
The export process is described [here](../../general-users/data-export.md).

The exported metadata (and related masterdata) can be imported in another openBIS instance by an instance admin via the admin UI, as described in [mastedata import and export](./masterdata-exports-and-imports.md).

Exported datasets need to be imported via the [eln-lims default dropbox](../../general-users/data-upload.md#data-upload-via-dropbox).

When importing both metadata and data in a different openBIS instance, first the metadata need to be imported and afterwards the data. This way datasets will be associated with the corresponding Experiments and/or Objects.

If the exported metadata consists of an **xlsx** folder that contains a **metadata.xlsx** file and a **scripts** folder and/or a **data** folder (see below), the xlsx folder needs to be zipped before import via the admin UI. The **xlsx.zip** can then be imported via the admin UI.

![image info](img/xls-folder-2.png)

