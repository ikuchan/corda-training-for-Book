package net.corda.training.state;

import net.corda.core.contracts.*;
import net.corda.core.identity.Party;
import net.corda.core.identity.AbstractParty;

import java.util.*;
import com.google.common.collect.ImmutableList;
import net.corda.core.serialization.ConstructorForDeserialization;
import net.corda.training.contract.IOUContract;

import javax.validation.constraints.NotNull;

/**
 * This is where you'll add the definition of your state object. Look at the unit tests in [IOUStateTests] for
 * instructions on how to complete the [IOUState] class.
 *
 */
@BelongsToContract(IOUContract.class)
public class IOUState implements LinearState {
    private final Amount<Currency> amount;
    private final Party lender;
    private final Party borrower;
    private final Amount<Currency> paid;
    private final UniqueIdentifier linearId;

    @ConstructorForDeserialization
    private IOUState(Amount<Currency> amount, Party lender, Party borrower, Amount<Currency> paid, UniqueIdentifier linearId) {
        this.amount = amount;
        this.lender = lender;
        this.borrower = borrower;
        this.paid = paid;
        this.linearId = linearId;
    }

    public IOUState(Amount<Currency> amount, Party lender, Party borrower) {
        this.amount = amount;
        this.lender = lender;
        this.borrower = borrower;
        this.paid = new Amount<>(0,amount.getToken());
        this.linearId = new UniqueIdentifier();
    }
    public Amount<Currency> getAmount() {
        return amount;
    }
    public Party getLender() {
        return lender;
    }
    public Party getBorrower() {
        return borrower;
    }
    public Amount<Currency> getPaid() {
        return paid;
    }
    @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(lender,borrower);
    }
    @Override
    public UniqueIdentifier getLinearId(){
        return getLinearId();
    }
    public IOUState pay(Amount<Currency>amountToPay){
        Amount<Currency> newAmountPaid = this.paid.plus(amountToPay);
        return new IOUState(amount, lender, borrower, newAmountPaid, linearId);
    }
    public IOUState withNewLender(Party newLender) {
        return new IOUState(amount, newLender, borrower, paid, linearId);
    }
}
