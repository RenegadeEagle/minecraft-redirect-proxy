# Minecraft Redirect Proxy
This is a project that allows users to setup a proxy where any minecraft client can connect to any remote host. This project is written in two parts. One using default java sockets, and the other using a netty implementation. Do not use the sockets implementation, it is unfinished, and is horribly inefficent.

This project would be useful for individuals who want to hide their IP address using a remote server, or possibly allowing an extra layer of filtering for server administrators. 
## Usage
To compile and use the netty version, build with dependencies using maven
```
mvn clean compile assembly:single
```
Run the jar and edit config.json accordingly.

##Configuration
```
{
  "versionName": "ProxyCup",
  "maxPlayers": 1337,
  "onlinePlayers": 133,
  "motd": "Couldnt connect to requested backend server. If you believe this to be an issue, contact the administrator of this proxy.",
  "port": 22000,
  "nodes": [
    {
      "hostname": "localhost",
      "remoteHostname": "mc.hypixel.net",
      "remoteHostPort": 25565
    },
    {
      "hostname": "127.0.0.1",
      "remoteHostname": "mc.arkhamnetwork.org",
      "remoteHostPort": 25565
    }
  ]
}
```

| Option        | Info         | Type  |
| ------------- |:-------------:| -----:|
| versionName     | The custom version name if connecting to backend server fails | String |
| maxPlayers     | Max players if connecting to backend server fails.      |  Integer |
| onlinePlayers | Online players if connecting to backend server fails.          |    Integer |
| motd | MOTD of server if connecting to backend server fails.          |    String |
| port | Port that the proxy server should listen on.          |    Integer |
| Nodes | An array of nodes which specify how to redirect requests based on the hostname of the server the client is trying to connect too.        |     |



