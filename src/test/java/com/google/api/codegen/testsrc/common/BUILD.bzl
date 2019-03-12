# This is an API workspace, having public visibility by default makes perfect sense.
package(default_visibility = ["//visibility:public"])

##############################################################################
# Common
##############################################################################
load("//rules_gapic:gapic.bzl", "proto_library_with_info")

proto_library(
    name = "library_proto",
    srcs = ["language_service.proto"],
    deps = ["//google/api:annotations_proto"],
)

proto_library_with_info(
    name = "library_proto_with_info",
    deps = [":library_proto"],
)

##############################################################################
# Java
##############################################################################
load("@io_grpc_grpc_java//:java_grpc_library.bzl", "java_grpc_library")
load("//rules_gapic/java:java_gapic.bzl", "java_gapic_library")
load("//rules_gapic/java:java_gapic_pkg.bzl", "java_gapic_assembly_gradle_pkg")

_JAVA_GRPC_DEPS = [
    "@com_google_api_grpc_proto_google_common_protos//jar",
]

java_proto_library(
    name = "library_java_proto",
    deps = [":library_proto"],
)

java_grpc_library(
    name = "library_java_grpc",
    srcs = [":library_proto"],
    deps = [":library_java_proto"] + _JAVA_GRPC_DEPS,
)

java_gapic_library(
    name = "library_java_gapic",
    src = ":library_proto_with_info",
    gapic_yaml = "library_gapic.yaml",
    service_yaml = "library.yaml",
    test_deps = [":library_java_grpc"],
    deps = [":library_java_proto"] + _JAVA_GRPC_DEPS,
)

[java_test(
    name = test_name,
    test_class = test_name,
    runtime_deps = [":library_java_gapic_test"],
) for test_name in [
    "com.google.example.library.v1.LibraryServiceTest",
]]

# Opensource Packages
java_gapic_assembly_gradle_pkg(
    name = "google-example-library-v1-java",
    client_deps = [":library_java_gapic"],
    client_group = "com.google.cloud",
    client_test_deps = [":library_java_gapic_test"],
    grpc_deps = [":library_java_grpc"],
    grpc_group = "com.google.api.grpc",
    proto_deps = [
        ":library_java_proto",
        ":library_proto",
    ] + _JAVA_GRPC_DEPS,
    version = "0.0.0-SNAPSHOT",
)
