package GBall.Shared;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.PriorityBlockingQueue;

import org.json.simple.parser.ParseException;

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
			byte[] buf = new byte[2048];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try
			{
				m_socket.receive(packet);
				String jstr = new String(packet.getData(), packet.getOffset(), packet .getLength());
//				System.out.println(jstr);
				MsgData msg = new MsgData(jstr, packet.getAddress(), packet.getPort());
				
//				ByteArrayInputStream bais = new ByteArrayInputStream(buf);
//				ObjectInputStream ois = new ObjectInputStream(bais);
//				MsgData msg = (MsgData) ois.readObject();
				m_messages.add(msg);
//				System.out.println("Message count: " + m_messages.size());
			} catch (IOException | ParseException e)
			{
				System.err.println("Failed to parse string");
				e.printStackTrace();
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
