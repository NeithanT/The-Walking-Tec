package Entity;

import java.io.Serializable;

public abstract class Entity extends Thread implements Serializable {
    
    protected String entityName;
    protected String actions;
    protected String imagePath;
    protected int healthPoints;
    protected int cost;
    protected int showUpLevel;
    
}
