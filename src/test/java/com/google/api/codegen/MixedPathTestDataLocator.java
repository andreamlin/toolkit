/* Copyright 2018 Google LLC
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

package com.google.api.codegen;

import com.google.api.tools.framework.model.testing.ClassPathTestDataLocator;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Prefer using this class instead of {@code TestDataLocator}.
 *
 * <p>A test data locator which first tries to find the specified resource on a file system, and if
 * the file was not found this class fallbacks to the default ({@link ClassPathTestDataLocator})
 * implementation.
 *
 * <p>This behavior is useful for cases when some parts of test infrastructure expect an actual file
 * on a disk (instead of classpath resource) and the resource is packaged inside a jar file (thus
 * cannot be handled as a regular file). Specifically when a test is executed by gradle or IDE the
 * compiled code and its resources are not packaged in a jar file, thus a resource can be read as a
 * regular file. Unfortunately it does not work that well in bazel, which normally packages compiled
 * classes and resources in a single jar file.
 */
public class MixedPathTestDataLocator extends ClassPathTestDataLocator {

  private final List<String> pathPrefixes;

  public MixedPathTestDataLocator(Class<?> classContext, String... pathPrefixes) {
    super(classContext);
    this.pathPrefixes = ImmutableList.copyOf(pathPrefixes);
  }

  public static MixedPathTestDataLocator create(Class<?> classContext) {
    return new MixedPathTestDataLocator(classContext, Paths.get("src", "test", "java").toString());
  }

  @Nullable
  @Override
  public URL resolveTestData(String name) {
    for (String pathPrefix : pathPrefixes) {
      try {
        Path filePath = Paths.get(pathPrefix, name);
        if (Files.isReadable(filePath)) {
          return filePath.toUri().toURL();
        }
      } catch (MalformedURLException e) {
        // Ignore, try another path or fallback to parent's implementation.
      }
    }
    return super.resolveTestData(name);
  }

  /**
   * Injects all files from a directory as virtual test data. This is non-recursive. Injects the
   * files using their full path, relative to the code repo root "./gapic-generator". This enables
   * the testing framework to view proto file imports as Bazel does, from the WORKSPACE-level
   * directory.
   */
  private void injectProtoFilesFromCodeRoot(Path pathFromCodeDir) {
    File dir = pathFromCodeDir.toFile();
    File[] directoryListing = dir.listFiles();
    if (directoryListing != null) {
      for (File child : directoryListing) {
        injectProtoTestFile(child.toPath());
      }
    } else {
      injectProtoTestFile(pathFromCodeDir);
    }
  }

  private void injectProtoTestFile(Path protoFile) {
    if (protoFile.toString().endsWith(".proto")) {
      injectVirtualTestData(protoFile.toString(), readOutFile(protoFile));
    }
  }

  private String readOutFile(Path file) {
    try {
      return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void addTestDataSource(Class<?> classContext, String testDataDir) {
    String relativeToThis = classContext.getPackage().getName().replace('.', '/');
    for (String pathPrefix : pathPrefixes) {
      Path pathFromCodeRepo = Paths.get(pathPrefix, relativeToThis, testDataDir);
      if (Files.exists(pathFromCodeRepo)) {
        injectProtoFilesFromCodeRoot(pathFromCodeRepo);
      }
    }
    super.addTestDataSource(classContext, testDataDir);
  }

  @Override
  public String fetchTestData(URL url) {
    if ("file".equals(url.getProtocol())) {
      try (Reader reader = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)) {
        return CharStreams.toString(reader);
      } catch (IOException e) {
        // Ignore, fallback to parent's implementation.
      }
    }

    return super.fetchTestData(url);
  }
}
