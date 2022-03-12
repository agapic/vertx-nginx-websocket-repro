# Vertx Websocket + Nginx Bug Reproducer Code
A simple WebSocket server in Vert.x.

* Switch to whichever Vert.X version you need.
* Logs all requests and headers.
* Prints all messages sent to it
* Small and dependency-free
* You can force response codes with the `x-rto-test-response` header

## Prerequisites
* `sudo npm install -g wscat` for creating websocket connections from CLI
* Docker
* Java 11

## Purpose
This is how to get a repro of the issue with Vert.X 3.9.12 or higher where after a certain number of connections the connectivity hangs.

It involves starting a local **nginx** proxy with configuration that mirrors what you would see from a running NGINX pod on Kubernetes

## Start a Local NGINX Proxy

Step 1: Build the JAR
```
mvn package
```

Step 2: Get your IP address of your local computer on the network
```
ifconfig en0 inet 
```

Step 3: Edit 'nginx.conf' and replace the upstream with your local IP address (mine was server 192.168.88.38). Make sure port 8080 is open!

Step 4: Run ./nginx.sh which will download, install, and launch NGINX on port **9090**

## Run the reproducer (local websocket server)

**Run the java code**
```bash
mvn exec:java
```

**Try a websocket connection via NGINX** (this reproduces the bug)
```
./force-response.sh 403 ws://localhost:9090/ws
```
And then in a separate terminal try connecting with a 200. Your connection will hang.
```
./force-response.sh 200 ws://localhost:9090/ws
```

**Try a websocket connection directly without NGINX** (no issues!)
```
./force-response.sh 403 ws://localhost:8080/ws
```

And then in a separate terminal try connecting with a 200. No issues because the connection objects are different
```
./force-response.sh 200 ws://localhost:9090/ws
```

## Vert.X settings which help
Vert.X keeps a socket open until its cleanup timer comes by on rejected/closed WebSockets. That means you can still
send messages to a closed websocket (see Jetty issues for similar root cause).

You can set the timeout to 0 to force close TCP connection immediately.

See the comments in Server.java

## Why does the Nodejs "ws" package not have the bug?
Theory: the Nodejs websocket package explicitly sends the Connection: close header which might force nginx **or the client** to
close the connection in its upstream cache.

## Why does Istio / Envoy Proxy work
Theory: According RFC - a rejected Websocket upgrade is any response other than a 200.
Envoy is protocol-aware and may close the connection (Connection Close) on non-2xx responses.