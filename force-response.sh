#!/bin/bash

response=$1
url=$2

request_id=$(uuidgen)
counter=0

echo "${request_id} looping ${response} once-per-second"
while [[ 1 == 1 ]]
do
wscat \
  --connect ${url} \
  --header "x-request-id: ${request_id}.${counter}" \
  --header "x-rto-test-response: ${response}"

sleep 1
let counter=counter+1
done