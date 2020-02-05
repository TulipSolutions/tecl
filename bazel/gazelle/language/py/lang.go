// Copyright 2019 Tulip Solutions B.V.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package javalang

import (
	"flag"
	"github.com/bazelbuild/bazel-gazelle/config"
	"github.com/bazelbuild/bazel-gazelle/label"
	"github.com/bazelbuild/bazel-gazelle/language"
	"github.com/bazelbuild/bazel-gazelle/language/proto"
	"github.com/bazelbuild/bazel-gazelle/repo"
	"github.com/bazelbuild/bazel-gazelle/resolve"
	"github.com/bazelbuild/bazel-gazelle/rule"
	"sort"
	"strings"
)

type pyLang struct{}

func NewLanguage() language.Language {
	return &pyLang{}
}

func (x *pyLang) Name() string {
	return "pyLang"
}

func (x *pyLang) Kinds() map[string]rule.KindInfo {
	return map[string]rule.KindInfo{
		"py_proto_library": {
			MatchAny:       false,
			NonEmptyAttrs:  map[string]bool{"deps": true},
			MergeableAttrs: map[string]bool{"deps": true},
		},
		"py_grpc_library": {
			MatchAny:       false,
			NonEmptyAttrs:  map[string]bool{"srcs": true, "deps": true},
			MergeableAttrs: map[string]bool{"srcs": true, "deps": true},
		},
	}
}

func (x *pyLang) Loads() []rule.LoadInfo {
	return []rule.LoadInfo{
		{
			Name:    "@nl_tulipsolutions_tecl//bazel/rules_proto_py:def.bzl",
			Symbols: []string{"py_proto_library", "py_grpc_library"},
		},
	}
}

func (x *pyLang) RegisterFlags(fs *flag.FlagSet, cmd string, c *config.Config) {
}

func (x *pyLang) CheckFlags(fs *flag.FlagSet, c *config.Config) error {
	return nil
}

func (x *pyLang) KnownDirectives() []string {
	return nil
}

func (x *pyLang) Configure(c *config.Config, rel string, f *rule.File) {
}

func ProtoRuleName(protoRuleName string) string {
	return strings.TrimSuffix(protoRuleName, "_proto") + "_py_proto_library"
}

func GrpcWebRuleName(protoRuleName string) string {
	return strings.TrimSuffix(protoRuleName, "_proto") + "_py_grpc_library"
}

func (x *pyLang) GenerateRules(args language.GenerateArgs) language.GenerateResult {
	var protoRuleNames []string
	protoPackages := make(map[string]proto.Package)
	for _, r := range args.OtherGen {
		if r.Kind() != "proto_library" {
			continue
		}
		pkg := r.PrivateAttr(proto.PackageKey).(proto.Package)
		protoPackages[r.Name()] = pkg
		protoRuleNames = append(protoRuleNames, r.Name())
	}
	sort.Strings(protoRuleNames)

	var rules []*rule.Rule
	for _, protoRuleName := range protoRuleNames {
		ppkg := protoPackages[protoRuleName]

		pyProtoRuleName := ProtoRuleName(protoRuleName)
		pyProtoRule := rule.NewRule("py_proto_library", pyProtoRuleName)
		pyProtoRule.SetAttr("deps", []string{":" + protoRuleName})
		pyProtoRule.SetAttr("visibility", []string{"//visibility:public"})
		rules = append(rules, pyProtoRule)

		if ppkg.HasServices {
			pyGrpcWebRuleName := GrpcWebRuleName(protoRuleName)
			pyGrpcWebRule := rule.NewRule("py_grpc_library", pyGrpcWebRuleName)
			pyGrpcWebRule.SetAttr("srcs", []string{":" + protoRuleName})
			pyGrpcWebRule.SetAttr("deps", []string{":" + pyProtoRuleName})
			pyGrpcWebRule.SetAttr("visibility", []string{"//visibility:public"})
			rules = append(rules, pyGrpcWebRule)
		}
	}

	if len(rules) == 0 {
		return language.GenerateResult{}
	}

	return language.GenerateResult{
		Gen:     rules,
		Imports: make([]interface{}, len(rules), len(rules)),
	}
}

func (x *pyLang) Fix(c *config.Config, f *rule.File) {
}

func (x *pyLang) Imports(c *config.Config, r *rule.Rule, f *rule.File) []resolve.ImportSpec {
	return nil
}

func (x *pyLang) Embeds(r *rule.Rule, from label.Label) []label.Label {
	return nil
}

func (x *pyLang) Resolve(c *config.Config, ix *resolve.RuleIndex, rc *repo.RemoteCache, r *rule.Rule, imports interface{}, from label.Label) {
}
