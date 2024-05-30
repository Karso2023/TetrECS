package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Visual User Interface component representing a single block in the grid.
 *
 * Extends Canvas and is responsible for drawing itself.
 *
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 *
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {
    private static final Logger logger = LogManager.getLogger(GameBlock.class);

    /**
     * For fade out method
     */
    private AnimationTimer animationTimer = null;

    /**
     * To design whether to use hover effect or not
     */
    private boolean hover = false;

    /**
     * Check if it is the middle block, then put the ot on it
     */
    private boolean isMiddleBlock = false;

    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {

            Color.TRANSPARENT,
            Color.LIGHTPINK,
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.YELLOWGREEN,
            Color.LIME,
            Color.GREEN,
            Color.DARKGREEN,
            Color.DARKTURQUOISE,
            Color.DEEPSKYBLUE,
            Color.AQUA,
            Color.AQUAMARINE,
            Color.BLUE,
            Color.MEDIUMPURPLE,
            Color.PURPLE
    };

    /**
     * gameBoard
     */
    private final GameBoard gameBoard;

    /**
     * Width
     */
    private final double width;

    /**
     * Height
     */
    private final double height;

    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);

    /**
     * Create a new single Game Block
     * @param gameBoard the board this block belongs to
     * @param x the column the block exists in
     * @param y the row the block exists in
     * @param width the width of the canvas to render
     * @param height the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        this.isMiddleBlock = (x == 1 && y == 1);

        //A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        //Do an initial paint
        paint();

        //Hover effect started
        setOnMouseEntered((e) -> {
            hover = true;
            paint();
        });

        //Hover effect ended
        setOnMouseExited((e) -> {
            hover = false;
            paint();
        });

        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);
    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }

    /**
     * Handle painting of the block canvas
     */
    public void paint() {
        GraphicsContext gc = getGraphicsContext2D();
        //If the block is empty, paint as empty
        if(value.get() == 0) {
            paintEmpty();
        } else {
            //If the block is not empty, paint with the colour represented by the value
            paintColor(COLOURS[value.get()]);
        }

        if(hover) {
            gc.setFill(Color.color(1, 0.3, 0.5, 0.5));
            gc.fillRect(0, 0, width, height);
        }

        if(isMiddleBlock) {
            Circle circle = new Circle();
            circle.setFill(Color.color(1,1, 1, 0.5));
            circle.getFill();
        }


    }

    /**
     * Paint this canvas empty
     */
    private void paintEmpty() {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Fill
        gc.setFill(Color.WHITE);

        //Set empty tiles
        DropShadow ds = new DropShadow();
        ds.setOffsetX(3.5);
        ds.setOffsetY(3.5);
        ds.setColor(Color.BLACK);
        gc.setEffect(ds);
        gc.fillRect(0,0, width, height);

        //Border
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Paint this canvas with the given colour
     * @param colour the colour to paint
     */
    private void paintColor(Paint colour) {
        var gc = getGraphicsContext2D();


        //Clear
        gc.clearRect(0,0,width,height);

        //Colour fill
        gc.setFill(colour);
        gc.fillRect(0,0, width, height);

        //Filled tiles
        DropShadow ds = new DropShadow();
        ds.setOffsetX(8);
        ds.setOffsetY(8);
        ds.setColor(Color.BLACK);
        gc.setEffect(ds);

        //Border
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Use this to flash and then fades out to indicate a cleared block
     */
    public void fadeOut(){
        animationTimer = new AnimationTimer() {
            double op = 1;
            @Override
            public void handle(long now) {

                paintEmpty();
                op -= 0.035;
                if(op <= 0.2){
                    this.stop();
                }
                else {
                    var gc = getGraphicsContext2D();
                    gc.setFill(Color.color(0.9, 0.3, 1, op));

                    gc.fillRect(0, 0, width, height);
                }

            }
        };
        animationTimer.start();
    }

    /**
     * Get the column of this block
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing it's colour
     * @return value
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }

    /**
     * To string
     * @return String
     */
    @Override
    public String toString() {
        return "GameBlock{" +
                "x=" + x +
                ", y=" + y +
                ", value=" + value.toString() +
                '}';
    }

}