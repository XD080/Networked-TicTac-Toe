/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package finaltictactoe;
import java.util.Scanner;
import java.awt.*;
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.awt.event.*;
import javax.imageio.*;
/**
 *
 * @author Sicheng Xie
 */
public class FinalTicTacToe extends Thread{
        private String ip;
	private int port;
	private JFrame frame;
        
	private int WIDTH = 540;
	private int HEIGHT = 540;

	private drawPanel panel;
	private Socket socket;
	private DataOutputStream output;
	private DataInputStream input;
        
        private String identity;
        
	private ServerSocket serverSocket;

	private Image board;
	private Image PlayerCircle;
	private Image EnemyX;

	private boolean yourTurn = false;//boolean checks which player's turn
	private boolean usingCircle = true;//boolean checks whether X or O should be used
	private boolean socketAccepted = false;//check if connection is established
        
	private boolean won = false;
	private boolean lost = false;
	private boolean tie = false;

	private int leftStart = 0; // draw a line at where three O or three X are placed by the user
	private int rightEnd = 0;
        
        private String[] gameBoard = new String[9];//A list with a length of 9 that represent the board
	private int[][] winCondition = new int[][] { { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 }, { 0, 3, 6 }, { 1, 4, 7 }, { 2, 5, 8 }, { 0, 4, 8 }, { 2, 4, 6 } };// all possible win conditions' locations where a winner can be found

	public FinalTicTacToe() {
            //load required images
            try {
                board = ImageIO.read(getClass().getResource("board.png"));
                PlayerCircle = ImageIO.read(getClass().getResource("playerCircle.png"));
                EnemyX = ImageIO.read(getClass().getResource("opponenentX.png"));
            } catch (IOException e) {
                System.out.println("Image Not Found");
            }
            
		Scanner myObj = new Scanner(System.in);  // Create a Scanner object
                System.out.println("Please Enter A IP Address");
                ip = myObj.nextLine();  // Read user input
                while (!validIP(ip)) {
			System.out.println("Please Try A Different IP Address");
			ip = myObj.nextLine();
		}
                System.out.println("Please Enter A Port Number");
                port = myObj.nextInt();
		while (!((port > 1) & (port < 65535))) {
			System.out.println("Please Try A Different Port Number");
			port = myObj.nextInt();
		}

                
		panel = new drawPanel();
		panel.setSize(new Dimension(WIDTH, HEIGHT));

		if (!connect()) initialingServer();// if server can't connected, create one on its own

		frame = new JFrame();
                frame.setResizable(false);
		frame.setContentPane(panel);
		frame.setSize(WIDTH, HEIGHT);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
                frame.setTitle(identity);
                
                
                new Thread(this) {
                    public void run() {
                        while (! won || !lost || !tie) {
                            turnChange();
                            panel.repaint();
                            if (!usingCircle && !socketAccepted) {
                                AwaitingResponse();
                            }
                        }
                    }
                }.start();
               
                
	}

	public static void main(String[] args) {
		FinalTicTacToe game = new FinalTicTacToe();
	}
        
        //Write the final result to Result.txt
        private void WriteToTheFile(String res)
        {
            try {
                File myObj = new File("Result.txt");
                if (myObj.createNewFile()) {
                    System.out.println("Result.txt created to store result");
                    FileWriter myWriter = new FileWriter("result.txt");
                    myWriter.write(identity + " " + res + "\n");
                    myWriter.close();
                } else {
                    System.out.println("Game result will be stored into Result.text");
                    FileWriter myWriter = new FileWriter("result.txt",true);
                    if (res == "TIE")   
                        myWriter.write(res + "\n");
                    myWriter.write(identity + " " + res + "\n");
                    myWriter.close();
                }
            } catch (IOException e) {
                System.out.println("An error occurred.");
            }
        }
        
        //check if the given IP address is valid
        private boolean validIP(String ip)
        {
            String format = "(\\d{1,2}|(0|1)\\d{2}|2[0-4]\\d|25[0-5])";
            String pattern = format + "\\."+ format + "\\."+ format + "\\."+ format;
            return (ip.matches(pattern) || ip.matches("localhost"));
        }
        
        //operating the game, will be called in the drawpanel class
        private void gameOperating(Graphics g) {
        g.drawImage(board, 0, 0, null);
        Graphics2D g2 = (Graphics2D) g;
        g.setFont(new Font("Serif", Font.BOLD, 50));
        if (socketAccepted) {
            // place X or O according to the players (JAVA Graphic Used)
            for (int i = 0; i < gameBoard.length; i++) {
                if (gameBoard[i] != null) {
                    if (gameBoard[i].equals("X")) {
                        if (usingCircle) {
                            g.drawImage(EnemyX, (i % 3) * 180 + (i % 3), (int) ((i / 3) * 180 + (i / 3)), null);
                        } else {
                            g.drawImage(PlayerCircle, (i % 3) * 180 + (i % 3), (int) ((i / 3) * 180 + (i / 3)), null);
                        }
                    } else if (gameBoard[i].equals("O")) {
                        if (usingCircle) {
                            g.drawImage(PlayerCircle, (i % 3) * 180 + (i % 3), (int) ((i / 3) * 180 + (i / 3)), null);
                        } else {
                            g.drawImage(EnemyX, (i % 3) * 180 + (i % 3), (int) ((i / 3) * 180 + (i / 3)), null);
                        }
                    }
                }
            }
            
            if (won || lost) {
                g.setColor(Color.BLACK);
                // draw a straight line that connects the streak with three same elements
                g.drawLine(leftStart % 3 * 180 + leftStart % 3 + 180 / 2, (int) ((leftStart / 3) * 180 + (leftStart / 3) + 180 / 2), rightEnd % 3 * 180 + rightEnd % 3 + 180 / 2, (int) ((rightEnd / 3) * 180 + (rightEnd / 3) + 180 / 2));
      
                if (won) {
                    g.setColor(Color.RED);
                    int sentenceLength = g2.getFontMetrics().stringWidth("You Won");
                    g.drawString("You Won", WIDTH / 2 - sentenceLength/2 , HEIGHT / 2);
                } else if (lost) {
                    g.setColor(Color.RED);
                    int stringWidth = g2.getFontMetrics().stringWidth("You Lost");
                    g.drawString("You Lost", WIDTH / 2 - stringWidth/2, HEIGHT / 2);
                }
            }
            
            if (tie) {
                g.setColor(Color.RED);
                int sentenceLength = g2.getFontMetrics().stringWidth("Tie");
                g.drawString("Tie", WIDTH / 2 - sentenceLength/2 , HEIGHT / 2);
            }
        }
        // if player hasnt appeared, having the program await
        else {
            g.setColor(Color.RED);
            g.setFont(new Font("Serif", Font.BOLD, 50));
            int sentenceLength = g2.getFontMetrics().stringWidth("Awaiting Player");
            g.drawString("Awaiting Player", WIDTH / 2 - sentenceLength/2,HEIGHT / 2);
        }

    }
        // opponent's turn operation
	private void turnChange() {
		if (!yourTurn) {
			try {
				int space = input.readInt();
				if (usingCircle) gameBoard[space] = "X";
				else gameBoard[space] = "O";
				checkXWins();
				checkForTie();
				yourTurn = true;
                                if(won) WriteToTheFile("WON");
                                if(lost) WriteToTheFile("LOST");
                                if(tie) WriteToTheFile("TIE");
			} catch (IOException e) {
				
			}
		}
	}
        // check if player has won
	private void checkOWins() {
		for (int i = 0; i < winCondition.length; i++) {
			if (usingCircle) {
				if (gameBoard[winCondition[i][0]] == "O" && gameBoard[winCondition[i][1]] == "O" && gameBoard[winCondition[i][2]] == "O") {
					leftStart = winCondition[i][0];
					rightEnd = winCondition[i][2];
					won = true;
				}
			} else {
				if (gameBoard[winCondition[i][0]] == "X" && gameBoard[winCondition[i][1]] == "X" && gameBoard[winCondition[i][2]] == "X") {
					leftStart = winCondition[i][0];
					rightEnd = winCondition[i][2];
					won = true;
				}
			}
		}
           
    }
        //check if oppoenent has won
	private void checkXWins() {
		for (int i = 0; i < winCondition.length; i++) {
			if (usingCircle) {
				if (gameBoard[winCondition[i][0]] == "X" && gameBoard[winCondition[i][1]] == "X" && gameBoard[winCondition[i][2]] == "X") {
					leftStart = winCondition[i][0];
					rightEnd = winCondition[i][2];
					lost = true;
                        
				}
			} else {
				if (gameBoard[winCondition[i][0]] == "O" && gameBoard[winCondition[i][1]] == "O" && gameBoard[winCondition[i][2]] == "O") {
					leftStart = winCondition[i][0];
					rightEnd = winCondition[i][2];
					lost = true;
                                    
				}
			}
		}
                
      
	}
        //check if it is a tie
	private void checkForTie() {
                if (!won & !lost)
                {
                    for (int i = 0; i < gameBoard.length; i++) {
                            if (gameBoard[i] == null) {
                                    return;
                            }
                    }
                    tie = true;
                }
	}
        
          //creating a socket server
	private void initialingServer() {
		try {
			serverSocket = new ServerSocket(port,8,InetAddress.getByName(ip));
                        
		} catch (IOException e) {
			
		}
		yourTurn = true;
		usingCircle = false;
	}
      
        
        //connceting player2 to existing server
	private boolean connect() {
		try {
			socket = new Socket(ip, port);
                        input = new DataInputStream(socket.getInputStream());
			output = new DataOutputStream(socket.getOutputStream());
			socketAccepted = true;
                        System.out.println("Successfully connected to the server.");
                        identity = "Challenger";
		} catch (IOException e) {
			System.out.println("Server: IP ADDRESS: " + ip + " Port number: " + port + " Not Found - Creating Own Server");
                        identity = "ServerHolder";
			return false;
                }
		return true;
	}
        
          //awaiting player2 to join
	private void AwaitingResponse() {
		Socket socket = null;
		try {
			socket = serverSocket.accept();
			output = new DataOutputStream(socket.getOutputStream());
			input = new DataInputStream(socket.getInputStream());
			socketAccepted = true;
			System.out.println("A Client Has Joined Your Server");
         
		} catch (IOException e) {
			
		}
	}
        
      
	private class drawPanel extends JPanel implements MouseListener {
		public drawPanel() {
			addMouseListener(this);
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			gameOperating(g);
		}

		@Override
		public void mouseClicked(MouseEvent me) {
			if (socketAccepted) {
				if (yourTurn && !won && !lost) {
                                        // get the approimate location and then convert into a range of 1-9
					int position =  me.getX() / 180 + me.getY() / 180 *3;
					if (gameBoard[position] == null ) {
						if (usingCircle) 
                                                    gameBoard[position] = "O";
						else 
                                                    gameBoard[position] = "X";
						yourTurn = false;
						repaint();
						try {
							output.writeInt(position);
						} catch (IOException ex) {
						}
						checkOWins();
						checkForTie();

					}
				}
                                
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {

		}

		@Override
		public void mouseReleased(MouseEvent e) {

		}

		@Override
		public void mouseEntered(MouseEvent e) {

		}

		@Override
		public void mouseExited(MouseEvent e) {

		}

	}}
