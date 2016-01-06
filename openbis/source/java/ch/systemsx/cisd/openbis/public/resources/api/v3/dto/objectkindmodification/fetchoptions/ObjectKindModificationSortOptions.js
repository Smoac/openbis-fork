define([ "require", "stjs", "dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
  var ObjectKindModificationSortOptions = function() {
    SortOptions.call(this);
  };
  stjs.extend(ObjectKindModificationSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
    prototype['@type'] = 'dto.objectkindmodification.fetchoptions.ObjectKindModificationSortOptions';
    constructor.serialVersionUID = 1;
  }, {});
  return ObjectKindModificationSortOptions;
})