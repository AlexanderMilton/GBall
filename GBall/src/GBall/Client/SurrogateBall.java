package GBall.Client;

import GBall.Shared.Ball;
import GBall.Shared.Const;
import GBall.Shared.Vector2D;

public class SurrogateBall extends Ball
{
	// surrogate position, speed, direction etc.
	private final Vector2D m_surPosition;
	private final Vector2D m_surSpeed;
	private long m_surLastUpdate = System.currentTimeMillis();
	
	public SurrogateBall(Vector2D position, Vector2D speed)
	{
		super(position, speed);
		m_surPosition = new Vector2D(position);
		m_surSpeed = new Vector2D(speed);
	}
	
	public Vector2D getPosition()
	{
		return m_surPosition;
	}
	
	public Vector2D getSpeed()
	{
		return m_surSpeed;
	}
	
	public void changeSpeed(Vector2D delta)
	{
		super.changeSpeed(delta, m_surSpeed);
	}
	
	public void displace(Vector2D displacement)
	{
		super.displace(displacement, m_surPosition);
	}
	
	public void deflectY()
	{
		m_surSpeed.setY(-m_surSpeed.getX());
	}
	
	public boolean givesPoints()
	{
		return false;
	}
		
	@Override
	public void move()
	{
		long currTime = System.currentTimeMillis();
		double delta = (double) (currTime- m_surLastUpdate)/1000.0;
		scaleSpeed(m_friction, m_surSpeed);
		
		m_surPosition.add(m_surSpeed.multiplyOperator(delta));
		
		m_surLastUpdate = currTime;
		
		checkRealState();
	}
	
	private void checkRealState()
	{
		long stateAge = System.currentTimeMillis() - m_lastUpdateTime;
		float f = 0.3f;
		if(stateAge > 0)
		{
			f = 10.0f / (float)stateAge;
//			System.out.println(f + " " + stateDiff + " " + stateAge);
		}
		
		double deltaX = Math.abs(m_surPosition.getX() - super.getPosition().getX());
		double deltaY = Math.abs(m_surPosition.getY() - super.getPosition().getY());
		
		if(deltaX > Const.SURROGATE_MAX_DIFFERENCE ||
		   deltaY > Const.SURROGATE_MAX_DIFFERENCE)
		{
			m_surPosition.set(super.getPosition());
		}
		
		
		m_surPosition.lerp(super.getPosition(), f);
		m_surSpeed.lerp(super.getSpeed(), f);
	}
}
