
package Table;

import Defense.Defense;
import java.awt.Image;


public class PlacedDefense {
    public final Defense definition;
    public final int row;
    public final int column;
    public final Image image;
    
    public PlacedDefense(Defense definition, int row, int column, Image image){
        this.definition = definition;
        this.row = row;
        this.column = column;
        this.image = image;
    }
}
