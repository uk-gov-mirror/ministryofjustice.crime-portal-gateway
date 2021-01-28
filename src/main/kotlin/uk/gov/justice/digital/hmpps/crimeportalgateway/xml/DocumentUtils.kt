package uk.gov.justice.digital.hmpps.crimeportalgateway.xml

import org.slf4j.LoggerFactory
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

object DocumentUtils {

    private val log = LoggerFactory.getLogger(this::class.java)

    private const val OU_CODE_LENGTH = 5

    private const val SOURCE_FILE_NAME = "source_file_name"

    private const val SOURCE_FILE_NAME_EXPR = "//source_file_name/text()"

    private const val DELIMITER = "_"

    fun getCourtCode(documents: Element, useXPath: Boolean): String? {
        return if (useXPath) getCourtCodeByXPath(documents) else getCourtCode(documents)
    }

    private fun getCourtCodeByXPath(documents: Element): String? {
        // XPathFactory not thread safe so make one each time
        val xPath: XPath = XPathFactory.newInstance().newXPath()
        val exp = xPath.compile(SOURCE_FILE_NAME_EXPR)
        val nodeList = exp.evaluate(documents, XPathConstants.NODESET) as NodeList
        for (i in 0 until nodeList.length) {
            getOuCode(nodeList.item(i))?.let {
                code ->
                return code
            }
        }
        return null
    }

    private fun getCourtCode(documents: Element): String? {
        val sourceFileNameNodes = documents.getElementsByTagName(SOURCE_FILE_NAME)
        for (j in 0 until sourceFileNameNodes.length) {
            val sourceFileNameElement = sourceFileNameNodes.item(j) as Element
            val childTextNodes: NodeList = sourceFileNameElement.childNodes
            for (k in 0 until childTextNodes.length) {
                return getOuCode(childTextNodes.item(k))
            }
        }
        return null
    }

    private fun getOuCode(item: Node): String? {
        // Source filename has the following format 146_27072020_2578_B01OB00_ADULT_COURT_LIST_DAILY
        val nodeValue = item.nodeValue
        val fileNameParts: Array<String> = nodeValue?.split(DELIMITER)?.toTypedArray() ?: emptyArray()
        if (fileNameParts.size >= 4) {
            fileNameParts[3].toUpperCase().let { ouCode ->
                if (ouCode.length >= OU_CODE_LENGTH) {
                    return ouCode.substring(0, OU_CODE_LENGTH)
                }
            }
        } else { log.error("failed to extract court code from: $nodeValue") }
        return null
    }
}
