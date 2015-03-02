package GBall.Server;

import java.awt.event.*;
import java.io.IOException;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

//import GBall.Client.GameWindow;
import GBall.Shared.Const;
import GBall.Shared.EntityManager;
import GBall.Shared.GameEntity;
import GBall.Shared.Listener;
import GBall.Shared.MsgData;
import GBall.Shared.ScoreKeeper;
import GBall.Shared.Vector2D;

public class World extends Thread
{

	public static final String SERVERIP = "127.0.0.1"; // 'Within' the emulator!
	public static final int SERVERPORT = 25001;

	private static class WorldSingletonHolder
	{
		public static final World instance = new World();
	}

	public static World getInstance()
	{
		return WorldSingletonHolder.instance;
	}

	private double m_lastTime = System.currentTimeMillis();
	private double m_actualFps = 0.0;

	private DatagramSocket m_socket;
	private Listener m_listener;
	private ArrayList<ClientConnection> m_clients = new ArrayList<ClientConnection>();

//	private GameWindow m_gameWindow = new GameWindow("Server");

	private World()
	{

	}

	public void process()
	{
		initBall();		// Ball must be initiated first

		// Marshal the state
		try
		{
			// ByteArrayOutputStream baos = new ByteArrayOutputStream();
			m_socket = new DatagramSocket(SERVERPORT);
			m_listener = new Listener(m_socket);
			m_listener.start();

			// ObjectOutputStream oos = new ObjectOutputStream(baos);
			// oos.writeObject(new MsgData());
			// oos.flush();
			//
			// byte[] buf = new byte[1024];
			//
			// buf = baos.toByteArray();
			//
			// DatagramPacket pack = new DatagramPacket(buf, buf.length,
			// m_serverAddress, SERVERPORT);
			// m_socket.send(pack);

		} catch (IOException e)
		{
			e.printStackTrace();
		}
		
		MsgData msg;
		
		
		int count = 0;
		while (true)
		{
			count++;
			if ((msg = m_listener.getMessage()) != null)
			{
				//System.out.println(msg.debugInfo());
				ClientConnection c = findClient(msg);
				if (c != null)
				{
					try
					{
						// Get player ID
						int shipID = msg.getInt("ID");
						
						EntityManager.getInstance().setAcceleration(shipID, msg.getDouble("acceleration"));
						EntityManager.getInstance().setRotation(shipID, msg.getInt("rotation"));
						c.m_lastUpdate = msg.getTimestamp();
					} catch (NullPointerException e)
					{
						// Do nothing;
					}
				} else
				{
					// A player has requested to join the game
					ClientConnection newCC = new ClientConnection(msg.m_address, msg.m_port, m_socket); 
					addClient(newCC);
					
					// Create a ship and distribute the ship information to all clients
					MsgData shipInfo = addNewPlayer();
					newCC.sendMessage(shipInfo.toString());
				}
			}
			if (newFrame())
			{
				System.out.println(count);
				count = 0;
				EntityManager.getInstance().updatePositions();
				EntityManager.getInstance().checkBorderCollisions(Const.DISPLAY_WIDTH, Const.DISPLAY_HEIGHT, true);
				EntityManager.getInstance().checkShipCollisions();
//				m_gameWindow.repaint();
				MsgData stateMsg = packState(EntityManager.getState()); 
				broadcast(stateMsg);
				String diffTimes = "";
				long currTime = System.currentTimeMillis();
				for(Iterator<ClientConnection> itr = m_clients.iterator(); itr.hasNext(); )
				{
					diffTimes = diffTimes + String.format("%04d ", new Integer((int)(currTime - itr.next().m_lastUpdate))); 
				}
//				System.out.println(diffTimes);
//				System.out.println(System.currentTimeMillis());
				try
				{
					sleep(Const.FRAME_WAIT);
				} catch(InterruptedException e)
				{
					//do nothing
				}
			}
		}
	}

	private ClientConnection findClient(MsgData msg)
	{
		ClientConnection c;
		for (Iterator<ClientConnection> itr = m_clients.iterator(); itr.hasNext();)
		{
			c = itr.next();

			if (c.testAddress(msg.m_address) && c.testPort(msg.m_port))
			{
				return c;
			}
		}
		return null;
	}

	private boolean addClient(ClientConnection c)
	{
		if (c != null)
		{
			m_clients.add(c);
			return true;
		}
		return false;
	}

	private boolean newFrame()
	{
		double currentTime = System.currentTimeMillis();
		double delta = currentTime - m_lastTime;
		boolean rv = (delta > (Const.FRAME_INCREMENT));
		if (rv)
		{
			m_lastTime += Const.FRAME_INCREMENT;
			if (delta > 10 * Const.FRAME_INCREMENT)
			{
				m_lastTime = currentTime;
			}
			m_actualFps = 1000 / delta;
		}
		return rv;
	}
	
	private void initBall()
	{
		// Ball
		EntityManager.getInstance().addBall(new Vector2D(Const.BALL_X, Const.BALL_Y), new Vector2D(0.0, 0.0));	
	}
	
	private MsgData packState(LinkedList<GameEntity> list)
	{
		// fetch data about each gameentity (ships and ball)
		ArrayList<MsgData> msgs = new ArrayList<MsgData>();
		GameEntity ge;
		for(Iterator<GameEntity> itr = list.iterator(); itr.hasNext(); )
		{
			ge = itr.next();
			
			msgs.add(ge.getMsgData());
		}
		
		// pack all data into combined message
		MsgData msg = new MsgData();
		msg.setParameter("EntityCount", msgs.size());
		msg.setParameter("Score", ScoreKeeper.getInstance().getScore());
		
		
		for(int i = 0; i < msgs.size(); i++)
		{
			msg.setParameter("entity"+i, msgs.get(i).getJSONObj());
		}
//		System.out.println(msg.toString());
		return msg;
	}
	
	private void broadcast(MsgData msg)
	{
		if(msg == null)
		{
			return;
		}
		
		ClientConnection c;
		for(Iterator<ClientConnection> itr = m_clients.iterator(); itr.hasNext();)
		{
			c = itr.next();
			
			c.sendMessage(msg.toString());
		}
	}

	private MsgData addNewPlayer()
	{
		// Get player count, create a new ID and create a reply message
		int tp = EntityManager.getTotalPlayers();
		MsgData msg = new MsgData();
		
		// Players join teams in an alternating manner
		if (tp % 2 == 1)
		{
			// Create a ship for Team 1
			double xPos = Const.START_TEAM1_SHIP1_X;
			double yPos = Const.START_TEAM1_SHIP1_Y + (tp * 25);
			Vector2D position = new Vector2D(xPos, yPos);
			Vector2D speed = new Vector2D(0.0, 0.0);
			Vector2D direction = new Vector2D(1.0, 0.0);
			int color = 0;		// color 0 for team 1
			int ID = tp + 1;
			
			EntityManager.getInstance().addShip(position, speed, direction, color, ID);

			// Build message
			msg.setParameter("position", position);
			msg.setParameter("speed", speed);
			msg.setParameter("direction", direction);
			msg.setParameter("color", color);
			msg.setParameter("newID", ID);
		}
		else
		{
			// Create a ship for Team 2
			double xPos = Const.START_TEAM2_SHIP2_X;
			double yPos = Const.START_TEAM2_SHIP2_Y - (tp * 25);
			Vector2D position = new Vector2D(xPos, yPos);
			Vector2D speed = new Vector2D(0.0, 0.0);
			Vector2D direction = new Vector2D(-1.0, 0.0);
			int color = 1;		// color 1 for team 2
			int ID = tp + 1;
			
			EntityManager.getInstance().addShip(position, speed, direction, color, ID);
			
			// Build message
			msg.setParameter("position", position);
			msg.setParameter("speed", speed);
			msg.setParameter("direction", direction);
			msg.setParameter("color", color);
			msg.setParameter("newID", ID);
			
		}		
		
		return msg;
		
		
//		switch(EntityManager.getTotalPlayers())
//		{
//		case 0:
//			// Team 1, Ship 1
//			EntityManager.getInstance().addShip(new Vector2D(Const.START_TEAM1_SHIP1_X, Const.START_TEAM1_SHIP1_Y), new Vector2D(0.0, 0.0), new Vector2D(1.0, 0.0), Const.TEAM1_COLOR, Const.SHIP1_ID);
//			return 1;
//		
//		case 1:
//			// Team 1, Ship 2
//			EntityManager.getInstance().addShip(new Vector2D(Const.START_TEAM1_SHIP2_X, Const.START_TEAM1_SHIP2_Y), new Vector2D(0.0, 0.0), new Vector2D(1.0, 0.0), Const.TEAM1_COLOR, Const.SHIP2_ID);
//			return 2;
//			
//		case 2:
//			// Team 2, Ship 3
//			EntityManager.getInstance().addShip(new Vector2D(Const.START_TEAM2_SHIP1_X, Const.START_TEAM2_SHIP1_Y), new Vector2D(0.0, 0.0), new Vector2D(-1.0, 0.0), Const.TEAM2_COLOR, Const.SHIP3_ID);
//			return 3;
//			
//		case 3:
//			// Team 2, Ship 4
//			EntityManager.getInstance().addShip(new Vector2D(Const.START_TEAM2_SHIP2_X, Const.START_TEAM2_SHIP2_Y), new Vector2D(0.0, 0.0), new Vector2D(-1.0, 0.0), Const.TEAM2_COLOR, Const.SHIP4_ID);
//			return 4;
//			
//		case 4:
//			// A fifth player tries to enter
//			System.err.println("Error: server already full");
//			return 0;
//			
//		default:
//			System.err.println("Error: illegal player count: " + EntityManager.getTotalPlayers());
//			System.exit(1);
//		}
//		return 0;
	}

	public double getActualFps()
	{

		return m_actualFps;
	}

	public void addKeyListener(KeyListener k)
	{
		// m_gameWindow.addKeyListener(k);
	}

}