package javaeva.server.go.tools;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 23.02.2004
 * Time: 16:52:16
 * To change this template use File | Settings | File Templates.
 */
public class entity  {
private String type = null;
private String basetype = null;
private String id = null;
private String name = null;
private String color = null;

public entity(){
}

public void setType(String type) {
this.type = type;
}

public void setBasetype(String basetype) {
this.basetype = basetype;
}

public void setId(String id) {
this.id = id;
}

public void setName(String name) {
this.name = name;
}

public void setColor(String color) {
this.color = color;
}

public String getType() {
return type;
}

public String getBasetype() {
return basetype;
}

public String getId() {
return id;
}

public String getName() {
return name;
}

public String getColor() {
return color;
}
}