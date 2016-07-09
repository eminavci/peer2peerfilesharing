##### net-team-b
##Peer-to-Peer(P2P) File Sharing

#####  What is it?
P2P file sharing is the process of sharing and transferring digital files from one computer to another. In a P2P network, each ‘peer’ is an end-user’s computer connected to the other ‘peer’ via the Internet – without going through an intermediary server. P2P program is an efficient way to share large files with others, such as personal video recordings or large sets of photos. P2P is also used to facilitate direct communications between computer or device users.

Peer-to-Peer File Sharing is a project implemented in Java language.  The project doesn't provide an interface. You can use it from console by executing the project jar. You can share your files and also you can receive the files which are shared by other peers.
Below images describe the architecture of our project in Mode-1 and Mode-2 respectively.


**Architecture for Mode-1**
![Alt text](http://s7.postimg.org/soc94mb6j/mode1.png "Architecture for Mode-1")

**Architecture for Mode-2**
![Alt text](http://s7.postimg.org/6qfsatw63/mode2.png "Architecture for Mode-2")

<br />

##### How to use?
Before starting to explain how to use our program, you should know what the mode represent in our program. Our program provide user to do upload/download data in 2 mode.
- **Mode-1** - Single Hub(Server), single Library
- **Mode-2** - Central Hub(Server), multiple Library

You can use the program by executing the Player.java and Hub.java classes from source code or calling the commands below over the jar(p2p.jar) you received from project build.

**Start a Hub Server from command line **
the parameters 1 and 42000 are mode and port number respectively.

```
java -cp myjar.jar com.bouncers.b2b.hub.Hub 1 42000
```

**Start a Player from command line**
the parameters 1 and 42000 are mode and port number respectively.
```
java -cp myjar.jar com.bouncers.b2b.player.Player 1 42100
```

After you have a running player instance, you can type the commands below to do what you want.

**1.) Upload a library**
In order to keep things simple, the user on the same machine with hub upload here. That is why there is no ip address for parameters. When user type the command below, a file whose extension is _.libr_ and name is _someFile.libr_ will be created on Desktop and the player instance will connect to the hub(server) running on 42000

```
/home/avci/mydocs/someFile 42000
```

**2.) Download Files**
The user uploading give .libr file to another player on another machine. This user will type the the command below to start downloading process.
```
C:\user\locationoflibrfile\someFile.libr
```

Even if the user stop the player, the history will be saved and after restarting the download process will continue from where it was stopped.

**3.) help and quit**
To see instructions and to quit the player program you can respectively type 'help' and 'quit'


 
