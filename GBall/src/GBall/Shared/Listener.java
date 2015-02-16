package GBall.Shared;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.PriorityBlockingQueue;

public class Listener extends Thread
{
	private final DatagramSocket m_socket;
	
	private PriorityBlockingQueue<MsgData> m_messages = new PriorityBlockingQueue<MsgData>();
	
	private boolean m_isRunning = true;
	
	public Listener(DatagramSocket socket)
	{
		m_socket = socket;
	}

	public void run()
	{
		while(m_isRunning)
		{
			byte[] buf = new byte[1024];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try
			{
				m_socket.receive(packet);
				ByteArrayInputStream bais = new ByteArrayInputStream(buf);
				ObjectInputStream ois = new ObjectInputStream(bais);
				MsgData msg = (MsgData) ois.readObject();
				m_messages.add(msg);
			} catch (IOException | ClassNotFoundException e)
			{
				continue;
			}
		}
	}
	
	public void kill()
	{
		m_isRunning = false;
	}
	
	public MsgData getMessage()
	{
		if(m_messages.isEmpty())
		{
			return null;
		}
		return m_messages.poll();
	}
}
