package fr.philr.medimaildownloader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.CredentialException;
import javax.swing.JTextPane;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import net.dongliu.requests.Parameter;
import net.dongliu.requests.Requests;
import net.dongliu.requests.Session;

public class MedimailSession {

	public void print(String str) {
		if (out != null)
			/*SwingUtilities.invokeLater(new Runnable()*/ {
				/*
				@Override
				public void run() {*/
					out.setText(out.getText() + "\n" + str);
					out.setCaretPosition(out.getText().length());
					out.update(out.getGraphics());
				/*}*/
			}/*);*/
		else
			System.out.println(str);
	}
	
	public void hookOutput(JTextPane textStatus) {
		this.out = textStatus;
	}

	private JTextPane out = null;
	private Session session;
	private int pageMax;
	private int folderID;
	private Map<Integer, MedimailMessage> messages;

	private static Pattern PAGINATION_NUMBERS = Pattern.compile("Page (\\d+) sur (\\d+)");
	private static Pattern TITLE_ID = Pattern.compile("https://medimail.mipih.fr/\\?m=liste&f=(\\d+)&o=title");
	private static Pattern KVP_ID = Pattern.compile("\\d+");

	public MedimailSession(String username, String password) throws CredentialException {
		session = Requests.session();
		String result = session.post("https://medimail.mipih.fr/")
				.body(Parameter.of("login", username), Parameter.of("password", password)).send().readToText();
		if (result.contains("Echec de l\\'authentification.") || result.contains("Session invalide") || result.contains("Le login doit être une adresse mail valide."))
			throw new CredentialException("Informations de connexion invalides."); 
		getUserInfo();
		messages = new HashMap<>();
	}

	private void getUserInfo(){
		String html = session.get("https://medimail.mipih.fr/").send().readToText();
		Document doc = Jsoup.parse(html);
		String pages = doc.select("#pagination").text();
		Matcher m = PAGINATION_NUMBERS.matcher(pages);
		m.find();
		pageMax = Integer.parseInt(m.group(2));
		Elements allHref = doc.select("a[href]");
		for (Element link : allHref) {
			String url = link.attr("href");
			Matcher m1 = TITLE_ID.matcher(url);
			m1.find();
			if (m1.matches()) {
				folderID = Integer.parseInt(m1.group(1));
				break;
			}
		}
	}

	private void extractMessages(Elements trs, boolean opened) {
		for (Element tr : trs.select("tr")) {
			String title = "";
			String sender = "";
			String date = "";
			int rank = 0;
			int messageId = -1;
			for (Element td : tr.select("td.middle")) {
				switch (rank) {
				case 1:
					title = td.text();
					break;
				case 2:
					sender = td.text();
					break;
				default:
					Matcher matcher = KVP_ID.matcher(td.attr("onclick"));
					matcher.find();
					messageId = Integer.parseInt(matcher.group(0));
					break;
				}
				rank++;
			}
			for (Element td : tr.select("td.right")) {
				date = td.text();
				break;
			}
			/*
			 * int k = 2; int l = 1/(k-2);
			 */
			MedimailMessage mm = new MedimailMessage(title, messageId, sender, date, opened);
			messages.put(mm.messageKey, mm);
		}

	}

	public void handlePages() {
		for (int i = 1; i <= pageMax; i++) {
			print("Traitement de la page " + i + "...");
			getPage(i);
		}
	}

	public void getPage(int id) {
		if (id <= pageMax && 0 <= id) {
			String url = String.format("https://medimail.mipih.fr/?m=liste&f=%d&o=date&p=%d", folderID, id);
			String messagesList = session.get(url).send().readToText();
			Document doc = Jsoup.parse(messagesList);
			Elements unopenMessages = doc.select("tr.unopen");
			Elements openMessages = doc.select("tr.open");
			extractMessages(unopenMessages, false);
			extractMessages(openMessages, true);
		}
	}

	public String getUrl(String url) {
		return session.get(url).send().readToText();
	}

	public byte[] getBytes(String url) {
		return session.get(url).send().readToBytes();
	}

	public ArrayList<MedimailMessage> getAllMessages() {
		ArrayList<MedimailMessage> list = new ArrayList<>();
		list.addAll(messages.values());
		Collections.sort(list);
		return list;
	}

}
