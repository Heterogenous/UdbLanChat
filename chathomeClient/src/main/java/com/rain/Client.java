package com.rain;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        //输入端口号
        Scanner scanner = new Scanner(System.in);
        //System.out.println("请输入聊天室的ip:");
        //System.out.println("请输入聊天室的端口号:");
        //int chatRoomPort = scanner.nextInt();
        System.out.print("请输入自己的端口号:");
        int port = scanner.nextInt();
        try {
            int chatRoomPort = 8000;//聊天室端口号
            String chatRoomIp = InetAddress.getLocalHost().getHostAddress();
            //创建套接字
            DatagramSocket socket = new DatagramSocket(port);
            //通知聊天室，我来了
            byte[] buffer = "---first---".getBytes(StandardCharsets.UTF_8);
            socket.send(new DatagramPacket(buffer, buffer.length,InetAddress.getByName(chatRoomIp),chatRoomPort));
            //接收线程
            new Thread(() -> {
                try {
                    //是否退出
                    //boolean isExit = false;
                    //创建接收数据数组
                    byte[] rData = new byte[1024 * 4];
                    //创建接收数据报
                    DatagramPacket rPacket = new DatagramPacket(rData,rData.length);
                    //循环接收数据
                    while (true){
                        //接收数据
                        socket.receive(rPacket);
                        //System.out.println("-------------"+rPacket.getPort());
                        //不打印自己端口号的信息
                        //打印数据(出现有方块，把rPacket.getData.length改成rPacket.getLength())
                        String info = new String(rPacket.getData(), 0,rPacket.getLength(),StandardCharsets.UTF_8);
                        //判断字符串字符是否是退出
                        if(info.contains("---exit---")) {
                            System.out.println("退出聊天室!");
                            return;
                        }

                        //截取信息
                        String msg = info.split("#")[0];
                        //System.out.println(msg);
                        //截取port和ip
                        String rport = info.split("#")[1].split(":")[1];
                        //System.out.println(rport);
                        String rip = info.split("#")[1].split(":")[0];
                        //System.out.println(rip);
                        //System.out.println("-----------"+info.split("#")[1]);
                        if(port !=Integer.parseInt(rport) || !InetAddress.getLocalHost().getHostAddress().equals(rip)) {
                            System.out.println(msg);
                        }else if(msg.contains("进入聊天室>")){
                            System.out.printf("欢迎您进入聊天室[%s:%d](输入exit可退出聊天室)\n",chatRoomIp,chatRoomPort);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();

            //发送线程
            new Thread(() -> {
                try {
                    boolean isExit = false;
                    //循环发送信息
                    while (!isExit) {
                        //输入信息
                        //System.out.print("我:");
                        String my = scanner.next();
                        byte[] sData ;
                        //创建数据组和数据报
                        DatagramPacket sPacket;
                        if(my.equals("exit")) {
                            //若退出,则不但给聊天室发，还给发给自己
                            sData = ("---"+my+"---").getBytes(StandardCharsets.UTF_8);//告诉接收组退出
                            sPacket = new DatagramPacket(sData,sData.length);
                            sPacket.setAddress(InetAddress.getByName(InetAddress.getLocalHost().getHostAddress()));
                            sPacket.setPort(port);
                            socket.send(sPacket);
                        }else {
                            sData = my.getBytes(StandardCharsets.UTF_8);
                            //创建数据组和数据报
                            sPacket = new DatagramPacket(sData,sData.length);
                        }
                        sPacket.setAddress(InetAddress.getByName(chatRoomIp));
                        sPacket.setPort(chatRoomPort);
                        //发送输数据报
                        socket.send(sPacket);
                        if(my.equals("exit")){
                            isExit = true;
                            scanner.close();
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

