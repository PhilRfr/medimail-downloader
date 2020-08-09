package fr.philr.medimaildownloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MedimailMessage implements Comparable<MedimailMessage> {
	final String title;
	final int messageKey;
	final String senderEmail;
	final String date;
	final boolean opened;
	String corps;
	Map<String, byte[]> files;
	private Date javaDate;

	public MedimailMessage(String title, int messageKey, String senderEmail, String date, boolean opened) {
		super();
		this.title = title;
		this.messageKey = messageKey;
		this.senderEmail = senderEmail;
		this.date = date;
		this.opened = opened;
		this.files = new HashMap<>();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			javaDate = format.parse(date);
		} catch (ParseException e) {
			javaDate = new Date();
		}
	}

	@Override
	public String toString() {
		return title + " - " + senderEmail + " - " + date;
	}

	public String getFilename() {
	    return toString().replaceAll("[^a-z éèÉÈÀàùÙçÇA-Z0-9-_\\.]", "_");
	}
	
	public String getTitle() {
		return title;
	}

	public int getMessageKey() {
		return messageKey;
	}

	public String getSenderEmail() {
		return senderEmail;
	}

	public String getDate() {
		return date;
	}

	public boolean isOpened() {
		return opened;
	}

	public void download(MedimailSession session) {
		String url = "https://medimail.mipih.fr/?m=open&idk=" + messageKey;
		String html = session.getUrl(url);
		Document doc = Jsoup.parse(html);
		Elements corps = doc.select("#corps");
		this.corps = corps.html();
		Elements params = corps.select("#kvp_param");
		for (Element subel : params) {
			String title = subel.select("h3").text().toLowerCase(Locale.FRANCE);
			if (title != null && title.contains("ocument")) {
				Elements links = subel.select("a");
				for (Element link : links) {
					String fileName = link.text();
					String fileUrl = link.attr("href");
					byte[] content = session.getBytes(fileUrl);
					files.put(fileName, content);
				}
				break;
			}
		}
	}

	public void saveToDirectory(String ref, MedimailSession session) {
		Path p = Paths.get(ref);
		Path path = new File(p.toFile(), getFilename()).toPath().toAbsolutePath();
		try {
			path.toFile().mkdirs();
			File message = new File(path.toFile(), "message.html");
			Utils.writeToFile(message.toPath().toString(), corps);
			for (Entry<String, byte[]> pair : files.entrySet()) {
				String fileName = pair.getKey();
				byte[] content = pair.getValue();
				File fullFilename = new File(path.toFile(), fileName);
				try (FileOutputStream fos = new FileOutputStream(fullFilename)) {
					fos.write(content);
				}
				session.print("Saving '" + fileName + "' as " + fullFilename.toString());
			}
			session.print("Saving message " + messageKey + " at " + path.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveToDirectory(MedimailSession session) {
		saveToDirectory(new File("messages").getAbsolutePath(), session);
	}

	@Override
	public int compareTo(MedimailMessage arg) {
        return this.javaDate.compareTo(arg.javaDate);
	}
	
}
