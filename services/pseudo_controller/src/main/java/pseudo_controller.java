/*
This is the code for the test controller, for testing the reactions between the Network Configuration manager and
the ACA.

Params:
1. Number of ports to generate to each aca node
2. IP of aca_node_one
3. IP of aca_node_two
4. User name of aca_nodes
5. Password of aca_nodes
*/
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.InputStream;

public class pseudo_controller {
    static String aca_node_one_ip = "ip_one";
    static String aca_node_two_ip = "ip_two";
    static String user_name = "root";
    static String password = "abcdefg";
    static int ports_to_generate_on_each_aca_node = 1;
    public static void main(String[] args){
        System.out.println("Start of the test controller");
        if(args.length == 5){
            System.out.println("User passed in params and we need to read them.");
            ports_to_generate_on_each_aca_node = Integer.parseInt(args[0]);
            aca_node_one_ip = args[1];
            aca_node_two_ip = args[2];
            user_name = args[3];
            password = args[4];
        }
        System.out.println("aca_node_one_ip: " + aca_node_one_ip + "\naca_node_two_ip: " + aca_node_two_ip + "\nuser name: "+user_name+"\npassword: "+password);
        execute_ssh_commands("docker ps", aca_node_one_ip, user_name, password);
        System.out.println("End of the test controller");
    }

    public static void execute_ssh_commands(String command, String host_ip, String host_user_name, String host_password){
        try{

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            Session session=jsch.getSession(host_user_name, host_ip, 22);
            session.setPassword(host_password);
            session.setConfig(config);
            session.connect();
            System.out.println("Connected");

            Channel channel=session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command);
            channel.setInputStream(null);
            ((ChannelExec)channel).setErrStream(System.err);

            InputStream in=channel.getInputStream();
            channel.connect();
            byte[] tmp=new byte[1024];
            while(true){
                while(in.available()>0){
                    int i=in.read(tmp, 0, 1024);
                    if(i<0)break;
                    System.out.print(new String(tmp, 0, i));
                }
                if(channel.isClosed()){
                    System.out.println("exit-status: "+channel.getExitStatus());
                    break;
                }
                try{Thread.sleep(1000);}catch(Exception ee){}
            }
            channel.disconnect();
            session.disconnect();
            System.out.println("DONE");
        }catch(Exception e){
            e.printStackTrace();
        }

    }
}
