package com.example.contract;

import com.example.state.IOUState;
import net.corda.core.contracts.AuthenticatedObject;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.TransactionForContract;
import net.corda.core.crypto.SecureHash;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

/**
 * A implementation of a basic smart contract in Corda.
 *
 * This contract enforces rules regarding the creation of a valid [IOUState], which in turn encapsulates an [IOU].
 *
 * For a new [IOU] to be issued onto the ledger, a transaction is required which takes:
 * - Zero input states.
 * - One output state: the new [IOU].
 * - An Create() command with the public keys of both the sender and the recipient.
 *
 * All contracts must sub-class the [Contract] interface.
 */
public class IOUContract implements Contract {
    /**
     * The verify() function of all the states' contracts must not throw an exception for a transaction to be
     * considered valid.
     */
    @Override
    public void verify(TransactionForContract tx) {
        requireThat(require -> {
            // Constraint on the value of the IOU.
            require.by("The IOU's value must be non-negative.",
                    ((IOUState) tx.getOutputs().get(0)).getIOUValue() > 0);

            // Constraints on the number of input/output states.
            require.by("No inputs should be consumed when issuing an IOU.",
                    tx.getInputs().isEmpty());
            require.by("Only one output state should be created.",
                    tx.getOutputs().size() == 1);

            // Constraint on the presence of the Create command.
            final AuthenticatedObject<Commands.Create> command =
                    requireSingleCommand(tx.getCommands(), Commands.Create.class);

            // Constraint on the signatories.
            require.by("All of the participants must be signers.",
                    command.getSigners().containsAll(tx.getOutputs().get(0).getParticipants()));

            return null;
        });
    }

    /**
     * This contract only implements one command, Create.
     */
    public interface Commands extends CommandData {
        class Create implements Commands {}
    }

    /** This is a reference to the underlying legal contract template and associated parameters. */
    private final SecureHash legalContractReference = SecureHash.sha256("IOU contract template and params");
    @Override public final SecureHash getLegalContractReference() { return legalContractReference; }
}