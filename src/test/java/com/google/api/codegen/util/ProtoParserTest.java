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
package com.google.api.codegen.util;

import static com.google.common.truth.Truth.assertThat;

import com.google.api.MethodSignature;
import com.google.api.Resource;
import com.google.api.ResourceSet;
import com.google.api.Retry;
import com.google.api.codegen.CodegenTestUtil;
import com.google.api.codegen.protoannotations.GapicCodeGeneratorAnnotationsTest;
import com.google.api.tools.framework.model.Field;
import com.google.api.tools.framework.model.Interface;
import com.google.api.tools.framework.model.MessageType;
import com.google.api.tools.framework.model.Method;
import com.google.api.tools.framework.model.Model;
import com.google.api.tools.framework.model.ProtoFile;
import com.google.api.tools.framework.model.testing.TestDataLocator;
import com.google.longrunning.OperationTypes;
import com.google.rpc.Code;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ProtoParserTest {
  private static String[] protoFiles = {"library.proto"};

  @ClassRule public static TemporaryFolder tempDir = new TemporaryFolder();
  private static Model model;
  private static TestDataLocator testDataLocator;
  private static ProtoFile libraryProtoFile;
  private static Field shelfNameField;
  private static Interface libraryService;
  private static Method deleteShelfMethod;
  private static Method getBigBookMethod;
  private static MessageType book;
  private static MessageType shelf;

  // Object under test.
  private static ProtoParser protoParser = new ProtoParser();

  @BeforeClass
  public static void startUp() {
    // Load and parse protofile.

    testDataLocator = TestDataLocator.create(GapicCodeGeneratorAnnotationsTest.class);
    testDataLocator.addTestDataSource(CodegenTestUtil.class, "testsrc/common");

    model = CodegenTestUtil.readModel(testDataLocator, tempDir, protoFiles, new String[0]);

    libraryProtoFile =
        model
            .getFiles()
            .stream()
            .filter(f -> f.getSimpleName().equals("library.proto"))
            .findFirst()
            .get();

    model.addRoot(libraryProtoFile);

    libraryService = libraryProtoFile.getInterfaces().get(0);

    shelf =
        libraryProtoFile
            .getMessages()
            .stream()
            .filter(m -> m.getSimpleName().equals("Shelf"))
            .findFirst()
            .get();
    shelfNameField =
        shelf.getFields().stream().filter(f -> f.getSimpleName().equals("name")).findFirst().get();

    book =
        libraryProtoFile
            .getMessages()
            .stream()
            .filter(m -> m.getSimpleName().equals("Book"))
            .findFirst()
            .get();
    shelfNameField =
        shelf.getFields().stream().filter(f -> f.getSimpleName().equals("name")).findFirst().get();

    libraryService = libraryProtoFile.getInterfaces().get(0);
    deleteShelfMethod = libraryService.lookupMethod("DeleteShelf");
    getBigBookMethod = libraryService.lookupMethod("GetBigBook");
  }

  @Test
  public void testGetResourcePath() {
    Field shelfNameField =
        shelf.getFields().stream().filter(f -> f.getSimpleName().equals("name")).findFirst().get();
    assertThat(protoParser.getResource(shelfNameField).getPath()).isEqualTo("shelves/*");
  }

  @Test
  public void testGetEmptyResource() {
    MessageType book =
        libraryProtoFile
            .getMessages()
            .stream()
            .filter(m -> m.getSimpleName().equals("Book"))
            .findFirst()
            .get();
    Field authorBookField =
        book.getFields().stream().filter(f -> f.getSimpleName().equals("author")).findFirst().get();
    assertThat(protoParser.getResource(authorBookField)).isNull();
  }

  /** Return the entity name, e.g. "shelf" for a resource field. */
  @Test
  public void testGetResourceEntityName() {
    String defaultEntityName = protoParser.getDefaultResourceEntityName(shelfNameField);
    assertThat(defaultEntityName).isEqualTo("shelf");
    assertThat(protoParser.getResourceEntityName(shelfNameField, defaultEntityName))
        .isEqualTo("shelf");
  }

  @Test
  public void testGetResourceSet() {
    Field bookNameField =
        book.getFields().stream().filter(f -> f.getSimpleName().equals("name")).findFirst().get();
    ResourceSet bookResourceSet = protoParser.getResourceSet(bookNameField);
    assertThat(bookResourceSet).isNotNull();
    assertThat(bookResourceSet.getBaseName()).isEqualTo("book_oneof");
    assertThat(bookResourceSet.getResourcesCount()).isEqualTo(3);
    assertThat(bookResourceSet.getResources(0))
        .isEqualTo(Resource.newBuilder().setBaseName("book").setPath("shelves/*/books/*").build());
    assertThat(bookResourceSet.getResources(2))
        .isEqualTo(
            Resource.newBuilder().setBaseName("deleted_book").setPath("_deleted-book_").build());
  }

  @Test
  public void testGetResourceTypeEntityNameFromOneof() {
    MessageType getBookFromAnywhereRequest =
        libraryProtoFile
            .getMessages()
            .stream()
            .filter(m -> m.getSimpleName().equals("GetBookFromAnywhereRequest"))
            .findFirst()
            .get();
    Field nameField =
        getBookFromAnywhereRequest
            .getFields()
            .stream()
            .filter(f -> f.getSimpleName().equals("name"))
            .findFirst()
            .get();
    assertThat(protoParser.getResourceTypeEntityName(nameField)).isEqualTo("book_oneof");

    Field altBookNameField =
        getBookFromAnywhereRequest
            .getFields()
            .stream()
            .filter(f -> f.getSimpleName().equals("alt_book_name"))
            .findFirst()
            .get();
    assertThat(protoParser.getResourceTypeEntityName(altBookNameField)).isEqualTo("book");
  }

  @Test
  public void getResourceEntityName() {
    assertThat(protoParser.getResourceEntityName(shelfNameField)).isEqualTo("shelf");
  }

  @Test
  public void testGetLongRunningOperation() {
    OperationTypes operationTypes = protoParser.getLongRunningOperation(getBigBookMethod);

    OperationTypes expected =
        OperationTypes.newBuilder()
            .setResponse("google.example.library.v1.Book")
            .setMetadata("google.example.library.v1.GetBigBookMetadata")
            .build();
    assertThat(operationTypes).isEqualTo(expected);
  }

  @Test
  public void testGetRetryOnExplicitRetryCodes() {
    Retry deleteShelfRetry = protoParser.getRetry(deleteShelfMethod);
    Retry expectedDeleteShelfRetry =
        Retry.newBuilder().addCodes(Code.UNAVAILABLE).addCodes(Code.DEADLINE_EXCEEDED).build();
    assertThat(deleteShelfRetry).isEqualTo(expectedDeleteShelfRetry);
  }

  @Test
  public void testIsHttpGetMethod() {
    assertThat(protoParser.isHttpGetMethod(deleteShelfMethod)).isFalse();
    assertThat(protoParser.isHttpGetMethod(getBigBookMethod)).isTrue();
  }

  @Test
  public void testGetServiceAddress() {
    String defaultHost = protoParser.getServiceAddress(libraryService);
    assertThat(defaultHost).isEqualTo("library-example.googleapis.com:1234");
  }

  @Test
  public void testGetRequiredFields() {
    Method publishSeriesMethod = libraryService.lookupMethod("PublishSeries");
    List<String> requiredFields = protoParser.getRequiredFields(publishSeriesMethod);
    assertThat(requiredFields).containsExactly("books", "series_uuid", "shelf");
  }

  @Test
  public void testGetResourceType() {
    MessageType getShelfRequest =
        libraryProtoFile
            .getMessages()
            .stream()
            .filter(m -> m.getSimpleName().equals("GetShelfRequest"))
            .findFirst()
            .get();
    Field shelves =
        getShelfRequest
            .getFields()
            .stream()
            .filter(f -> f.getSimpleName().equals("name"))
            .findFirst()
            .get();
    String shelfType = protoParser.getResourceType(shelves);
    assertThat(shelfType).isEqualTo("google.example.library.v1.Shelf");
  }

  @Test
  public void testGetMethodSignatures() {
    Method getShelfMethod =
        libraryProtoFile
            .getInterfaces()
            .stream()
            .filter(i -> i.lookupMethod("GetShelf") != null)
            .findFirst()
            .get()
            .lookupMethod("GetShelf");
    List<MethodSignature> getShelfFlattenings = protoParser.getMethodSignatures(getShelfMethod);
    assertThat(getShelfFlattenings.size()).isEqualTo(3);

    MethodSignature firstSignature = getShelfFlattenings.get(0);
    assertThat(firstSignature.getFieldsList().size()).isEqualTo(1);
    assertThat(firstSignature.getFieldsList().get(0)).isEqualTo("name");

    MethodSignature additionalSignature = getShelfFlattenings.get(1);
    assertThat(additionalSignature.getFieldsList().size()).isEqualTo(2);
    assertThat(additionalSignature.getFieldsList().get(0)).isEqualTo("name");
    assertThat(additionalSignature.getFieldsList().get(1)).isEqualTo("message");

    MethodSignature additionalSignature2 = getShelfFlattenings.get(2);
    assertThat(additionalSignature2.getFieldsList())
        .containsExactly("name", "message", "string_builder");
  }

  @Test
  public void testEmptySignature() {
    // Test that we can detect empty method signatures.
    Method listShelvesMethod =
        libraryProtoFile
            .getInterfaces()
            .stream()
            .filter(i -> i.lookupMethod("ListShelves") != null)
            .findFirst()
            .get()
            .lookupMethod("ListShelves");
    List<MethodSignature> listShelvesFlattenings =
        protoParser.getMethodSignatures(listShelvesMethod);
    assertThat(listShelvesFlattenings.size()).isEqualTo(1);
    MethodSignature emptySignature = listShelvesFlattenings.get(0);
    assertThat(emptySignature.getFieldsList().size()).isEqualTo(0);
  }

  /** The OAuth scopes for this service (e.g. "https://cloud.google.com/auth/cloud-platform"). */
  @Test
  public void testGetAuthScopes() {
    List<String> scopes = protoParser.getAuthScopes(libraryService);
    assertThat(scopes)
        .containsExactly(
            "https://www.googleapis.com/auth/library",
            "https://www.googleapis.com/auth/cloud-platform");
  }
}
