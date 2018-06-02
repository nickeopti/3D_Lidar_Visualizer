package pkg3dlidar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javafx.animation.Interpolator;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * @author Nicklas Boserup
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        Point[] points = getPoints();
        
        BorderPane root = new BorderPane();
        
        HBox settings = new HBox(10);
        settings.setPadding(new Insets(10));
        Slider v_x = new Slider(-Math.PI/2d, Math.PI/2d, 0);
        Slider v_z = new Slider(-Math.PI*4, Math.PI*4, 0);
        Slider z = new Slider(-1000, 300, 0);
        Slider focal = new Slider(5, 250, 200);
        Slider scaling = new Slider(0.1, 20, 1);

        Slider x = new Slider(-2000, 2000, 0);
        Slider y = new Slider(-2000, 2000, 0);
        Slider sy = new Slider(-2000, 2000, 0);
        
        settings.getChildren().addAll(v_x, v_z, z, focal, scaling, x, y, sy);
        HBox.setHgrow(v_x, Priority.ALWAYS);
        HBox.setHgrow(v_z, Priority.ALWAYS);
        HBox.setHgrow(z, Priority.ALWAYS);
        HBox.setHgrow(focal, Priority.ALWAYS);
        HBox.setHgrow(scaling, Priority.ALWAYS);
        HBox.setHgrow(x, Priority.ALWAYS);
        HBox.setHgrow(y, Priority.ALWAYS);
        HBox.setHgrow(sy, Priority.ALWAYS);
        
        ResizableCanvas canvas = new ResizableCanvas() {
            Point[] projectedPoints;

            @Override
            public void setProjectedPoints(Point... points) {
                projectedPoints = points;
            }

            @Override
            public void draw() {
                if (projectedPoints == null)
                    return;

                GraphicsContext gc = getGraphicsContext2D();
                gc.clearRect(0, 0, getWidth(), getHeight());

                for (int i = projectedPoints.length-1; i > 0; --i) {
                    Point p = projectedPoints[i];
                    if (p.z < 0)
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
        draw(canvas, v_x.getValue(), v_z.getValue(), focal.getValue(), z.getValue(), x.getValue(), y.getValue(), sy.getValue(), points);
        canvas.setOnScroll(me -> {
            if (!me.isShiftDown() && !me.isControlDown()) {
                v_x.setValue(v_x.getValue() + me.getDeltaY() / 720d);
                v_z.setValue(v_z.getValue() - me.getDeltaX() / 720d);
            } else if(me.isShiftDown() && !me.isControlDown()) {
                focal.setValue(focal.getValue() + me.getDeltaX() / 100d);
                y.setValue(y.getValue() - me.getDeltaY() / 10d);
            } else if (!me.isShiftDown() && me.isControlDown()) {
                x.setValue(x.getValue() + me.getDeltaY()*Math.sin(v_z.getValue()) / 10d - me.getDeltaX()*Math.cos(v_z.getValue()) / 10d);
                sy.setValue(sy.getValue() - me.getDeltaY()*Math.cos(v_z.getValue()) / 10d - me.getDeltaX()*Math.sin(v_z.getValue()) / 10d);

            }
            draw(canvas, v_x.getValue(), v_z.getValue(), focal.getValue(), z.getValue(), x.getValue(), y.getValue(), sy.getValue(), points);
        });

        Pane p = new Pane(canvas);
        canvas.widthProperty().bind(p.widthProperty());
        canvas.heightProperty().bind(p.heightProperty());
        root.setCenter(p);
        root.setBottom(settings);
        root.setStyle("-fx-base: rgb(50,50,50); -fx-focus-color: transparent;");
        
        Scene scene = new Scene(root, 900, 500);

        Transition move1 = new Transition() {
            {
                setCycleDuration(Duration.seconds(5));
                setCycleCount(1);
                setInterpolator(Interpolator.EASE_BOTH);
            }
            @Override
            protected void interpolate(double frac) {
                x.setValue((346.6237110397198 - -23.143080885953673) * frac + -23.143080885953673);
                y.setValue((-200 - -276.0) * frac + -276.0);
                sy.setValue((-142.86789514357395 - 34.16866685833426) * frac + 34.16866685833426);
                v_x.setValue((-0.38888888888888884 - -0.4041296601282294) * frac + -0.4041296601282294);
                v_z.setValue((1.1666666666666667 - 1.272370248375952) * frac + 1.272370248375952);
                focal.setValue((150 - 210) * frac + 210);
                draw(canvas, v_x.getValue(), v_z.getValue(), focal.getValue(), z.getValue(), x.getValue(), y.getValue(), sy.getValue(), points);
            }
        };
        Transition move2 = new Transition() {
            {
                setCycleDuration(Duration.seconds(5));
                setCycleCount(1);
                setInterpolator(Interpolator.EASE_BOTH);
            }
            @Override
            protected void interpolate(double frac) {
                x.setValue((-927.4145815319733 - 346.6237110397198) * frac + 346.6237110397198);
                y.setValue(-200);
                sy.setValue((336.7428105970479 - -142.86789514357395) * frac + -142.86789514357395);
                v_x.setValue(-0.38888888888888884);
                v_z.setValue(1.1666666666666667);
                focal.setValue((210 - 150) * frac + 150);
                draw(canvas, v_x.getValue(), v_z.getValue(), focal.getValue(), z.getValue(), x.getValue(), y.getValue(), sy.getValue(), points);
            }
        };
        Transition move3 = new Transition() {
            {
                setCycleDuration(Duration.seconds(5));
                setCycleCount(1);
                setInterpolator(Interpolator.EASE_BOTH);
            }
            @Override
            protected void interpolate(double frac) {
                x.setValue((-734.06512064928 - -927.4145815319733) * frac + -927.4145815319733);
                y.setValue((-524.0 - -200.0) * frac + -200.0);
                sy.setValue((249.70267056744578 - 336.7428105970479) * frac + 336.7428105970479);
                v_x.setValue(-0.38888888888888884);
                v_z.setValue(1.1666666666666667);
                draw(canvas, v_x.getValue(), v_z.getValue(), focal.getValue(), z.getValue(), x.getValue(), y.getValue(), sy.getValue(), points);
            }
        };
        Transition move4 = new Transition() {
            {
                setCycleDuration(Duration.seconds(5));
                setCycleCount(1);
                setInterpolator(Interpolator.EASE_BOTH);
            }
            @Override
            protected void interpolate(double frac) {
                x.setValue((-403.08814483523236 - -734.06512064928) * frac + -734.06512064928);
                y.setValue((-852.0 - -524.0) * frac + -524.0);
                sy.setValue((138.6070294243774 - 249.70267056744578) * frac + 249.70267056744578);
                v_x.setValue((-1.166666666666667 - -0.38888888888888884) * frac + -0.38888888888888884);
                v_z.setValue(1.1666666666666667);
                draw(canvas, v_x.getValue(), v_z.getValue(), focal.getValue(), z.getValue(), x.getValue(), y.getValue(), sy.getValue(), points);
            }
        };
        Transition move5 = new Transition() {
            {
                setCycleDuration(Duration.seconds(5));
                setCycleCount(1);
                setInterpolator(Interpolator.EASE_BOTH);
            }
            @Override
            protected void interpolate(double frac) {
                x.setValue((45.67455016555969 - -403.08814483523236) * frac + -403.08814483523236);
                y.setValue((-1256.0 - -852.0) * frac + -852.0);
                sy.setValue((12.999499268233212 - 138.6070294243774) * frac + 138.6070294243774);
                v_x.setValue((-1.5707963267948966 - -1.166666666666667) * frac + -1.166666666666667);
                v_z.setValue((-0.3888888888888892 - 1.1666666666666667) * frac + 1.1666666666666667);
                draw(canvas, v_x.getValue(), v_z.getValue(), focal.getValue(), z.getValue(), x.getValue(), y.getValue(), sy.getValue(), points);
            }
        };
        Transition move6 = new Transition() {
            {
                setCycleDuration(Duration.seconds(20));
                setCycleCount(1);
                setInterpolator(Interpolator.EASE_BOTH);
            }
            @Override
            protected void interpolate(double frac) {
                x.setValue((-23.143080885953673 - 45.67455016555969) * frac + 45.67455016555969);
                y.setValue((-276.0 - -1256.0) * frac + -1256.0);
                sy.setValue((34.16866685833426 - 12.999499268233212) * frac + 12.999499268233212);
                v_x.setValue((-0.4041296601282294 - -1.5707963267948966) * frac + -1.5707963267948966);
                v_z.setValue((7.555555555555538 - -0.3888888888888892) * frac + -0.3888888888888892);
                draw(canvas, v_x.getValue(), v_z.getValue(), focal.getValue(), z.getValue(), x.getValue(), y.getValue(), sy.getValue(), points);
            }
        };
        Transition seq = new SequentialTransition(move1, move2, move3, move4, move5, move6);
        seq.setCycleCount(1);

        scene.setOnKeyPressed(ke -> {
            if (ke.getCode() == KeyCode.P) {
                System.out.println(x.getValue() + "," + y.getValue() + "," + sy.getValue() + "\t" + v_x.getValue() + "," + v_z.getValue() + "\t" + focal.getValue());
            } else if (ke.getCode() == KeyCode.R) {
                seq.playFromStart();
            }
        });

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

    public void draw(ResizableCanvas canvas, double v_x, double v_z, double focal, double z, double x, double y, double sy, Point... points) {
        Task<Point[]> calculatePoints = new Task<Point[]>() {
            @Override
            protected Point[] call() throws Exception {
                //Point[] projectedPoints = getProjectedPoints(z, focal, getRotatedPoints(v_x, v_z, points));
                Point[] projectedPoints = getProjectedPoints(z, focal, getRotatedPoints(v_x, v_z, getTranslatedPoints(x, y, sy, points)));
                Arrays.sort(projectedPoints, Comparator.comparingDouble(p -> p.z));
                return projectedPoints;
            }
        };
        calculatePoints.setOnSucceeded(e -> {
            canvas.setProjectedPoints(calculatePoints.getValue());
            canvas.draw();
        });
        new Thread(calculatePoints).start();
    }

    public Point[] getPoints() throws IOException {
        File toRead = new File( System.getProperty("user.home") + "/Documents/3D_Torvet_28_04_2.log");
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

    public Point[] getTranslatedPoints(double x, double y, double z, Point... points) {
        Point[] translatedPoints = new Point[points.length];
        for (int i = 0; i < translatedPoints.length; i++) {
            translatedPoints[i] = new Point(points[i].x + x, points[i].y + y, points[i].z + z);
        }
        return translatedPoints;
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
                    -focalLength / (points[i].z) * points[i].x,
                    -focalLength / (points[i].z) * points[i].y,
                    points[i].z-z);
        }
        
        return projectedPoints;
    }
    
    public class Point {
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

        public abstract void setProjectedPoints(Point... points);

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
