# Pokemon-MMO

This is created as an assignment for Sunway University's Distributed System where we were required to create a multiplayer game using the framework AKKA and the language Scala. ScalaFX and JavaFX libraries were used to create the scenes.

This project uses the concept of the existing Pokemon game where the player may roam around in the world and encounter wild pokemons.
When encoutering wild pokemons, they are prompted to the battle screen where they may battle them with their own pokemons, each having
a minimum of 1 and up to 4 skills, the usage of potions allows the player to heal their injured pokemons and pokeballs allows them to
capture the wild pokemons. Trainer system has also been implemented allowing players to battle them. Among winning battles, cash
and experience are awarded, players may use cash to purchase a variety of potions and pokeballs which will aid them. Experience points on the other hand is only awarded to pokemons which participated in the won battle, they may level up and be able to learn new skills.

In addition, the game has been made into an MMO where other players may connect to the same server. They may speak by typing in the chat box or battle each other.

Instructions to run:

1) Run the sbt.bat and wait for it to load
2) Type in run and enter to launch the application
3) Make sure the server is turned on (The ip-address 127.0.0.1 is only applicable if the server app is ran on the same computer)

Instructions to play:

1) Move around using arrow keys
2) E to interact with NPCs to access houses, portals, shop, PC.. etc
3) CTRL to start typing in chat box, Enter to close it or send the typed message
4) To send battle request, select the player then press battle on the bottom right hand side

Screenshots:

Login:
![Imgur](https://i.imgur.com/SZSVlAL.png)

Ingame:
![Imgur](https://i.imgur.com/jdLeowp.png)

Shop:
![Imgur](https://i.imgur.com/EYYJ6SG.png)

PC:
![Imgur](https://i.imgur.com/z0LjU6O.png)

Healer:
![Imgur](https://i.imgur.com/DjmnAn2.png)

Battle request:
![Imgur](https://i.imgur.com/zOOWavZ.png)

In battle:
![Imgur](https://i.imgur.com/quHOFtn.png)
