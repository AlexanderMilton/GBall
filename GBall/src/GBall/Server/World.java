package GBall.Server;

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

// thread only to enable sleeping
public class World extends Thread
{
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


	private World()
	{

	}

	public void process()
	{
		initBall();		// Ball must be initiated first

		try
		{
			// create a new socket and start the listener thread
			m_socket = new DatagramSocket(SERVERPORT);
			m_listener = new Listener(m_socket);
			m_listener.start();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		
		
		MsgData msg; // declared here to avoid allocating and deallocating as much memory each frame		
		while (true)
		{
			// check if a message has arrived
			if ((msg = m_listener.getMessage()) != null)
			{
				// check if the message came from an already connected client
				ClientConnection c = findClient(msg);
				if (c != null)
				{
					try
					{
						// Get player ID
						int shipID = msg.getInt("ID");
						// set client acceleration and rotation, and update the client's update-time
						EntityManager.getInstance().setAcceleration(shipID, msg.getDouble("acceleration"));
						EntityManager.getInstance().setRotation(shipID, msg.getInt("rotation"));
						c.m_lastUpdate = msg.getTimestamp();
					} catch (NullPointerException e)
					{
						// Do nothing;
					}
				} else
				{
					// A new player has requested to join the game
					ClientConnection newCC = new ClientConnection(msg.m_address, msg.m_port, m_socket); 
					addClient(newCC);
					
					// Create a ship and distribute the ship information to all clients
					MsgData shipInfo = addNewPlayer();
					newCC.sendMessage(shipInfo.toString());
				}
			}
			// check if enough time has passed since last frame
			if (newFrame())
			{
				// move all entities and perform collision checks/handling
				EntityManager.getInstance().updatePositions();
				EntityManager.getInstance().checkBorderCollisions(Const.DISPLAY_WIDTH, Const.DISPLAY_HEIGHT, true);
				EntityManager.getInstance().checkShipCollisions();
				
				// pack the game state and send it to all clients
				MsgData stateMsg = packState(EntityManager.getState()); 
				broadcast(stateMsg);
				try
				{
					// sleep awhile to avoid taxing the processor as much.
					// this is a bit shorter than the time needed for a new frame in order to allow for some 
					// message processing before calculating the next frame 
					sleep(Const.FRAME_WAIT);
				} catch(InterruptedException e)
				{
					//do nothing
				}
			}
		}
	}

	// returns null if not found
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
		// create Ball
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

		Vector2D position;
		Vector2D speed = new Vector2D(0.0, 0.0);
		Vector2D direction = new Vector2D(1.0, 0.0);
		int color;
		int ID = tp + 1;
		
		// Players join teams in an alternating manner
		if (tp % 2 == 1)
		{
			// Create a ship for Team 1
			double xPos = Const.START_TEAM1_SHIP1_X;
			double yPos = Const.START_TEAM1_SHIP1_Y + (tp * 25);
			position = new Vector2D(xPos, yPos);
			color = 0;		// color 0 for team 1
		}
		else
		{
			// Create a ship for Team 2
			double xPos = Const.START_TEAM2_SHIP2_X;
			double yPos = Const.START_TEAM2_SHIP2_Y - (tp * 25);
			position = new Vector2D(xPos, yPos);
			color = 1;		// color 1 for team 2
		}
		
		EntityManager.getInstance().addShip(position, speed, direction, color, ID);

		// Build message about the new player
		msg.setParameter("position", position);
		msg.setParameter("speed", speed);
		msg.setParameter("direction", direction);
		msg.setParameter("color", color);
		msg.setParameter("newID", ID);
		
		return msg;
	}

	public double getActualFps()
	{
		return m_actualFps;
	}
}