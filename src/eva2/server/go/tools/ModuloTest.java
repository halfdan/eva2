package eva2.server.go.tools;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 10.08.2004
 * Time: 17:52:37
 * To change this template use File | Settings | File Templates.
 */
public class ModuloTest {
    public static void main(String[] args) {
        double d = 0.75, p = 1.45;
        int     w = 1, e = 2;
        System.out.println(d+"%"+w+" = "+ (d%w));
        System.out.println(d+"%"+e+" = "+ (d%e));
        System.out.println(d+"/"+w+" = "+ ((int)(d/w)));
        System.out.println(d+"/"+p+" = "+ ((int)(d/p)));
        System.out.println(d+"/"+e+" = "+ ((int)(d/e)));
    }
}
