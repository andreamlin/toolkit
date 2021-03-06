/* Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.api.codegen.viewmodel;

import com.google.auto.value.AutoValue;

/* A view model for the standalone sample function pararmeters. */
@AutoValue
public abstract class SampleFunctionParameterView {

  public abstract String typeName();

  public abstract String identifier();

  public abstract InitValueView initValue();

  /** We need to keep the cli flag names in the same case (snake_case) across all languages. */
  public abstract String cliFlagName();

  public abstract String description();

  public static Builder newBuilder() {
    return new AutoValue_SampleFunctionParameterView.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder typeName(String val);

    public abstract Builder identifier(String val);

    public abstract Builder initValue(InitValueView val);

    public abstract Builder cliFlagName(String val);

    public abstract Builder description(String val);

    public abstract SampleFunctionParameterView build();
  }
}
