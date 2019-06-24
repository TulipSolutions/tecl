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

import os
from docutils import nodes
from docutils.parsers.rst import directives
from sphinx.util.docutils import SphinxDirective
from sphinx.util.fileutil import copy_asset


class ContentTabsDirective(SphinxDirective):
    """
    A container directive with content-tabs class. To be used with ContentTabsContainerDirective in the content.
    """
    has_content = True
    option_spec = {
        'class': directives.unchanged,
    }

    def run(self):
        self.assert_has_content()
        self.env.temp_data['content_tabs_is_first_tab'] = True

        text = '\n'.join(self.content)
        node = nodes.container(text, classes=['content-tabs'])

        class_ = self.options.get('class', '').strip()
        if class_:
            node['classes'].append(class_)

        self.add_name(node)
        self.state.nested_parse(self.content, self.content_offset, node)

        return [node]


class ContentTabsContainerDirective(SphinxDirective):
    """
    Content of one of the tabs of a ContentTabsDirective.
    """
    has_content = True
    required_arguments = 1

    option_spec = {
        'sidebar': directives.flag,
    }

    def run(self):
        self.assert_has_content()

        lang = self.arguments[0].strip()
        if lang not in self.config.contentui_languages:
            self.state.document.reporter.warning(
                "contentui: Undefined language '%s' for tab-container directive. Configured values are: "
                % lang + repr(self.config.contentui_languages), line=self.lineno)
            return []

        text = '\n'.join(self.content)
        classes = ['code-example', 'tab-content', 'tab-%s' % lang]
        if 'sidebar' in self.options:
            node = nodes.sidebar(classes=classes)
            node += nodes.paragraph('', "%s code example" % lang, classes=["sidebar-title"])
        else:
            node = nodes.container(classes=classes)
        node += nodes.paragraph(text)

        # Make the first tab active by default, this prevents rendering of all tabs
        if self.env.temp_data['content_tabs_is_first_tab']:
            node['classes'].append('active')
            self.env.temp_data['content_tabs_is_first_tab'] = False

        self.add_name(node)
        self.state.nested_parse(self.content, self.content_offset, node)

        return [node]


def copy_statics(app, exc):
    if exc is None:
        for srcfile in ('contentui.css', 'contentui.js'):
            src = os.path.join(os.path.dirname(__file__), srcfile)
            dst = os.path.join(app.outdir, '_static')
            assert os.path.isfile(src)
            assert os.path.isdir(dst)
            copy_asset(src, dst)


def setup(app):
    app.add_directive('content-tabs', ContentTabsDirective)
    app.add_directive('tab-container', ContentTabsContainerDirective)
    app.add_config_value('contentui_languages', [], 'html')
    app.connect('build-finished', copy_statics)
    app.add_css_file('contentui.css')
    app.add_js_file('contentui.js')
