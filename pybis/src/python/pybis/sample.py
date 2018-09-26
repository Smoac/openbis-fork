from .property import PropertyHolder
from .attribute import AttrHolder
from .openbis_object import OpenBisObject 
from .definitions import openbis_definitions
from .utils import VERBOSE

class Sample(OpenBisObject):
    """ A Sample is one of the most commonly used objects in openBIS.
    """

    def __init__(self, openbis_obj, type, project=None, data=None, props=None, **kwargs):
        self.__dict__['openbis'] = openbis_obj
        self.__dict__['type'] = type
        self.__dict__['p'] = PropertyHolder(openbis_obj, type)
        self.__dict__['a'] = AttrHolder(openbis_obj, 'Sample', type)

        if data is not None:
            self._set_data(data)

        if project is not None:
            setattr(self, 'project', project)

        if props is not None:
            for key in props:
                setattr(self.p, key, props[key])

        if kwargs is not None:
            for key in kwargs:
                setattr(self, key, kwargs[key])

    def _set_data(self, data):
        # assign the attribute data to self.a by calling it
        # (invoking the AttrHolder.__call__ function)
        self.a(data)
        self.__dict__['data'] = data

        # put the properties in the self.p namespace (without checking them)
        for key, value in data['properties'].items():
            self.p.__dict__[key.lower()] = value


    def __dir__(self):
        return [
            'props', 'get_parents()', 'get_children()', 
            'add_parents()', 'add_children()', 'del_parents()', 'del_children()',
            'get_datasets()', 'get_experiment()',
            'space', 'project', 'experiment', 'tags',
            'set_tags()', 'add_tags()', 'del_tags()',
            'add_attachment()', 'get_attachments()', 'download_attachments()',
            'save()', 'delete()'
        ]

    @property
    def props(self):
        return self.__dict__['p']

    @property
    def type(self):
        return self.__dict__['type']

    @type.setter
    def type(self, type_name):
        sample_type = self.openbis.get_sample_type(type_name)
        self.p.__dict__['_type'] = sample_type
        self.a.__dict__['_type'] = sample_type

    def __getattr__(self, name):
        return getattr(self.__dict__['a'], name)

    def __setattr__(self, name, value):
        if name in ['set_properties', 'set_tags', 'add_tags']:
            raise ValueError("These are methods which should not be overwritten")

        setattr(self.__dict__['a'], name, value)

    def _repr_html_(self):
        return self.a._repr_html_()

    def __repr__(self):
        return self.a.__repr__()

    def set_properties(self, properties):
        self.openbis.update_sample(self.permId, properties=properties)

    def save(self):
        props = self.p._all_props()

        if self.is_new:
            request = self._new_attrs()
            request["params"][1][0]["properties"] = props
            resp = self.openbis._post_request(self.openbis.as_v3, request)

            if VERBOSE: print("Sample successfully created.")
            new_sample_data = self.openbis.get_sample(resp[0]['permId'], only_data=True)
            self._set_data(new_sample_data)
            return self

        else:
            request = self._up_attrs()
            request["params"][1][0]["properties"] = props
            self.openbis._post_request(self.openbis.as_v3, request)
            if VERBOSE: print("Sample successfully updated.")
            new_sample_data = self.openbis.get_sample(self.permId, only_data=True)
            self._set_data(new_sample_data)

    def delete(self, reason):
        self.openbis.delete_entity(entity='Sample',id=self.permId, reason=reason)
        if VERBOSE: print("Sample {} successfully deleted.".format(self.permId))

    def get_datasets(self, **kwargs):
        return self.openbis.get_datasets(sample=self.permId, **kwargs)

    def get_projects(self, **kwargs):
        return self.openbis.get_project(withSamples=[self.permId], **kwargs)

    def get_experiment(self):
        try:
            return self.openbis.get_experiment(self._experiment['identifier'])
        except Exception:
            pass

    @property
    def experiment(self):
        try:
            return self.openbis.get_experiment(self._experiment['identifier'])
        except Exception:
            pass
