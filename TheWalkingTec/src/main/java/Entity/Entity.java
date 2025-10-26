package Entity;

import java.io.Serializable;

public abstract class Entity extends Thread implements Serializable {
    
    protected String name;
    protected String actions;
    protected int healthPoints;
    protected int cost;
    protected int showUpLevel;
    
}
