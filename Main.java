package pkg3dlidar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javafx.animation.Animation;
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

    private Slider v_x, v_z, focal, scaling, x, y, z;
    private ResizableCanvas canvas;
    Point[] points;
    double radius = 2.5;

    @Override
    public void start(Stage primaryStage) throws IOException {
        points = getPoints();
        
        BorderPane root = new BorderPane();
        
        HBox settings = new HBox(10);
        settings.setPadding(new Insets(10));
        v_x = new Slider(-Math.PI/2d, Math.PI/2d, 0);
        v_z = new Slider(-Math.PI*4, Math.PI*4, 0);
        focal = new Slider(5, 500, 200);
        scaling = new Slider(0.1, 20, 1);

        x = new Slider(-2000, 2000, 0);
        y = new Slider(-2000, 2000, 0);
        z = new Slider(-2000, 2000, 0);
        
        settings.getChildren().addAll(v_x, v_z, focal, scaling, x, y, z);
        HBox.setHgrow(v_x, Priority.ALWAYS);
        HBox.setHgrow(v_z, Priority.ALWAYS);
        HBox.setHgrow(focal, Priority.ALWAYS);
        HBox.setHgrow(scaling, Priority.ALWAYS);
        HBox.setHgrow(x, Priority.ALWAYS);
        HBox.setHgrow(y, Priority.ALWAYS);
        HBox.setHgrow(z, Priority.ALWAYS);
        
        canvas = new ResizableCanvas() {
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
                    double scale = scaling.getValue();
                    gc.fillOval(
                            p.x*scale - radius/2 + getWidth()/2,
                            p.y*scale - radius/2 + getHeight()*0.75,
                            radius, radius);
                }
            }
        };
        draw(canvas, v_x.getValue(), v_z.getValue(), focal.getValue(), x.getValue(), y.getValue(), z.getValue());
        canvas.setOnScroll(me -> {
            if (!me.isShiftDown() && !me.isControlDown()) {
                v_x.setValue(v_x.getValue() + me.getDeltaY() / 720d);
                v_z.setValue(v_z.getValue() - me.getDeltaX() / 720d);
            } else if(me.isShiftDown() && !me.isControlDown()) {
                focal.setValue(focal.getValue() + me.getDeltaX() / 100d);
                y.setValue(y.getValue() - me.getDeltaY() / 10d);
            } else if (!me.isShiftDown() && me.isControlDown()) {
                x.setValue(x.getValue() + me.getDeltaY()*Math.sin(v_z.getValue()) / 10d - me.getDeltaX()*Math.cos(v_z.getValue()) / 10d);
                z.setValue(z.getValue() - me.getDeltaY()*Math.cos(v_z.getValue()) / 10d - me.getDeltaX()*Math.sin(v_z.getValue()) / 10d);

            }
            draw(canvas, v_x.getValue(), v_z.getValue(), focal.getValue(), x.getValue(), y.getValue(), z.getValue());
        });

        Pane p = new Pane(canvas);
        canvas.widthProperty().bind(p.widthProperty());
        canvas.heightProperty().bind(p.heightProperty());
        root.setCenter(p);
        root.setBottom(settings);
        root.setStyle("-fx-base: rgb(50,50,50); -fx-focus-color: transparent;");
        
        Scene scene = new Scene(root, 900, 500);


        // General show-off
        Transition move1 = cameraTransition(-0.38888888888888895, 1.2222222222222225, 202.00000000000003, 454.8901054188032, -156.0, -190.85747921887472,
                -0.1819074379060071, 1.283185307179604, 250.0, -67.99154384377512, -144.0, 10.087008724062809);
        Transition move2 = cameraTransition(-0.38888888888888895, 1.2222222222222225, 202.00000000000003, -706.7777125403236, -156.0, 231.30822369654925,
                -0.38888888888888895, 1.2222222222222225, 202.00000000000003, 454.8901054188032, -156.0, -190.85747921887472);
        Transition move3 = cameraTransition(-0.8333333333333336, 1.2222222222222225, 202.00000000000003, -590.2349864667545, -512.0, 188.95503020017992,
                -0.38888888888888895, 1.2222222222222225, 202.00000000000003, -706.7777125403236, -156.0, 231.30822369654925);
        Transition move4 = cameraTransition(-1.5707963267948966, -0.38888888888888895, 250.0, 67.79158071525913, -1252.0, 65.97306792418387,
                -0.8333333333333336, 1.2222222222222225, 202.00000000000003, -590.2349864667545, -512.0, 188.95503020017992);
        Transition move5 = cameraTransition(-0.1819074379060071, 7.56637061435919, 250.0, -67.99154384377512, -144.0, 10.087008724062809,
                -1.5707963267948966, -0.38888888888888895, 250.0, 67.79158071525913, -1252.0, 65.97306792418387, 30);

        // Drone 156
        Transition move6 = cameraTransition(-0.44444444444444453, 1.2222222222222225, 498.80000000000007, -681.8028481418011, -284.0, 254.50197926729413,
                -0.44444444444444453, 1.2222222222222225, 498.80000000000007, -754.5984929448392, -428.0, 276.700945407857, 6);
        Transition move7 = cameraTransition(-0.2777777777777778, 1.166666666666667, 499.6, -433.79550226537975, -112.0, 122.33406942782315,
                -0.44444444444444453, 1.2222222222222225, 498.80000000000007, -681.8028481418011, -284.0, 254.50197926729413, 9);
        Transition move8 = cameraTransition(-0.26211942666666666, 1.2738631900000004, 500.0, -102.50189509131866, -126.86122599999999, 19.370578834418026,
                -0.2777777777777778, 1.166666666666667, 499.6, -433.79550226537975, -112.0, 122.33406942782315, 7);
        Transition move9 = cameraTransition(-0.31767498222222224, 7.884974301111091, 500.0, -102.50189509131866, -126.86122599999999, 19.370578834418026,
                -0.26211942666666666, 1.2738631900000004, 500.0, -102.50189509131866, -126.86122599999999, 19.370578834418026, 18);

        // Drone 154
        Transition move10 = cameraTransition(-1.5707963267948966, -0.3888888888888886, 495.2000000000003, 19.223428528442927, -556.0, 118.74837632763084,
                -1.5707963267948966, -0.3888888888888886, 492.8000000000004, 11.640218450097734, -1184.0, 100.24175791940502, 30);
        Transition move11 = cameraTransition(-1.5707963267948966, -0.3888888888888886, 496.4000000000002, 26.138598956481026, -356.0, 146.17428411481603,
                -1.5707963267948966, -0.3888888888888886, 495.2000000000003, 19.223428528442927, -556.0, 118.74837632763084, 14);
        Transition move12 = cameraTransition(-0.1819074379060071, -0.3888888888888886, 498.80000000000007, -34.52708167028052, -228.0, -1.8786631509906444,
                -1.5707963267948966, -0.3888888888888886, 496.4000000000002, 26.138598956481026, -356.0, 146.17428411481603, 7);
        Transition move13 = cameraTransition(-0.070796326794896, 1.9444444444444453, 500.0, -47.23641980546831, -36.0, -93.08432484019427,
                -0.1819074379060071, -0.3888888888888886, 498.80000000000007, -34.52708167028052, -228.0, -1.8786631509906444, 11);

        // Linear show-offs
        Transition move14 = cameraTransition(-1.5707963267948966, -0.3888888888888886, 500.0, 4.375189446221631, -360.60319985609567, 103.61089582230049,
                -1.5707963267948966, -0.38888888888888895, 438.053441295546, 55.02529743339124, -1536.0, 78.22773999329186, 25);
        Transition move15 = cameraTransition(-0.12635188235045156, 1.2777777777777783, 500.0, -1433.8929639514379, -108.60319985609567, 556.2213914778429,
                -0.12635188235045156, 1.2777777777777783, 500.0, 418.1713937593736, -108.60319985609567, -173.85368335844052, 25);
        Transition move16 = cameraTransition(-0.01524077123934045, 1.2222222222222228, 500.0, -486.3935541675944, -68.60319985609567, 165.44174717386008,
                -0.4041296601282294, 1.2222222222222228, 500.0, -493.9124397207279, -552.6031998560957, 168.17421127040004, 25);
        Transition move17 = cameraTransition(-0.45968521568378495, 12.166666666666632, 500.0, -241.26356484064917, -120.60319985609567, 14.679123858390922,
                -0.45968521568378495, 5.999999999999987, 500.0, -241.26356484064917, -220.60319985609567, 14.679123858390922, 25);
        Transition move18 = cameraTransition(-0.7374629934615629, 7.510815058803635, 499.6, 204.12488541462113, -412.60319985609567, -81.0310088625334,
                -0.7930185490171184, 7.510815058803635, 500.0, -1098.3584115854858, -948.6031998560957, 482.04737026040846, 25);

        //Transition seq = new SequentialTransition(move1, move2, move3, move4, move5); // General show-off
        //Transition seq = new SequentialTransition(move6, move7, move8, move9); // Drone 156
        //Transition seq = new SequentialTransition(move10, move11, move12, move13); // Drone 154
        //Transition seq = new SequentialTransition(move14, move15, move16, move17, move18); // Linear show-offs
        Transition seq = new SequentialTransition(move15, move16, move17, move14, move18, move1, move2, move3, move4, move5); // Linear show-offs
        seq.setCycleCount(-1);

        scene.setOnKeyPressed(ke -> {
            switch (ke.getCode()) {
                case P:
                    System.out.println(v_x.getValue() + ", " + v_z.getValue() + ", " + focal.getValue() + ", " + x.getValue() + ", " + y.getValue() + ", " + z.getValue());
                    break;
                case R: case SPACE: case PAGE_DOWN: case PAGE_UP:
                    if (ke.isControlDown() || ke.isShiftDown() || ke.getCode() == KeyCode.PAGE_UP) {
                        seq.playFromStart();
                        break;
                    }
                    if (seq.getStatus() == Animation.Status.RUNNING)
                        seq.pause();
                    else
                        seq.play();
                    break;
                case PLUS: case ADD: case F5:
                    radius++;
                    draw(canvas, v_x.getValue(), v_z.getValue(), focal.getValue(), x.getValue(), y.getValue(), z.getValue());
                    break;
                case MINUS: case SUBTRACT: case PERIOD:
                    if (radius > 1)
                        radius--;
                    draw(canvas, v_x.getValue(), v_z.getValue(), focal.getValue(), x.getValue(), y.getValue(), z.getValue());
                    break;
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

    public void draw(ResizableCanvas canvas, double v_x, double v_z, double focal, double x, double y, double z) {
        Task<Point[]> calculatePoints = new Task<Point[]>() {
            @Override
            protected Point[] call() throws Exception {
                Point[] projectedPoints = Camera.getProjectedPoints(focal, Camera.getRotatedPoints(v_x, 0, v_z, Camera.getTranslatedPoints(x, y, z, points)));
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
        //File toRead = new File( System.getProperty("user.home") + "/Documents/3D_Torvet_28_04_2.log");
        File toRead = new File( System.getProperty("user.home") + "/Documents/3D-scanning.log");
        List<String> data = Files.readAllLines(toRead.toPath());
        
        ArrayList<Point> points = new ArrayList<>();
        
        for (int n = 0; n < data.size(); n++) {
            int distance = 1;
            try {
                distance = Integer.parseInt(data.get(n));
            } catch(Exception e) {
                continue;
            }
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

    public Transition cameraTransition(double new_v_x, double new_v_z, double new_focal, double new_x, double new_y, double new_z,
                                       double old_v_x, double old_v_z, double old_focal, double old_x, double old_y, double old_z) {
        return cameraTransition(new_v_x, new_v_z, new_focal, new_x, new_y, new_z, old_v_x, old_v_z, old_focal, old_x, old_y, old_z, 5);
    }

    public Transition cameraTransition(double new_v_x, double new_v_z, double new_focal, double new_x, double new_y, double new_z,
                                       double old_v_x, double old_v_z, double old_focal, double old_x, double old_y, double old_z,
                                       double duration) {
        Transition move = new Transition() {
            {
                setCycleDuration(Duration.seconds(duration));
                setCycleCount(1);
                setInterpolator(Interpolator.EASE_BOTH);
            }
            @Override
            protected void interpolate(double frac) {
                v_x.setValue((new_v_x - old_v_x) * frac + old_v_x);
                v_z.setValue((new_v_z - old_v_z) * frac + old_v_z);
                focal.setValue((new_focal - old_focal) * frac + old_focal);
                x.setValue((new_x - old_x) * frac + old_x);
                y.setValue((new_y - old_y) * frac + old_y);
                z.setValue((new_z - old_z) * frac + old_z);
                draw(canvas, v_x.getValue(), v_z.getValue(), focal.getValue(), x.getValue(), y.getValue(), z.getValue());
            }
        };

        return move;
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
