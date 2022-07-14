package com.rain;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ChatRoom {
    public static void main(String[] args) {
        DatagramSocket socket = null;
        try {
            //创建套接字
            socket = new DatagramSocket(8000);//聊天室端口:8000
            //用hashset来接收转发信息接口
            HashSet<Integer> set = new HashSet<>();
            HashMap<Integer,String> map = new HashMap<>();
            //创建用于接收和转发的数据数组,要在64k以内
            byte[] rData = new byte[1024 * 4];
            byte[] sData = new byte[1024 * 4];
            //创建两个数据报，一个用于接收，一个用于转发
            DatagramPacket rPacket = new DatagramPacket(rData, rData.length);
            DatagramPacket sPacket = new DatagramPacket(sData, sData.length);
            System.out.printf("------------------聊天室<%s:%d>-----------------\n",InetAddress.getLocalHost().getHostAddress(),8000);

            //循环接收数据
            while (true){
                //接收数据
                socket.receive(rPacket);
                //将获取的端口\ip存入map中
                Integer rPort = rPacket.getPort();
                String rIp=rPacket.getAddress().getHostAddress();
                //创建信息和需要合并的信息
                String msg="";
                String info="";
                msg = new String(rPacket.getData(),0,rPacket.getLength(),StandardCharsets.UTF_8);
                //先判断是否存在这样的套接字
                if(msg.equals("---first---")){
                    //如果map里面不存在，则说明是第一次进入
                    map.put(rPort,rIp);//先存入map集合
                    info = "<"+rIp+":"+rPort+" ("+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+") 进入聊天室>";
                    //在聊天室打印信息
                    //System.out.printf("来自 %s:%d 的信息---> %s\n",rIp,rPort,msg);
                    System.out.println(info);
                }else if(msg.equals("---exit---")){
                    //从map中移除
                    map.remove(rPort);
                    //在聊天室打出退出信息
                    info = "<"+rIp+":"+rPort+" ("+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) +") 退出聊天室>";
                    System.out.println(info);
                    //msg = "exit#"+rIp+":"+rPort;
                    //再将退出消息发回
//                    sPacket.setData(msg.getBytes(StandardCharsets.UTF_8));
//                    sPacket.setPort(rPort);
//                    sPacket.setAddress(InetAddress.getByName(rIp));
//                    //将数据报发送出去
//                    socket.send(sPacket);
                }else{
                    //消息并且合并信息
                    info = "来自 "+rIp+":"+rPort+" 的消息---> "+msg;
                    //在聊天室打印信息
                    //System.out.printf("来自 %s:%d 的信息---> %s\n",rIp,rPort,msg);
                    System.out.println(info);
                }
                //循环转发数据
                for(Map.Entry<Integer,String> entry : map.entrySet()){
                    //将谁发送的端口放到数据末尾
                    String sInfo = info + "#"+rIp+":"+rPort;
                    //设置发送数据
                    sPacket.setData(sInfo.getBytes(StandardCharsets.UTF_8));
                    //设置发送的端口
                    sPacket.setPort(entry.getKey());
                    //设置发送的ip
                    sPacket.setAddress(InetAddress.getByName(entry.getValue()));
                    //将数据报发送出去
                    socket.send(sPacket);
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if(socket!=null)socket.close();
        }
    }
}
