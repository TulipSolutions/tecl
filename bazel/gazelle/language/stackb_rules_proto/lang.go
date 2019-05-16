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

package stackb

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

const validateProtoImportString = "validate/validate.proto"

type stackb struct{}

func NewLanguage() language.Language {
	return &stackb{}
}

func (x *stackb) Name() string {
	return "stackb"
}

func (x *stackb) Kinds() map[string]rule.KindInfo {
	return map[string]rule.KindInfo{
		"node_proto_compile": {
			MatchAny:       false,
			NonEmptyAttrs:  map[string]bool{"deps": true},
			MergeableAttrs: map[string]bool{"deps": true},
		},
		"node_grpc_compile": {
			MatchAny:       false,
			NonEmptyAttrs:  map[string]bool{"deps": true},
			MergeableAttrs: map[string]bool{"deps": true},
		},
		"commonjs_grpc_compile": {
			MatchAny:       false,
			NonEmptyAttrs:  map[string]bool{"deps": true},
			MergeableAttrs: map[string]bool{"deps": true},
		},
	}
}

func (x *stackb) Loads() []rule.LoadInfo {
	return []rule.LoadInfo{
		{
			Name:    "@build_stack_rules_proto//node:node_proto_compile.bzl",
			Symbols: []string{"node_proto_compile"},
		},
		{
			Name:    "@build_stack_rules_proto//node:node_grpc_compile.bzl",
			Symbols: []string{"node_grpc_compile"},
		},
		{
			Name:    "@build_stack_rules_proto//github.com/grpc/grpc-web:commonjs_grpc_compile.bzl",
			Symbols: []string{"commonjs_grpc_compile"},
		},
	}
}

func (x *stackb) RegisterFlags(fs *flag.FlagSet, cmd string, c *config.Config) {
}

func (x *stackb) CheckFlags(fs *flag.FlagSet, c *config.Config) error {
	return nil
}

func (x *stackb) KnownDirectives() []string {
	return nil
}

func (x *stackb) Configure(c *config.Config, rel string, f *rule.File) {
}

func JsProtoRuleName(protoRuleName string) string {
	return strings.TrimSuffix(protoRuleName, "_proto") + "_js_proto_compile"
}

func JsGrpcNodeRuleName(protoRuleName string) string {
	return strings.TrimSuffix(protoRuleName, "_proto") + "_js_grpc_node_compile"
}

func JsGrpcWebRuleName(protoRuleName string) string {
	return strings.TrimSuffix(protoRuleName, "_proto") + "_js_grpc_web_compile"
}

func (x *stackb) GenerateRules(args language.GenerateArgs) language.GenerateResult {
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

		jsProtoRule := rule.NewRule("node_proto_compile", JsProtoRuleName(protoRuleName))
		jsProtoRule.SetAttr("deps", []string{":" + protoRuleName})
		jsProtoRule.SetAttr("visibility", []string{"//visibility:public"})
		rules = append(rules, jsProtoRule)

		if ppkg.HasServices {
			jsGrpcNodeProtoRule := rule.NewRule("node_grpc_compile", JsGrpcNodeRuleName(protoRuleName))
			jsGrpcNodeProtoRule.SetAttr("deps", []string{":" + protoRuleName})
			jsGrpcNodeProtoRule.SetAttr("visibility", []string{"//visibility:public"})
			rules = append(rules, jsGrpcNodeProtoRule)

			jsGrpcWebProtoRule := rule.NewRule("commonjs_grpc_compile", JsGrpcWebRuleName(protoRuleName))
			jsGrpcWebProtoRule.SetAttr("deps", []string{":" + protoRuleName})
			jsGrpcWebProtoRule.SetAttr("visibility", []string{"//visibility:public"})
			jsGrpcWebProtoRule.SetAttr("plugin_options", []string{"import_style=commonjs", "mode=grpcwebtext"})
			rules = append(rules, jsGrpcWebProtoRule)
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

func (x *stackb) Fix(c *config.Config, f *rule.File) {
}

func (x *stackb) Imports(c *config.Config, r *rule.Rule, f *rule.File) []resolve.ImportSpec {
	return nil
}

func (x *stackb) Embeds(r *rule.Rule, from label.Label) []label.Label {
	return nil
}

func (x *stackb) Resolve(c *config.Config, ix *resolve.RuleIndex, rc *repo.RemoteCache, r *rule.Rule, imports interface{}, from label.Label) {
}
