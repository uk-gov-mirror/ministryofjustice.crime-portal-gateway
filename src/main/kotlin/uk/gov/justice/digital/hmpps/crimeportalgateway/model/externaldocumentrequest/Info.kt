package uk.gov.justice.digital.hmpps.crimeportalgateway.model.externaldocumentrequest

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.LocalDate
import java.util.Objects
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

data class Info(

    @Valid
    @Positive
    @JsonIgnore
    @NotNull
    val sequence: Long,

    @field:NotBlank
    @field:Size(min = 5, message = "Invalid ou code")
    @JsonIgnore
    val ouCode: String,

    @field:NotNull
    @JsonIgnore
    val dateOfHearing: LocalDate
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val that = other as Info
        return ouCode == that.ouCode && Objects.equals(dateOfHearing, that.dateOfHearing)
    }

    override fun hashCode(): Int {
        return Objects.hash(ouCode, dateOfHearing)
    }

    companion object {
        const val SOURCE_FILE_NAME_ELEMENT = "source_file_name"
    }
}
