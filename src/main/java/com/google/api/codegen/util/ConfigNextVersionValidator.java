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
package com.google.api.codegen.util;

import com.google.api.codegen.ConfigProto;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import javax.annotation.Nonnull;

public class ConfigNextVersionValidator {

  public static String CONFIG_V2_MAJOR_VERSION = "2";
  public static String CONFIG_V2_VERSION = CONFIG_V2_MAJOR_VERSION + ".0.0"; // "2.0.0"

  public void checkIsNextVersionConfig(@Nonnull ConfigProto configV1Proto)
      throws IllegalStateException {
    if (!configV1Proto.getConfigSchemaVersion().startsWith(CONFIG_V2_MAJOR_VERSION + ".")
        && !configV1Proto.getConfigSchemaVersion().equals(CONFIG_V2_MAJOR_VERSION)) {
      throw new IllegalStateException(
          String.format(
              "Provided ConfigProto version is %s but should be >= %s",
              configV1Proto.getConfigSchemaVersion(), CONFIG_V2_VERSION));
    }
    ByteString serialized = configV1Proto.toByteString();

    try {
      com.google.api.codegen.v2.ConfigProto v2Proto =
          com.google.api.codegen.v2.ConfigProto.parseFrom(serialized);
      if (!v2Proto.getUnknownFields().asMap().isEmpty()) {
        throw new IllegalStateException(
            String.format(
                "Unknown fields in configProto: %s",
                v2Proto.getUnknownFields().asMap().toString()));
      }
    } catch (InvalidProtocolBufferException e) {
      throw new IllegalStateException(e);
    }
  }
}
