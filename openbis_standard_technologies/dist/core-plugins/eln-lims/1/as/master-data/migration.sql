-- General Types

UPDATE controlled_vocabularies SET code = 'STORAGE.STORAGE_VALIDATION_LEVEL', is_internal_namespace = TRUE WHERE code = 'STORAGE_VALIDATION_LEVEL';
UPDATE controlled_vocabularies SET code = 'STORAGE_POSITION.STORAGE_BOX_SIZE', is_internal_namespace = TRUE WHERE code = 'STORAGE_BOX_SIZE';
UPDATE controlled_vocabularies SET code = 'SUPPLIER.LANGUAGE', is_internal_namespace = TRUE WHERE code = 'LANGUAGE';
UPDATE controlled_vocabularies SET code = 'SUPPLIER.PREFERRED_ORDER_METHOD', is_internal_namespace = TRUE WHERE code = 'PREFERRED_ORDER_METHOD';
UPDATE controlled_vocabularies SET code = 'PRODUCT.CURRENCY', is_internal_namespace = TRUE WHERE code = 'CURRENCY';
UPDATE controlled_vocabularies SET code = 'ORDER.ORDER_STATUS', is_internal_namespace = TRUE WHERE code = 'ORDER_STATUS';
UPDATE controlled_vocabularies SET code = 'WELL.COLOR_ENCODED_ANNOTATIONS', is_internal_namespace = TRUE WHERE code = 'COLOR_ENCODED_ANNOTATIONS';

UPDATE property_types SET is_internal_namespace = TRUE WHERE code = 'ELN_SETTINGS';
UPDATE property_types SET is_internal_namespace = TRUE WHERE code = 'NAME';
UPDATE property_types SET is_internal_namespace = TRUE WHERE code = 'XMLCOMMENTS';
UPDATE property_types SET is_internal_namespace = TRUE WHERE code = 'ANNOTATIONS_STATE';
UPDATE property_types SET is_internal_namespace = TRUE WHERE code = 'SHOW_IN_PROJECT_OVERVIEW';
UPDATE property_types SET is_internal_namespace = TRUE WHERE code = 'DEFAULT_OBJECT_TYPE';
UPDATE property_types SET is_internal_namespace = TRUE WHERE code = 'HISTORY_ID';

UPDATE property_types SET code = 'STORAGE.ROW_NUM', is_internal_namespace = TRUE WHERE code = 'ROW_NUM';
UPDATE property_types SET code = 'STORAGE.COLUMN_NUM', is_internal_namespace = TRUE WHERE code = 'COLUMN_NUM';
UPDATE property_types SET code = 'STORAGE.BOX_NUM', is_internal_namespace = TRUE WHERE code = 'BOX_NUM';
UPDATE property_types SET code = 'STORAGE.STORAGE_SPACE_WARNING', is_internal_namespace = TRUE WHERE code = 'STORAGE_SPACE_WARNING';
UPDATE property_types SET code = 'STORAGE.BOX_SPACE_WARNING', is_internal_namespace = TRUE WHERE code = 'BOX_SPACE_WARNING';
UPDATE property_types SET code = 'STORAGE.STORAGE_VALIDATION_LEVEL', is_internal_namespace = TRUE WHERE code = 'STORAGE_VALIDATION_LEVEL';

UPDATE property_types SET code = 'STORAGE_POSITION.STORAGE_CODE', is_internal_namespace = TRUE WHERE code = 'STORAGE_CODE';
UPDATE property_types SET code = 'STORAGE_POSITION.STORAGE_RACK_ROW', is_internal_namespace = TRUE WHERE code = 'STORAGE_RACK_ROW';
UPDATE property_types SET code = 'STORAGE_POSITION.STORAGE_RACK_COLUMN', is_internal_namespace = TRUE WHERE code = 'STORAGE_RACK_COLUMN';
UPDATE property_types SET code = 'STORAGE_POSITION.STORAGE_BOX_NAME', is_internal_namespace = TRUE WHERE code = 'STORAGE_BOX_NAME';
UPDATE property_types SET code = 'STORAGE_POSITION.STORAGE_BOX_SIZE', is_internal_namespace = TRUE WHERE code = 'STORAGE_BOX_SIZE';
UPDATE property_types SET code = 'STORAGE_POSITION.STORAGE_BOX_POSITION', is_internal_namespace = TRUE WHERE code = 'STORAGE_BOX_POSITION';
UPDATE property_types SET code = 'STORAGE_POSITION.STORAGE_USER', is_internal_namespace = TRUE WHERE code = 'STORAGE_USER';

UPDATE property_types SET code = 'SUPPLIER.COMPANY_ADDRESS_LINE_1', is_internal_namespace = TRUE WHERE code = 'COMPANY_ADDRESS_LINE_1';
UPDATE property_types SET code = 'SUPPLIER.COMPANY_ADDRESS_LINE_2', is_internal_namespace = TRUE WHERE code = 'COMPANY_ADDRESS_LINE_2';
UPDATE property_types SET code = 'SUPPLIER.COMPANY_FAX', is_internal_namespace = TRUE WHERE code = 'COMPANY_FAX';
UPDATE property_types SET code = 'SUPPLIER.COMPANY_PHONE', is_internal_namespace = TRUE WHERE code = 'COMPANY_PHONE';
UPDATE property_types SET code = 'SUPPLIER.COMPANY_EMAIL', is_internal_namespace = TRUE WHERE code = 'COMPANY_EMAIL';
UPDATE property_types SET code = 'SUPPLIER.COMPANY_LANGUAGE', is_internal_namespace = TRUE WHERE code = 'COMPANY_LANGUAGE';
UPDATE property_types SET code = 'SUPPLIER.CUSTOMER_NUMBER', is_internal_namespace = TRUE WHERE code = 'CUSTOMER_NUMBER';

UPDATE property_types SET code = 'PRODUCT.CATALOG_NUM', is_internal_namespace = TRUE WHERE code = 'CATALOG_NUM';
UPDATE property_types SET code = 'PRODUCT.PRICE_PER_UNIT', is_internal_namespace = TRUE WHERE code = 'PRICE_PER_UNIT';
UPDATE property_types SET code = 'PRODUCT.CURRENCY', is_internal_namespace = TRUE WHERE code = 'CURRENCY';

UPDATE property_types SET code = 'ORDERING.ORDER_STATUS', is_internal_namespace = TRUE WHERE code = 'ORDER_STATUS';

UPDATE property_types SET code = 'ORDER.SHIP_TO', is_internal_namespace = TRUE WHERE code = 'SHIP_TO';
UPDATE property_types SET code = 'ORDER.BILL_TO', is_internal_namespace = TRUE WHERE code = 'BILL_TO';
UPDATE property_types SET code = 'ORDER.SHIP_ADDRESS', is_internal_namespace = TRUE WHERE code = 'SHIP_ADDRESS';
UPDATE property_types SET code = 'ORDER.CONTACT_PHONE', is_internal_namespace = TRUE WHERE code = 'CONTACT_PHONE';
UPDATE property_types SET code = 'ORDER.CONTACT_FAX', is_internal_namespace = TRUE WHERE code = 'CONTACT_FAX';
UPDATE property_types SET code = 'ORDER.ADDITIONAL_INFORMATION', is_internal_namespace = TRUE WHERE code = 'ADDITIONAL_INFORMATION';
UPDATE property_types SET code = 'ORDER.ORDER_STATE', is_internal_namespace = TRUE WHERE code = 'ORDER_STATE';

UPDATE property_types SET code = 'WELL.COLOR_ENCODED_ANNOTATION', is_internal_namespace = TRUE WHERE code = 'COLOR_ENCODED_ANNOTATION';

-- General Types Annotations

UPDATE property_types SET code = 'ANNOTATION.SYSTEM.COMMENTS', is_internal_namespace = FALSE WHERE code = 'COMMENTS';
UPDATE sample_properties SET value = replace(value, 'COMMENTS=', 'ANNOTATION.SYSTEM.COMMENTS=') WHERE stpt_id IN (SELECT id FROM sample_type_property_types WHERE prty_id = (SELECT id FROM property_types WHERE code = 'ANNOTATIONS_STATE'));

UPDATE property_types SET code = 'ANNOTATION.REQUEST.QUANTITY_OF_ITEMS', is_internal_namespace = FALSE WHERE code = 'QUANTITY_OF_ITEMS';
UPDATE sample_properties SET value = replace(value, 'QUANTITY_OF_ITEMS=', 'ANNOTATION.REQUEST.QUANTITY_OF_ITEMS=') WHERE stpt_id IN (SELECT id FROM sample_type_property_types WHERE prty_id = (SELECT id FROM property_types WHERE code = 'ANNOTATIONS_STATE'));

-- Life Sciences Types Annotations

UPDATE property_types SET code = 'ANNOTATION.SYSTEM.QUANTITY', is_internal_namespace = FALSE WHERE code = 'QUANTITY';
UPDATE sample_properties SET value = replace(value, 'QUANTITY=', 'ANNOTATION.SYSTEM.QUANTITY=') WHERE stpt_id IN (SELECT id FROM sample_type_property_types WHERE prty_id = (SELECT id FROM property_types WHERE code = 'ANNOTATIONS_STATE'));
UPDATE property_types SET code = 'ANNOTATION.SYSTEM.PLASMID_ANNOTATION', is_internal_namespace = FALSE WHERE code = 'PLASMID_ANNOTATION';
UPDATE sample_properties SET value = replace(value, 'PLASMID_ANNOTATION=', 'ANNOTATION.SYSTEM.PLASMID_ANNOTATION=') WHERE stpt_id IN (SELECT id FROM sample_type_property_types WHERE prty_id = (SELECT id FROM property_types WHERE code = 'ANNOTATIONS_STATE'));
UPDATE property_types SET code = 'ANNOTATION.SYSTEM.PLASMID_RELATIONSHIP', is_internal_namespace = FALSE WHERE code = 'PLASMID_RELATIONSHIP';
UPDATE sample_properties SET value = replace(value, 'PLASMID_RELATIONSHIP=', 'ANNOTATION.SYSTEM.PLASMID_RELATIONSHIP=') WHERE stpt_id IN (SELECT id FROM sample_type_property_types WHERE prty_id = (SELECT id FROM property_types WHERE code = 'ANNOTATIONS_STATE'));

-- Saved ELN configuration types Annotations
UPDATE sample_properties SET value = replace(value, '"TYPE":"COMMENTS"', '"TYPE":"ANNOTATION.SYSTEM.COMMENTS"') WHERE stpt_id IN (SELECT id FROM sample_type_property_types WHERE prty_id = (SELECT id FROM property_types WHERE code = 'ELN_SETTINGS'));
UPDATE sample_properties SET value = replace(value, '"TYPE":"QUANTITY_OF_ITEMS"', '"TYPE":"ANNOTATION.REQUEST.QUANTITY_OF_ITEMS"') WHERE stpt_id IN (SELECT id FROM sample_type_property_types WHERE prty_id = (SELECT id FROM property_types WHERE code = 'ELN_SETTINGS'));
UPDATE sample_properties SET value = replace(value, '"TYPE":"QUANTITY"', '"TYPE":"ANNOTATION.SYSTEM.QUANTITY"') WHERE stpt_id IN (SELECT id FROM sample_type_property_types WHERE prty_id = (SELECT id FROM property_types WHERE code = 'ELN_SETTINGS'));
UPDATE sample_properties SET value = replace(value, '"TYPE":"PLASMID_ANNOTATION"', '"TYPE":"ANNOTATION.SYSTEM.PLASMID_ANNOTATION"') WHERE stpt_id IN (SELECT id FROM sample_type_property_types WHERE prty_id = (SELECT id FROM property_types WHERE code = 'ELN_SETTINGS'));
UPDATE sample_properties SET value = replace(value, '"TYPE":"PLASMID_RELATIONSHIP"', '"TYPE":"ANNOTATION.SYSTEM.PLASMID_RELATIONSHIP"') WHERE stpt_id IN (SELECT id FROM sample_type_property_types WHERE prty_id = (SELECT id FROM property_types WHERE code = 'ELN_SETTINGS'));
