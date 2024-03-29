package GBall.Client;

import java.awt.event.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import GBall.Shared.Const;
import GBall.Shared.EntityManager;
import GBall.Shared.KeyConfig;
import GBall.Shared.Listener;
import GBall.Shared.MsgData;
import GBall.Shared.ScoreKeeper;
import GBall.Shared.Ship;
import GBall.Shared.Vector2D;

// thread to enable sleep
public class World extends Thread
{
	public static String SERVERIP = "127.0.0.1"; // can be set from commandline
	public static final int SERVERPORT = 25001;
	public static InetAddress SERVERADDRESS;

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

	private final GameWindow m_gameWindow = new GameWindow("Geometry Ball Tournament 2015");
	private InputListener m_inputListener;
	
	private Ship ship; // reference to the local player's ship

	private World()
	{
		
	}

	public void process()
	{
		m_inputListener = new InputListener(new KeyConfig(KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP));
		EntityManager.getInstance().addBall(new SurrogateBall(new Vector2D(Const.BALL_X, Const.BALL_Y), new Vector2D()));

		try
		{
			m_socket = new DatagramSocket();
			m_listener = new Listener(m_socket);
			m_listener.start();
			SERVERADDRESS = InetAddress.getByName(SERVERIP);
			
			// Send join request
			MsgData msg = new MsgData();
			byte[] buf = msg.toString().getBytes();
			DatagramPacket pack = new DatagramPacket(buf, buf.length, SERVERADDRESS, SERVERPORT);
			m_socket.send(pack);
			
			msg = null;
			// Receive new player data
			while(msg == null)
			{ // check and assignment split to different statements because Java
				msg = m_listener.getMessage();
			}
			
			// calculate difference between local time and server time
			long timeDiff = msg.getTimestamp() - System.currentTimeMillis();
			MsgData.m_offset = timeDiff;
			System.out.println(msg.getTimestamp() + "\n"
					+ System.currentTimeMillis() + " " + timeDiff + "\n"
					+ new MsgData().getTimestamp());

			
			// Create a ship using the new player data
			ship = new SurrogateShip(msg.getVector("position"), msg.getVector("speed"), msg.getVector("direction"), msg.getInt("color"), msg.getInt("newID"));
			// Create already existing entities
			initEntities();
			// Add the new player ship
			EntityManager.getInstance().addShip(ship);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		MsgData msg; // declaration here to avoid allocation/deallocation of variable each frame
		while (true)
		{
			// check if it's time to calculate a new frame
			if (newFrame())
			{
				// check if a new game state has arrived from the server
				if((msg = m_listener.getMessage()) != null)
				{
					updateState(msg);
				}
				// pack input and send to server
				msg = new MsgData();
				msg.setParameter("ID", ship.getID());
				msg.setParameter("rotation", m_inputListener.getRotation());
				msg.setParameter("acceleration", m_inputListener.getAcceleration());
				sendMsg(msg);
				
				// set acceleration and rotation for local ship surrogate for client side prediction
				ship.setAcceleration(m_inputListener.getAcceleration());
				ship.setRotation(m_inputListener.getRotation());
				
				// calculate movement and collisions
				EntityManager.getInstance().updatePositions();
				EntityManager.getInstance().checkBorderCollisions(Const.DISPLAY_WIDTH, Const.DISPLAY_HEIGHT, false);
				EntityManager.getInstance().checkShipCollisions();
				m_gameWindow.repaint();
				try
				{
					// sleep a bit to reduce unneccesary workload for processor
					sleep(Const.FRAME_WAIT);
				} catch(InterruptedException e)
				{
					//Do nothing
				}
			}
		}
	}
	
	// update all entities to match server state
	private void updateState(MsgData msg)
	{
		int count = msg.getInt("EntityCount");
		
		// Check for new players
		if (count > EntityManager.getState().size())
		{
			System.out.println("NEW ENTITY DETECTED. COUNT: " + count);
			initNewEntity(count-1);
		}
		
		for(int i = 0; i < count; i++)
		{
			EntityManager.getInstance().setState(i, new MsgData(msg.getJSONObj("entity" + i)));
		}
		// update score
		ScoreKeeper.getInstance().setScore(msg.getVector("Score"));
	}

	private void sendMsg(MsgData msg)
	{
		try
		{
			byte[] buf = msg.toString().getBytes();

			DatagramPacket pack = new DatagramPacket(buf, buf.length, SERVERADDRESS, SERVERPORT);
			m_socket.send(pack);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private boolean newFrame()
	{
		double currentTime = System.currentTimeMillis();
		double delta = currentTime - m_lastTime;
		boolean rv = (delta > Const.FRAME_INCREMENT);
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
	
	private void initEntities()
	{
		// Create players in arbitrary positions to be updated with messages
		for(int i = 1; i < ship.getID(); i++)
		{
			initNewEntity(i);
		}
	}

	// create a new entity and calculate position and color based on entity id
	private void initNewEntity(int count)
	{
		if (count % 2 == 0)
		{
			EntityManager.getInstance().addShip(new SurrogateShip(new Vector2D(Const.START_TEAM1_SHIP1_X, Const.START_TEAM1_SHIP1_Y + (count * 50)), new Vector2D(0.0, 0.0), new Vector2D(1.0, 0.0), 0, count));
		}
		else
		{
			EntityManager.getInstance().addShip(new SurrogateShip(new Vector2D(Const.START_TEAM2_SHIP1_X, Const.START_TEAM2_SHIP1_Y + (count * 50)), new Vector2D(0.0, 0.0), new Vector2D(-1.0, 0.0), 1, count));
		}
	}

	public double getActualFps()
	{
		return m_actualFps;
	}

	public void addKeyListener(KeyListener k)
	{
		m_gameWindow.addKeyListener(k);
	}

}