package no.nova.training.data.transfer

sealed interface TransferState {
    data object Idle : TransferState
    data object Fetching : TransferState
    data object Validating : TransferState
    data object Saving : TransferState
    data object Confirming : TransferState
    data class Success(val programName: String, val weeks: Int, val days: Int) : TransferState
    data class Error(val message: String) : TransferState
}
