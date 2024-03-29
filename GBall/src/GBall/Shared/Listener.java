package GBall.Shared;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.PriorityBlockingQueue;

import org.json.simple.parser.ParseException;

//Listens for messages and stores them sorted by timestamp.
public class Listener extends Thread
{
	private final DatagramSocket m_socket;
	
	// sorts all messages according to timestamp, so the first message in the queue is the oldest
	protected PriorityBlockingQueue<MsgData> m_messages = new PriorityBlockingQueue<MsgData>();
	
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
				// receive a json string in a datagrampacket and rebuild a MsgData instance from it.
				m_socket.receive(packet);
				String jstr = new String(packet.getData(), packet.getOffset(), packet .getLength());
				MsgData msg = new MsgData(jstr, packet.getAddress(), packet.getPort());
				
				add(msg);
			} catch (IOException | ParseException e)
			{
				System.err.println("Failed to parse string");
				e.printStackTrace();
				continue;
			}
			
		}
	}
	
	protected void add(MsgData msg)
	{
		m_messages.add(msg);
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
