/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package routeandriches;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import routeandriches.controller.GameController;
import routeandriches.controller.MainMenuController;

/**

 * Represents the Main component.

 */

public class Main extends Application {

    @Override
    /**
     * Executes start.
     */
    public void start(Stage stage) throws Exception {
        showMainMenu(stage);
        stage.show();
    }

    /**
     * Executes showMainMenu.
     */
    public void showMainMenu(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main-menu.fxml"));
        Parent root = loader.load();

        MainMenuController controller = loader.getController();
        controller.setMainApp(this);
        controller.setStage(stage);

        stage.setMinWidth(1100);
        stage.setMinHeight(600);
        switchSceneWithTransition(stage, root, "Route and Riches");
    }

    /**
     * Executes showGame.
     */
    public void showGame(Stage stage, String saveFilePath) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
        Parent root = loader.load();

        GameController controller = loader.getController();
        if (saveFilePath != null && !saveFilePath.isBlank()) {
            controller.loadGameFromFile(saveFilePath);
        }

        stage.setMinWidth(1100);
        stage.setMinHeight(600);
        switchSceneWithTransition(stage, root, "Route and Riches");

        // Keep launch behavior deterministic across DPI/display setups.
        stage.setMaximized(true);
        stage.toFront();
    }

    private void switchSceneWithTransition(Stage stage, Parent newRoot, String title) {
        Scene currentScene = stage.getScene();

        if (currentScene == null) {
            Scene firstScene = new Scene(newRoot, 1280, 720);
            stage.setTitle(title);
            stage.setScene(firstScene);
            newRoot.setOpacity(0.0);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(380), newRoot);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
            return;
        }

        double width = Math.max(1.0, currentScene.getWidth());
        double height = Math.max(1.0, currentScene.getHeight());
        Scene nextScene = new Scene(newRoot, width, height);
        stage.setTitle(title);
        stage.setScene(nextScene);

        newRoot.setOpacity(0.0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(260), newRoot);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    /**
     * Executes main.
     */
    public static void main(String[] args) {
        launch(args);
    }
}

