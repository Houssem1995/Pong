package game;

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import network.NetworkConnection;
import network.Paquet;


public class Pong extends PongBase {
	private static final long serialVersionUID = 7657998555042629676L;
	
	private NetworkConnection sock;
	private InetAddress server_host;
	private int server_port = 6000;
	
	
	/**
	 * Programme principal
	 * 
	 * @param arg
	 */
	public static void main(String[] arg) {
		Pong jp = new Pong();
		jp.start();
	}
	
	
	/**
	 * Connexion serveur avant l'initialisation de la partie graphique
	 * et du jeu en lui m�me.
	 */
	@Override
	public void start() {
		try {
			server_host = InetAddress.getByName("localhost");
		} catch (UnknownHostException e) {
			System.err.println("Impossible de contacter le serveur : " + e);
			System.exit(1);
		}
		
		// connexion au serveur
		try{
			sock = new NetworkConnection();
		} catch (IOException e) {
			System.err.println("Erreur � la connexion : " + e.getMessage());
			System.exit(1);
		}
		
		initGUI("Pong");
		
		// un esp�ce de handshake
		try {
			sock.sendAndWaitConfirm(server_host, server_port, "HELLO");
		} catch (IOException e) {
			System.err.println("Erreur � l'envoi de la demande de connexion au serveur : " + e.getMessage());
			System.exit(1);
		}
		
		super.start();
	}
	
	/**
	 * On met � jour le jeu selon les infos transmises par le serveur
	 * 
	 * @note Sera appel�e par le thread.
	 */
	@Override
	public void run() {
		
		Paquet p;
		while (true) {
			try {
				p = sock.tryReceive(5);
			} catch (IOException e) {
				p = null;
			}
			
			if(p != null && p.getMessage() != null)
				executeCmd(p.getMessage());

			repaint();

			try {
				Thread.sleep(5); //pause pour ralentir le jeu
			} catch (InterruptedException e) {
				// rien
			}
		}
	}
	
	/**
	 * Met � jour la position du pav� du joueur 1 par rapport
	 * aux mouvements de la souris
	 * 
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 * 
	 * @param e Event li� � la souris
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		try {
			joueur2.y = e.getY() - 25;
				
			sock.send(server_host, server_port, MSG_MOVE + " P2 " + joueur2.y);
		} catch (IOException ex) {
			System.err.println("Erreur � l'envoi des coordonn�es du pav� vers le serveur : "+ ex);
		}
		
		repaint();
	}
}