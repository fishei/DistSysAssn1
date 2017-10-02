package Models;

public class User {
    private String userName;
    private int id;

    public User(String userName, int userId){
        this.userName = userName;
        this.id = userId;
    }

    public String getUserName()
    {
        return userName;
    }

    public int getId()
    {
        return id;
    }
}
