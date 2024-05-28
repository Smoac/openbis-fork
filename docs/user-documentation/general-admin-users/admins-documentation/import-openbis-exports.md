# Imports of openBIS exports

It is possible to export metadata and data from one openBIS instance and import them to another openBIS instance.
The export process is described [here](../../general-users/data-export.md).


## Metadata import

The exported metadata (and related masterdata) can be imported in another openBIS instance by an instance admin via the admin UI, as described in [mastedata import and export](./masterdata-exports-and-imports.md). 

Exported metadata (and masterdata) are contained in a **xlsx** folder, as shown below.

![image info](img/xlxs-folder.png)


Metadata and masterdata are contained in the **metadata.xlsx** file. If some of the exported types have validation plugins or dynamic property plugins, these are exported to a **scripts** folder inside the **xlsx** folder. If some exported entities contain either large text fields that exceed the length of an Excel cell or metadata in spreadheets, these are exprted to a **data** folder inside the **xlsx** folder.
If a **data** folder and/or a **scripts** folder are present in the exported **xlsx** folder, the **xlsx** folder needs to be zipped and the **xlsx.zip** file can be imported via admin UI.
If only the **metadata.xlsx** file is contained in the **xlsx** folder, the metadata.xlsx file can be directly uploaded via admin UI.



## Datasets import

Exported datasets are contained in a **data** folder in a format ready to be imported via [eln-lims default dropbox](../../general-users/data-upload.md#data-upload-via-dropbox).

![image info](img/import-data-folder.png)

The folders contained in the **data** folder need to be placed in the **eln-lims incoming directory** and from here will be uploaded to the corresponsing openBIS entities. The metadata of the datasets is read from the **metadata.json** file contained inside each dataset folder.

When importing both metadata and data in a different openBIS instance, first the metadata need to be imported and afterwards the data. 



