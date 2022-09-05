In this project, a tic tac toe game that allows two players at maximum is established. The user should try to give a ip address and a port number. If no server can be found with that ip address and port number, the user will create a server with such ip address and port number. Other player can run the same program again and enter the ip address and port number that player 1 has entered. Once the second player's connection to the server is detected, the tick tac toe game would start.

Topic Used:
Networking(sockets):
Servers are being created in this project and two players can play together.

File IO:
The user's input is read and the game results are saved to a text file.
DataOutputStream and DataInputStream are both used to interpret the postion that "X" or "O" should be placed on the board. The input is used for the player2 and the output is used for player1.

Graphic:
Three png images are put to the screen using drawImage()
If a winner is detected, a line that connects the three "O"s and "X"s will be drawn on the pannel.This indicates why a player wins the game. This is achieved by drawLine()

