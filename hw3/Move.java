package hw3;

/**
 * Created with IntelliJ IDEA.
 * User: aakritprasad
 * Date: 8/19/13
 * Time: 5:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class Move //extends ClientResponse{
{
    private int id;
    private String color;
    private int location;

    Move(int id, String color, int loc){
        this.id = id;
        this.color = color;
        this.location = loc;
    }

    public int getID(){
        return this.id;
    }

    public int getLocation(){
        return this.location;
    }

    public String getColor(){
        return this.color;
    }
}
