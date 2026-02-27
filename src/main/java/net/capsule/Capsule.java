package net.capsule;

import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.swing.JFrame;
import javax.swing.UIManager;

import org.json.JSONObject;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.Animation;
import me.ramazanenescik04.diken.game.Config;
import me.ramazanenescik04.diken.gui.window.OptionWindow;
import me.ramazanenescik04.diken.log.ConsoleLog;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.IOResource;
import me.ramazanenescik04.diken.resource.IResource;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import net.capsule.account.Account;
import net.capsule.gui.DockView;
import net.capsule.studio.StudioPanel;
import net.capsule.util.CapsuleApiClient;
import net.capsule.util.ImageUtil;
import net.capsule.util.StoragePaths;

public class Capsule {
    public static final Version version = new Version("0.4.0");
    public static Capsule instance;

    public Account account;
    public DikenEngine gameEngine;
    public JFrame gameFrame;

    private final StoragePaths storagePaths;
    private final CapsuleApiClient apiClient;
    private final ImageUtil imageUtil;

    public Capsule(StoragePaths storagePaths, CapsuleApiClient apiClient, ImageUtil imageUtil) {
        this.storagePaths = storagePaths;
        this.apiClient = apiClient;
        this.imageUtil = imageUtil;

        account = Account.getAccountLocalFile();
        gameEngine = new DikenEngine(320 * 4, 240 * 4, 2);

        gameFrame = new JFrame("Capsule");
        gameFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        gameFrame.add(gameEngine);
        setWindowIcon();
        gameFrame.pack();
        gameFrame.setLocationRelativeTo(null);
        gameFrame.setVisible(true);

        gameFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });

        gameEngine.start();
        Thread.startVirtualThread(Capsule.this::checkUpdate);
    }

    private void setWindowIcon() {
        try {
            Bitmap icon = (Bitmap) IOResource
                    .loadResource(URI.create("http://capsule.net.tr/favicon.png").toURL().openStream(), EnumResource.IMAGE);
            gameFrame.setIconImage(Toolkit.getDefaultToolkit().createImage(icon.toBytes("png")));
        } catch (MalformedURLException ignored) {
        } catch (IOException ignored) {
        }
    }

    public void close() {
        gameEngine.stop();

        if (account != null) {
            account.saveAccountLocalFile();
        }

        gameFrame.dispose();
    }

    public static void main(String[] args) {
        log("Starting Capsule " + version);

        StoragePaths storagePaths = new StoragePaths();
        CapsuleApiClient apiClient = new CapsuleApiClient(storagePaths);
        ImageUtil imageUtil = new ImageUtil();

        try {
            Files.createDirectories(storagePaths.getDirectoryPath());
            Files.createDirectories(storagePaths.getLogsPath());
            Files.createDirectories(storagePaths.getCachePath());
            Files.createDirectories(storagePaths.getVersionsPath());

            Config.defaultConfigFile = storagePaths.getDirectoryPath().resolve("config.dat").toFile();
            ConsoleLog.setLogDirectory(storagePaths.getLogsPath().toFile());
        } catch (IOException e) {
            failAndExit("Uygulama klasörleri oluşturulamadı: " + e.getMessage(), 1);
        }

        instance = new Capsule(storagePaths, apiClient, imageUtil);
        Map<String, String> argMap = parseArgs(args);

        if (argMap.containsKey("apikey")) {
            String apikey = argMap.get("apikey");
            try {
                Account loggedAccount = apiClient.login(UUID.fromString(apikey));
                if (loggedAccount == null) {
                    failAndExit("API_KEY geçerli değil. Lütfen Geçerli bir API_KEY gir", 1);
                }
                instance.account = loggedAccount;
            } catch (Exception e) {
                failAndExit("API_KEY doğrulanamadı: " + e.getMessage(), 1);
            }
        }

        if (instance.account == null) {
            failAndExit("Hesabınızla Giriş Yapmadınız! Lütfen https://capsule.net.tr'den giriniz!", 1);
        }

        instance.account.saveAccountLocalFile();

        if (argMap.containsKey("studio") || argMap.containsKey("s")) {
            openStudio(argMap);
            return;
        }

        if (argMap.containsKey("game")) {
            String id = argMap.get("game");
            Capsule.instance.gameEngine.setCurrentScreen(new GameLoadingScreen(Integer.parseInt(id)));
            return;
        }

        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI.create("http://capsule.net.tr/"));
            }
        } catch (IOException ignored) {
        }
        System.exit(0);
    }

    private static void openStudio(Map<String, String> argMap) {
        int gameID = -1;
        String value = argMap.get("studio");

        if (value != null && !value.isEmpty()) {
            gameID = Integer.parseInt(value);
        }

        try {
            if (!instance.apiClient.isThisGameYours(gameID, instance.account)) {
                failAndExit("Düzenlemeye çalıştığınız oyun size ait değil! Lütfen kendi oyununuzu düzenleyin!", -1);
            }
        } catch (IOException | InterruptedException e) {
            failAndExit("Oyun sahipliği doğrulanamadı: " + e.getMessage(), -1);
        }

        EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                JFrame frame = instance.gameFrame;
                frame.setVisible(false);

                StudioPanel window = new StudioPanel();
                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        File layoutFile = instance.storagePaths.getDirectoryPath().resolve("layout.xml").toFile();
                        DockView.saveLayout(window.control, layoutFile);

                        try {
                            window.theGameData.saveWorld();
                        } catch (IOException ex) {
                            throw new CapsuleException("World kaydedilemedi", ex);
                        }
                    }
                });
                frame.add(window);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                frame.toFront();
            } catch (Exception e) {
                throw new CapsuleException("Studio açılamadı", e);
            }
        });
    }

    private static void failAndExit(String message, int code) {
        OptionWindow.showMessage(message, "Error", OptionWindow.ERROR_MESSAGE, OptionWindow.OK_BUTTON);
        System.exit(code);
    }

    public StoragePaths getStoragePaths() {
        return storagePaths;
    }

    public CapsuleApiClient getApiClient() {
        return apiClient;
    }

    public ImageUtil getImageUtil() {
        return imageUtil;
    }

    public static void loadResources() {
        Bitmap def_body = (Bitmap) IOResource.loadResource(Capsule.class.getResourceAsStream("/default_c3/body.png"),
                EnumResource.IMAGE);
        Bitmap def_hand = (Bitmap) IOResource.loadResource(Capsule.class.getResourceAsStream("/default_c3/hand.png"),
                EnumResource.IMAGE);
        Bitmap def_face = (Bitmap) IOResource.loadResource(Capsule.class.getResourceAsStream("/default_c3/face.png"),
                EnumResource.IMAGE);

        ArrayBitmap def_avatar = new ArrayBitmap(new Bitmap[][] { { def_body, def_hand, def_face } });
        ResourceLocator.addResource(new ResourceLocator.ResourceKey("capsule", "default_avatar"), (IResource) def_avatar);

        Animation leftWalkAnim = (Animation) IOResource.loadResource(
                Capsule.class.getResourceAsStream("/default_c3/animation/walkanim-left.bin"), EnumResource.ANIMATION);
        Animation rightWalkAnim = (Animation) IOResource.loadResource(
                Capsule.class.getResourceAsStream("/default_c3/animation/walkanim-right.bin"), EnumResource.ANIMATION);

        Animation idleAnim = (Animation) IOResource.loadResource(
                Capsule.class.getResourceAsStream("/default_c3/animation/idleanim.bin"), EnumResource.ANIMATION);

        ResourceLocator.addResource(new ResourceLocator.ResourceKey("capsule", "leftWalkAnim"), (IResource) leftWalkAnim);
        ResourceLocator.addResource(new ResourceLocator.ResourceKey("capsule", "rightWalkAnim"),
                (IResource) rightWalkAnim);
        ResourceLocator.addResource(new ResourceLocator.ResourceKey("capsule", "idleAnim"), (IResource) idleAnim);

        ArrayBitmap menu_buttons = new ArrayBitmap(
                IOResource.loadResourceAndCut(Capsule.class.getResourceAsStream("/menubuttons.png"), 16, 16));
        ResourceLocator.addResource(new ResourceLocator.ResourceKey("capsule", "menu_buttons"), (IResource) menu_buttons);

        ArrayBitmap materials = new ArrayBitmap(
                IOResource.loadResourceAndCut(Capsule.class.getResourceAsStream("/materials.png"), 16, 16));
        ResourceLocator.addResource(new ResourceLocator.ResourceKey("capsule", "materials"), (IResource) materials);
    }

    public static Map<String, String> parseArgs(String[] args) {
        Map<String, String> options = new HashMap<>();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.startsWith("--")) {
                String key = arg.substring(2);
                String value = "";

                if (key.contains("=")) {
                    String[] parts = key.split("=", 2);
                    key = parts[0];
                    value = parts[1];
                } else if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                    value = args[++i];
                }

                options.put(key, value);
            } else if (arg.startsWith("-")) {
                String key = arg.substring(1);
                String value = "";

                if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                    value = args[++i];
                }

                options.put(key, value);
            }
        }

        return options;
    }

    public static void log(String string) {
        System.out.println("[Capsule] " + string);
    }

    public static JSONObject checkUpdate() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://capsule.net.tr/api/v1/checkversion.php?version=" + version.toString()))
                    .GET().build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            return new JSONObject(response.body());
        } catch (Exception e) {
            return null;
        }
    }
}
