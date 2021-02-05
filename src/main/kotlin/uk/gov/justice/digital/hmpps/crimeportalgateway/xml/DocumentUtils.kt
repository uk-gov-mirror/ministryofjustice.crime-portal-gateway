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
    private const val COURT_DETAIL_LENGTH = 7

    private const val SOURCE_FILE_NAME = "source_file_name"

    private const val SOURCE_FILE_NAME_EXPR = "//source_file_name/text()"

    private const val DELIMITER = "_"

    fun getCourtDetail(documents: Element, useXPath: Boolean): Pair<String, Int>? {
        return if (useXPath) getCourtCodeByXPath(documents) else getCourtCode(documents)
    }

    private fun getCourtCodeByXPath(documents: Element): Pair<String, Int>? {
        // XPathFactory not thread safe so make one each time
        val xPath: XPath = XPathFactory.newInstance().newXPath()
        val exp = xPath.compile(SOURCE_FILE_NAME_EXPR)
        val nodeList = exp.evaluate(documents, XPathConstants.NODESET) as NodeList
        for (i in 0 until nodeList.length) {
            getCourtDetail(nodeList.item(i))?.let {
                code ->
                return code
            }
        }
        return null
    }

    private fun getCourtCode(documents: Element): Pair<String, Int>? {
        val sourceFileNameNodes = documents.getElementsByTagName(SOURCE_FILE_NAME)
        for (j in 0 until sourceFileNameNodes.length) {
            val sourceFileNameElement = sourceFileNameNodes.item(j) as Element
            val childTextNodes: NodeList = sourceFileNameElement.childNodes
            for (k in 0 until childTextNodes.length) {
                return getCourtDetail(childTextNodes.item(k))
            }
        }
        return null
    }

    private fun getCourtDetail(item: Node): Pair<String, Int>? {
        // Source filename has the following format 146_27072020_2578_B01OB00_ADULT_COURT_LIST_DAILY
        val nodeValue = item.nodeValue
        val fileNameParts: Array<String> = nodeValue?.split(DELIMITER)?.toTypedArray() ?: emptyArray()
        if (fileNameParts.size >= 4) {
            fileNameParts[3].toUpperCase().let { ouCode ->
                if (ouCode.length >= OU_CODE_LENGTH) {
                    val court = ouCode.substring(0, OU_CODE_LENGTH)
                    val room = getRoomFromFileName(ouCode)
                    return Pair(court, room)
                }
            }
        } else { log.error("failed to extract court code from: $nodeValue") }
        return null
    }

    fun getFileName(documents: Element): String? {
        val xPath: XPath = XPathFactory.newInstance().newXPath()
        val exp = xPath.compile(SOURCE_FILE_NAME_EXPR)
        val nodeList = exp.evaluate(documents, XPathConstants.NODESET) as NodeList
        for (i in 0 until nodeList.length) {
            return nodeList.item(i).textContent
        }
        return null
    }

    private fun getRoomFromFileName(ouCode: String): Int {
        if (ouCode.length >= COURT_DETAIL_LENGTH) {
            return ouCode.substring(OU_CODE_LENGTH, OU_CODE_LENGTH + 2).toIntOrNull() ?: 0
        }
        return 0
    }
}
