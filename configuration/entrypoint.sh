#!/bin/bash

# Copy the Forwarding Secret
cp configuration_files/forwarding.secret ./forwarding.secret

# Update resources.json with the forwarding secret
secret=$(cat ./forwarding.secret)
jq --arg secret "$secret" '.["velocity-secret"] = $secret' ./configuration/resources.json > ./configuration/resources.json.tmp
mv ./configuration/resources.json.tmp ./configuration/resources.json

# Replace the secret in settings.yml
sed -i "s/secret: '.*'/secret: '$secret'/" ./settings.yml


# Run the Java application with optional pre-arguments and environment variables
java $PREVIEW_FLAG -jar "$JAR_FILE" $ARGS