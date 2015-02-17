package GBall.Shared;

public class KeyConfig
{
	private final int m_leftKey;
	private final int m_rightKey;
	private final int m_accelerateKey;

	public KeyConfig(int left, int right, int accelerate)
	{
		m_leftKey = left;
		m_rightKey = right;
		m_accelerateKey = accelerate;
	}

	public int leftKey()
	{
		return m_leftKey;
	}

	public int rightKey()
	{
		return m_rightKey;
	}

	public int accelerateKey()
	{
		return m_accelerateKey;
	}

}