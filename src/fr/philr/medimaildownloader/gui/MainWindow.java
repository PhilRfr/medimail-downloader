package fr.philr.medimaildownloader.gui;

import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.security.auth.login.CredentialException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import fr.philr.medimaildownloader.MedimailMessage;
import fr.philr.medimaildownloader.MedimailProperties;
import fr.philr.medimaildownloader.MedimailSession;

public class MainWindow {

	private JFrame frmClientDeTlchargement;
	//private MedimailMessageModel modele;
	private MedimailSession session = null;
	private JTable table;
	private JTextField loginField;
	private JPasswordField passwordField;
	private JButton btnSave;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frmClientDeTlchargement.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		initialize();
	}

	private String chooseDirectory() {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle("Choisir un répertoire de destination");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		//
		// disable the "All files" option.
		//
		chooser.setAcceptAllFileFilterUsed(false);
		//
		if (chooser.showOpenDialog(this.frmClientDeTlchargement.getContentPane()) == JFileChooser.APPROVE_OPTION)
			return chooser.getSelectedFile().getAbsolutePath();
		else
			return null;
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		MedimailProperties props = MedimailProperties.INSTANCE;
		frmClientDeTlchargement = new JFrame();
		frmClientDeTlchargement.setTitle("Client de téléchargement rapide Medimail");
		frmClientDeTlchargement.setBounds(100, 100, 800, 600);
		frmClientDeTlchargement.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmClientDeTlchargement.getContentPane().setLayout(null);

		loginField = new JTextField();
		loginField.setBounds(154, 12, 154, 19);
		frmClientDeTlchargement.getContentPane().add(loginField);
		loginField.setColumns(10);

		passwordField = new JPasswordField();
		passwordField.setBounds(154, 43, 154, 19);
		frmClientDeTlchargement.getContentPane().add(passwordField);

		JLabel loginLabel = new JLabel("Utilisateur :");
		loginLabel.setBounds(12, 12, 119, 15);
		loginLabel.setLabelFor(loginField);
		frmClientDeTlchargement.getContentPane().add(loginLabel);

		JLabel passwordLabel = new JLabel("Mot de passe :");
		passwordLabel.setLabelFor(passwordField);
		passwordLabel.setBounds(12, 45, 154, 15);
		frmClientDeTlchargement.getContentPane().add(passwordLabel);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setEnabled(false);
		scrollPane.setBounds(12, 127, 775, 329);
		frmClientDeTlchargement.getContentPane().add(scrollPane);

		table = new JTable();
		MedimailMessageModel mm = new MedimailMessageModel();
		table.setModel(mm);
		table.setBounds(27, 116, 374, 273);
		scrollPane.setViewportView(table);
		table.setAutoCreateRowSorter(true);
		table.setDefaultRenderer(OpenedState.class, new SeenRenderer());

		loginField.setText(props.getLogin());
		passwordField.setText(props.getPassword());

		JButton btnLogin = new JButton("Connexion");
		btnLogin.setBounds(343, 12, 117, 25);
		frmClientDeTlchargement.getContentPane().add(btnLogin);

		btnSave = new JButton("Enregistrer les messages sélectionnés");
		btnSave.setEnabled(false);
		btnSave.setBounds(226, 468, 317, 25);
		btnSave.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String res = chooseDirectory();
				if (res != null) {
					for (MedimailMessage loop : mm.getPickedMessages()) {
						loop.download(session);
						loop.saveToDirectory(res, session);
					}
					System.exit(0);
				}
			}
		});
		frmClientDeTlchargement.getContentPane().add(btnSave);
		
		JButton btnNewButton = new JButton("Enregistrer les informations de connexion");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MedimailProperties.INSTANCE.save();
			}
		});
		btnNewButton.setBounds(343, 43, 339, 25);
		frmClientDeTlchargement.getContentPane().add(btnNewButton);

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(12, 72, 775, 50);
		frmClientDeTlchargement.getContentPane().add(scrollPane_1);
		
		JTextPane textStatus = new JTextPane();
		textStatus.setEditable(false);
		scrollPane_1.setViewportView(textStatus);
		
		JButton btnSaveAll = new JButton("Enregistrer tout");
		btnSaveAll.setEnabled(false);
		btnSaveAll.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String res = chooseDirectory();
				if (res != null) {
					for (MedimailMessage loop : mm.getAllMessages()) {
						loop.download(session);
						loop.saveToDirectory(res, session);
					}
					System.exit(0);
				}
			}
		});
		btnSaveAll.setBounds(12, 468, 193, 25);
		frmClientDeTlchargement.getContentPane().add(btnSaveAll);
		btnLogin.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				btnLogin.setEnabled(false);
				textStatus.setText("Veuillez patienter...");
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						String pwd = new String(passwordField.getPassword());
						try {
							session = new MedimailSession(loginField.getText(), pwd);
							session.hookOutput(textStatus);
							session.handlePages();
							mm.feedMessages(session.getAllMessages());
							textStatus.setText(textStatus.getText() + "\nTerminé !");
							btnSave.setEnabled(true);
							btnSaveAll.setEnabled(true);
						} catch (CredentialException e) {
							showMessageDialog(frmClientDeTlchargement, e.getMessage());
							btnLogin.setEnabled(true);
							e.printStackTrace();
						}
						
					}
				});
			}
		});
	}
}
