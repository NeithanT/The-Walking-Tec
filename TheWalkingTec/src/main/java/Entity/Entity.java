package Entity;

import java.io.Serializable;

public abstract class Entity extends Thread implements Serializable {
    
    protected int healthPoints;
    protected int cost;
    protected int showUpLevel;
    protected String name;
    protected String actions;
    
}
