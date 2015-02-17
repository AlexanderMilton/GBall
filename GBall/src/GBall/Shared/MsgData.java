package GBall.Shared;

import java.io.Serializable;

import org.json.simple.*;

@SuppressWarnings("unchecked")
public class MsgData implements Serializable, Comparable<MsgData>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JSONObject obj = new JSONObject();

//	public Vector2D m_position;
//	public Vector2D m_initialPosition;
//	public Vector2D m_initialDirection;
//	public Vector2D m_speed;
//	public Vector2D m_direction; // Should always be unit vector; determines the
//									// object's facing
//	
//	public int m_rotation;
//	public double m_acceleration;
//	public long m_timestamp;
//	
//	public MsgData m_prevMsg = null;

	
	public MsgData()
	{
//		m_position = new Vector2D();
//		m_initialPosition = new Vector2D();
//		m_initialDirection = new Vector2D();
//		m_speed = new Vector2D();
//		m_direction = new Vector2D();
//		m_timestamp = System.currentTimeMillis();
		
		obj.put("timestamp", System.currentTimeMillis());
	}
	
	public void setPrevMessage(JSONObject pObj)
	{
		obj.put("prevMsg", pObj);
	}
	
	public void setParameter(String key, int value)
	{
		obj.put(key, value);
	}
	
	public void setParameter(String key, double value)
	{
		obj.put(key, value);
	}
	
	public void setParameter(String key, Vector2D value)
	{
		obj.put(key, value);
	}
	
	public Vector2D getVector(String key)
	{
		return (Vector2D) obj.get(key);
	}
	
	public int getInt(String key)
	{
		return (int) obj.get(key);
	}
	
	public double getDouble(String key) 
	{
		return (double) obj.get(key);
	}
	
	public long getTimestamp()
	{
		return (long) obj.get("timestamp");
	}

//	public MsgData(Vector2D position, Vector2D initialPosition, Vector2D initialDirection, Vector2D speed, Vector2D direction)
//	{
//
//		m_position = position;
//		m_initialPosition = initialPosition;
//		m_initialDirection = initialDirection;
//		m_speed = speed;
//		m_direction = direction;
//		m_timestamp = System.currentTimeMillis();
//	}
	
	@Override
	public int compareTo(MsgData o)
	{
		if(o == null)
		{
			throw new NullPointerException();
		} else if(o.getClass() != this.getClass())
		{
			throw new ClassCastException();
		}
		
		if(getTimestamp() < o.getTimestamp())
		{
			return -1;
		} else if(getTimestamp() > o.getTimestamp())
		{
			return 1;
		} else
		{
			return 0;
		}
	}

	@Override
	public String toString()
	{
//		String string = m_position + ", "
//						+ m_initialPosition + ", "
//						+ m_initialDirection + ", "
//						+ m_speed + ", "
//						+ m_direction + ", "
//						+ m_rotation + ", "
//						+ m_acceleration + ", "
//						+ m_timestamp + " : ";
//		if(m_prevMsg != null)
//		{
//			string += m_prevMsg;
//		}
//		else
//		{
//			string += "null";
//		}
//		return string;
		
		return obj.toJSONString();
	}
}
