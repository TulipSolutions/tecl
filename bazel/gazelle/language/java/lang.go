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

const (
	validateProtoImportString = "validate/validate.proto"
	ProtoImportsKey           = "_proto_imports"
	ProtoRuleNameKey          = "_proto_rule_name"
)

type javaLang struct{}

func NewLanguage() language.Language {
	return &javaLang{}
}

func (x *javaLang) Name() string {
	return "javaLang"
}

func (x *javaLang) Kinds() map[string]rule.KindInfo {
	return map[string]rule.KindInfo{
		"java_proto_library": {
			MatchAny:       false,
			NonEmptyAttrs:  map[string]bool{"deps": true},
			MergeableAttrs: map[string]bool{"deps": true},
		},
		"java_grpc_library": {
			MatchAny: false,
			NonEmptyAttrs: map[string]bool{
				"deps": true,
				"srcs": true,
			},
			MergeableAttrs: map[string]bool{"srcs": true},
			ResolveAttrs:   map[string]bool{"deps": true},
		},
		"reactor_grpc_library": {
			MatchAny: false,
			NonEmptyAttrs: map[string]bool{
				"proto": true,
				"deps":  true,
			},
			MergeableAttrs: map[string]bool{"proto": true},
			ResolveAttrs:   map[string]bool{"deps": true},
		},
		"pgv_java_proto_library": {
			MatchAny: false,
			NonEmptyAttrs: map[string]bool{
				"deps":      true,
				"java_deps": true,
			},
			MergeableAttrs: map[string]bool{"deps": true},
			ResolveAttrs:   map[string]bool{"java_deps": true},
		},
		"java_validate_interceptor_library": {
			MatchAny: false,
			NonEmptyAttrs: map[string]bool{
				"deps": true,
				"srcs": true,
			},
			MergeableAttrs: map[string]bool{"srcs": true},
			ResolveAttrs:   map[string]bool{"deps": true},
		},
	}
}

func (x *javaLang) Loads() []rule.LoadInfo {
	return []rule.LoadInfo{
		{
			Name:    "@io_grpc_grpc_java//:java_grpc_library.bzl",
			Symbols: []string{"java_grpc_library"},
		},
		{
			Name:    "@com_envoyproxy_protoc_gen_validate//bazel:pgv_proto_library.bzl",
			Symbols: []string{"pgv_java_proto_library"},
		},
		{
			Name:    "@nl_tulipsolutions_tecl//bazel/rules_proto_validate_interceptor:def.bzl",
			Symbols: []string{"java_validate_interceptor_library"},
		},
		{
			Name:    "@com_salesforce_servicelibs_reactive_grpc//bazel:java_reactive_grpc_library.bzl",
			Symbols: []string{"reactor_grpc_library"},
		},
	}
}

func (x *javaLang) RegisterFlags(fs *flag.FlagSet, cmd string, c *config.Config) {
}

func (x *javaLang) CheckFlags(fs *flag.FlagSet, c *config.Config) error {
	return nil
}

func (x *javaLang) KnownDirectives() []string {
	return nil
}

func (x *javaLang) Configure(c *config.Config, rel string, f *rule.File) {
}

func ProtoRuleName(protoRuleName string) string {
	return strings.TrimSuffix(protoRuleName, "_proto") + "_jvm_proto"
}

func GrpcRuleName(protoRuleName string) string {
	return strings.TrimSuffix(protoRuleName, "_proto") + "_jvm_grpc"
}

func ReactorGrpcRuleName(protoRuleName string) string {
	return strings.TrimSuffix(protoRuleName, "_proto") + "_reactor_grpc"
}

func ValidateRuleName(protoRuleName string) string {
	return strings.TrimSuffix(protoRuleName, "_proto") + "_jvm_validate"
}

func ValidateInterceptorRuleName(protoRuleName string) string {
	return strings.TrimSuffix(protoRuleName, "_proto") + "_jvm_validate_interceptor"
}

func (x *javaLang) GenerateRules(args language.GenerateArgs) language.GenerateResult {
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
		imports := make([]string, 0, len(ppkg.Imports)+1)
		for i := range ppkg.Imports {
			imports = append(imports, i)
		}
		imports = append(imports, ppkg.Name)
		sort.Strings(imports)

		javaProtoRuleName := ProtoRuleName(protoRuleName)
		javaProtoRule := rule.NewRule("java_proto_library", javaProtoRuleName)
		javaProtoRule.SetAttr("deps", []string{":" + protoRuleName})
		javaProtoRule.SetAttr("visibility", []string{"//visibility:public"})
		rules = append(rules, javaProtoRule)

		if ppkg.HasServices {
			javaGrpcRuleName := GrpcRuleName(protoRuleName)
			javaGrpcRule := rule.NewRule("java_grpc_library", javaGrpcRuleName)
			javaGrpcRule.SetAttr("srcs", []string{":" + protoRuleName})
			javaGrpcRule.SetAttr("deps", []string{":" + javaProtoRuleName})
			javaGrpcRule.SetAttr("visibility", []string{"//visibility:public"})
			rules = append(rules, javaGrpcRule)

			reactorGrpcRuleName := ReactorGrpcRuleName(protoRuleName)
			reactorGrpcRule := rule.NewRule("reactor_grpc_library", reactorGrpcRuleName)
			reactorGrpcRule.SetAttr("proto", ":"+protoRuleName)
			javaReactorGrpcDeps := []string{":" + javaGrpcRuleName}
			reactorGrpcRule.SetAttr("deps", javaReactorGrpcDeps)
			reactorGrpcRule.SetAttr("visibility", []string{"//visibility:public"})
			rules = append(rules, reactorGrpcRule)
		}

		if ppkg.Imports[validateProtoImportString] {
			javaValidateRuleName := ValidateRuleName(protoRuleName)
			javaValidateRule := rule.NewRule("pgv_java_proto_library", javaValidateRuleName)
			javaValidateRule.SetAttr("visibility", []string{"//visibility:public"})
			javaValidateRule.SetAttr("deps", []string{":" + protoRuleName})
			javaValidateRule.SetAttr("java_deps", []string{":" + javaProtoRuleName})
			rules = append(rules, javaValidateRule)

			if ppkg.HasServices {
				javaValidateInterceptorRuleName := ValidateInterceptorRuleName(protoRuleName)
				javaValidateInterceptorRule := rule.NewRule("java_validate_interceptor_library", javaValidateInterceptorRuleName)
				javaValidateInterceptorRule.SetAttr("srcs", []string{":" + protoRuleName})
				javaValidateInterceptorDeps := []string{":" + javaProtoRuleName, ":" + javaValidateRuleName}
				sort.Strings(javaValidateInterceptorDeps)
				javaValidateInterceptorRule.SetAttr("deps", javaValidateInterceptorDeps)
				javaValidateInterceptorRule.SetAttr("visibility", []string{"//visibility:public"})
				rules = append(rules, javaValidateInterceptorRule)
			}
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

func (x *javaLang) Fix(c *config.Config, f *rule.File) {
}

func (x *javaLang) Imports(c *config.Config, r *rule.Rule, f *rule.File) []resolve.ImportSpec {
	return nil
}

func (x *javaLang) Embeds(r *rule.Rule, from label.Label) []label.Label {
	return nil
}

func (x *javaLang) Resolve(c *config.Config, ix *resolve.RuleIndex, rc *repo.RemoteCache, r *rule.Rule, imports interface{}, from label.Label) {
}
