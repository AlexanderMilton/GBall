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

public class World
{

	public static final String SERVERIP = "127.0.0.1"; // 'Within' the emulator!
	public static final int SERVERPORT = 4444;

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

	private final GameWindow m_gameWindow = new GameWindow("Client");
	private InputListener m_inputListener;
	
	private Ship ship;

	private World()
	{
		
	}

	public void process()
	{
		m_inputListener = new InputListener(new KeyConfig(KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP));
//		initPlayers();
		EntityManager.getInstance().addBall(new Vector2D(Const.BALL_X, Const.BALL_Y), new Vector2D());

		// Marshal the state
		try
		{
			m_socket = new DatagramSocket();
			m_listener = new Listener(m_socket);
			m_listener.start();
			InetAddress m_serverAddress = InetAddress.getByName("localhost");
			
			// Send join request
			MsgData msg = new MsgData();
			byte[] buf = msg.toString().getBytes();
			DatagramPacket pack = new DatagramPacket(buf, buf.length, m_serverAddress, SERVERPORT);
			m_socket.send(pack);
			
			msg = null;
			// Receive new player data
			while(msg == null)
			{
				msg = m_listener.getMessage();
			}
			
			// Create a ship using the new player data
			ship = new Ship(msg.getVector("position"), msg.getVector("position"), msg.getVector("position"), msg.getInt("color"), msg.getInt("newID"));
			EntityManager.getInstance().addShip(ship);
			

		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		MsgData prevMsg = null;
		MsgData msg;
		while (true)
		{
			if (newFrame())
			{
				if((msg = m_listener.getMessage()) != null)
				{
//					System.out.println(msg.debugInfo());
					updateState(msg);
				}
//				System.out.println(System.currentTimeMillis());
				/*LinkedList<GameEntity> entities = EntityManager.getState();
				GameEntity ge;
				for(Iterator<GameEntity> itr = entities.iterator(); itr.hasNext();)
				{
					ge = itr.next();
					sendMsg(ge.getMsgData());
				}*/
				/*ship.setRotation(m_inputListener.getRotation());
				ship.setAcceleration(m_inputListener.getAcceleration());*/
				msg = new MsgData();
				msg.setParameter("ID", ship.getID());
				msg.setParameter("rotation", m_inputListener.getRotation());
				msg.setParameter("acceleration", m_inputListener.getAcceleration());
				//msg.m_prevMsg = prevMsg;
//				System.out.println(EntityManager.getState().get(1).getPosition().toJSONString());
				sendMsg(msg);
				//msg.m_prevMsg = null;
//				prevMsg = msg;
				EntityManager.getInstance().updatePositions();
				EntityManager.getInstance().checkBorderCollisions(Const.DISPLAY_WIDTH, Const.DISPLAY_HEIGHT);
				EntityManager.getInstance().checkShipCollisions();
				m_gameWindow.repaint();
			}
		}
	}
	
	private void updateState(MsgData msg)
	{
		int count = msg.getInt("EntityCount");
		count = Math.min(count, EntityManager.getState().size()); // hack to avoid arrayindexoutofboundsexception
		for(int i = 0; i < count; i++)
		{
			EntityManager.getInstance().setState(i, new MsgData(msg.getJSONObj("entity" + i)));
		}
		ScoreKeeper.getInstance().setScore(msg.getVector("Score"));
	}

	private void sendMsg(MsgData msg)
	{
		try
		{
			InetAddress m_serverAddress;

			m_serverAddress = InetAddress.getByName("localhost");

			byte[] buf = msg.toString().getBytes();

			DatagramPacket pack = new DatagramPacket(buf, buf.length, m_serverAddress, SERVERPORT);
			m_socket.send(pack);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
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

//	private void initPlayers()
//	{
//		// The order in which the entities are added are important as the index corresponds to their ID:s
//		
//		// Ball
//		EntityManager.getInstance().addBall(new Vector2D(Const.BALL_X, Const.BALL_Y), new Vector2D(0.0, 0.0));
//		
//		// Team 1
//		EntityManager.getInstance().addShip(new Vector2D(Const.START_TEAM1_SHIP1_X, Const.START_TEAM1_SHIP1_Y), new Vector2D(0.0, 0.0), new Vector2D(1.0, 0.0), Const.TEAM1_COLOR, Const.SHIP1_ID);
//
//		EntityManager.getInstance().addShip(new Vector2D(Const.START_TEAM1_SHIP2_X, Const.START_TEAM1_SHIP2_Y), new Vector2D(0.0, 0.0), new Vector2D(1.0, 0.0), Const.TEAM1_COLOR, Const.SHIP2_ID);
//
//		// Team 2
//		EntityManager.getInstance().addShip(new Vector2D(Const.START_TEAM2_SHIP1_X, Const.START_TEAM2_SHIP1_Y), new Vector2D(0.0, 0.0), new Vector2D(-1.0, 0.0), Const.TEAM2_COLOR, Const.SHIP3_ID);
//
//		EntityManager.getInstance().addShip(new Vector2D(Const.START_TEAM2_SHIP2_X, Const.START_TEAM2_SHIP2_Y), new Vector2D(0.0, 0.0), new Vector2D(-1.0, 0.0), Const.TEAM2_COLOR, Const.SHIP4_ID);
//	}

	public double getActualFps()
	{

		return m_actualFps;
	}

	public void addKeyListener(KeyListener k)
	{
		m_gameWindow.addKeyListener(k);
	}

}