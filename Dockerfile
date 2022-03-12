FROM amazoncorretto:11

ARG build_number=1
ENV VERTICLE_FILE vertx-reproducer-0.0.$build_number-fat.jar

# Used to provide remote debugging args in test environment.
# We need both an ARG and an ENV variable because the ARG is used to pass in the java_args in the "docker build"
# command, but it can only be used at build time. The ENV is needed at runtime for use in the CMD.
ARG java_args=""
ENV JAVA_ARGS $java_args

# Set the location of the verticles
ENV VERTICLE_HOME /usr/verticles

# Copy your fat jar to the container
COPY target/$VERTICLE_FILE $VERTICLE_HOME/

# Launch the verticle
WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java $JAVA_ARGS -jar $VERTICLE_FILE"]