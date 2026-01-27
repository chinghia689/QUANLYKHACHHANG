package com.customer.util;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Helper class for common UI animations.
 */
public class AnimationHelper {

    public enum Direction {
        LEFT, RIGHT, TOP, BOTTOM
    }

    /**
     * Fade in animation.
     */
    public static void fadeIn(Node node, Duration duration) {
        node.setOpacity(0);
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    /**
     * Fade in with default duration (300ms).
     */
    public static void fadeIn(Node node) {
        fadeIn(node, Duration.millis(300));
    }

    /**
     * Fade out animation.
     */
    public static void fadeOut(Node node, Duration duration) {
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.play();
    }

    /**
     * Slide in animation from specified direction.
     */
    public static void slideIn(Node node, Direction direction, Duration duration) {
        double startX = 0;
        double startY = 0;

        switch (direction) {
            case LEFT:
                startX = -50;
                break;
            case RIGHT:
                startX = 50;
                break;
            case TOP:
                startY = -50;
                break;
            case BOTTOM:
                startY = 50;
                break;
        }

        node.setTranslateX(startX);
        node.setTranslateY(startY);
        node.setOpacity(0);

        TranslateTransition translate = new TranslateTransition(duration, node);
        translate.setFromX(startX);
        translate.setFromY(startY);
        translate.setToX(0);
        translate.setToY(0);

        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(0);
        fade.setToValue(1);

        ParallelTransition parallel = new ParallelTransition(translate, fade);
        parallel.play();
    }

    /**
     * Slide in with default duration (400ms).
     */
    public static void slideIn(Node node, Direction direction) {
        slideIn(node, direction, Duration.millis(400));
    }

    /**
     * Add scale on hover effect to a node.
     */
    public static void addScaleOnHover(Node node, double scaleFactor) {
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), node);
        scaleUp.setToX(scaleFactor);
        scaleUp.setToY(scaleFactor);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), node);
        scaleDown.setToX(1);
        scaleDown.setToY(1);

        node.setOnMouseEntered(e -> scaleUp.playFromStart());
        node.setOnMouseExited(e -> scaleDown.playFromStart());
    }

    /**
     * Add scale on hover with default factor (1.05).
     */
    public static void addScaleOnHover(Node node) {
        addScaleOnHover(node, 1.05);
    }

    /**
     * Pulse animation for attention.
     */
    public static void pulse(Node node) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), node);
        scale.setFromX(1);
        scale.setFromY(1);
        scale.setToX(1.1);
        scale.setToY(1.1);
        scale.setCycleCount(2);
        scale.setAutoReverse(true);
        scale.play();
    }

    /**
     * Shake animation for error feedback.
     */
    public static void shake(Node node) {
        TranslateTransition translate = new TranslateTransition(Duration.millis(50), node);
        translate.setFromX(0);
        translate.setByX(10);
        translate.setCycleCount(6);
        translate.setAutoReverse(true);
        translate.setOnFinished(e -> node.setTranslateX(0));
        translate.play();
    }

    /**
     * Smooth content transition for page switching.
     */
    public static void transitionContent(Node oldContent, Node newContent, Runnable onComplete) {
        if (oldContent != null) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(150), oldContent);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                if (onComplete != null) {
                    onComplete.run();
                }
                fadeIn(newContent, Duration.millis(200));
            });
            fadeOut.play();
        } else {
            if (onComplete != null) {
                onComplete.run();
            }
            fadeIn(newContent, Duration.millis(200));
        }
    }
}
