# Modded Bomberman

Modded Bomberman is a networked multiplayer game. It's idea is based on a classic Bomberman game and has some modifications to make it more fun to play. It has initially been created as a university project, but it has since become more of a personal project. Feel free to use it in any way you please.

## Install/Run

### Server and clients on one machine

1. Download *bomberman.zip*.
2. Unzip it.
3. Run *run_server.bat*.
4. Run at least 2 (and at most 4) instances of *run_client.bat* - number depending on how many players do you want.
5. Have fun playing!

### Server and clients each on different machine

1. Download *bomberman.zip* on each of the machines.
2. Unzip it (on each).
3. On one machine run *run_server.bat*. This machine will be the server.
4. Check the ip adress of the server.
5. On other machines do as follows:
  1. Open *run_client.bat* in a text editor.
  2. Modify the 3. line by replacing *127.0.0.1* by the ip adress of the server.
  3. Save changes.
  4. Run *run_client.bat*.
6. Have fun playing!

## Technology used

Java, NetBeans IDE 8.0

## Authors

* Paweł Wachowski
* Anna Kaliszewicz

## License

Public Domain

(for full license contents check: http://unlicense.org/UNLICENSE )
