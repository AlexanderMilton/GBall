package GBall.Client;

import GBall.Shared.Const;
import GBall.Shared.Ship;
import GBall.Shared.Vector2D;

public class SurrogateShip extends Ship
{
	// surrogate position, speed, direction etc.
	private final Vector2D m_surPosition;
	private final Vector2D m_surSpeed;
	private final Vector2D m_surDirection;
	private int m_surRotation = 0;
	private double m_surAcceleration = 0.0;
	private long m_surLastUpdate = System.currentTimeMillis();

	public SurrogateShip(Vector2D position, Vector2D speed, Vector2D direction, int col, int id)
	{
		super(position, speed, direction, col, id);
		m_surPosition = new Vector2D(position);
		m_surSpeed = new Vector2D(speed);
		m_surDirection = new Vector2D(direction);
	}
	
	@Override
	public void setRotation(int r)
	{
		m_surRotation = r;
	}
	
	@Override
	public void setAcceleration(double a)
	{
		if (a > m_maxAcceleration)
		{
			m_surAcceleration = m_maxAcceleration;
		} else if (a < (-m_maxAcceleration))
		{     
			m_surAcceleration = -m_maxAcceleration;
		} else
			m_surAcceleration = a;
	}

	@Override
	public void move()
	{
		Vector2D prevPos = new Vector2D(m_surPosition);
		if(m_surRotation != 0)
		{
			m_surDirection.rotate(m_surRotation * Const.SHIP_ROTATION);
			scaleSpeed(Const.SHIP_TURN_BRAKE_SCALE, m_surSpeed);
		}
		
		long currTime = System.currentTimeMillis();
		double delta = (double) (currTime- m_surLastUpdate)/1000.0;
		if(m_surAcceleration > 0)
		{
			changeSpeed(m_surDirection.multiplyOperator(m_surAcceleration * delta), m_surSpeed);
		}
		else
		{
			scaleSpeed(m_friction, m_surSpeed);
		}
		m_surPosition.add(m_surSpeed.multiplyOperator(delta));
		
		m_surLastUpdate = currTime;
		
		checkRealState();
		
		prevPos.subtract(m_surPosition);
//		System.out.println(prevPos + " " + m_surAcceleration + " " + m_surRotation + " : " + delta);
	}
	
	private void checkRealState()
	{
		float f = 0.3f;
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
		m_surDirection.lerp(super.getDirection(), f);
		m_surSpeed.lerp(super.getSpeed(), f);

		
//		System.out.println(m_surSpeed.dotProduct(super.getSpeed()) + "\n"
//				+ m_surSpeed + " " + super.getSpeed()); 
	}
	
	@Override
	public Vector2D getPosition()
	{
//		System.out.println(m_surPosition + " " + super.getPosition() + " : " + m_surAcceleration + " " + m_surRotation);
		return m_surPosition;
//		return super.getPosition();
	}
	
	@Override
	public Vector2D getDirection()
	{
		return m_surDirection;
	}
	
	@Override
	public void deflectX()
	{
		m_surSpeed.setX(-m_surSpeed.getX());
	}
	
	@Override
	public void deflectY()
	{
		m_surSpeed.setY(-m_surSpeed.getY());
	}
}
