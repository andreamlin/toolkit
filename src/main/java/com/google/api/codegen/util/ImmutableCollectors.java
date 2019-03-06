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

import autovalue.shaded.com.google.common.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.function.Function;
import java.util.stream.Collector;

public class ImmutableCollectors {
  public static <T, K, V> Collector<T, ?, ImmutableMap<K,V>> toImmutableMap(
      Function<? super T, ? extends K> keyMapper,
      Function<? super T, ? extends V> valueMapper) {
    return Collector.of(
        ImmutableMap.Builder<K, V>::new,
        (b, e) -> b.put(keyMapper.apply(e), valueMapper.apply(e)),
        (b1, b2) -> b1.putAll(b2.build()),
        ImmutableMap.Builder::build);
  }

  public static <K> Collector<K, ?, ImmutableList<K>> toImmutableList() {
    return Collector.of(
        ImmutableList.Builder<K>::new,
        (b, e) -> b.add(e),
        (b1, b2) -> b1.addAll(b2.build()),
        ImmutableList.Builder::build);
  }
}
