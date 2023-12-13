package me.wy.gooloader.gooloader.util;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderStroke;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class RGBButton extends Button {

    public Thread thread;

    public boolean stop;

    public float hue;

    public RGBButton() {
        super();
        thread = new Thread(() -> {
            while(!stop) {
                Color color = Color.hsb(hue, 1, 0.9);
                hue = hue + 0.5f;
                if (hue > 360) {
                    hue = 0;
                }
                this.setStyle("-fx-border-width: 2; -fx-border-color: " + color.toString().replace("0x", "#") + ";");
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

}
