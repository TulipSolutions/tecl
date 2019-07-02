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

# Most of this file is taken from the original implementation of the 'literalinclude' directive included in Sphinx core;
# file sphinx/directives/code.py.

from docutils import nodes
from docutils.parsers.rst import directives
from six import text_type

from sphinx.directives.code import (
    dedent_lines,
    LiteralInclude,
    LiteralIncludeReader
)
from sphinx.locale import __
from sphinx.util import logging
from sphinx.util.nodes import set_source_info

if False:
    # For type annotation
    from typing import Any, Dict, List, Tuple  # NOQA
    from sphinx.application import Sphinx  # NOQA
    from sphinx.config import Config  # NOQA

logger = logging.getLogger(__name__)


def caption_wrapper(literal_node, caption):
    # type: (nodes.Node, unicode) -> nodes.container
    container_node = nodes.container('', literal_block=True,
                                     classes=['literal-block-wrapper'])
    container_node += nodes.strong(caption, caption)
    container_node += literal_node
    return container_node


def get_min_indent_nonempty_line(lines):
    min = -1
    for raw_line in lines:
        stripped_line = raw_line.strip()
        if not stripped_line:
            continue
        lstripped_line = raw_line.lstrip()
        num_chars_lstripped = len(raw_line) - len(lstripped_line)
        if num_chars_lstripped < min or min == -1:
            min = num_chars_lstripped
    if min == -1:
        min = 0
    return min


class CodeIncludeReader(LiteralIncludeReader):
    begin_marker_prefix = " CODEINCLUDE-BEGIN-MARKER: "
    end_marker_prefix = " CODEINCLUDE-END-MARKER: "

    def __init__(self, filename, options, config):
        # type: (unicode, Dict, Config) -> None
        self.filename = filename
        self.options = options
        self.encoding = options.get('encoding', config.source_encoding)
        # Not calling superclass __init__ on purpose.

    def read(self, location=None):
        # type: (Any) -> Tuple[unicode, int]
        filters = [self.markerid_filter,
                   self.autodedent_filter]
        lines = self.read_file(self.filename, location=location)
        for func in filters:
            lines = func(lines, location=location)

        return ''.join(lines), len(lines)

    def autodedent_filter(self, lines, location=None):
        # type: (List[unicode], Any) -> List[unicode]
        if 'no-auto-dedent' in self.options:
            return lines
        else:
            dedent_level = get_min_indent_nonempty_line(lines)
            logger.debug(__('autodedent: %d' % dedent_level), location=location)
            return dedent_lines(lines, dedent_level, location=location)

    def markerid_filter(self, lines, location=None):
        # type: (List[unicode], Any) -> List[unicode]
        if 'marker-id' in self.options:
            marker_str = self.options['marker-id'].strip()
            begin_str = self.begin_marker_prefix + marker_str
            end_str = self.end_marker_prefix + marker_str
            return_lines = []
            within_boundary = False
            for lineno, line in enumerate(lines):
                if line.rstrip().endswith(begin_str):
                    if within_boundary:
                        logger.warning(__('Repetitive begin-marker for marker-id \'%s\' on line %d'
                                          % (marker_str, lineno + 1)), location=location)
                    within_boundary = True
                elif line.rstrip().endswith(end_str):
                    if not within_boundary:
                        logger.warning(__('End-marker for marker-id \'%s\' on line %d, without having seen the '
                                          'begin-marker' % (marker_str, lineno + 1)), location=location)
                    within_boundary = False
                elif self.begin_marker_prefix in line or self.end_marker_prefix in line:
                    # Skip lines with other markers.
                    logger.debug(
                        __('Skipping line number %d with non-matching marker' % (lineno + 1)),
                        location=location
                    )
                elif within_boundary:
                    return_lines.append(line)

            if not return_lines:
                logger.warning(__('No matching lines for marker \'%s\'' % marker_str), location=location)

            return return_lines
        else:
            logger.info(__('marker-id not provided; outputting all lines in file'), location=location)
            return lines


class CodeInclude(LiteralInclude):
    """
    Like ``.. literalinclude:: file.ext``, but using markers instead of line offsets, auto-dedent and auto-language
    features.

    Markers are composed of a prefix and an ID, to match as literal string in source files. They are supposed to be on a
    line by themselves, as they are omitted in the output themselves. Also the prefix should already be unique to the
    use of this marker, as lines with markers which are not matching with the given marker-id will be omitted as well
    too.

    Markers with the same ID can occur multiple times and the matched range will be simply concatenated in the output.

    The language is automatically detected by looking at the file extension. Recognized are those in the
    ``auto_language_map`` class member dict. To override, use the ``:language:`` field.

    Code is automatically dedented (by its minimum indent on a non-empty line) - to turn that off, use the
    ``:no-auto-dedent:`` field (flag).
    """
    option_spec = {
        'no-auto-dedent': directives.flag,
        'marker-id': directives.unchanged,
        'language': directives.unchanged,
        'encoding': directives.encoding,
        'tab-width': int,
        'class': directives.class_option,
        'caption': directives.unchanged,
    }
    auto_language_map = {
        '.py': 'python',
        '.bazel': 'python',
        '.bzl': 'python',
        '.java': 'java',
        '.go': 'go',
        '.js': 'js',
    }

    def run(self):
        # type: () -> List[nodes.Node]
        document = self.state.document
        if not document.settings.file_insertion_enabled:
            return [document.reporter.warning('File insertion disabled',
                                              line=self.lineno)]
        try:
            location = self.state_machine.get_source_and_line(self.lineno)
            rel_filename, filename = self.env.relfn2path(self.arguments[0])
            self.env.note_dependency(rel_filename)

            reader = CodeIncludeReader(filename, self.options, self.config)
            text, lines = reader.read(location=location)

            retnode = nodes.literal_block(text, text, source=filename)
            set_source_info(self, retnode)
            if 'language' in self.options:
                retnode['language'] = self.options['language']
            else:
                for ext, lang in self.auto_language_map.items():
                    if filename.endswith(ext):
                        retnode['language'] = lang
                        break
            retnode['classes'] += self.options.get('class', [])

            if 'caption' in self.options:
                caption = self.options['caption']
                retnode = caption_wrapper(retnode, caption)

            # retnode will be note_implicit_target that is linked from caption and numref.
            # when options['name'] is provided, it should be primary ID.
            self.add_name(retnode)

            return [retnode]
        except Exception as exc:
            return [document.reporter.warning(text_type(exc), line=self.lineno)]


def setup(app):
    # type: (Sphinx) -> Dict[unicode, Any]
    directives.register_directive('codeinclude', CodeInclude)

    return {
        'parallel_read_safe': True,
        'parallel_write_safe': True,
    }
