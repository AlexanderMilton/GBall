package GBall.Client;


public class GBMain
{
	public static void main(String[] argc)
	{
		if(argc.length > 0)
		{
			World.SERVERIP = argc[0];
		}
		World.getInstance().process();
	}
}