###about organization of the work, technology, etc
The programming language we used to implement P2P file sharing project is Java.
We used maven which is a software project management and comprehension tool in order to be able to handle our dependencies.
We used couple of external libraries such as jackson and apache-commons.

###compared to our objectives: what works, what does not work yet
We strongly believe that we coded all requirements even more than what was asked in project.
Our project is able to run in 2 mode.
- **Mode-1** - Single Hub, single Library
- **Mode-2** - Central Hub, multiple Library

We tested Mode-1 and Mode-2 and it is working successfully. All tasks are achieved successfully
However we haven't  made a test Mode-2 with with multiple Library yet.
We would like to write a function for the status of downloading but we did not have time for that.

### the difficulties we faced
The main difficulty we faced was the ambiguity of project instructions for meta group. Because some parts were optional some compulsory. Since this effect the structure of our messages between player-player and player-hub, we had disagreements with meta groups. We did not know what aim was expected and what to do with meta group in project defense until a week before project delivery.

The second difficulty we had was dealing with the problem of synchronizing multiple threads for most of time for a single resource and the understanding of buffered data received and sent over a socket. Furthermore, we have a added some extra features which was not asked in project( For example, we keep history of downloading process. If player stop in the middle of downloading and restart, the process will continue from where it was stopped). This made our project a bit complex.


### The lessons we learned
We have understood the motivation in the team is quite important and keeping coding as simple as is very important for further. That's why we deleted all our code and re-write 2 times in order to keep it simple.

###What we would keep for next time
We have learned how to use threads and what we can do with threads. 
We have also learned socket programming(for example, how an online game can be implemented over socket communication)
 and the structure of TCP.
We learned some software development methodologies for example keeping the components of your porject to be less depended on each other.

###what we would do differently
We could not organize the parts to be implemented among team members. That's why the development cycle was not as efficient as we expected.  We will do task sharing according the members skills and abilities.


