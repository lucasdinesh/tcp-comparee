package bank.ui.graphic.action;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.AbstractTableModel;
import bank.business.AccountOperationService;
import bank.business.BusinessException;
import bank.business.domain.Branch;
import bank.business.domain.CurrentAccountId;
import bank.business.domain.Deposit;
import bank.business.domain.Transaction;
import bank.ui.TextManager;
import bank.ui.graphic.BankGraphicInterface;
import bank.ui.graphic.GUIUtils;

public class PendentDepositsAction extends AccountAbstractAction {

	public enum StatementType {
		MONTHLY, PERIOD;
	}


	private class TransactionTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 2497950520925208080L;

		private List<Transaction> transactions;

		public TransactionTableModel(CurrentAccountId id, List<Deposit> transactions) {
			this.transactions = new ArrayList<>(transactions);
		}

		@Override
		public int getColumnCount() {
			return 7;
		}

		@Override
		public String getColumnName(int column) {
			String key = null;
			switch (column) {
			case 0:
				key = "dep.num";
				break;
			case 1:
				key = "date";
				break;
			case 2:
				key = "location";
				break;
			case 3:
				key = "operation.type";
				break;
			case 4:
				key = "details";
				break;
			case 5:
				key = "pendent.amount";
				break;
			case 6:
				key = "status";
				break;
			default:
				assert false;
				break;
			}
			return textManager.getText(key);
		}

		@Override
		public int getRowCount() {
			return transactions.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Transaction t = transactions.get(rowIndex);
			Object val = null;
			switch (columnIndex) {
			case 0:
				val = rowIndex+1;
				break;
			case 1:
				val = GUIUtils.DATE_TIME_FORMAT.format(t.getDate());
				break;
			case 2:
				val = t.getLocation();
				break;
			case 3:
				val = textManager.getText("operation." + t.getClass().getSimpleName());
				break;
			case 4:
				val = ((Deposit) t).getEnvelope();
				break;
			case 5:
				if(((Deposit) t).getPendentAmount() != 0) {					
					val = "+ " + ((Deposit) t).getPendentAmount();
				}else {
					val = "+ " + ((Deposit) t).getAmount();
				}
				break;
			case 6:
				val = textManager.getText(String.valueOf(((Deposit) t).getStatus()));
				break;
				
			default:
				assert false;
				break;
			}
			return val;
		}

	}


	private static final long serialVersionUID = 5090183202921964451L;
	
	private JPanel cards;
	private JDialog dialog;
	private JTable transactions;
	protected JFormattedTextField nDeposit;

	public PendentDepositsAction(BankGraphicInterface bankInterface, TextManager textManager,
			AccountOperationService accountOperationService) {
		super(bankInterface, textManager, accountOperationService);

		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		putValue(Action.NAME, textManager.getText("action.pendents"));
	}

	public void close() {
		dialog.dispose();
		dialog = null;
	}


	@Override
	public void execute() {
		JPanel accountPanel = new JPanel(new GridLayout(2, 2, 5, 5));
		initAndAddAccountFields(accountPanel);
		
		// Selecionar o depósito
		JPanel selectDepositPanel = new JPanel(new GridLayout(2, 2, 5, 5));
		this.nDeposit = new JFormattedTextField(NumberFormat.getIntegerInstance());
		selectDepositPanel.add(new JLabel("Digite o número do depósito desejado:"));
		selectDepositPanel.add(nDeposit);
		selectDepositPanel.setVisible(false);
		
		// Cards
		JPanel radioBtPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		this.cards = new JPanel(new CardLayout());

		JPanel cardsPanel = new JPanel();
		cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.PAGE_AXIS));
		cardsPanel.add(accountPanel);
		cardsPanel.add(radioBtPanel);
		cardsPanel.add(selectDepositPanel);
		cardsPanel.add(cards);

		// Confirmation Buttons
		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton cancelButton = new JButton(textManager.getText("button.close"));
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				close();
			}
		});
		buttonsPanel.add(cancelButton);
		JButton okButton = new JButton(textManager.getText("button.ok"));
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showPendentsDeposits(selectDepositPanel);
				
			}
		});
		buttonsPanel.add(okButton);

		// Statement result
		JPanel transactionsPanel = new JPanel();
		transactionsPanel.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
		transactions = new JTable();
		JScrollPane scrollPane = new JScrollPane(transactions, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		transactionsPanel.add(scrollPane);

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
		mainPanel.add(cardsPanel, BorderLayout.CENTER);
		mainPanel.add(buttonsPanel, BorderLayout.SOUTH);

		JPanel pane = new JPanel(new BorderLayout());
		pane.add(mainPanel, BorderLayout.NORTH);
		pane.add(transactionsPanel, BorderLayout.CENTER);

		this.dialog = GUIUtils.INSTANCE.createDialog(bankInterface.getFrame(), "action.pendents", pane);
		this.dialog.setVisible(true);
	}

	private void showPendentsDeposits(JPanel selectDepositPanel) {

		try {
			if (!checkAccountFields())
				return;

			List<Deposit> transactions = accountOperationService.getPendentDeposits(
					((Number) branch.getValue()).longValue(), ((Number) accountNumber.getValue()).longValue());
			
			this.transactions.setModel(
					new TransactionTableModel(new CurrentAccountId(new Branch(((Number) branch.getValue()).longValue()),
							((Number) accountNumber.getValue()).longValue()), transactions));
			
			showSelectionField(transactions, selectDepositPanel);
			
		} catch (BusinessException be) {
			GUIUtils.INSTANCE.showMessage(bankInterface.getFrame(), be.getMessage(), be.getArgs(),
					JOptionPane.WARNING_MESSAGE);
			log.warn(be);
		} catch (Exception exc) {
			GUIUtils.INSTANCE.handleUnexceptedError(bankInterface.getFrame(), exc);
		}
	}
	
	private void showSelectionField(List<Deposit> transactions, JPanel selectDepositPanel) throws BusinessException{
		if(!transactions.isEmpty()) {
			
			selectDepositPanel.setVisible(true);
			
			if(!nDeposit.getText().isEmpty()) {
				final Integer selectedDeposit = Integer.parseInt(nDeposit.getText()) - 1;
				System.out.println(selectedDeposit);
				
				if(selectedDeposit < 0 || (selectedDeposit+1) > transactions.size()) {
					throw new BusinessException("exception.invalid.deposit");
				}
				
				if(selectedDeposit <= transactions.size() && selectedDeposit >= 0) {
									
					JPanel decisionDepositPanel = new JPanel(new GridLayout(5, 5, 5, 5));
					
					Deposit atual = transactions.get(selectedDeposit);
					decisionDepositPanel.add(new JLabel("Conta: "+atual.getAccount().getId().getNumber()));
					decisionDepositPanel.add(new JLabel("Envelope: "+atual.getEnvelope()));
					if(transactions.get(selectedDeposit).getPendentAmount() == 0){
						decisionDepositPanel.add(new JLabel("Valor: "+atual.getAmount()));
					} else {
						decisionDepositPanel.add(new JLabel("Valor: "+atual.getPendentAmount()));
					}
					
					StringBuffer sb = new StringBuffer();
					
					JButton confirmButton = new JButton(textManager.getText("button.confirm"));
					JButton recuseButton = new JButton(textManager.getText("button.recuse"));
					confirmButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							if(transactions.get(selectedDeposit).getPendentAmount() > 0){
								atual.getAccount().setBalance(atual.getPendentAmount());
								atual.setAmount(atual.getPendentAmount());
								atual.setPendentAmount(0);
								atual.setStatus(1);
							}else {
								atual.setStatus(1);
							}
							
							sb.append(textManager.getText("message.operation.succesfull")).append("\n");
							int indice = selectedDeposit + 1;
							sb.append("Depósito "+ indice + " FINALIZADO");
							
							GUIUtils.INSTANCE.showMessage(bankInterface.getFrame(), sb.toString(),
									JOptionPane.INFORMATION_MESSAGE);
							dialog.dispose();
						}
					});
					
					recuseButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							if(transactions.get(selectedDeposit).getPendentAmount() == 0){
								atual.getAccount().setBalance(-atual.getAmount());
								atual.setAmount(0);
								atual.setStatus(3);
							}else {
								atual.setStatus(3);
							}
							
							sb.append(textManager.getText("message.operation.succesfull")).append("\n");
							int indice = selectedDeposit + 1;
							sb.append("Depósito "+ indice + " CANCELADO");
							
							GUIUtils.INSTANCE.showMessage(bankInterface.getFrame(), sb.toString(),
									JOptionPane.INFORMATION_MESSAGE);
							dialog.dispose();
						}
					});
					
					this.dialog.dispose();
					
					decisionDepositPanel.add(confirmButton);
					decisionDepositPanel.add(recuseButton);
					
					this.dialog = GUIUtils.INSTANCE.createDialog(bankInterface.getFrame(),
							"decide.box",decisionDepositPanel);
					this.dialog.setVisible(true);
				}
			}
		}else {
			transactions.clear();
			selectDepositPanel.setVisible(false);
		}
	}

}
