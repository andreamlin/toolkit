FROM openjdk-8-jre

# Install system packages.
RUN apt-get update \
  && apt-get install -y --no-install-recommends \
    pandoc \
  && rm -rf /var/lib/apt/lists/*

# Add protoc and our common protos.
COPY --from=gcr.io/gapic-images/api-common-protos:beta /usr/local/bin/protoc /usr/local/bin/protoc
COPY --from=gcr.io/gapic-images/api-common-protos:beta /protos/ /protos/

# Add our code to the Docker image.
ADD . /usr/src/gapic-generator/

# Install the tool within the image.
RUN gradle build /usr/src/gapic-generator

# Define the generator as an entry point.
ENTRYPOINT ["/usr/src/gapic-generator/docker-entrypoint.sh"]