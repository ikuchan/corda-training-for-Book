package net.corda.training.contract;

import net.corda.core.contracts.*;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

import net.corda.core.identity.AbstractParty;
import net.corda.core.transactions.LedgerTransaction;

import net.corda.training.state.IOUState;

import java.security.PublicKey;
import java.util.*;

/**
 * This is the contract code which defines how the [IOUState] behaves. Looks at the unit tests in
 * [IOUContractTests] for more insight on how this contract verifies a transaction.
 */

// LegalProseReference: this is just a dummy string for the time being.

//@LegalProseReference(uri = "<prose_contract_uri>")
public class IOUContract implements Contract {
    public static final String IOU_CONTRACT_ID = "net.corda.training.contract.IOUContract";

    /**
     * The IOUContract can handle three transaction types involving [IOUState]s.
     * - Issuance: Issuing a new [IOUState] on the ledger, which is a bilateral agreement between two parties.
     * - Transfer: Re-assigning the lender/beneficiary.
     * - Settle: Fully or partially settling the [IOUState] using the Corda [Cash] contract.
     */
    public interface Commands extends CommandData {
        class Issue extends TypeOnlyCommandData implements Commands{}
        class Transfer extends TypeOnlyCommandData implements Commands{}
        class Settle extends TypeOnlyCommandData implements Commands{}
    }
    /**
     * The contract code for the [IOUContract].
     * The constraints are self documenting so don't require any additional explanation.
     */
    @Override
    public void verify(LedgerTransaction tx) {

        // We can use the requireSingleCommand function to extract command data from transaction.
        final CommandWithParties<Commands> command = requireSingleCommand(tx.getCommands(), Commands.class);
        final Commands commandData = command.getValue();

        /**
         * This command data can then be used inside of a conditional statement to indicate which set of tests we
         * should be performing - we will use different assertions to enable the contract to verify the transaction
         * for issuing, settling and transferring.
         */
        if (commandData.equals(new Commands.Issue())) {

            requireThat(require -> {

                require.using("No inputs should be consumed when issuing an IOU.", tx.getInputStates().size() == 0);
                require.using( "Only one output state should be created when issuing an IOU.", tx.getOutputStates().size() == 1);

                IOUState outputState = tx.outputsOfType(IOUState.class).get(0);
                require.using( "A newly issued IOU must have a positive amount.", outputState.amount.getQuantity() > 0);
                require.using( "The lender and borrower cannot have the same identity.", outputState.lender.getOwningKey() != outputState.borrower.getOwningKey());

                List<PublicKey> signers = tx.getCommands().get(0).getSigners();
                HashSet<PublicKey> signersSet = new HashSet<>();
                for (PublicKey key: signers) {
                    signersSet.add(key);
                }

                List<AbstractParty> participants = tx.getOutputStates().get(0).getParticipants();
                HashSet<PublicKey> participantKeys = new HashSet<>();
                for (AbstractParty party: participants) {
                    participantKeys.add(party.getOwningKey());
                }

                require.using("Both lender and borrower together only may sign IOU issue transaction.", signersSet.containsAll(participantKeys) && signersSet.size() == 2);
                return null;
            });
        }
    }

}