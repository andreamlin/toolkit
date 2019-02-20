// /* Copyright 2017 Google LLC
//  *
//  * Licensed under the Apache License, Version 2.0 (the "License");
//  * you may not use this file except in compliance with the License.
//  * You may obtain a copy of the License at
//  *
//  *      https://www.apache.org/licenses/LICENSE-2.0
//  *
//  * Unless required by applicable law or agreed to in writing, software
//  * distributed under the License is distributed on an "AS IS" BASIS,
//  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  * See the License for the specific language governing permissions and
//  * limitations under the License.
//  */
// package com.google.api.codegen.config;
//
// import com.google.api.codegen.transformer.ImportTypeTable;
// import com.google.api.codegen.util.Name;
// import com.google.api.codegen.util.TypeName;
// import com.google.api.tools.framework.model.Field;
// import com.google.api.tools.framework.model.Oneof;
// import com.google.api.tools.framework.model.TypeRef.Cardinality;
// import com.google.common.base.Preconditions;
// import javax.annotation.Nullable;
//
// /**
//  * Wrapper class around the protobuf Field class and the Discovery-doc Schema class.
//  *
//  * <p>This class abstracts the format (protobuf, discovery, etc) of the source from a resource type
//  * definition.
//  */
// public class ProtoOneofModel implements FieldModel {
//   private final Oneof oneof;
//   private final ProtoTypeRef protoTypeRef;
//
//   /* Create a FieldModel object from a non-null Field object. */
//   public ProtoField(Oneof oneof) {
//     Preconditions.checkNotNull(protoField);
//     this.protoField = protoField;
//     this.protoTypeRef = ProtoTypeRef.create(protoField.getType());
//   }
//   String getSimpleName();
//
//   String getFullName();
//
//   /* Return the name of this field when it is a parameter to an RPC method. */
//   String getNameAsParameter();
//
//   /* Return the name of this field when it is a parameter to an RPC method. */
//   Name getNameAsParameterName();
//
//   String getTypeFullName();
//
//   /* @return if the underlying resource is a map type. */
//   boolean isMap();
//
//   /* @return if the underlying resource is a proto Messsage. */
//   boolean isMessage();
//
//   /* @return if the underlying resource is String-typed. */
//   boolean isString();
//
//   /* @return if the underlying resource is byte-typed. */
//   boolean isBytes();
//
//   /* @return if the underlying resource can be repeated in the parent resource. */
//   boolean isRepeated();
//
//   /* @return if this field is required to be non-null in the parent object. */
//   boolean isRequired();
//
//   /* @return if this parameter may be in a ResourceName. */
//   boolean mayBeInResourceName();
//
//   /* @return the full name of the parent. */
//   String getParentFullName();
//
//   /* @return the simple name of the parent. */
//   String getParentSimpleName();
//
//   /* @return the parent of this model, if the parent is a FieldModel, else return this object. */
//   TypeName getParentTypeName(ImportTypeTable typeTable);
//
//   /* @return the cardinality of the resource. */
//   Cardinality getCardinality();
//
//   /* @return if this resource is an enum. */
//   boolean isEnum();
//
//   /* @return if this is a primitive type. */
//   boolean isPrimitive();
//
//   /* Get the description of the element scoped to the visibility as currently set in the model. */
//   String getScopedDocumentation();
//
//   // Functions that are specific to the source type.
//
//   /* @return the type of this object, formatted as a String. */
//   String getKind();
//
//   @Nullable
//   Oneof getOneof();
//
//   TypeModel getType() {
//
//   }
// }
