Introduction
============

The ipad-ui core plugin module defines the services used by the openBIS iPad app. The iPad app is currently read-only, so there is only one service used by the iPad app, the ipad-read-service-v1. In the future, the iPad app may gain the ability to modify entities, requiring the creation of an additional service.

The ipad-read-service-v1 Service
================================

Summary
-------

The purpose of the read service is to provide information about entities to the iPad. To do this, it receives requests for data, retrieves the information necessary to fulfill this request, and translates between the openBIS entities, such as experiment and sample, and iPad entities. iPad entities are not the same as openBIS entities. The iPad has a much more generic understanding of entities and ties entities very closely to information displayed in the UI. Since the correspondence between openBIS entities and iPad entities is not simply one-to-one, one openBIS entity may be spread over several iPad entities and one iPad entity can contain information from multiple openBIS entities, the communication model is designed to make it possible for the service to map back and forth between iPad entities and openBIS entities.

iPad Data Model
---------------

The iPad data model tracks information for displaying and navigating between entities on the iPad. Each element in the model is used for a specific purpose in the UI. This table summarizes the pieces of the iPad entity data model and describes what the information is used for.

<table>
	<thead>
		<tr>
			<th>Column</th>
			<th>Description</th>
			<th>Use</th>
			<th>Examples</th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>SUMMARY_HEADER</td>
			<td>A short summary of the entity.</td>
			<td>Shown in listings and as the header for detail views.</td>
			<td>openBIS code</td>
		</tr>
		<tr>
			<td>SUMMARY</td>
			<td>A potentially longer summary of the entity.</td>
			<td>Shown in listings and in detail views.</td>
			<td>The content of a <code>description</code> property</td>
		</tr>
		<tr>
			<td>IDENTIFIER</td>
			<td>An identifier for the object.</td>
			<td>Shown in detail views.</td>
			<td>openBIS identifier</td>
		</tr>
		<tr>
			<td>PERM_ID</td>
			<td>A stable identifier for the object.</td>
			<td>Used internally to map from server data to local data.</td>
			<td>openBIS permId</td>
		</tr>
		<tr>
			<td>REFCON</td>
			<td>Data that is passed unchanged back to the server when a row is modified. This can be used by the server to encode whatever it needs in order to modify the row.</td>
			<td>For server use only; transmitted to the server on every call that it is appropriate.</td>
			<td>openBIS entity kind + type + permId</td>
		</tr>
		<tr>
			<td>CATEGORY</td>
			<td>A category identifier for showing the entity. If empty or None, then the the entity in this row is not shown in top level navigation views. Such entities may appear as children of other entities.</td>
			<td>Used to group entities together</td>
			<td>openBIS entity type</td>
		</tr>
		<tr>
			<td>IMAGE_URL</td>
			<td>A url for an image associated with this entity. If None or empty, no image is shown.</td>
			<td>Shown in detail views.</td>
			<td>An image from the DSS. An external image.</td>
		</tr>
		<tr>
			<td>CHILDREN</td>
			<td>The permIds of the children of this entity. Transmitted as JSON.</td>
			<td>Used to navigate between entities. Entities with children allow drill down and the children are shown in the drill-down view.</td>
			<td>Sample / data set children. An experiments samples or data sets may also be shown this way.</td>
		</tr>
		<tr>
			<td>PROPERTIES</td>
			<td>Properties (metadata) that should be displayed for this entity. Transmitted as JSON.</td>
			<td>Shown in detail views.</td>
			<td>openBIS properties</td>
		</tr>
	</tbody>
</table>


There are two fields in the data model that have a purpose beyond the UI. These are the `PERM_ID` field and the `REFCON` field. The `PERM_ID` field is assumed to be a stable identifier for the iPad entity and, thus, cannot change. The `PERM_ID` field is used by the iPad app to associate information from the server to the correct entity on the app.

The `REFCON` field is a field that is not touched by the iPad app at all. The server is free to place whatever information it wishes in this field. The content of the `REFCON` is sent back to the server when the iPad makes requests for more data for an existing entity. The server can thus use the `REFCON` to keep track for itself how several openBIS entities are merged into one iPad entity, for example. The `REFCON` is updated on every request, so the service can modify this value if it is appropriate.

Communication
-------------

The communication model between the iPad and the service has been designed to transmit only the necessary information on demand, when needed. The iPad specifies what kind of data it needs in the parameter key `request`. The response, as with all aggregation services, is in the form of a table model. The service then returns only the information requested back to the iPad. The `PERM_ID` and `REFCON` are always returned, as they are used by the communication model to match responses to iPad entities and iPad entities to openBIS entities.

### Requests


<table>
	<thead>
		<tr>
			<th>requestKey</th>
			<th>Arguments</th>
			<th>Description</th>
			<th>Use</th>
			<th>Columns</th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>ROOT</td>
			<td>None</td>
			<td>Return root entities.</td>
			<td>Used to initialize the root navigation view.</td>
			<td>PERM_ID, REFCON, CATEGORY, SUMMARY_HEADER, SUMMARY, CHILDREN</td>
		</tr>
		<tr>
			<td>DRILL</td>
			<td>entities : List of {"PERM_ID" : String, REFCON : String}</td>
			<td>Return navigational information for the specified entities.</td>
			<td>Used to navigate. The result should at least include data for children of children to permit efficient navigation on the iPad.</td>
			<td>PERM_ID, REFCON, CATEGORY, SUMMARY_HEADER, SUMMARY, CHILDREN</td>
		</tr>
		<tr>
			<td>DETAIL</td>
			<td>entities : List of {"PERM_ID" : String, REFCON : String}</td>
			<td>Return detail information for the specified entities.</td>
			<td>Display the entity in the detail view. This should return all information necessary to show the entity</td>
			<td>PERM_ID, REFCON, SUMMARY_HEADER, SUMMARY, IDENTIFIER, IMAGE_URL, PROPERTIES</td>
		</tr>
	</tbody>
</table>


### Headers

A table listing the different headers and which requests they are used in.


Communication Model
-------------------

On startup, the iPad app requests 

### Communication diagram

See the diagram in ...

