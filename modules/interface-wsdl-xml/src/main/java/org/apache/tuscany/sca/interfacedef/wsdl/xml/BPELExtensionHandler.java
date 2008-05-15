package org.apache.tuscany.sca.interfacedef.wsdl.xml;

import java.io.PrintWriter;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.ExtensionSerializer;
import javax.wsdl.extensions.ExtensionDeserializer;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

/**
 * A WSDL extension processor for extension elements introduced by BPEL - in particular
 * the <partnerLinkType.../> elements
 * @author Mike Edwards
 *
 * @version $Rev$ $Date$
 */
public class BPELExtensionHandler implements ExtensionSerializer, ExtensionDeserializer {
	
	private final String localName = "partnerLinkType";
	private final String roleName = "role";

	/**
	 * Marshals the BPEL partner link type extension element to XML
	 * See (@link javax.wsdl.extensions.ExtensionSerializer)
	 */
	public void marshall(Class parentType, QName elementType, ExtensibilityElement theElement,
			PrintWriter writer, Definition def, ExtensionRegistry extReg)
			throws WSDLException {
		// The format of the Partner Link Type in XML is as follows:
		// <foo:partnerLinkType name="bar">
		//    <foo:role name="somename" portType="xyz:portTypeName"/>
		//    <foo:role name="othername" portType="xyz:portTypeName2"/>
		// <foo:partnerLinkType>
		BPELPartnerLinkTypeExt thePLinkType = (BPELPartnerLinkTypeExt) theElement;
		QName theType = thePLinkType.getElementType();
		
		writer.println("<" + theType.toString() + 
				       " name=\"" + thePLinkType.getName() + "\">");
		for( int i = 0; i < 2; i++ ) {
			if( thePLinkType.getRoleName( i ) != null ) {
				writer.println( "<{" + theType.getNamespaceURI() + "}role" 
						       + " name=\"" + thePLinkType.getRoleName(i) + "\" portType=\"" 
						       + thePLinkType.getRolePortType(i) + "\">");
			} // end if
		} // end for
		writer.println("</" + theType.toString() + ">");
	} // end marshall

	/**
	 * Unmarshals the BPEL partner link type element from XML
	 * See (@link javax.wsdl.extensions.ExtensionDeserializer)
	 */
	public ExtensibilityElement unmarshall(Class theClass, QName elementType,
			Element theElement, Definition def, ExtensionRegistry extReg)
			throws WSDLException {
		// System.out.println("BPELExtensionHandler unmarshall called");
		
		// Check that this elementType really is a partnerLinkType element
		if( !elementType.getLocalPart().equals(localName) ) return null;
		BPELPartnerLinkTypeExt theExtension = new BPELPartnerLinkTypeExt();
		theExtension.setElementType(elementType);
		theExtension.setName( theElement.getAttribute("name") );
		
		//Fetch the child "role" elements
		NodeList theRoles = theElement.getElementsByTagNameNS("*", roleName);
		for ( int i=0; i < theRoles.getLength(); i++ ) {
			if( i > 1 ) break;
			Element roleNode = (Element)theRoles.item(i);
			String roleName = roleNode.getAttribute("name");
			String portType = roleNode.getAttribute("portType");
			// The PortType attribute is a QName in prefix:localName format - convert to a QName
			QName rolePortType = getQNameValue( def, portType );
			theExtension.setRole( i, roleName, rolePortType );
		} // end for
		return theExtension;
	} // end unmarshall

	
    /**
     * Returns a QName from a string.  
     * @param definition - a WSDL Definition
     * @param value - the String from which to form the QName in the form "pref:localName"
     * @return
     */
    protected QName getQNameValue(Definition definition, String value) {
        if (value != null && definition != null) {
            int index = value.indexOf(':');
            String prefix = index == -1 ? "" : value.substring(0, index);
            String localName = index == -1 ? value : value.substring(index + 1);
            String ns = definition.getNamespace(prefix);
            if (ns == null) {
                ns = "";
            }
            return new QName(ns, localName, prefix);
        } else {
            return null;
        }
    } // end getQNameValue
	
} // end BPELExtensionHandler
