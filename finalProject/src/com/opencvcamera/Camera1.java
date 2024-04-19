package com.opencvcamera;


import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.CvType;
//import org.opencv.core.CvType.CV_8U;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat; 
import org.opencv.core.MatOfInt;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.opencv.highgui.HighGui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.*;

public class Camera1 extends Thread implements ActionListener {
    private JFrame frame; 
    private JPanel buttonPanel;
    JPanel cameraPanel;
    
    public JButton bt1; 
    Mat frameMat;

    public Camera1() {
        frame = new JFrame("Face Detection");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Camera panel in the center
        cameraPanel = new JPanel(new FlowLayout());
        frame.getContentPane().add(BorderLayout.CENTER, cameraPanel);
        cameraPanel.setPreferredSize(new Dimension(640, 40));

        // Button panel at the bottom
         buttonPanel = new JPanel(new FlowLayout());
       frame.getContentPane().add(BorderLayout.SOUTH, buttonPanel);

        bt1 = new JButton("Save");
        bt1.setBounds(750, 500, 80, 30);
        bt1.addActionListener(this);
        buttonPanel.add(bt1);
       // frame.add(buttonPanel);

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public void run() {
        frame.setVisible(true);

        VideoCapture capture = new VideoCapture(0); // 0 corresponds to the default camera

        if (!capture.isOpened()) {
            System.out.println("Error: Camera not detected!");
            return;
        }

        frameMat = new Mat();
        MatOfRect faceDetections = new MatOfRect();

        CascadeClassifier faceCascade = new CascadeClassifier();
        faceCascade.load("C:\\Users\\ashis\\eclipse-workspace\\finalProject\\haarcascade_frontalface_default.xml"); // Make sure to provide the correct path

        BufferedImage bufferedImage;

        while (true) {
            if (capture.read(frameMat)) {
               // Imgproc.cvtColor(frameMat, frameMat, Imgproc.COLOR_BGR2RGB);

                faceCascade.detectMultiScale(frameMat, faceDetections, 1.1, 3, 0, new Size(30, 30), new Size());

                for (Rect rect : faceDetections.toArray()) {
                    Imgproc.rectangle(frameMat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0), 2);
                }

                bufferedImage = matToBufferedImage(frameMat);
                cameraPanel.getGraphics().drawImage(bufferedImage, 0, 0, cameraPanel.getWidth(), cameraPanel.getHeight(), null);
                buttonPanel.add(bt1);

            }
        }
    }

    private BufferedImage matToBufferedImage(Mat matrix) {
        int cols = matrix.cols();
        int rows = matrix.rows();
        int elemSize = (int) matrix.elemSize();
        byte[] data = new byte[cols * rows * elemSize];
        int type;

        matrix.get(0, 0, data);

        switch (matrix.channels()) {
            case 1:
                type = BufferedImage.TYPE_BYTE_GRAY;
                break;
            case 3:
                type = BufferedImage.TYPE_3BYTE_BGR;
                // bgr to rgb
                byte b;
                for (int i = 0; i < data.length; i = i + 3) {
                    b = data[i];
                    data[i] = data[i + 2];
                    data[i + 2] = b;
                }
                break;
            default:
                return null;
        }

        BufferedImage image2 = new BufferedImage(cols, rows, type);
        image2.getRaster().setDataElements(0, 0, cols, rows, data);

        return image2;
    }
    
    
    public void actionPerformed(ActionEvent e)
    {
    	if(e.getSource()==bt1) {
    		dataloger();
    		imagesaver();
    	}
    }
    
    public void dataloger()
    {
    	
    	
    	String url = "jdbc:oracle:thin:@localhost:1521:xe";  // Corrected JDBC URL
        String username = "system";
        String password = "9852";
        String name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        
        String sql="insert into timelog values(?)";
        
        try {
        	Class.forName("oracle.jdbc.driver.OracleDriver");

            // Establish the connection
            Connection con = DriverManager.getConnection(url, username, password);

            // Create a statement
            Statement st = con.createStatement();
            
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(1, name);
            preparedStatement.executeUpdate();

        	
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Connection failed");
            if (e instanceof SQLException) {
                SQLException sqlException = (SQLException) e;
                System.out.println("SQL Error: " + sqlException.getMessage());
                sqlException.printStackTrace();
            } else {
                e.printStackTrace();
            }
        }
    }
    
    
    public void imagesaver() {
    	String name = JOptionPane.showInputDialog(this, "Enter the image name");
        if (name == null) {
            name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        }
        // Write to file
        Imgcodecs.imwrite("projectimages/" + name + ".jpg", frameMat);
    }
    
    public static void main(String args[]) {
    	Camera1 ob=new Camera1();
		ob.start();
    }

    
}
