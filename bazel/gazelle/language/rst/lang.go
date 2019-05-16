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

package rst

import (
	"flag"
	"github.com/bazelbuild/bazel-gazelle/config"
	"github.com/bazelbuild/bazel-gazelle/label"
	"github.com/bazelbuild/bazel-gazelle/language"
	"github.com/bazelbuild/bazel-gazelle/repo"
	"github.com/bazelbuild/bazel-gazelle/resolve"
	"github.com/bazelbuild/bazel-gazelle/rule"
	"sort"
	"strings"
)

type rstLang struct{}

func NewLanguage() language.Language {
	return &rstLang{}
}

func (x *rstLang) Name() string {
	return "rstLang"
}

func (x *rstLang) Kinds() map[string]rule.KindInfo {
	return map[string]rule.KindInfo{
		"rst_proto": {
			MatchAny:       false,
			NonEmptyAttrs:  map[string]bool{"deps": true},
			MergeableAttrs: map[string]bool{"deps": true},
		},
	}
}

func (x *rstLang) Loads() []rule.LoadInfo {
	return []rule.LoadInfo{
		{
			Name:    "@nl_tulipsolutions_tecl//bazel/rules_protodoc:def.bzl",
			Symbols: []string{"rst_proto"},
		},
	}
}

func (x *rstLang) RegisterFlags(fs *flag.FlagSet, cmd string, c *config.Config) {
}

func (x *rstLang) CheckFlags(fs *flag.FlagSet, c *config.Config) error {
	return nil
}

func (x *rstLang) KnownDirectives() []string {
	return nil
}

func (x *rstLang) Configure(c *config.Config, rel string, f *rule.File) {
}

func (x *rstLang) GenerateRules(args language.GenerateArgs) language.GenerateResult {
	var protoRuleNames []string
	for _, r := range args.OtherGen {
		if r.Kind() != "proto_library" {
			continue
		}
		protoRuleNames = append(protoRuleNames, r.Name())
	}
	sort.Strings(protoRuleNames)

	var rules []*rule.Rule
	for _, protoRuleName := range protoRuleNames {
		rstProtoRuleName := strings.TrimSuffix(protoRuleName, "_proto") + "_rst_proto"
		rstProtoLibrary := rule.NewRule("rst_proto", rstProtoRuleName)
		rstProtoLibrary.SetAttr("deps", []string{":" + protoRuleName})
		rstProtoLibrary.SetAttr("visibility", []string{"//visibility:public"})
		rules = append(rules, rstProtoLibrary)
	}

	if len(rules) == 0 {
		return language.GenerateResult{}
	}

	return language.GenerateResult{
		Gen:     rules,
		Imports: make([]interface{}, len(rules), len(rules)),
	}
}

func (x *rstLang) Fix(c *config.Config, f *rule.File) {
}

func (x *rstLang) Imports(c *config.Config, r *rule.Rule, f *rule.File) []resolve.ImportSpec {
	return nil
}

func (x *rstLang) Embeds(r *rule.Rule, from label.Label) []label.Label {
	return nil
}

func (x *rstLang) Resolve(c *config.Config, ix *resolve.RuleIndex, rc *repo.RemoteCache, r *rule.Rule, imports interface{}, from label.Label) {
}
