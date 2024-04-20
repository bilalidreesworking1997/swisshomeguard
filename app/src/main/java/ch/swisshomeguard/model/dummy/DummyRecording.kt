/**
 * dev server
 * SwissHomeGuard mocked API
 *
 * OpenAPI spec version: 1.0.0
 *
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */
package ch.swisshomeguard.model.dummy

/**
 *
 * @param file_path
 * @param preview_url
 * @param file_size
 */
data class DummyRecording(
    val file_path: kotlin.String,
    val file_size: kotlin.Int,
    val preview_url: kotlin.String? = null
)
