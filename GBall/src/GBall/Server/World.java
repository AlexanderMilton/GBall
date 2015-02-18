package GBall.Server;

import java.awt.event.*;
import java.io.IOException;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Iterator;

import GBall.Client.GameWindow;
import GBall.Shared.Const;
import GBall.Shared.EntityManager;
import GBall.Shared.Listener;
import GBall.Shared.MsgData;
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
	private ArrayList<ClientConnection> m_clients = new ArrayList<ClientConnection>();

	private GameWindow m_gameWindow = new GameWindow("Server");

	private World()
	{

	}

	public void process()
	{
		initPlayers();

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MsgData msg;
		while (true)
		{
			if ((msg = m_listener.getMessage()) != null)
			{
				System.out.println(msg.debugInfo());
				ClientConnection c = findClient(msg);
				if (c != null)
				{
					try
					{
						EntityManager.getInstance().setAcceleration(msg.getDouble("acceleration"));
						EntityManager.getInstance().setRotation(msg.getInt("rotation"));
					} catch (NullPointerException e)
					{
						// Do nothing;
					}
				} else
				{
					addClient(new ClientConnection(msg.m_address, msg.m_port, m_socket));
				}
			}
			if (newFrame())
			{
				EntityManager.getInstance().updatePositions();
				EntityManager.getInstance().checkBorderCollisions(Const.DISPLAY_WIDTH, Const.DISPLAY_HEIGHT);
				EntityManager.getInstance().checkShipCollisions();
				m_gameWindow.repaint();
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

	private void initPlayers()
	{
		// Team 1
		EntityManager.getInstance().addShip(new Vector2D(Const.START_TEAM1_SHIP1_X, Const.START_TEAM1_SHIP1_Y), new Vector2D(0.0, 0.0), new Vector2D(1.0, 0.0), Const.TEAM1_COLOR);

		EntityManager.getInstance().addShip(new Vector2D(Const.START_TEAM1_SHIP2_X, Const.START_TEAM1_SHIP2_Y), new Vector2D(0.0, 0.0), new Vector2D(1.0, 0.0), Const.TEAM1_COLOR);

		// Team 2
		EntityManager.getInstance().addShip(new Vector2D(Const.START_TEAM2_SHIP1_X, Const.START_TEAM2_SHIP1_Y), new Vector2D(0.0, 0.0), new Vector2D(-1.0, 0.0), Const.TEAM2_COLOR);

		EntityManager.getInstance().addShip(new Vector2D(Const.START_TEAM2_SHIP2_X, Const.START_TEAM2_SHIP2_Y), new Vector2D(0.0, 0.0), new Vector2D(-1.0, 0.0), Const.TEAM2_COLOR);

		// Ball
		EntityManager.getInstance().addBall(new Vector2D(Const.BALL_X, Const.BALL_Y), new Vector2D(0.0, 0.0));
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