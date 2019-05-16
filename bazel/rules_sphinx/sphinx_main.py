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
import sys

"""Wraps `sphinx-build`, taking first argument as path to the bazel-out/stable-status.txt file which is consumed in
`conf.py`. All other arguments are passed on to `sphinx-build`.
"""

if __name__ == "__main__":
    os.environ['BAZEL_STABLE_STATUS_FILE'] = os.path.abspath(sys.argv[1])
    # 'Sniff' the sources input directory as a way to know what it would be a configuration-time in conf.py.
    # Needed for pointing to custom CSS/JS.
    os.environ['BAZEL_SPHINX_INPUT_DIR'] = os.path.abspath(sys.argv[-2])

    from sphinx.cmd.build import main
    sys.exit(main(sys.argv[2:]))
