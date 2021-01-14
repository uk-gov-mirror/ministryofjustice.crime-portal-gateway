package uk.gov.justice.digital.hmpps.crimeportalgateway.xml

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
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

    private val sourceFileName = "5_26102020_2992_B10JQ00_ADULT_COURT_LIST_DAILY"
    private val sourceFileNameElement = "<source_file_name>$sourceFileName</source_file_name>"

    @Test
    fun `get court code from correctly formed ExternalDocumentRequest`() {

        val externalDocument = toXml(StringReader(xmlFile.readText()))

        val courtCode = DocumentUtils.getCourtCode(externalDocument.documentElement)

        assertThat(courtCode).isEqualTo("B10JQ")
    }

    @Test
    fun `get empty set when there is no source_file_name`() {

        val str: String = xmlFile.readText().replace(sourceFileNameElement, "")
        val externalDocument = toXml(StringReader(str))

        val courtCode = DocumentUtils.getCourtCode(externalDocument.documentElement)

        assertThat(courtCode).isNull()
    }

    @Test
    fun `get empty set when there is no source_file_name value`() {

        val str: String = xmlFile.readText().replace(sourceFileNameElement, "<source_file_name />")
        val externalDocument = toXml(StringReader(str))

        val courtCode = DocumentUtils.getCourtCode(externalDocument.documentElement)

        assertThat(courtCode).isNull()
    }

    @Test
    fun `get empty set when source_file_name is invalid`() {

        val str: String = xmlFile.readText().replace(sourceFileNameElement, "<source_file_name>5_26102020_2992_</source_file_name>")
        val externalDocument = toXml(StringReader(str))

        val courtCode = DocumentUtils.getCourtCode(externalDocument.documentElement)

        assertThat(courtCode).isNull()
    }

    companion object {

        private lateinit var xmlFile: File

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            xmlFile = File("./src/test/resources/external-document-request/request-B10JQ.xml")
        }
    }
}
