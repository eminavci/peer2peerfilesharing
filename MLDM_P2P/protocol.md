# Vocabulary
**Stuff:** The actual data to share. Can be one file, or a folder containing several files/folders. Each file will be split into books.

**Library:** "Torrent file" in BitTorrent. This is a description of the stuff to exchange. The file extension .libr is used.

**Book:** "Piece" in BitTorrent. This represents a part of a file of a stuff to download. This is the unit of exchange: players exchange books. The books are all of the same size, except the last one (if the file size is not a multiple of the book size).

**Player:** "Peer" in BitTorrent. This is a program exchanging stuff with others players.

**Hub:** "tracker" in BitTorrent. This is a program that maintains an updated list of all players exchanging the stuff.

# Protocol definitions of Peer to Peer File Sharing Application
Our Application enables clients (peers or players) to share files among each other with the help of tracker (server) which is basically called hub. File sharing is going to be implemented on socket communication using TCP network protocol. There are two sub applications in our project:
- A Tracker Server (Hub) 
- Client (Peer, actually both client and a server named as Players)

## What does Hub Server do?
A Hub server (Tracker) is going to work in 2 different modes: Mode-1 and Mode-2.

**Mode-1:** In this mode, Tracker only keep list of addresses of players(peers) which are available to share or download files without knowing which player has which file and book of file. If a player asks to download a file, it makes a connection over TCP to the hub and ask some available address which it can connect to download file. The Hub (Server) randomly selects and returns a sub list of available peers. The player (peer) sends request to these players. If requested client has the book to deliver, the sharing will starts otherwise it will send an appropriate error-code so that the peer (player) will not ask it again. In this mode, Hub (Server) periodically checks the peers to keep the list of available peers updated.

**Mode-2:** In this mode, The Hub can handle players who are dealing with multiple library files. That is why the hub keeps the list of players with library information. A player ask list of players from Hub by sending the libary file information. And Hub returns the list of players who can have data for this library.

We are planning to implement the feature of asking which peer has which books (piece of file) on player (peer) side instead of on Hub (Server), since we think that it can make extra load on Hub (Server).  But if it is required, we can also add this feature on Hub (Server) as Mode-3.

## What a Player (Peer) does?
Each player knows the address of Hub (Server). They get information of other players from Hub (Server). When a connection starts between players, they try to assign some unique ids to each other so that they will not get confused about which files are shared. For example there can be multiple file shared between 2 players.

## Library file
This file is going to be created by player who wants to share data. The file content type is simply JSON, since we think that it will be easy for us to take this information in application level. The size of books for each stuff (file) is 256 KB. The file will include the following information:
1. Name (file name)
2. Description (some description for file, it can be used for search maybe)
3. hubIp
4. hubPort
5. Number-of-Books
6. Number-of-Files
7. Book-Size
8. List-Of-Books (for each file)

**Note1:** In our API, the data is serialized and mapped by Jackson Java API. For the communication messages you can use null setting for the attribute in json string. For example if there is no content of .libr file, then use content=null

**Note2:**  For managing to separate file in chunks (books) we use Java RandomAccessFile class.


# Protocol implementation
### Note
- **Mode-1** - Single Hub, single Library
- **Mode-2** - Central Hub, multiple Library


### Syntax of the .libr file
```
{
  "hubIp" : "127.0.0.1",
  "hubPort" : "54321",
  "name" : "Batman",
  "bookSize" : "25600000",
  "numberOfFiles" : "3",
  "size" : "4399511612",
  "files" : [ {
    "name" : "Batman/01.Batman Begins.avi",
    "size" : "1466669077",
    "numberOfBooks" : "58",
    "books" : [ {
      "index" : "1",
      "size" : "25600000",
      "shaCode" : "af3d4f47b6a70631e69d623478466315a9d524eb"
    }, {
      "index" : "2",
      "size" : "25600000",
      "shaCode" : "bf111321afc004b1ed4b0062c41bbfcf3a02014b"
    },
    ...,
       {
      "index" : "58",
      "size" : "7469077",
      "shaCode" : "bcc7869b9c5d00dff4df65c73ee0ba5bb925f491"
    } ]
  }, {
    "name" : "Batman/02.The Dark Knight.avi",
    "size" : "1466251284",
    "numberOfBooks" : "58",
    "books" : [ {
      "index" : "1",
      "size" : "25600000",
      "shaCode" : "eadc29b4b00349dc7a8213c3f4b259a2076588e7"
    }, {
      "index" : "2",
      "size" : "25600000",
      "shaCode" : "99264bd5340e6fb5fafe7b869feb52c77724d220"
    }, 
    ...,
       {
      "index" : "58",
      "size" : "7051284",
      "shaCode" : "c5638e75681d945c1b3e40bc8c1aa5efa2727276"
    } ]
  }, {
    "name" : "Batman/03.The Dark Knight Rises.avi",
    "size" : "1466591251",
    "numberOfBooks" : "58",
    "books" : [ {
      "index" : "1",
      "size" : "25600000",
      "shaCode" : "08f0195061d683c722575136b72208c8baed25a3"
    }, {
      "index" : "2",
      "size" : "25600000",
      "shaCode" : "c2224d4aa2292dad22636fb5b3a47642c0dfbca7"
    }, 
    ...,
       {
      "index" : "58",
      "size" : "7391251",
      "shaCode" : "997ad81a258cbc1087840285621b98c94a8a5a5f"
    } ]
  } ]
}
```

**Note 3:** All the following text messages in JSON have to be sent in only one line (it allows to simplify the implementation since we just have to read one line to obtain one message). They are represented in this protocol in several lines only to make it readable and understandable.

## 1. Communications between a player and a hub
### 1.1. Player introduce himself to the hub.
A player sends a request to inform the Hub that he wants only to share the stuff handled by this hub (not download). For this, the client gives his connection information.
```
{
    "command":"UPLOADING",
    "port":12345,
    "object":"library name"
}
```

### 1.2. Player wants to connect to other players.
A player sends a request to inform the Hub that he wants both to download and upload the stuff handled by this hub.
```
{
    "command":"DOWNLOADING",
    "port":12345,
    "object":"library name"
}
```

### 1.3. Hub send a list of other players
As a response to the message 1.2, the hub gives connection information of a subset of the other players that are also sharing this stuff.
```
{
    "command":"HUB_RESPONSE",
    "players":
    [
        {"ip":"192.168.168.168","port":54687},
        ...,
        {"ip":"2.18.16.1","port":12345}
    ]
}
```

### 1.4. Keep alive
Periodically, the Hub checks the validity of the players with an empty TCP request. If it is alive, no error will occur. The Hub just opens a TCP connection to each player. If no exception occurs, he closes the socket, otherwise, he removes the player from the list of available players.

### 1.5. Complaining message from Player To Hub
If a player can not connect to another player, he complain to hub about that. After that, hub received the information of complained Player and check if it is true or not
```
{
    "command":"PLAYER_COMPLAIN",
    "object": 
    {
    	"ip":"168.55.55.2",
    	"port": 48755
    }
}
```


## 2. Communications between two players
### 2.0. Player open a TCP connection to another player
Before sending any of the messages described below to another player, a player as to send the connection message just after opening the socket. For the other player to know who is contacting him, the first one sends his server socket port. By doing this, the two players know that they are connected between each other. And if the Hub sends the connection IP and socket server port of the other player, they can ignore it because they know they have an active connection with this player of the given IP and socket server port by looking at their list of active connection.
```
{
    "command":"PLAYER_CONNECTION",
    "port":12345
}
```
### 2.1. Asking the list of books
A first player requests the list of books available to a second player.
```
{
    "command":"REQUEST_AVAILABLE_BOOKS",
    "object":["librName1", "librName2" ..]
}
```

### 2.2 Sending the list of books
The second player answer to the first one the list of books available for each file.
```
{
    "command":"RESPONSE_AVAILABLE_BOOKS",
    "object":
    {"librName2":{"libr2File1Name":[1,2,3,4,5,6,7,8,9,10],"libr2File2Name":[1,2,3,4,5,6,7,8,9,10]},
    "librName1":{"libr1File1Name":[1,2,3,4,5,6,7,8,9,10],"libr1File2Name":[1,2,3,4,5,6,7,8,9,10]}}
    }
```

### 2.3. Downloading
A first player requests to a second player to download  books of a file.
```
{
    "command":"DOWNLOADING_BOOK",
    "object":
    {"librName2":{"libr2File1Name":[1,2,3,4,5],"libr2File2Name":[6,7,8,9,10]},
    "librName1":{"libr1File1Name":[3,4,7,8,9],"libr1File2Name":[8,9,10]}}
    }
}
```
### 2.4. Uploading
The second player sends a message to inform the first player that he will upload a book. And then send the book itself as bytes.
```
{
    "command":"UPLOADING_BOOK",
    "object":
    {
	    "stuffName": "librName1",
	    "fileName": "libr1File1Name"
	    "bookNumber": 7,
	    "size": 256000
    }
}
Just after sending this message, the player sends the bytes of the book.
After this make your validation: find the SHA1 from the received book and compare it with the one coming from the .libr file
```

## Socket implementation
All connections have to be done with TCP socket.

For sending the message 1.1, the Player opens a TCP socket to the Hub, send the message and immediately close the socket.

For sending the message 1.2, the Player opens a TCP socket to the Hub, send the message, wait to receive the message 1.3 containing the response from the Hub and then close the socket.

Between two players A and B, we keep the TCP connections opened. 
- First player A obtains the connection information of B (IP and socket server port) from the Hub.
- Then A create a socket to B.
- Then A immediately send the message 2.0 to B such that B knows the connection information about A.
- Finally, A and B store the socket linking them in their list of active connections. When they want to send a message among 2.1, 2.2, 2.3, or 2.4 they use the existing socket. And at the same time, they launch a thread to continuously listen to the socket for any incoming 2.1, 2.2, 2.3 and 2.4 messages.
