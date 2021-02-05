package uk.gov.justice.digital.hmpps.crimeportalgateway.xml

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.File
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

fun toXml(string: StringReader): Document {
    val dbFactory = DocumentBuilderFactory.newInstance()
    val dBuilder = dbFactory.newDocumentBuilder()
    return dBuilder.parse(InputSource(string))
}

internal class DocumentUtilsTest {

    private val sourceFileName = "5_26102020_2992_B10JQ05_ADULT_COURT_LIST_DAILY"
    private val sourceFileNameElement = "<source_file_name>$sourceFileName</source_file_name>"

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `get court code and room from correctly formed ExternalDocumentRequest`(useXPath: Boolean) {

        val externalDocument = toXml(StringReader(xmlFile.readText()))

        val courtCode = DocumentUtils.getCourtDetail(externalDocument.documentElement, useXPath)

        assertThat(courtCode?.first).isEqualTo("B10JQ")
        assertThat(courtCode?.second).isEqualTo(5)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `get court code from ExternalDocumentRequest where there is no room`(useXPath: Boolean) {

        val str: String = xmlFile.readText().replace(sourceFileNameElement, "<source_file_name>5_26102020_2992_B10JQ_ADULT_COURT_LIST_DAILY</source_file_name>")
        val externalDocument = toXml(StringReader(str))

        val courtCode = DocumentUtils.getCourtDetail(externalDocument.documentElement, useXPath)

        assertThat(courtCode?.first).isEqualTo("B10JQ")
        assertThat(courtCode?.second).isEqualTo(0)
    }

    @Test
    fun `get filename from correctly formed ExternalDocumentRequest`() {

        val externalDocument = toXml(StringReader(xmlFile.readText()))

        val courtCode = DocumentUtils.getFileName(externalDocument.documentElement)

        assertThat(courtCode).isEqualTo("5_26102020_2992_B10JQ05_ADULT_COURT_LIST_DAILY")
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `get empty set when there is no source_file_name`(useXPath: Boolean) {

        val str: String = xmlFile.readText().replace(sourceFileNameElement, "")
        val externalDocument = toXml(StringReader(str))

        val courtCode = DocumentUtils.getCourtDetail(externalDocument.documentElement, useXPath)

        assertThat(courtCode).isNull()
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `get empty set when there is no source_file_name value`(useXPath: Boolean) {

        val str: String = xmlFile.readText().replace(sourceFileNameElement, "<source_file_name />")
        val externalDocument = toXml(StringReader(str))

        val courtCode = DocumentUtils.getCourtDetail(externalDocument.documentElement, useXPath)

        assertThat(courtCode).isNull()
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `get empty set when source_file_name is invalid`(useXPath: Boolean) {

        val str: String = xmlFile.readText().replace(sourceFileNameElement, "<source_file_name>5_26102020_2992_</source_file_name>")
        val externalDocument = toXml(StringReader(str))

        val courtCode = DocumentUtils.getCourtDetail(externalDocument.documentElement, useXPath)

        assertThat(courtCode).isNull()
    }

    companion object {

        private lateinit var xmlFile: File

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            xmlFile = File("./src/test/resources/external-document-request/request-B10JQ01.xml")
        }
    }
}
