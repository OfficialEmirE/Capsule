package net.capsule.studio.windows;

import javax.swing.JDialog;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JScrollPane;
import javax.swing.JEditorPane;
import javax.swing.JButton;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class EditMessageDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	private JEditorPane descEditorPane;
	private String text;
	
	public EditMessageDialog(java.awt.Frame frame, String text) {
		super(frame, true);
		this.text = text;
		setType(Type.UTILITY);
		setTitle("Edit Message");
		
		setBounds(100, 100, 572, 605);
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);
		
		JButton okButton = new JButton("OK");
		okButton.setActionCommand("ok");
		okButton.addActionListener(this);
		panel.add(okButton);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);
		panel.add(cancelButton);
		
		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		descEditorPane = new JEditorPane();
		descEditorPane.setFont(new Font("Tahoma", Font.BOLD, 11));
		descEditorPane.setText(text);
		scrollPane.setViewportView(descEditorPane);
	}

	public String getText() {
		return text;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("ok")) {
			this.text = descEditorPane.getText();
		}
		dispose();
	}
}
