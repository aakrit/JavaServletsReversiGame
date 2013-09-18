package hw3;

/**
 * Created with IntelliJ IDEA.
 * User: aakritprasad
 * Date: 8/24/13
 * Time: 6:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class Player
{

    private String name;


    private int id;
    private int color;

    public Player(int id, String name, int color)  {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }
    public int getColor()
    {
        return color;
    }

    public int getId() {
        return id;
    }




}
