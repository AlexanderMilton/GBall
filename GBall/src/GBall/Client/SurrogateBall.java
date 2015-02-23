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
//			m_surDirection.set(super.getDirection());
		}
		/*if(Math.abs(m_surPosition.getX() - super.getPosition().getX()) > Const.SURROGATE_MAX_DIFFERENCE)
		{
			m_surPosition.setX(super.getPosition().getX());
		}
		if(Math.abs(m_surPosition.getY() - super.getPosition().getY()) > Const.SURROGATE_MAX_DIFFERENCE)
		{
			m_surPosition.setY(super.getPosition().getY());
		}
		
		if(m_surDirection.dotProduct(super.getDirection()) < 0.5)
		{
			m_surDirection.set(super.getDirection());
		}*/
		
		m_surPosition.lerp(super.getPosition(), f);
		m_surSpeed.lerp(super.getSpeed(), f);

		
//		System.out.println(m_surSpeed.dotProduct(super.getSpeed()) + "\n"
//				+ m_surSpeed + " " + super.getSpeed()); 
	}
}
