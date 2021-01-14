package uk.gov.justice.digital.hmpps.crimeportalgateway.xml

import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

object DocumentUtils {

    private const val OU_CODE_LENGTH = 5

    private const val SOURCE_FILE_NAME_EXPR = "//source_file_name/text()"

    private const val DELIMITER = "_"

    fun getCourtCode(documents: Element): String? {
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

    private fun getOuCode(item: Node): String? {
        // Source filename has the following format 146_27072020_2578_B01OB00_ADULT_COURT_LIST_DAILY
        val fileNameParts: Array<String> = item.nodeValue?.split(DELIMITER)?.toTypedArray() ?: emptyArray()
        if (fileNameParts.size >= 4) {
            fileNameParts[3].toUpperCase().let { ouCode ->
                if (ouCode.length >= OU_CODE_LENGTH) {
                    return ouCode.substring(0, OU_CODE_LENGTH)
                }
            }
        }
        return null
    }
}
