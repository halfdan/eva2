package eva2.server.go.tools;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 23.02.2004
 * Time: 16:53:16
 * To change this template use File | Settings | File Templates.
 */
public class point  extends entity {
private int x = 0;
private int y = 0;

public point() {
}

public point(int x, int y) {
setX(x);
setY(y);

setType("type1");
setBasetype("basetype1");
setColor("red");
setName("my_name");
}

public void setX(int x) {
this.x = x;
}

public void setY(int y) {
this.y = y;
}

public int getX() {
return x;
}

public int getY() {
return y;
}

public void setType(String type) {
super.setType(type);
}

public void setBasetype(String basetype) {
super.setBasetype(basetype);
}

public void setColor(String color) {
super.setColor(color);
}

public void setId(String id) {
super.setId(id);
}

public void setName(String name) {
super.setName(name);
}
}