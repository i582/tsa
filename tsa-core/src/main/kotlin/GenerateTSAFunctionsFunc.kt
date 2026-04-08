import kotlin.io.path.Path
import kotlin.io.path.bufferedWriter
import kotlin.math.max

private const val PATH_IN_JETTON_RESOURCES = "tsa-jettons/src/main/resources/imports/tsa_functions.fc"
private const val PATH_IN_TEST_RESOURCES = "tsa-test/src/test/resources/imports/tsa_functions.fc"
private const val PATH_IN_SAFETY_PROPERTIES_EXAMPLES_TEST_RESOURCES =
    "tsa-safety-properties-examples/src/test/resources/imports/tsa_functions.fc"
private val pathsForTsaFunctions =
    listOf(
        PATH_IN_JETTON_RESOURCES,
        PATH_IN_SAFETY_PROPERTIES_EXAMPLES_TEST_RESOURCES,
        PATH_IN_TEST_RESOURCES,
        PATH_IN_SAFETY_PROPERTIES_EXAMPLES_TEST_RESOURCES,
    ).map(::Path)

private const val MAX_PARAMETERS = 10
private const val DOUBLE_SEPARATOR = "\n\n"

fun main() {
    val prefix =
        """
        ;; generated

        ;; auxiliary functions
        """.trimIndent()

    val auxiliaryFunctions =
        List(MAX_PARAMETERS) { i ->
            val params = i + 1
            val typeParams = ('A'..'Z').take(params).joinToString()
            "forall $typeParams -> ($typeParams) return_$params() asm \"NOP\";"
        }.joinToString(separator = "\n")

    val firstApiFunctions =
        """
        ;; API functions

        () tsa_forbid_failures() impure asm "701 EXTCALL";

        () tsa_allow_failures() impure asm "702 EXTCALL";

        () tsa_assert(int condition) impure asm "703 EXTCALL";

        () tsa_assert_not(int condition) impure asm "704 EXTCALL";

        forall A -> () tsa_fetch_value(A value, int value_id) impure asm "705 EXTCALL";

        () tsa_send_internal_message(int contract_id, int input_id) impure asm "706 EXTCALL";

        cell tsa_get_c4(int contract_id) impure asm "707 EXTCALL";

        () tsa_send_external_message(int contract_id, int input_id) impure asm "708 EXTCALL";

        int tsa_get_balance(int contract_id) impure asm "709 EXTCALL";

        ;; this function shouldn't be used while executing contract [contract_id] (inside checker handlers)
        () tsa_set_c4(int contract_id, cell value) impure asm "710 EXTCALL";

        () tsa_make_address_random(slice address) impure asm "711 EXTCALL";
        
        () tsa_make_slice_independent_from_random_addresses(slice independent_slice) impure asm "712 EXTCALL";
        
        int tsa_input_was_accepted(int input_id) impure asm "713 EXTCALL";
        
        () tsa_send_internal_message_with_body(slice body, int contract_id, int input_id) impure asm "714 EXTCALL";
        
        () tsa_send_external_message_with_body(slice body, int contract_id, int input_id) impure asm "715 EXTCALL";
        """.trimIndent()

    val mkSymbolicApiFunctions =
        """
        ;; making symbolic values API functions

        int tsa_mk_int(int bits, int signed) impure asm "100 EXTCALL";
        """.trimIndent()

    val callFunctions =
        List(MAX_PARAMETERS + 1) { retParams ->
            List(MAX_PARAMETERS + 1) { putParams ->
                val typeParams = ('A'..'Z').take(max(retParams + putParams, 1))
                val retTypeParams = typeParams.take(retParams).joinToString(prefix = "(", postfix = ")")
                val putParamsRendered =
                    typeParams
                        .takeLast(putParams)
                        .mapIndexed { index, paramType ->
                            "$paramType p$index, "
                        }.joinToString(separator = "")
                val methodId = 10000 + retParams * 100 + putParams
                """
                forall ${typeParams.joinToString()} -> $retTypeParams tsa_call_${retParams}_$putParams(${putParamsRendered}int id_contract, int id_method) impure asm "$methodId EXTCALL";
                """.trimIndent()
            }
        }.flatten().joinToString(prefix = ";; calling methods functions$DOUBLE_SEPARATOR", separator = DOUBLE_SEPARATOR)

    val code =
        listOf(
            prefix,
            auxiliaryFunctions,
            firstApiFunctions,
            mkSymbolicApiFunctions,
            callFunctions,
        ).joinToString(separator = DOUBLE_SEPARATOR)

    pathsForTsaFunctions.forEach { path ->
        path.bufferedWriter().use {
            it.append(code)
        }
    }
}
