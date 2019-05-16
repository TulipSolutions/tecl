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

package auth

import (
	"context"
	"strings"
)

type TulipAuth struct {
	Token string
}

// Add a JWT token to outgoing calls to services in the private API
func (auth TulipAuth) GetRequestMetadata(ctx context.Context, uri ...string) (map[string]string, error) {
	m := make(map[string]string)
	if strings.Contains(strings.Join(uri, ""), "priv") {
		m["authorization"] = "Bearer " + auth.Token
	}
	return m, nil
}

func (TulipAuth) RequireTransportSecurity() bool {
	return true
}
