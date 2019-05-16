# Copyright 2019 Tulipsolutions B.V.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# -*- coding: utf-8 -*-
import copy
import os
from collections import OrderedDict

from docutils import nodes
from docutils.statemachine import StringList

from sphinx import addnodes
from sphinx.directives import ObjectDescription, directives
from sphinx.domains import Domain, ObjType
from sphinx.environment import NoUri
from sphinx.roles import XRefRole
from sphinx.transforms.post_transforms import ReferencesResolver
from sphinx.util import logging
from sphinx.util.docfields import GroupedField, TypedField
from sphinx.util.fileutil import copy_asset
from sphinx.util.nodes import make_refnode

logger = logging.getLogger(__name__)

_protobuf_obj_type_to_pretty = {
    "enum": "Enum",
    "message": "Message",
    "servicemethod": "gRPC service method",
}

# Keep a map of external object (field) types to a tuple of the URL and a tooltip text.
OBJTYPES_EXTERNAL_TO_URL_TITLE = dict()

# Well-known built-in scalar types.
WKT_SCALAR_TYPE_URL_TITLE = ("https://developers.google.com/protocol-buffers/docs/proto3#scalar",
                             "Protobuf Well-Known-Type (scalar)")
WKT_SCALAR_TYPES = {
    'double',
    'float',
    'int32',
    'sfixed32',
    'sint32',
    'fixed32',
    'uint32',
    'int64',
    'sfixed64',
    'sint64',
    'fixed64',
    'uint64',
    'bool',
    'string',
    'bytes',
}
for d in ({s: WKT_SCALAR_TYPE_URL_TITLE} for s in WKT_SCALAR_TYPES):
    OBJTYPES_EXTERNAL_TO_URL_TITLE.update(d)


class ProtobufEnumValueField(GroupedField):

    def make_field(self, types, domain, items, env=None):
        # Override to:
        # * use addnodes.desc_name node type (monospaced) as with message field.
        # * do not bother to find cross-references for names of enum values (it's not a type).
        # * do not add a '-' after value if no content follows.
        # * do not wrap in a paragraph - a nodes.inline is sufficient, saving vertical space depending on the theme.
        # * allow for field option just as with messages, e.g. ":value GREEN default:"
        # * never collapse
        fieldname = nodes.field_name('', self.label)
        listnode = self.list_type()
        for fieldarg, content in items:
            fieldsplit = fieldarg.split(" ")
            enumfieldname = fieldsplit[0]
            field_options = [] if len(fieldsplit) == 1 else fieldsplit[1:]

            par = nodes.inline()
            par += addnodes.desc_name(enumfieldname, enumfieldname)

            field_options_annotation_text = ", ".join(field_options)
            if field_options_annotation_text:
                par += nodes.Text(' ')
                par += addnodes.desc_annotation(field_options_annotation_text, field_options_annotation_text)

            # content is a list, because GroupedField
            if content and content[0] and content[0].astext():
                par += nodes.Text(' -- ')
                par += content
            listnode += nodes.list_item('', par)

        fieldbody = nodes.field_body('', listnode)
        return nodes.field('', fieldname, fieldbody)


class ProtobufMessageField(TypedField):
    # TODO: create custom field implementation that is typed, but not grouped to display fields in a nicer way; no lists
    #       inside field definitions.
    # TODO: create our own DocFieldTransformer to reduce the mess a bit in make_field().

    collapse_single_field_in_oneof = True

    def make_field(self, types, domain, items, env=None):
        # Override to:
        # * skip 'par += nodes.Text(' -- ')' if no content.
        # * print field name monospaced font
        # * Don't bother trying to make xref for the field *name* (fieldarg)
        # * handle more options in type arg, e.g. 'repeated', 'required'
        # * handle oneofs in a another level of items
        # * do not use a paragraph, but an inline element to wrap items in, because of weird conditional removal of it
        #   in the DocFieldTransformer; comment "collect the content, trying not to keep unnecessary paragraphs"
        def handle_item(fieldarg, content):
            return_node = nodes.inline()

            fieldsplit = fieldarg.split(" ")
            fieldname = fieldsplit[0]
            field_options = [] if len(fieldsplit) == 1 else fieldsplit[1:]
            if "repeated" in field_options:
                field_options.remove("repeated")
                fieldname += "[]"

            # A oneof field option should only occur once, so we just get the first one, or None.
            oneof_field_option = next((o for o in field_options if o.startswith("oneof_")), None)
            if oneof_field_option:
                field_options.remove(oneof_field_option)

            # A type_ field option should only occur once, so we just get the first one, or None.
            type_field_option = next((o for o in field_options if o.startswith("type_")), None)
            if type_field_option:
                field_options.remove(type_field_option)

            return_node += addnodes.desc_name(fieldname, fieldname)
            for option in field_options:
                return_node += nodes.Text(' ')
                return_node += addnodes.desc_annotation(option, option)

            typename = None
            if fieldarg in types:
                return_node += nodes.Text(' (')
                # NOTE: using .pop() here to prevent a single type node to be
                # inserted twice into the doctree, which leads to
                # inconsistencies later when references are resolved
                fieldtype = types.pop(fieldarg)
                if len(fieldtype) == 1 and isinstance(fieldtype[0], nodes.Text):
                    typename = u''.join(n.astext() for n in fieldtype)
                    return_node.extend(self.make_xrefs(self.typerolename, domain, typename, addnodes.literal_emphasis,
                                                       env=env))
                else:
                    return_node += fieldtype
                return_node += nodes.Text(')')
            if (content and len(content[0])) or type_field_option == "type_enum":
                return_node += nodes.Text(' -- ')
            if content and len(content[0]):
                return_node += content
            if type_field_option == "type_enum":
                inline_node = nodes.inline('', '')
                ref = addnodes.pending_xref('',
                                            refdomain="protobuf",
                                            reftype="enum_example_values_ok",
                                            reftarget=typename,
                                            refwarn=True,
                                            support_smartquotes=False)
                ref += inline_node
                return_node += ref
            return return_node, oneof_field_option

        fieldname = nodes.field_name('', self.label)
        if len(items) == 1 and self.can_collapse:
            fieldarg, content = items[0]
            bodynode, oneof_field_option = handle_item(fieldarg, content)
        else:
            bodynode = self.list_type()
            # Group oneof fields into one deeper level of list items.
            # OrderedDict to retain the order and to keep code relatively concise with .setdefault() feature of a Dict.
            oneof_list_nodes = OrderedDict()
            for fieldarg, content in items:
                itemnode, oneof_field_option = handle_item(fieldarg, content)
                if oneof_field_option:
                    oneof_list_nodes.setdefault(oneof_field_option, self.list_type())
                    if not oneof_list_nodes[oneof_field_option].children:
                        # If this is the first item in the oneof, insert a text at the regular field level before we
                        # start adding the second level of list items.
                        oneof_list_nodes[oneof_field_option] += nodes.Text("One of the following fields ")
                        if oneof_field_option.endswith("_required"):
                            oneof_list_nodes[oneof_field_option] += nodes.emphasis("must", "must")
                        else:
                            oneof_list_nodes[oneof_field_option] += nodes.emphasis("may", "may")
                        oneof_list_nodes[oneof_field_option] += nodes.Text(" be set:")
                    oneof_list_nodes[oneof_field_option] += nodes.list_item('', itemnode)
                else:
                    bodynode += nodes.list_item('', itemnode)
            for oneof_index, oneof_items_node in oneof_list_nodes.items():
                list_item_nodes_in_oneof = oneof_items_node.traverse(nodes.list_item)
                if len(list_item_nodes_in_oneof) > 1 or not self.collapse_single_field_in_oneof:
                    oneof_items_node['classes'].append(oneof_index)
                    oneof_items_node['classes'].append("oneof")
                    bodynode += nodes.list_item('', oneof_items_node)
                else:
                    bodynode += list_item_nodes_in_oneof[0]
        fieldbody = nodes.field_body('', bodynode)
        return nodes.field('', fieldname, fieldbody)

    def make_xref(self, rolename, domain, target,
                  innernode=nodes.emphasis, contnode=None, env=None):
        if domain != ProtobufDomain.name:
            logger.warning("%s.make_xref() was called for the non-Protobuf domain? Ignoring.", self.__class__.__name__)
            return

        if target in OBJTYPES_EXTERNAL_TO_URL_TITLE:
            refuri, reftitle = OBJTYPES_EXTERNAL_TO_URL_TITLE[target]
            refnode = nodes.reference('', '', internal=False, refuri=refuri, reftype='external-docs-wkt',
                                      refdomain='std', refexplicit=False, reftitle=reftitle)
            refnode += nodes.inline('', target)
            return refnode

        return super(ProtobufMessageField, self).make_xref(rolename, domain, target, innernode, contnode, env)


class ProtobufXRefRole(XRefRole):

    def __init__(self, obj_type_name="", remove_prefix_func=lambda x: x, **kwargs):
        XRefRole.__init__(self, **kwargs)
        self.obj_type_name = obj_type_name
        self.remove_prefix_func = remove_prefix_func

    def make_xref_contnode(self, title):
        node = nodes.inline()
        typenode = nodes.inline(self.obj_type_name, self.obj_type_name, classes=['objtype'])
        spacernode = nodes.inline("", " ", classes=['spacer'])
        titlenode = nodes.inline(self.remove_prefix_func(title), self.remove_prefix_func(title), classes=['sig'])
        node.extend([typenode, spacernode, titlenode])
        return node


class ProtobufEnumExampleValuesXRefRole(XRefRole):

    def process_link(self, env, *args):
        # Store env to be able to get to the domain instance
        self._env = env
        return super(ProtobufEnumExampleValuesXRefRole, self).process_link(env, *args)


def protobuf_resource_anchor(obj_type, name):
    return obj_type + '-' + name


class BaseProtobufObjectDescription(ObjectDescription):
    required_arguments = 1
    has_content = True

    option_spec = copy.copy(ObjectDescription.option_spec)
    option_spec.update({
        'title': directives.unchanged,
        'src_base_url': directives.unchanged,
        'src_file_path': directives.unchanged,
        'src_linerange': directives.unchanged,
        # Synopsis is used to show before the content of the directive, but also planned for future use in a
        # domain-specific index.
        'synopsis': directives.unchanged,
    })

    # Make a section with title for this description so that it shows up in the ToC. Title text defaults to the
    # signature, but can be overridden by the :title: option.
    make_section_and_title = False

    skip_general_index_if_domain_index = True

    @staticmethod
    def resource_anchor_str_static(objtype, sig):
        return "%s-%s" % (objtype, sig)

    def resource_anchor_str(self, sig):
        return self.__class__.resource_anchor_str_static(self.objtype, sig)

    def handle_signature(self, sig, signode):
        # TODO: add to ToC tree of current document. Perhaps get inspiration from
        # https://github.com/rtfd/sphinx-autoapi/blob/60464d0d237d0753e5147a0ec1677f01708a545c/autoapi/extension.py
        # on how to do that.
        sig = sig.strip()
        protobuf_object_type_str = _protobuf_obj_type_to_pretty[self.objtype]
        signode += addnodes.desc_type(text=protobuf_object_type_str)
        signode += nodes.inline('', ' -- ')
        shortname = ProtobufDomain.roles[self.objtype].remove_prefix_func(sig)
        if shortname == sig:
            signamenode = addnodes.desc_name(text=sig)
        else:
            signamenode = addnodes.desc_name()
            signamenode += nodes.abbreviation(text=shortname, explanation=sig)
        signode += signamenode
        signode['sig'] = sig
        self.handle_options(sig, signode)
        return sig

    def add_target_and_index(self, name_cls, sig, signode):
        anchor = self.resource_anchor_str(sig)
        fullname = "%s - %s" % (_protobuf_obj_type_to_pretty[self.objtype], sig)

        if anchor not in self.state.document.ids:
            signode['names'].append(sig)
            signode['ids'].append(anchor)
            signode['first'] = (not self.names)
            self.state.document.note_explicit_target(signode)

        protobufdomain = self.env.get_domain(ProtobufDomain.name)
        protobufdomain.add_protobuf_object(self, anchor, fullname, sig, signode)

        if self.skip_general_index_if_domain_index and not protobufdomain.indices:
            # TODO: improve listing in generic index:
            #       - sorting/grouping by last component name (a.b.c.Obj should be with the 'O', not 'a')
            #       - link to all references/uses
            #       Or just create a better custom domain specific index and don't support the general index.
            self.indexnode['entries'].append(('single', sig, fullname, '', None))

    def handle_options(self, sig, signode):
        if not self.make_section_and_title and self.options.get('title'):
            self.state_machine.reporter.warning(
                "A ':title:' option was given for Protobuf object '%s' in document '%s', but this was ignored since "
                "the Protobuf type '%s' is configured not to render any title."
                % (sig, self.env.docname, self.objtype),
                line=self.lineno)
        # Source links
        file_path = self.options.get('src_file_path', '').strip()
        if file_path:
            base_url = self.options.get('src_base_url', '').strip() or self.env.config.protobufdomain_sourcelinks_base_url
            # TODO via config/template to allow custom line range anchors (e.g. '#1' for Gerrit or '#L1-L7' for GitHub).
            linerange = self.options.get('src_linerange', '').strip()
            linerange_anchor = "#" + linerange.split("-", 1)[0]
            url = base_url + file_path + linerange_anchor
            # class and title looks as with 'viewcode' extension. Rather than extending that extension with support for
            # this domain, create our own link for now...
            pnode = nodes.reference('', '', internal=False, refuri=url, reftype='viewcode', refdomain='std',
                                    refexplicit=False)
            pnode['classes'].append('viewcode-link')
            pnode['classes'].append('headerlink')
            pnode += nodes.inline('', 'source')
            srclink_node = addnodes.only(expr='html')
            srclink_node += pnode
            signode += srclink_node

    def before_content(self):
        synopsis = self.options.get('synopsis')
        if synopsis:
            self.content = StringList([synopsis, ""]) + self.content
        super(BaseProtobufObjectDescription, self).before_content()

    def run(self):
        baseclass_indexnode, baseclass_contentnode = super(BaseProtobufObjectDescription, self).run()
        baseclass_contentnode['classes'].append("protobuf")
        baseclass_contentnode['classes'].append("protobuf_" + self.objtype)

        if self.make_section_and_title:
            first_sig = self.get_signatures()[0]
            title_text = self.options.get('title', ProtobufDomain.roles[self.objtype].remove_prefix_func(first_sig))
            wrapper_node = nodes.section()
            wrapper_node += nodes.title(title_text, title_text)
            wrapper_node += baseclass_contentnode
            anchor = self.resource_anchor_str(first_sig)
            wrapper_node['ids'].append(anchor)
            return [baseclass_indexnode, wrapper_node]

        return [baseclass_indexnode, baseclass_contentnode]


class ProtobufEnum(BaseProtobufObjectDescription):
    doc_field_types = [
        ProtobufEnumValueField('values', label='Defined values', names=('value',)),
    ]

    option_spec = copy.copy(BaseProtobufObjectDescription.option_spec)
    option_spec.update({
        'example_values': directives.unchanged,
    })

    def handle_options(self, sig, signode):
        super(ProtobufEnum, self).handle_options(sig, signode)
        example_values_str = self.options.get('example_values', '')
        protobufdomain = self.env.get_domain(ProtobufDomain.name)
        if example_values_str:
            example_values = [n.strip() for n in example_values_str.split(',')]
            protobufdomain.set_enum_example_values_ok(sig, example_values)
        else:
            protobufdomain.set_enum_example_values_ok(sig, [])


class ProtobufMessage(BaseProtobufObjectDescription):
    doc_field_types = [
        ProtobufMessageField('fields', label='Fields', names=('field',),
                             typerolename='obj', typenames=('fieldtype',)),
    ]


class ProtobufServiceMethod(BaseProtobufObjectDescription):
    make_section_and_title = True

    option_spec = copy.copy(BaseProtobufObjectDescription.option_spec)
    option_spec.update({
        'service': directives.unchanged_required,
        'input': directives.unchanged_required,
        'output': directives.unchanged_required,
        'output_stream': lambda x: directives.choice(x, ("yes", "no")),
        'input_stream': lambda x: directives.choice(x, ("yes", "no")),
    })

    def handle_options(self, sig, signode):
        super(ProtobufServiceMethod, self).handle_options(sig, signode)
        shortname = ProtobufDomain.roles[self.objtype].remove_prefix_func(self.options.get('input'))
        params_in = addnodes.desc_parameterlist()
        if shortname == self.options.get('input'):
            paramnode = nodes.inline(text=self.options.get('input'), classes=["inputtype"])
        else:
            paramnode = nodes.inline(classes=["inputtype"])
            paramnode += nodes.abbreviation(text=shortname, explanation=self.options.get('input'))
        params_in.append(paramnode)

        signode += nodes.inline('', ' ')
        signode += params_in
        signode += nodes.inline(text=" -> ")

        if self.options.get('output_stream') == "yes":
            signode += nodes.inline(text="stream of ", classes=["type_annotation", "streamannotation"])
            signode.parent['classes'].append("streaming")

        shortname = ProtobufDomain.roles[self.objtype].remove_prefix_func(self.options.get('output'))
        params_out = addnodes.desc_parameterlist()
        if shortname == self.options.get('output'):
            paramnode = nodes.inline(text=self.options.get('output'), classes=["outputtype"])
        else:
            paramnode = nodes.inline(classes=["outputtype"])
            paramnode += nodes.abbreviation(text=shortname, explanation=self.options.get('output'))
        params_out.append(paramnode)

        signode += params_out

    def before_content(self):
        service_name = self.options.get('service')
        if service_name:
            self.content = StringList(["part of service " + service_name, ""]) + self.content
        super(ProtobufServiceMethod, self).before_content()


class ProtobufDomain(Domain):
    """Protobuf domain."""

    name = 'protobuf'
    label = 'Protobuf'

    object_types = {
        'message': ObjType('message', 'obj'),
        'servicemethod': ObjType('servicemethod', 'obj'),
        'enum': ObjType('enum', 'obj'),
    }

    directives = {
        'message': ProtobufMessage,
        'servicemethod': ProtobufServiceMethod,
        'enum': ProtobufEnum,
    }

    roles = {
        'message': ProtobufXRefRole('message'),
        'servicemethod': ProtobufXRefRole('service method'),
        'enum': ProtobufXRefRole('enum'),
        # Dummy role to re-render objects again via the cross-references interface.
        'include': ProtobufXRefRole(),
        # Dummy role to include example values of enums inline, via the cross-references interface.
        'enum_example_values_ok': ProtobufXRefRole(),
    }

    initial_data = {
        'message': {},
        'servicemethod': {},
        'enum': {},
        'include': {},
        'enum_example_values_ok': {},
    }

    def __init__(self, env, *args, **kwargs):
        super(ProtobufDomain, self).__init__(env, *args, **kwargs)
        self.prefixes_to_remove = None

    def strip_prefix_object_name(self, s):
        if self.prefixes_to_remove is None:
            if self.env.config.protobufdomain_strip_prefixes:
                if self.env.config.protobufdomain_strip_prefixes_match_longest:
                    self.prefixes_to_remove = sorted(list(self.env.config.protobufdomain_strip_prefixes),
                                                                 key=len, reverse=True)
                else:
                    self.prefixes_to_remove = list(self.env.config.protobufdomain_strip_prefixes)
            else:
                self.prefixes_to_remove = []
        for prefix in self.prefixes_to_remove or []:
            if s.startswith(prefix):
                return s[len(prefix):]
        return s

    def config_ready(self):
        for role in self.roles.values():
            # We can't do this as part of the class declaration, because it's used in the XRefRoles, and those are
            # accessed on __init__ time of the Domain. The config isn't ready yet at that point...
            role.remove_prefix_func = self.strip_prefix_object_name

    def add_protobuf_object(self, object_description, anchor, fullname, sig, signode):
        """Add parsed directive to domain data store for later indexing and resolving cross-references."""
        objectstore = self.data[object_description.objtype]
        if sig in objectstore:
            object_description.state_machine.reporter.warning(
                "Duplicate Protobuf object description of '%s', other instance in '%s'. "
                "Use the ':noindex:' option for one of them."
                % (fullname, self.env.doc2path(objectstore[sig]["docname"])),
                line=object_description.lineno)
            return

        self.data[object_description.objtype][sig] = {
            "docname": self.env.docname,
            "anchor": anchor,
            "fullname": fullname,
        }

        # This is to maintain a reference in the domaindata for use with literal includes (:protobuf:include:`sig`).
        self.data['include'][sig] = signode.parent

    def set_enum_example_values_ok(self, sig, values):
        self.data['enum_example_values_ok'][sig] = values

    # TODO: look at implementing resolve_any_xref - it's the 'new' interface that we should use.
    def resolve_xref(self, env, fromdocname, builder, typ, target, node, contnode):
        doctree = env.get_doctree(fromdocname)
        orig_typ = typ

        # Support looking up by 'obj' typ as well, as objects of any type are unique in the Protobuf domain anyway.
        # (I.e. you can't have a message and an enum with the name name). We need this, because messages can have fields
        # of type message and type enum. It allows us to reference Protobuf objects without having to specify the type
        # all the time and simply denote field type by their already unique name.
        if typ in ('obj', 'include'):
            for try_role in self.object_types.keys():
                if target in self.data[str(try_role)]:
                    typ = try_role
                    logger.debug("Resolved obj type: typ = %s, orig_typ = %s", typ, orig_typ)
                    break

        # Hack to return the actual protobuf message description rather than a link to it.
        if orig_typ == 'include':
            logger.debug("Protobufdomain include: target = %s" % target)
            content_target = self.data['include'].get(str(target))
            logger.debug("Protobufdomain include: content_target = %s" % content_target)
            if not isinstance(content_target, nodes.Element):
                doctree.reporter.warning('Cannot resolve reference to %r' % node[0][0],
                                         line=node.line)
                return None
            return content_target

        # Hack to return the enum example values.
        if typ == 'enum_example_values_ok':
            try:
                example_values = self.data[str(typ)][target]
            except KeyError:
                doctree.reporter.warning('Cannot resolve reference to %r' % target, line=node.line)
                return None
            if example_values:
                example_values_node = nodes.inline('', '')
                example_values_node += nodes.Text("E.g.: ")
                for i, value in enumerate(example_values):
                    example_values_node += nodes.literal(value, value)
                    if i != (len(example_values) - 1):
                        example_values_node += nodes.Text(', ', ', ')
                example_values_node += nodes.Text(". ")
                return example_values_node
            else:
                raise NoUri()

        # Regular links for references.
        try:
            data = self.data[str(typ)][target]
        except KeyError:
            text = node[0][0]
            if typ not in self.roles:
                doctree.reporter.warning("Unknown cross-reference role '%s' in reference '%r'" % (typ, text))
                return None
            resnode = self.roles[typ].result_nodes(doctree, env, node, None)[0][0]
            if isinstance(resnode, addnodes.pending_xref):
                doctree.reporter.warning('Cannot resolve reference to %r' % text, line=node.line)
                return None
            return resnode
        else:
            new_contnode = self.roles[typ].make_xref_contnode(target)
            refnode = make_refnode(builder, fromdocname, data["docname"], data["anchor"],
                                   new_contnode, data["fullname"])
            refnode['classes'].extend(["protobuf_internal", typ])
            return refnode


def copy_statics(app, exception):
    if exception is not None:
        return
    for srcfile in ('protobufdomain.css',):
        src = os.path.join(os.path.dirname(__file__), srcfile)
        dst = os.path.join(app.outdir, '_static')
        assert os.path.isfile(src)
        assert os.path.isdir(dst)
        copy_asset(src, dst)


def process_config(app):
    app.env.domains[ProtobufDomain.name].config_ready()


def setup(app):
    app.add_domain(ProtobufDomain)
    app.add_css_file('protobufdomain.css')
    app.connect('build-finished', copy_statics)
    # On builder-inited, instead of config-inited, because we need the environment to be ready as well.
    app.connect('builder-inited', process_config)
    # Note that the order in the list matters. With protobufdomain_strip_prefixes = ['foo.', 'foo.bar.'] matches with
    # 'foo.bar.baz', you will get 'bar.baz', but when the list is reversed, you'll get 'baz'. Set
    # protobufdomain_strip_prefixes_match_longest = True to reverse-sort the entries on length to always match the
    # longest prefix in the list instead.
    app.add_config_value('protobufdomain_strip_prefixes', [], 'env')
    app.add_config_value('protobufdomain_strip_prefixes_match_longest', False, 'env')
    app.add_config_value('protobufdomain_sourcelinks_base_url', '', 'env')
    # Part of the hack to return the actual protobuf message description rather than a link to it, is to ensure the
    # pending_xref objects, that may be part of the tree of nodes, will be resolved. We do this by adding the generic
    # ReferencesResolver again, so that it will call all domains to resolve them.
    app.add_post_transform(ReferencesResolver)
