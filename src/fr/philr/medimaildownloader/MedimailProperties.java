package fr.philr.medimaildownloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class MedimailProperties {
	public static MedimailProperties INSTANCE = new MedimailProperties();
	Properties prop = new Properties();

	private MedimailProperties() {
		File f = new File("medimail.properties");
		try (InputStream is = new FileInputStream(f)) {
			prop.load(is);
		} catch (IOException io) {
			prop.setProperty("login", "yourloginhere");
			prop.setProperty("password", "yourpasswordhere");
			try (OutputStream os = new FileOutputStream(f)) {
				prop.store(os, "Default");
			} catch (Exception e) {
			}
		}
	}

	public String getLogin() {
		return (String) prop.getOrDefault("login", "login");
	}

	public String getPassword() {
		return (String) prop.getOrDefault("password", "password");
	}

	public void save() {
		File f = new File("medimail.properties");
		try (OutputStream os = new FileOutputStream(f)) {
			prop.store(os, "Default");
		} catch (Exception e) {
		}
	}
}
