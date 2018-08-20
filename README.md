# GameServer
This is the server component for Blasteroids. It can be used with either the local JavaFX client or the JavaScript web client.

## How It Works

The server performs the following actions:

1) Maintains the authoritative game state, which all clients connected to it must conform to
2) Updates the game state with respect to the passage of time
3) Transmits the game state to all connected clients
4) Receives user input from all connected clients

The game state is a collection of every Space Object in the game, including players, asteroids, geodes, and explosions. It also reads the saved high score file and manipulates it.

If the server is receiving no player input, the game state is updated as time goes on in an entirely predictable manner. However, it is possible to change the course of the game state via sending player input.

The transmission of data from the server is done via serializing the current game state into a Netstring and transmitting that to clients for them to parse. Transmission of data _to_ the server is done in an analogous manner.
