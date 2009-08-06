package eva2.gui;

import java.rmi.Naming;
import java.util.ArrayList;

import eva2.tools.jproxy.RMIInvocationHandler;


/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 14.12.2004
 * Time: 11:33:10
 * To change this template use File | Settings | File Templates.
 */

class ServerNode implements java.io.Serializable {
    public String  m_ServerName;
    public int     m_CPUs;

    public ServerNode(String name, int cpus) {
        m_ServerName    = name;
        m_CPUs          = cpus;
    }
    public ServerNode(ServerNode a) {
        this.m_CPUs         = a.m_CPUs;
        this.m_ServerName   = a.m_ServerName;
    }
    public Object clone() {
        return (Object) new ServerNode(this);
    }
}

public class PropertyRemoteServers implements java.io.Serializable {

    private ServerNode[]        m_AvailableNodes;
//    private String              m_ClassToStart  = "wsi.ra.jproxy.RMIServer";
    private transient String    m_password      = "";
    private String              m_Login         = "";
//    private boolean             m_DeployJar     = true;
//    private String              m_JarToDeploy   = "JOpt.jar";

    public PropertyRemoteServers() {
        this.m_AvailableNodes = new ServerNode[0];
        this.addServerNode("exampleNode.uni-tuebingen.de", 2);
        this.setLogin("username");
        this.setPassword("");
    }

    public PropertyRemoteServers(PropertyRemoteServers e) {
        if (e.m_AvailableNodes != null) {
            this.m_AvailableNodes = new ServerNode[e.m_AvailableNodes.length];
            for (int i = 0; i < e.m_AvailableNodes.length; i++) {
                this.m_AvailableNodes[i] = (ServerNode)e.m_AvailableNodes[i].clone();
            }
        }
    }

    public Object clone() {
        return (Object) new PropertyRemoteServers(this);
    }

    /** This method adds a server to the server nodes list
     * It will check whether or not the given name is already
     * in the current list of nodes is so it breaks.
     * @param name  The name of the server
     * @param cpus  The number of cpus on the server
     */

    public void addServerNode(String name, int cpus) {
        // first check for double instances
        for (int i = 0; i < this.m_AvailableNodes.length; i++)  {
            if (this.m_AvailableNodes[i].m_ServerName.equalsIgnoreCase(name)) {
                if (cpus > this.m_AvailableNodes[i].m_CPUs) this.m_AvailableNodes[i].m_CPUs = cpus;
                return;
            }
        }

        // now add the guy
        ServerNode[]   newList = new ServerNode[this.m_AvailableNodes.length+1];
        ServerNode     newNode = new ServerNode(name, cpus);
        for (int i = 0; i < this.m_AvailableNodes.length; i++) {
            newList[i] = this.m_AvailableNodes[i];
        }
        newList[this.m_AvailableNodes.length]   = newNode;
        this.m_AvailableNodes                   = newList;
    }

    /** This method removes a surplus node from the current list
     * @param name  The name of the server to remove
     */
    public void removeServerNode(String name) {
        ArrayList newList = new ArrayList();
        for (int i = 0; i < this.m_AvailableNodes.length; i++)  {
            if (!this.m_AvailableNodes[i].m_ServerName.equalsIgnoreCase(name)) {
                newList.add(this.m_AvailableNodes[i]);
            } else {
                this.killServer(this.m_AvailableNodes[i].m_ServerName);
            }
        }
        this.m_AvailableNodes = new ServerNode[newList.size()];
        for (int i = 0; i < this.m_AvailableNodes.length; i++) {
            this.m_AvailableNodes[i] = (ServerNode)newList.get(i);
        }
    }

    /** This method removes and deactivates all servers
     *
     */
    public void removeAll() {
        for (int i = 0; i < this.m_AvailableNodes.length; i++) {
            this.killServer(this.m_AvailableNodes[i].m_ServerName);
        }
        this.m_AvailableNodes = new ServerNode[0];
    }

    /** This method returns an unchecked list of server instances
     * an server with n nodes will occur n times in the returned
     * server list
      * @return A list of server instances, with double instances.
     */
    public String[] getServerNodes() {
        String[] result;
        ArrayList<String> tmpList = new ArrayList<String>();
        for (int i = 0; i < this.m_AvailableNodes.length; i++) {
            for (int j = 0; j < this.m_AvailableNodes[i].m_CPUs; j++) {
                tmpList.add(this.m_AvailableNodes[i].m_ServerName);
            }
        }
        result = new String[tmpList.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = (String) tmpList.get(i);
        }
        return result;
    }

    /** This method returns an checked list of server instances
     * an server with n nodes will occur n times in the returned
     * server list
      * @return A list of server instances, with double instances.
     */
    public String[] getCheckedServerNodes() {
        String[] result;
        ArrayList tmpList = new ArrayList();
        for (int i = 0; i < this.m_AvailableNodes.length; i++) {
            if (this.isServerOnline(this.m_AvailableNodes[i].m_ServerName)) {
                for (int j = 0; j < this.m_AvailableNodes[i].m_CPUs; j++) {
                    tmpList.add(this.m_AvailableNodes[i].m_ServerName);
                }
            }
        }
        result = new String[tmpList.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = (String) tmpList.get(i);
        }
        return result;
    }

    /** This method will check whether or not a given server is online
     * @param name  The name of the server to check
     * @return true if server is online, false else
     */
    public boolean isServerOnline(String name) {
        try {
            String[] list = Naming.list("rmi://" + name + ":" + 1099);
            if (list == null) return false;
            else return true;
        } catch (Exception et) {
            return false;
        }
    }

    /** This method will try to start all server in the current list
     */
    public void startServers() {
       /* ServerStarter   serverstarter;
        for (int i = 0; i < this.m_AvailableNodes.length; i++) {
            if (!this.isServerOnline(this.m_AvailableNodes[i].m_ServerName)) {
                serverstarter = new ServerStarter();
                serverstarter.setPasswd(this.m_password);
                serverstarter.setClass2Run(this.m_ClassToStart);
                serverstarter.setDeployJarFile(this.m_DeployJar);
                serverstarter.setJarfilename(this.m_JarToDeploy);
                serverstarter.setHostname(this.m_AvailableNodes[i].m_ServerName);
                serverstarter.setLogin(this.m_Login);
                try {
                    serverstarter.startServer();
                } catch (Exception e) {
                    System.out.println("Problems starting the server: " + e.getMessage());
                }
            }
        }*/
    }

    /** This method kills the servers
     * previously started
     */
    public void killServers() {
        for (int i = 0; i < this.m_AvailableNodes.length; i++) {
            this.killServer(this.m_AvailableNodes[i].m_ServerName);
        }
    }

    /** This method kills a single server
     * @param name  The name of the server to kill
     */
    public void killServer(String name) {
        if (this.isServerOnline(name)) {
            try {
                String[] list = Naming.list("rmi://" + name + ":" + 1099);
                for (int j = 0; j < list.length; j++) {
                    System.out.println(""+list[j]);
                    if (list[j].indexOf(this.m_Login) > 0) {
                        RMIInvocationHandler x = (RMIInvocationHandler) Naming.lookup("rmi:"+list[j]);
                        //x.invoke("killThread", null);
                    }
                }
             } catch (Exception e) {
                System.out.println("Error : "+e.getMessage());
            }
        }
    }

    /** This method returns the number of servers
     * @return the size
     */
    public int size() {
        return this.m_AvailableNodes.length;
    }

    public ServerNode get(int i) {
        if ((i >= 0) && (i < this.m_AvailableNodes.length))
            return this.m_AvailableNodes[i];
        else
            return null;
    }

    public String writeToText() {
        String result = "";
        for (int i = 0; i < this.m_AvailableNodes.length; i++) {
            result += this.m_AvailableNodes[i].m_ServerName +"\t"+this.m_AvailableNodes[i].m_CPUs+"\n";
        }
        return result;
    }

    public void setNameFor(int i, String name) {
        if ((i >= 0) && (i < this.m_AvailableNodes.length)) this.m_AvailableNodes[i].m_ServerName = name;
    }

    public void setCPUsFor(int i, int c) {
        if ((i >= 0) && (i < this.m_AvailableNodes.length)) this.m_AvailableNodes[i].m_CPUs = c;
    }

    public void readFromText(String text) {
        String[] lines = text.split("\n");
        this.removeAll();
        for (int i = 0; i < lines.length; i++) {
            String[] rickel = lines[i].split("\t");
            this.addServerNode(rickel[0].trim(), new Integer(rickel[1].trim()).intValue());
        }
    }

    public static void main(String[] args) {
        PropertyRemoteServers test = new PropertyRemoteServers();
        test.m_password = "";
        test.addServerNode("raold1.informatik.uni-tuebingen.de", 2);
        //test.addServerNode("raold2.informatik.uni-tuebingen.de", 2);
        System.out.println(" Getting running Servers:");
        String[] servers =  test.getCheckedServerNodes();
        for (int i = 0; i < servers.length; i++) {
            System.out.println("Server "+i+": "+servers[i]);
        }
        System.out.println("\n Starting Servers: ");
        test.startServers();
        System.out.println(" Getting running Servers:");
        servers =  test.getCheckedServerNodes();
        for (int i = 0; i < servers.length; i++) {
            System.out.println("Server "+i+": "+servers[i]);
        }
        test.killServers();
    }

    public String getLogin() {
        return this.m_Login;
    }
    public void setLogin(String l) {
        this.m_Login = l;
    }
    public String getPassword() {
        return this.m_password;
    }
    public void setPassword(String l) {
        this.m_password = l;
    }
    public void setPassword(char[] l) {
        this.m_password = new String(l);
    }
}