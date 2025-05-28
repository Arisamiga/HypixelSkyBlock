#!/bin/bash

# Copy the Forwarding Secret
cp configuration_files/forwarding.secret ./forwarding.secret

# Update resources.json with the forwarding secret
secret=$(cat ./forwarding.secret)
jq --arg secret "$secret" '.["velocity-secret"] = $secret' ./configuration/resources.json > ./configuration/resources.json.tmp
mv ./configuration/resources.json.tmp ./configuration/resources.json

# Replace the secret in settings.yml
sed -i "s/secret: '.*'/secret: '$secret'/" ./settings.yml

# Start supervisord to manage the services
exec /usr/bin/supervisord -c ./supervisord.conf

# Wait for supervisord to start
sleep 5

# Start specific services using supervisorctl
supervisorctl start skyblockcore_island
supervisorctl start skyblockcore_hub
supervisorctl start skyblockcore_farming
supervisorctl start nanolimbo
supervisorctl start service_api
supervisorctl start service_auctionhouse
supervisorctl start service_bazaar
supervisorctl start service_itemtracker

# Keep the container running
tail -f /dev/null