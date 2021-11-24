package bank.ui.text.command;

import bank.business.AccountOperationService;
import bank.business.domain.Deposit;
import bank.business.domain.STATUS;
import bank.ui.text.BankTextInterface;
import bank.ui.text.UIUtils;

/**
 * @author Ingrid Nunes
 * 
 */
public class DepositCommand extends Command {

	private final AccountOperationService accountOperationService;

	public DepositCommand(BankTextInterface bankInterface,
			AccountOperationService accountOperationService) {
		super(bankInterface);
		this.accountOperationService = accountOperationService;
	}

	@Override
	public void execute() throws Exception {
		Long branch = bankInterface.readBranchId();
		Long accountNumber = bankInterface.readCurrentAccountNumber();
		Long envelope = UIUtils.INSTANCE.readLong("envelope");
		Double amount = UIUtils.INSTANCE.readDouble("amount");
		STATUS status = UIUtils.INSTANCE.readStatus("status");

		Deposit deposit = accountOperationService.deposit(bankInterface
				.getOperationLocation().getNumber(), branch, accountNumber,
				envelope, amount, status);

		System.out.println(getTextManager().getText(
				"message.operation.succesfull"));
		System.out.println(getTextManager().getText("deposit") + ": "
				+ deposit.getAmount());
	}

}