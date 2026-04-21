package routeandriches.controller;

import java.io.File;
import java.io.InputStream;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import routeandriches.Main;

public class MainMenuController {

    private Main mainApp;
    private Stage stage;

    @FXML
    private Label menuStatusLabel;
    @FXML
    private ImageView logoImageView;
    @FXML
    private Label logoFallbackLabel;

    @FXML
    private void initialize() {
        loadLogoImage();
        animateLogo();
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleNewGame() {
        if (mainApp == null || stage == null) {
            return;
        }

        try {
            mainApp.showGame(stage, null);
        } catch (Exception e) {
            setStatus("Failed to start new game");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLoadSaveFile() {
        if (mainApp == null || stage == null) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Save File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON Save Files", "*.json"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile == null) {
            setStatus("Load cancelled");
            return;
        }

        try {
            mainApp.showGame(stage, selectedFile.getAbsolutePath());
        } catch (Exception e) {
            setStatus("Failed to load: " + selectedFile.getName());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExitGame() {
        Platform.exit();
    }

    private void setStatus(String message) {
        if (menuStatusLabel != null) {
            menuStatusLabel.setText(message);
        }
    }

    private void loadLogoImage() {
        final String logoPath = "/routeandriches/assets/route-riches-logo.png";

        try (InputStream stream = getClass().getResourceAsStream(logoPath)) {
            if (stream == null) {
                showFallbackLogo("Add logo file at routeandriches/assets/route-riches-logo.png");
                return;
            }

            Image logo = new Image(stream);
            if (logo.isError()) {
                showFallbackLogo("Could not decode logo image");
                return;
            }

            logoImageView.setImage(logo);
            logoImageView.setVisible(true);
            logoImageView.setManaged(true);
            logoFallbackLabel.setVisible(false);
            logoFallbackLabel.setManaged(false);
            setStatus("Ready");
        } catch (Exception e) {
            showFallbackLogo("Failed loading logo image");
        }
    }

    private void animateLogo() {
        TranslateTransition floatLogo = new TranslateTransition(Duration.seconds(2.4), logoImageView);
        floatLogo.setFromY(0.0);
        floatLogo.setToY(-5.0);
        floatLogo.setAutoReverse(true);
        floatLogo.setCycleCount(TranslateTransition.INDEFINITE);
        floatLogo.setInterpolator(Interpolator.EASE_BOTH);
        floatLogo.play();
    }

    private void showFallbackLogo(String message) {
        logoImageView.setVisible(false);
        logoImageView.setManaged(false);
        logoFallbackLabel.setVisible(true);
        logoFallbackLabel.setManaged(true);
        logoFallbackLabel.setContentDisplay(ContentDisplay.CENTER);
        setStatus(message);
    }
}
