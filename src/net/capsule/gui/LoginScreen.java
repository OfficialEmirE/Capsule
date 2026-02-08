package net.capsule.gui;

import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import me.ramazanenescik04.diken.gui.compoment.Button;
import me.ramazanenescik04.diken.gui.compoment.PasswordField;
import me.ramazanenescik04.diken.gui.compoment.Text;
import me.ramazanenescik04.diken.gui.compoment.TextField;
import me.ramazanenescik04.diken.gui.screen.Screen;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import net.capsule.account.Account;
import net.capsule.util.Util;

public class LoginScreen extends Screen {
	
	private Future<Account> accountFuture;

	private TextField usernameField;
	private PasswordField passwordField;
	private Bitmap capsuleLogoImage;
	private String statusMessage;

	private boolean finished;
	private Account account;

	public LoginScreen() {
		capsuleLogoImage = ((Bitmap) ResourceLocator.getResource(new ResourceLocator.ResourceKey("capsule", "logo"))).resize(627 / 4, 205 / 4);
		
		accountFuture = new FutureTask<Account>(() -> {			
			return account;
		});
	}
	
	public Future<Account> getAccount() {
		return accountFuture;
	}
	
	public void openScreen() {
		int width = engine.getScaledWidth();
		int height = engine.getScaledHeight();
		
		this.usernameField = new TextField(width / 2 - 100, engine.getScaledHeight() / 2 - 25, 200, 20);
		this.passwordField = new PasswordField(width / 2 - 100, engine.getScaledHeight() / 2 + 5, 200, 20);
		
		Button loginButton = new Button("Login", width / 2 - 50, height / 2 + 35, 100, 20).setRunnable(() -> {
			if (finished) {
				return;
			}
			
			String username = usernameField.getText();
			String password = passwordField.getText();
			if (username.isEmpty() || password.isEmpty()) {
				this.statusMessage = "Username and password cannot be empty.";
				passwordField.setText("");
			} else {
				account = Util.login(username, password);
				if (account == null) {
					this.statusMessage = "Your Account Password and Username Are Incorrect!";
					passwordField.setText("");
				} else {
					this.statusMessage = "";
					((FutureTask<Account>) accountFuture).run();
				}
			}			
		});
		
		Button cancelButton = new Button("Cancel", width / 2 - 50, height / 2 + 65, 100, 20).setRunnable(() -> {
			if (finished) {
				return;
			}
			
			this.finished = true;
			((FutureTask<Account>) accountFuture).cancel(true);
		});
		
		this.getContentPane().add(usernameField);
		this.getContentPane().add(passwordField);
		this.getContentPane().add(loginButton);
		this.getContentPane().add(cancelButton);
	}
	
	public void resized() {
		int width = engine.getScaledWidth();
		int height = engine.getScaledHeight();
		
		if (usernameField != null) {
			usernameField.setBounds(width / 2 - 100, height / 2 - 25, 200, 20);
		}
		
		if (passwordField != null) {
			passwordField.setBounds(width / 2 - 100, height / 2 + 5, 200, 20);
		}
		
		Button loginButton = (Button) this.getContentPane().get(2);
		
		if (loginButton != null) {
			loginButton.setBounds(width / 2 - 50, height / 2 + 40, 100, 20);
		}
		
		Button cancelButton = (Button) this.getContentPane().get(3);
		
		if (cancelButton != null) {
			cancelButton.setBounds(width / 2 - 50, height / 2 + 70, 100, 20);
		}
	}
	
	public void render(Bitmap bitmap) {
		bitmap.clear(0xff3b3b3b);
		super.render(bitmap);
		
		if (capsuleLogoImage != null) {
			bitmap.draw(capsuleLogoImage, (engine.getScaledWidth() - capsuleLogoImage.w) / 2, 50);
		}
		if (statusMessage != null) {
			bitmap.drawText(statusMessage, engine.getScaledWidth() / 2 - Text.stringBitmapWidth(statusMessage, this.engine.defaultFont) / 2, engine.getScaledHeight() / 2 + 90, false);
		}
		
		bitmap.drawText("Username:", engine.getScaledWidth() / 2 - 100, engine.getScaledHeight() / 2 - 35, false);
		bitmap.drawText("Password:", engine.getScaledWidth() / 2 - 100, engine.getScaledHeight() / 2 - 4, false);
	}

}
