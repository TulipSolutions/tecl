# Copyright 2019 Tulip Solutions B.V.
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
# Part of this code is inspired from Envoy:
# https://github.com/envoyproxy/envoy/blob/3c0984bdb339f9c47895f639d0e119efc568fcaf/tools/protodoc/protodoc.py

import copy
import functools
import logging
import os
import re
import sys

from google.protobuf.compiler import plugin_pb2
from google.protobuf.descriptor_pb2 import FieldDescriptorProto

from validate import validate_pb2

logging.basicConfig()
logger = logging.getLogger(os.path.basename(__file__))

# General non-Protobuf domain entities will be labaled with the following prefix. Can be used to cross-reference from
# non-Protodoc sourced documentation.
CROSS_REF_LABEL_PREFIX = 'protodoc_'

# Key-value annotation regex.
ANNOTATION_REGEX = re.compile(r'\[#([\w-]+?):(.*?)\]\s?', re.DOTALL)
# Page/section titles with special prefixes in the proto comments
DOC_TITLE_ANNOTATION = 'protodoc-title'
# Not implemented yet annotation on leading comments, leading to insertion of warning on field.
NOT_IMPLEMENTED_WARN_ANNOTATION = 'not-implemented-warn'
# Not implemented yet annotation on leading comments, leading to hiding of field.
NOT_IMPLEMENTED_HIDE_ANNOTATION = 'not-implemented-hide'
# Comment. Just used for adding text that will not go into the docs at all.
COMMENT_ANNOTATION = 'comment'
# proto compatibility status.
PROTO_STATUS_ANNOTATION = 'proto-status'
# mark this enum value (field) as possible example value that's OK. Can be set for multiple times in an enum.
ENUM_VALUE_EXAMPLE_OK_ANNOTATION = 'example-value-ok'


VALID_ANNOTATIONS = {
    DOC_TITLE_ANNOTATION,
    NOT_IMPLEMENTED_WARN_ANNOTATION,
    NOT_IMPLEMENTED_HIDE_ANNOTATION,
    COMMENT_ANNOTATION,
    PROTO_STATUS_ANNOTATION,
    ENUM_VALUE_EXAMPLE_OK_ANNOTATION,
}

# TODO: get field name with google.protobuf.descriptor_pb2 module?
WKT_PRETTY_TYPE_NAMES = {
    FieldDescriptorProto.TYPE_DOUBLE: 'double',
    FieldDescriptorProto.TYPE_FLOAT: 'float',
    FieldDescriptorProto.TYPE_INT32: 'int32',
    FieldDescriptorProto.TYPE_SFIXED32: 'sfixed32',
    FieldDescriptorProto.TYPE_SINT32: 'sint32',
    FieldDescriptorProto.TYPE_INT64: 'int64',
    FieldDescriptorProto.TYPE_SFIXED64: 'sfixed64',
    FieldDescriptorProto.TYPE_SINT64: 'sint64',
    FieldDescriptorProto.TYPE_FIXED32: 'fixed32',
    FieldDescriptorProto.TYPE_UINT32: 'uint32',
    FieldDescriptorProto.TYPE_FIXED64: 'fixed64',
    FieldDescriptorProto.TYPE_UINT64: 'uint64',
    FieldDescriptorProto.TYPE_BOOL: 'bool',
    FieldDescriptorProto.TYPE_STRING: 'string',
    FieldDescriptorProto.TYPE_BYTES: 'bytes',
}

WKT_TYPES_SIGNED = {
    FieldDescriptorProto.TYPE_DOUBLE,
    FieldDescriptorProto.TYPE_FLOAT,
    FieldDescriptorProto.TYPE_INT32,
    FieldDescriptorProto.TYPE_SFIXED32,
    FieldDescriptorProto.TYPE_SINT32,
    FieldDescriptorProto.TYPE_INT64,
    FieldDescriptorProto.TYPE_SFIXED64,
    FieldDescriptorProto.TYPE_SINT64,
    FieldDescriptorProto.TYPE_FIXED32,
}

WKT_TYPES_UNSIGNED = {
    FieldDescriptorProto.TYPE_FIXED32,
    FieldDescriptorProto.TYPE_UINT32,
    FieldDescriptorProto.TYPE_FIXED64,
    FieldDescriptorProto.TYPE_UINT64,
}

WKT_TYPES_OTHER = {
    FieldDescriptorProto.TYPE_BOOL,
    FieldDescriptorProto.TYPE_STRING,
    FieldDescriptorProto.TYPE_BYTES,
}

WKT_TYPES_ALL = (WKT_TYPES_SIGNED | WKT_TYPES_UNSIGNED | WKT_TYPES_OTHER)

RST_DEFAULT_INDENT_SPACES = 3


def map_lines(func, multiline_str, skip_first=False):
    """Apply a function across each line in a flat string.

    Args:
      func: A string transform function for a line.
      multiline_str: A string consisting of potentially multiple lines.
    Returns:
      A flat string with f applied to each line.
    """
    if not skip_first:
        return '\n'.join(func(line) for line in multiline_str.split('\n'))
    splits = multiline_str.split('\n')
    return_str = splits[0]
    if len(splits) > 1:
        return_str += '\n'.join(func(line) for line in splits[1:])
    return return_str


def strip_leading_space(s):
    """Remove leading space in flat comment strings."""
    return map_lines(lambda s: s[1:], s)


def indent_lines(multiline_str, num_spaces=RST_DEFAULT_INDENT_SPACES, skip_first=False):
    def indent_string(spaces, line):
        return ' ' * spaces + line
    return map_lines(functools.partial(indent_string, num_spaces), multiline_str, skip_first=skip_first)


def merge_two_dicts(x, y):
    # Python 3.5+ could use a much simpler 'return {**x, **y}'
    z = x.copy()
    z.update(y)
    return z


class SourceCodeInfo(object):
    """Wrapper for SourceCodeInfo proto. For more information what paths are, please refer to the Protobuf documentation
    on:
    https://github.com/protocolbuffers/protobuf/blob/a18680890bd70407f889ccd652282f8092927397/src/google/protobuf/descriptor.proto#L726-L857  # noqa: E501
    """

    # TODO: obtain this from the (meta) field descriptor of google/protobuf/descriptor.proto.
    PATH_NUMBER_MESSAGE = 4
    PATH_NUMBER_MESSAGE_FIELD = 2
    PATH_NUMBER_ENUM = 5
    PATH_NUMBER_ENUM_VALUE = 2
    PATH_NUMBER_SERVICE = 6
    PATH_NUMBER_SERVICE_METHOD = 2

    def __init__(self, source_code_info):
        self._source_code_info_proto = source_code_info
        # Dict that maps Protobuf paths (as Python tuple) to Protobuf locations.
        self._leading_comments = {
            tuple(location.path): location.leading_comments for location in self._source_code_info_proto.location
        }
        logger.debug("self._leading_comments " + repr(self._leading_comments))
        # Line number range in .proto file, by path. Line numbers by the Protobuf API are zero-based, so we add 1.
        self._line_spans = {
            tuple(location.path): (location.span[0] + 1, location.span[2] + 1)
            for location in self._source_code_info_proto.location
        }
        logger.debug("self._line_spans " + repr(self._line_spans))
        self._file_level_comment = None

    @property
    def file_level_comment(self):
        if self._file_level_comment:
            return self._file_level_comment
        comment = ''
        earliest_detached_comment = max(max(location.span) for location in self._source_code_info_proto.location)
        for location in self._source_code_info_proto.location:
            if location.leading_detached_comments and location.span[0] < earliest_detached_comment:
                comment = strip_leading_space(''.join(location.leading_detached_comments)) + '\n'
                earliest_detached_comment = location.span[0]
        self._file_level_comment = comment
        return comment

    def leading_comment_by_path(self, path):
        return self._leading_comments.get(path, None)

    def leading_comment_message(self, msg_index):
        return self.leading_comment_by_path((SourceCodeInfo.PATH_NUMBER_MESSAGE, msg_index))

    def leading_comment_message_field(self, msg_index, field_index):
        return self.leading_comment_by_path((SourceCodeInfo.PATH_NUMBER_MESSAGE, msg_index,
                                             SourceCodeInfo.PATH_NUMBER_MESSAGE_FIELD, field_index))

    def leading_comment_enum(self, enum_index):
        return self.leading_comment_by_path((SourceCodeInfo.PATH_NUMBER_ENUM, enum_index))

    def leading_comment_enum_value(self, enum_index, value_index):
        return self.leading_comment_by_path((SourceCodeInfo.PATH_NUMBER_ENUM, enum_index,
                                             SourceCodeInfo.PATH_NUMBER_ENUM_VALUE, value_index))

    def leading_comment_service(self, svc_index):
        return self.leading_comment_by_path((SourceCodeInfo.PATH_NUMBER_SERVICE, svc_index))

    def leading_comment_service_method(self, svc_index, method_index):
        return self.leading_comment_by_path((SourceCodeInfo.PATH_NUMBER_SERVICE, svc_index,
                                             SourceCodeInfo.PATH_NUMBER_SERVICE_METHOD, method_index))

    def line_span_by_path(self, path):
        return self._line_spans.get(tuple(path), (None, None))


class ProtodocError(Exception):
    """Base error class for the protodoc module."""
    pass


def format_anchor(label):
    return '.. _%s:\n\n' % label


def format_include_rst(filepath):
    return ".. include:: %s\n\n" % filepath


def file_cross_ref_label(msg_name):
    return '%sfile_%s' % (CROSS_REF_LABEL_PREFIX, msg_name)


def format_header(style, text):
    """Format RST header.

    Args:
      style: underline style, e.g. '=', '-'.
      text: header text
    Returns:
      RST formatted header.
    """
    return '%s\n%s\n\n' % (text, style * len(text))


def format_comment_with_annotations(s, annotations, type_name):
    if NOT_IMPLEMENTED_WARN_ANNOTATION in annotations:
        s += '\n.. WARNING::\n   Not implemented at this time.\n\n'

    if PROTO_STATUS_ANNOTATION in annotations:
        if type_name in ('message', 'enum', 'service', 'field', 'servicemethod'):
            status = annotations[PROTO_STATUS_ANNOTATION]
            if status in ('draft', 'experimental'):
                s += ('\n.. WARNING::\n   This %s has been labeled as *%s*.\n\n' % (type_name, status))
            elif status == 'frozen':
                s += ('\n.. HINT::\n   This %s has been marked as stable.\n\n' % (type_name, status))
            else:
                raise ProtodocError('Unknown proto status: %s' % status)
        else:
            raise ProtodocError('Unsupported object type for proto-status annotation')
    return s


def extract_annotations(s, type_name):
    """Extract annotations from a given comment string.

    Args:
      s: string that may contains annotations.
    Returns:
      Pair of string with with annotations stripped and annotation map.
    """
    annotations = dict()
    groups = re.findall(ANNOTATION_REGEX, s)
    # Remove annotations.
    without_annotations = re.sub(ANNOTATION_REGEX, '', s)
    for group in groups:
        annotation = group[0]
        if annotation not in VALID_ANNOTATIONS:
            raise ProtodocError('Unknown annotation: %s' % annotation)
        annotations[group[0]] = group[1].lstrip()
    return format_comment_with_annotations(without_annotations, annotations, type_name), annotations


def _get_filename_for_message(proto_file_output_dir, msg_name):
    return proto_file_output_dir + "msg_%s" % msg_name + '.rst'


def _get_filename_for_service(proto_file_output_dir, svc_name):
    return proto_file_output_dir + "svc_%s" % svc_name + '.rst'


class Protodoc(object):

    def __init__(self, proto_file, plugin_pb2_code_generator_response, file_header_underline_char='='):
        logger.debug("New Protodoc object for proto file '%s'" % proto_file.name)
        self._proto_file = proto_file
        self._source_code_info = SourceCodeInfo(proto_file.source_code_info)
        self._plugin_pb2_code_generator_response = plugin_pb2_code_generator_response
        # Find the earliest detached comment, attribute it to file level.
        # Also extract file level titles if any.
        self.file_level_stripped_comment, self.file_level_annotations = \
            extract_annotations(self._source_code_info.file_level_comment, 'file')
        self._file_header_underline_char = file_header_underline_char
        # TODO nice to have - set up own class logger with showing output for which file, because protoc does not.

    def generate_rsts(self):
        if NOT_IMPLEMENTED_HIDE_ANNOTATION in self.file_level_annotations:
            logger.debug("NOT_IMPLEMENTED_HIDE_ANNOTATION for file: '%s' -- not generating any output",
                         self._proto_file.name)
            return

        all_messages_rst = ""
        for index, msg in enumerate(self._proto_file.message_type):
            logger.debug("Message index '%d' for file: '%s': '%s'" % (index, self._proto_file.name, msg.name))
            rst = self.format_message(msg, index)
            all_messages_rst += rst + "\n"
            logger.debug("Message '%s' RST:\n" + rst, msg.name)

        services_rst = ''
        all_servicemethods_includes_rst = ''
        for index, svc in enumerate(self._proto_file.service):
            logger.debug("Service index '%d' for file: '%s': '%s'" % (index, self._proto_file.name, svc.name))
            services_rst += self.format_service(svc, index) + "\n"
            for method in svc.method:
                all_servicemethods_includes_rst += ":protobuf:include:`%s.%s`\n\n" \
                                                   % (self._proto_file.package, method.name)

        all_enums_rst = ""
        for index, enum in enumerate(self._proto_file.enum_type):
            logger.debug("Enum index '%d' for file: '%s': '%s'" % (index, self._proto_file.name, enum.name))
            rst = self.format_enum(enum, index)
            all_enums_rst += rst + "\n\n"
            logger.debug("Enum '%s' RST:\n" + rst, enum.name)

        if all_messages_rst or all_enums_rst:
            header_name = self.file_level_annotations.get(DOC_TITLE_ANNOTATION, self._proto_file.name)
            all_objects_title = format_header("=", header_name)
            all_servicemethods_title = format_header("-", "Service methods") if services_rst else ""
            all_messages_title = format_header("-", "Messages") if all_messages_rst else ""
            all_enums_title = format_header("-", "Enumerations") if all_enums_rst else ""
            self.add_rst_output_file_to_response(self.filename_for_objects_reference,
                                                 "\n".join(m for m in [all_objects_title,
                                                                       all_servicemethods_title,
                                                                       services_rst,
                                                                       all_messages_title,
                                                                       all_messages_rst,
                                                                       all_enums_title,
                                                                       all_enums_rst] if m))

    @property
    def rst_output_dir(self):
        """
        Determines and returns the (base) directory to output files to for the given self._proto_file. It transforms it
        to a 'normalized' form, like the protobuf compiler would use too, replacing hyphens with underscores, e.g.
        ./my-package/myproto.proto -> ./my_package/myproto.

        :return: string of base directory to declare output files to.
        """
        proto_file_name_base = self._proto_file.name.replace("-", "_")
        proto_extension = ".proto"
        if proto_file_name_base.endswith(proto_extension):
            proto_file_name_base = proto_file_name_base[:-len(proto_extension)]

        proto_file_name_base += "/"
        return proto_file_name_base

    def add_rst_output_file_to_response(self, filename, content):
        f = self._plugin_pb2_code_generator_response.file.add()
        f.name = filename
        f.content = content

    @property
    def filename_for_objects_reference(self):
        return self.rst_output_dir + 'objects.rst'

    @property
    def format_file_header(self):
        anchor = format_anchor(file_cross_ref_label(self._proto_file.name))
        if DOC_TITLE_ANNOTATION in self.file_level_annotations:
            return anchor + format_header(self._file_header_underline_char,
                                          self.file_level_annotations[DOC_TITLE_ANNOTATION])
        return anchor + format_header(self._file_header_underline_char, self._proto_file.name)

    def common_object_properties(self, path=None):
        linerange = ""
        if path:
            start, end = self._source_code_info.line_span_by_path(path)
            if start:
                linerange += str(start)
                if end:
                    linerange += "-" + str(end)
        if linerange:
            linerange = ":src_linerange: %s\n" % linerange
        return ":src_file_path: %s\n%s" % (self._proto_file.name, linerange)

    def format_message(self, msg, index):
        # Skip messages synthesized to represent map types. Not sure what this does -- being cautious.
        if msg.options.map_entry:
            logger.warning("Found a message with unsupported map_entry options. Skipping message '%s'." % msg.name)
            return ''

        raw_comment = self._source_code_info.leading_comment_message(index)
        body_comment, annotations = extract_annotations(strip_leading_space(raw_comment), 'message')
        body_comment = body_comment.strip()
        if NOT_IMPLEMENTED_HIDE_ANNOTATION in annotations:
            logger.debug("NOT_IMPLEMENTED_HIDE_ANNOTATION for message: '%s', skipping." % msg.name)
            return ''

        body_part_rst = (indent_lines(body_comment) + "\n\n") if body_comment else ""
        message_rst = ".. protobuf:message:: %s.%s\n" % (self._proto_file.package, msg.name)

        # Loop over the oneof declarations so that we can map the fields to groups per oneof.
        # Map oneof_index -> oneof_decl
        msg_oneof_decls = {
            index: oneof_decl for index, oneof_decl in enumerate(msg.oneof_decl)
        }
        logger.debug("oneofs: " + repr(msg_oneof_decls))

        fields_part_rst = ""
        for field_index, field in enumerate(msg.field):
            logger.debug("Field '%s' in message '%s'", field, msg.name)
            field_rst = self.format_message_field(msg, index, field, field_index, msg_oneof_decls)
            logger.debug("field_rst = '%s'", repr(field_rst))
            fields_part_rst += field_rst
        fields_part_rst = indent_lines(fields_part_rst)
        logger.debug("fields_part_rst = '%s'", repr(fields_part_rst))

        properties_rst = indent_lines(self.common_object_properties(path=[4, index]).rstrip()) + "\n\n"

        return "".join([message_rst, properties_rst, body_part_rst, fields_part_rst])

    @staticmethod
    def field_validation_rules_pretty_str(validation_rules_as_dict, field):
        # TODO: show ranges nicely?
        items = []
        rules_dict = copy.copy(validation_rules_as_dict)
        if field.type == field.TYPE_ENUM and rules_dict.get('not_in', None) == [0]:
            del rules_dict['not_in']
            items.append("non-default value only")
        if rules_dict.get('unique'):
            del rules_dict['unique']
            items.append("all elements in set unique")
        if rules_dict.get('defined_only'):
            del rules_dict['defined_only']
            items.append("defined values only")
        for c_name, c_value in rules_dict.items():
            if type(c_value) == bool and c_value:
                items.append(c_name)
            elif c_name == 'gt' and field.type in WKT_TYPES_UNSIGNED:
                if c_value != 0:
                    items.append("> %d" % c_value)
                else:
                    # Don't bother to add captain obvious >0 for unsigned (would already be marked as required - unless
                    # that does not apply to a oneof context).
                    continue
            elif c_name == 'gte':
                items.append(">= %d" % c_value)
            elif c_name == 'lt':
                items.append("< %d" % c_value)
            elif c_name == 'lte':
                items.append("<= %d" % c_value)
            else:
                items.append("%s: %s" % (c_name, c_value))
        return ", ".join(items)

    @staticmethod
    def recurse_validate_pb2_any_rules(validate_pb2_rules_message):
        result = dict()
        for field_type, rules in validate_pb2_rules_message.ListFields():
            # Recurse on FieldRules only.
            if (isinstance(validate_pb2_rules_message, validate_pb2.FieldRules)
                    or isinstance(rules, validate_pb2.FieldRules)):
                child_dict = Protodoc.recurse_validate_pb2_any_rules(rules)
                result = merge_two_dicts(result, child_dict)
            else:
                result[field_type.name] = rules
        return result

    @staticmethod
    def get_field_validation_rules_as_dict(field):
        '''
        Transforms Envoy's protoc-gen-validate rules on a field as flat dict of constraints, because the rules are
        represented as Protobuf (proto2) nested messages which are harder to translate to readable English later. Due to
        the nature of nested messages, this uses recursion.
        See the Protobuf definitions for a reference what constraints can be set:
        https://github.com/envoyproxy/protoc-gen-validate/blob/v0.0.14/validate/validate.proto

        :param field: Regular protobuf message field
        :return: dict of constraint names mapped to the constraint value (which can be a list of values for e.g. 'in'
                 constraints).
        '''

        if not field.options.HasExtension(validate_pb2.rules):
            return dict()
        return Protodoc.recurse_validate_pb2_any_rules(field.options.Extensions[validate_pb2.rules])

    @staticmethod
    def is_field_required(field):
        """
        Best-guess indication whether this field is required by implication of known validate_pb2.rules. Not extensive;
        does not check for const validations, regular expressions, etc.

        :param field_rules: field option extension object by validate_pb2.rules.
        :return: True if the validate_pb2 rules indicate a validation that implies that this field is required, false if
                 no indication of it.
        """
        if not field.options.HasExtension(validate_pb2.rules):
            return False
        rules_dict = Protodoc.get_field_validation_rules_as_dict(field)
        logger.debug("rules_dict for field '%s': '%s'" % (field.name, rules_dict))

        if field.type in WKT_TYPES_SIGNED and any((
            rules_dict.get('gt', -1) >= 0,
            rules_dict.get('gte', 0) > 0,
            rules_dict.get('lt', 1) <= 0,
            rules_dict.get('lte', 0) < 0,
        )):
            return True

        if field.type in WKT_TYPES_UNSIGNED and any((
            rules_dict.get('gt', -1) >= 0,
            rules_dict.get('gte', 0) > 0,
        )):
            return True

        if (field.type in (WKT_TYPES_UNSIGNED | WKT_TYPES_SIGNED) or field.type == field.TYPE_ENUM) and any((
            rules_dict.get('in') and 0 not in rules_dict.get('in'),
            0 in rules_dict.get('not_in', []),
        )):
            return True

        # TODO: more in/not_in for string/bytes etc.

        if any((
            rules_dict.get('min_items', 0) > 0,
            rules_dict.get('min_bytes', 0) > 0,
            rules_dict.get('min_len', 0) > 0,
            rules_dict.get('len', 0) > 0,
            rules_dict.get('contains'),
            rules_dict.get('prefix'),
            rules_dict.get('suffix'),
            rules_dict.get('pattern'),
            rules_dict.get('ip'),
            rules_dict.get('ipv4'),
            rules_dict.get('ipv6'),
            rules_dict.get('email'),
            rules_dict.get('hostname'),
            rules_dict.get('uri'),
            rules_dict.get('uri_ref'),
        )):
            return True

    def format_message_field(self, msg, msg_index, field, field_index, msg_oneof_decls={}):
        validation_rules_dict = self.get_field_validation_rules_as_dict(field)
        field_type_annotation_set = set()

        siblings_in_oneof = False
        if field.HasField('oneof_index'):
            oneof_required = msg_oneof_decls[field.oneof_index].options.HasExtension(validate_pb2.required)
            siblings_in_oneof = len([f for f in msg.field
                                     if f.HasField("oneof_index") and f.oneof_index == field.oneof_index]) > 1
            field_type_annotation_set.add("oneof_%d%s" % (field.oneof_index, "_required" if oneof_required else ""))
            # Do not add 'required' annotations for each field if this is part of a oneof. (If the oneof itself is
            # required, then the requirement annotation will be part of the oneof annotation itself - 'must' instead of
            # 'may'.) However, if there's only one field in the oneof and the oneof itself is required, add the
            # annotation again in the field itself.
            # TODO: fix this in the Sphinx Protobuf domain some time so that we don't need to special case this here?
            if oneof_required and not siblings_in_oneof:
                field_type_annotation_set.add("required")

        if "required" not in field_type_annotation_set \
                and not siblings_in_oneof \
                and self.is_field_required(field):
            field_type_annotation_set.add("required")

        if field.label == field.LABEL_REPEATED:
            field_type_annotation_set.add("repeated")

        logger.debug("validation_annotations: " + repr(field_type_annotation_set))
        raw_comment = self._source_code_info.leading_comment_message_field(msg_index, field_index)
        stripped_comment, annotations = extract_annotations(strip_leading_space(raw_comment), 'field')
        stripped_comment = stripped_comment.strip()
        field_type_name = self.format_field_type_name(field)
        # We need to add the primary type of field here (e.g. message/enum/wkt) as a hint for Sphinx, so that it doesn't
        # need to parse all objects before it understands it's an enum/message/wkt based on the type_name.
        if field.type == field.TYPE_ENUM:
            logger.debug("field.type enum " + repr(field.type))
            field_type_annotation_set.add("type_enum")
        elif field.type == field.TYPE_MESSAGE:
            logger.debug("field.type message " + repr(field.type))
            field_type_annotation_set.add("type_message")
        elif field.type in WKT_TYPES_ALL:
            logger.debug("field.type WKT " + WKT_PRETTY_TYPE_NAMES.get(field.type))
            field_type_annotation_set.add("type_wkt")
        else:
            logger.warning("field.type UNKNOWN (%d) for field %r" % (field.type, field.name))
        if field_type_annotation_set:
            field_type_annotations = " " + " ".join(field_type_annotation_set)
        else:
            field_type_annotations = ""
        if not field_type_name:
            raise ProtodocError("Unknown field type for field '%s' in message '%s'" % (field.name, msg.name))
        # field_type is the primary type (e.g. 'message', 'enum', or 'sint32' for a WKT.)
        # field_type_name is the enum/message type (e.g. 'common.Market' with field_type = 'message')
        return_rst = ":field %s %s%s:" % (field_type_name, field.name, field_type_annotations)
        if stripped_comment:
            return_rst += " " + indent_lines(stripped_comment, skip_first=True)

        field_validation_rules_pretty_str = Protodoc.field_validation_rules_pretty_str(validation_rules_dict, field)
        if field_validation_rules_pretty_str:
            validation_rules_comment = "\n\n" if stripped_comment else " "
            validation_rules_comment += "validation constraints: "
            validation_rules_comment += "*%s*" % field_validation_rules_pretty_str
            validation_rules_comment = indent_lines(validation_rules_comment, skip_first=not(stripped_comment))
            return_rst += validation_rules_comment

        return return_rst + "\n"

    def format_enum(self, enum, index):
        raw_comment = self._source_code_info.leading_comment_enum(index)
        body_comment, annotations = extract_annotations(strip_leading_space(raw_comment), 'enum')
        if NOT_IMPLEMENTED_HIDE_ANNOTATION in annotations:
            logger.debug("NOT_IMPLEMENTED_HIDE_ANNOTATION for enum: '%s', skipping." % enum.name)
            return ''

        body_comment = body_comment.strip()
        body_part_rst = (indent_lines(body_comment) + "\n\n") if body_comment else ""

        enum_rst = ".. protobuf:enum:: %s.%s\n" % (self._proto_file.package, enum.name)
        values_part_rst = ""
        example_values = []
        for value_index, value in enumerate(enum.value):
            value_rst, example_value_or_none = self.format_enum_value(index, value, value_index)
            logger.debug("value_rst = '%s'", repr(value_rst))
            values_part_rst += value_rst
            if example_value_or_none is not None:
                example_values.append(example_value_or_none)

        values_part_rst = indent_lines(values_part_rst.rstrip())
        logger.debug("values_part_rst = '%s'", repr(values_part_rst))
        properties_rst_part_common = self.common_object_properties(path=[5, index]).rstrip()
        properties_rst_part_examples = ":example_values: %s" % (", ".join(example_values)) if example_values else ""
        properties_rst = indent_lines("\n".join([properties_rst_part_common, properties_rst_part_examples])) + "\n\n"
        return "".join([enum_rst, properties_rst, body_part_rst, values_part_rst])

    def format_enum_value(self, enum_index, value, value_index):
        raw_comment = self._source_code_info.leading_comment_enum_value(enum_index, value_index)
        stripped_comment, annotations = extract_annotations(strip_leading_space(raw_comment), 'field')

        return_rst = ":value %s:" % "".join((value.name,
                                             " default" if value_index == 0 else "",
                                             ))

        if stripped_comment:
            return_rst += " " + indent_lines(stripped_comment)
        return return_rst + "\n", (value.name if ENUM_VALUE_EXAMPLE_OK_ANNOTATION in annotations else None)

    def format_service(self, svc, index):
        raw_comment = self._source_code_info.leading_comment_service(index)
        comment, annotations = extract_annotations(strip_leading_space(raw_comment), 'service')
        if NOT_IMPLEMENTED_HIDE_ANNOTATION in annotations:
            logger.debug("NOT_IMPLEMENTED_HIDE_ANNOTATION for service: '%s', skipping." % svc.name)
            return ''
        if comment or annotations:
            logger.warning("Unsupported documentation for leading comments on services. Please document in service "
                           "*methods* for '%s'.", svc.name)

        return "\n".join(self.format_servicemethod(svc, index, method, method_index)
                         for method_index, method in enumerate(svc.method))

    def format_servicemethod(self, svc, svc_index, svc_method, index):
        raw_comment = self._source_code_info.leading_comment_service_method(svc_index, index)
        stripped_comment, annotations = extract_annotations(strip_leading_space(raw_comment), 'servicemethod')
        if NOT_IMPLEMENTED_HIDE_ANNOTATION in annotations:
            logger.debug("NOT_IMPLEMENTED_HIDE_ANNOTATION for service method: '%s', skipping." % svc_method.name)
            return ''

        input_type, output_type = svc_method.input_type.lstrip('.'), svc_method.output_type.lstrip('.')

        body_part_rst = "\n" + indent_lines(stripped_comment)
        method_rst = ".. protobuf:servicemethod:: %s.%s" % (self._proto_file.package, svc_method.name)

        properties_rst = self.common_object_properties(path=[6, svc_index, 2, index])
        properties_rst += ":service: %s\n:input: %s\n:output: %s\n:input_stream: %s\n:output_stream: %s" % (
            svc.name,
            input_type,
            output_type,
            "yes" if svc_method.client_streaming else "no",
            "yes" if svc_method.server_streaming else "no",
        )
        if annotations.get(DOC_TITLE_ANNOTATION):
            properties_rst += "\n:title: %s" % annotations.get(DOC_TITLE_ANNOTATION)
        properties_rst = indent_lines(properties_rst.rstrip())
        return "\n".join([method_rst, properties_rst, body_part_rst])

    @staticmethod
    def format_field_type_name(field):
        if field.type in WKT_PRETTY_TYPE_NAMES:
            return WKT_PRETTY_TYPE_NAMES[field.type]
        else:
            return field.type_name.lstrip('.')


def _get_direct_proto_file_from_request(plugin_pb2_code_generator_request):
    """
    Filter out file(s) feeded by protoc in the plugin_pb2.CodeGeneratorRequest() as transitive dependencies.

    :param plugin_pb2_code_generator_request: instance of plugin_pb2.CodeGeneratorRequest
    :return: single item of the list plugin_pb2_code_generator_request.proto_file which is not a transitive dependency.
    """
    for proto_file in plugin_pb2_code_generator_request.proto_file:
        if not (proto_file.name in plugin_pb2_code_generator_request.file_to_generate):
            # Add dependencies to the Sphinx rule explicitly if you want them to output RST.
            logger.debug("Skipping proto dependency file: '%s'" % proto_file.name)
            continue
        return proto_file
    else:
        raise ProtodocError("Could not determine proto file to process (find non-dependency).")


def main():
    # Uncomment to enable to debug logging.
    # logger.setLevel(logging.DEBUG)

    logger.debug("Python version info: %r" % sys.version_info)

    request = plugin_pb2.CodeGeneratorRequest()
    request.ParseFromString(sys.stdin.read())
    response = plugin_pb2.CodeGeneratorResponse()

    # Dependent proto files are included in the plugin_pb2.CodeGeneratorRequest(). Only process the proto file which the
    # protoc compiler was invoked with.
    proto_file = _get_direct_proto_file_from_request(request)

    Protodoc(proto_file, response).generate_rsts()

    sys.stdout.write(response.SerializeToString())


if __name__ == '__main__':
    main()
