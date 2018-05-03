package pkg3dlidar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * @author Nicklas Boserup
 */
public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) throws IOException {
        //Point[] points = getPoints();
        ObservableList<Point> pointList = FXCollections.observableArrayList();
        
        BorderPane root = new BorderPane();
        
        HBox settings = new HBox(10);
        settings.setPadding(new Insets(10));
        Slider v_x = new Slider(-Math.PI/2d, Math.PI/2d, 0);
        Slider v_z = new Slider(-Math.PI*2, Math.PI*2, 0);
        Slider z = new Slider(-1000, 300, 0);
        Slider focal = new Slider(5, 70, 20);
        Slider scaling = new Slider(0.1, 20, 1);
        
        settings.getChildren().addAll(v_x, v_z, z, focal, scaling);
        HBox.setHgrow(v_x, Priority.ALWAYS);
        HBox.setHgrow(v_z, Priority.ALWAYS);
        HBox.setHgrow(z, Priority.ALWAYS);
        HBox.setHgrow(focal, Priority.ALWAYS);
        HBox.setHgrow(scaling, Priority.ALWAYS);

        ResizableCanvas canvas = new ResizableCanvas() {
            @Override
            public void draw() {
                Point[] points = pointList.toArray(new Point[0]);
                Point[] projectedPoints = getProjectedPoints(z.getValue(), focal.getValue(), getRotatedPoints(v_x.getValue(), v_z.getValue(), points));
                
                Arrays.sort(projectedPoints, Comparator.comparingDouble(p -> p.z));
                
                GraphicsContext gc = getGraphicsContext2D();
                gc.clearRect(0, 0, getWidth(), getHeight());
                
                //for (Point p : projectedPoints) {
                for (int i = projectedPoints.length-1; i > 0; --i) {
                    Point p = projectedPoints[i];
                    if (p.z <= focal.getValue())
                        continue;
                    gc.setFill(Color.hsb(p.z/2000d*360, 1, 1));
                    double radius = 4, scale = scaling.getValue();
                    gc.fillOval(
                            p.x*scale - radius/2 + getWidth()/2,
                            p.y*scale - radius/2 + getHeight()*0.75,
                            radius, radius);
                }
            }
        };
        pointList.addListener((ListChangeListener<Point>) c -> canvas.draw());
        
        v_x.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> canvas.draw());
        v_z.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> canvas.draw());
        z.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> canvas.draw());
        focal.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> canvas.draw());
        scaling.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> canvas.draw());
        
        Pane p = new Pane(canvas);
        canvas.widthProperty().bind(p.widthProperty());
        canvas.heightProperty().bind(p.heightProperty());
        root.setCenter(p);
        root.setBottom(settings);
        root.setStyle("-fx-base: rgb(50,50,50); -fx-focus-color: transparent;");
        
        Scene scene = new Scene(root, 900, 500);
        
        primaryStage.setTitle("3D Lidarscanning - Eksamensprojekt");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    public Point[] getPoints() throws IOException {
        File toRead = new File("/home/nicke/Documents/3D_Aula_3.log");
        List<String> data = Files.readAllLines(toRead.toPath());
        
        ArrayList<Point> points = new ArrayList<>();
        
        for (int n = 0; n < data.size(); n++) {
            int distance = Integer.parseInt(data.get(n));
            if (distance == 1)
                continue;
            distance += 5;
            
            double v_x = -(n % 400) * 2*Math.PI / 400d;
            double v_z = (n/400) * Math.PI / 180d;
            
            double[] r_x0 = {1, 0, 0};
            double[] r_x1 = {0, Math.cos(v_x), -Math.sin(v_x)};
            double[] r_x2 = {0, Math.sin(v_x), Math.cos(v_x)};
            Matrix r_x = new Matrix(r_x0, r_x1, r_x2);

            double[] r_y0 = {Math.cos(v_x), 0, Math.sin(v_x)};
            double[] r_y1 = {0, 1, 0};
            double[] r_y2 = {-Math.sin(v_x), 0, Math.cos(v_x)};
            Matrix r_y = new Matrix(r_y0, r_y1, r_y2);
            
            double[] r_z0 = {Math.cos(v_z), -Math.sin(v_z), 0};
            double[] r_z1 = {Math.sin(v_z), Math.cos(v_z), 0};
            double[] r_z2 = {0, 0, 1};
            Matrix r_z = new Matrix(r_z0, r_z1, r_z2);

            Matrix r = r_y.multiplyMatrix(r_z);
            
            double[] vector = {distance, 0, 0};
            Matrix rotatedVector = r.multiplyMatrix(new Matrix(vector).transform());
            points.add(new Point(rotatedVector.getValue(0, 0), rotatedVector.getValue(1, 0), rotatedVector.getValue(2, 0)));
        }
        
        return points.toArray(new Point[0]);
    }
    
    public Point[] getRotatedPoints(double v_x, double v_z, Point... points) {
        double[] r_x0 = {1, 0, 0};
        double[] r_x1 = {0, Math.cos(v_x), -Math.sin(v_x)};
        double[] r_x2 = {0, Math.sin(v_x), Math.cos(v_x)};
        Matrix r_x = new Matrix(r_x0, r_x1, r_x2);
        
        double[] r_y0 = {Math.cos(v_z), 0, Math.sin(v_z)};
        double[] r_y1 = {0, 1, 0};
        double[] r_y2 = {-Math.sin(v_z), 0, Math.cos(v_z)};
        Matrix r_y = new Matrix(r_y0, r_y1, r_y2);
        
        double[] r_z0 = {Math.cos(v_z), -Math.sin(v_z), 0};
        double[] r_z1 = {Math.sin(v_z), Math.cos(v_z), 0};
        double[] r_z2 = {0, 0, 1};
        Matrix r_z = new Matrix(r_z0, r_z1, r_z2);
        
        Matrix r = r_x.multiplyMatrix(r_y);
        
        Point[] rotatedPoints = new Point[points.length];
        for (int i = 0; i < rotatedPoints.length; i++) {
            double[] vector = {points[i].x, points[i].y, points[i].z};
            Matrix rotatedVector = r.multiplyMatrix(new Matrix(vector).transform());
            rotatedPoints[i] = new Point(rotatedVector.getValue(0, 0), rotatedVector.getValue(1, 0), rotatedVector.getValue(2, 0));
        }
        
        return rotatedPoints;
    }
    
    public Point[] getProjectedPoints(double z, double focalLength, Point... points) {
        Point[] projectedPoints = new Point[points.length];
        for (int i = 0; i < projectedPoints.length; i++) {
            projectedPoints[i] = new Point(
                    -focalLength / (points[i].z-z-focalLength) * points[i].x,
                    -focalLength / (points[i].z-z-focalLength) * points[i].y,
                    points[i].z-z);
        }
        
        return projectedPoints;
    }
    
    public static class Point {
        public double x, y, z;
        public Point(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        @Override
        public String toString() {
            return "(" + x + ", " + y + "," + z + ")";
        }
    }
    
    public abstract class ResizableCanvas extends Canvas {

        public abstract void draw();
        
        public ResizableCanvas() {
            widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> draw());
            heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> draw());
        }
        
        @Override
        public boolean isResizable() {
            return true;
        }

        @Override
        public double prefWidth(double height) {
            return getWidth();
        }

        @Override
        public double prefHeight(double width) {
            return getHeight();
        }
        
    }
    
}
