# -*- coding: utf-8 -*-
#
# Configuration file for the Sphinx documentation builder.
#
# This file does only contain a selection of the most common options. For a
# full list see the documentation:
# http://www.sphinx-doc.org/en/master/config

# Set localization variables to default values to ensure more reproducible builds.
import os

os.environ['LANG'] = 'C'
os.environ['LC_ALL'] = 'C'

# -- Project information -----------------------------------------------------

project = 'Exchange Infrastructure API'
copyright = 'Tulip Solutions B.V.'
author = 'Tulip Solutions B.V. Development'

# Parse Bazel's stable-status.txt file and consume the variables from there.
bazel_status_vars = dict()
for line in open(os.environ['BAZEL_STABLE_STATUS_FILE'], 'r'):
    if not line.strip():
        continue
    key, value = line.split(' ', 1)
    bazel_status_vars.update({key: value.strip()})

# TODO: For all non-existing keys accessed in bazel_status_vars, this should fail hard, instead of falling back to a
#       default value.
# TODO: Obtain actual version from protocol repository, even when built from prototype repostitory.
#       See Issue: trading-platform/178.
# The short X.Y version
version = bazel_status_vars.get('STABLE_GIT_DESCRIBE_ABBREV0', 'UNRELEASED')
# The full version
release = bazel_status_vars.get('STABLE_GIT_DESCRIBE_HEAD_LONG', 'UNRELEASED-OR-NOT-PROVIDED')
git_sha1 = bazel_status_vars.get('STABLE_GIT_HEAD_SHA1', 'no_git_data')
sourcelinks_branch = "master"

# -- General configuration ---------------------------------------------------

# Tested with Sphinx 1.8
needs_sphinx = '1.8'

# Add any Sphinx extension module names here, as strings. They can be
# extensions coming with Sphinx (named 'sphinx.ext.*') or your custom
# ones.
# Add custom ones in //docs/sphinx_extensions/<pkg>/<module>.py and list it here
# as 'docs.sphinx_extensions.<pkg>.<module>'.
extensions = [
    'docs.sphinx_extensions.contentui.contentui',
    'docs.sphinx_extensions.protobufdomain.protobufdomain',
]

contentui_languages = ['Go', 'Java', 'Node', 'Python']

protobufdomain_strip_prefixes = [
    "tulipsolutions.api.",
    "tulipsolutions.api.priv.",
    "tulipsolutions.api.pub.",
    "tulipsolutions.api.common.",
]
protobufdomain_strip_prefixes_match_longest = True
protobufdomain_sourcelinks_base_url = "https://github.com/tulipsolutions/tecl/blob/%s/" % sourcelinks_branch

# The suffix(es) of source filenames.
source_suffix = ['.rst', '.md']

# The master toctree document.
master_doc = 'index'

# The name of the Pygments (syntax highlighting) style to use.
# Try some the Pygments demo page, e.g. http://pygments.org/demo/6783651/
# pygments_style = 'native'

# -- Options for HTML output -------------------------------------------------

# The theme to use for HTML and HTML Help pages.  See the documentation for
# a list of builtin themes.
#
html_theme = 'sphinx_rtd_theme'

# Theme options are theme-specific and customize the look and feel of a theme
# further.  For a list of options available for each theme, see the
# documentation.
html_theme_options = {
    'style_external_links': True,
    # Allows to add pages to the toctree and the HTML sidebar, without having
    # to display them on the page itself (::toctree: with :hidden:).
    'includehidden': True,
}

html_show_sphinx = False

# Extra variables available in the templates of the theme. Can also be passed
# on the command line during build-time with sphinx-build using -A KEY=VALUE.
html_context = {
    # 'build_id': os.environ.get('BUILD_ID'),
    # 'build_url': os.environ.get('BUILD_URL'),
    'commit': git_sha1,
    'contentui_languages': contentui_languages,
}

# By default, the static path is configured relative to this configuration file,
# so we need to point to the absolute path of the Bazel-created input directory
# where all files in 'src/' (which includes a '_static/' directory) are
# gathered.
html_static_path = [os.environ['BAZEL_SPHINX_INPUT_DIR'] + '/_static']
templates_path = [os.environ['BAZEL_SPHINX_INPUT_DIR'] + '/_templates']

html_css_files = ['css/custom.css']
html_js_files = ['js/custom.js']

html_favicon = os.environ['BAZEL_SPHINX_INPUT_DIR'] + '/_static/img/favicon.png'

html_logo = os.environ['BAZEL_SPHINX_INPUT_DIR'] + '/_static/img/tulip-logo.svg'
