
package com.netsuite.webservices.lists.accounting;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import com.netsuite.webservices.platform.common.EmployeeSearchRowBasic;
import com.netsuite.webservices.platform.common.OtherNameCategorySearchRowBasic;
import com.netsuite.webservices.platform.core.SearchRow;


/**
 * <p>Java class for OtherNameCategorySearchRow complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OtherNameCategorySearchRow">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:core_2014_2.platform.webservices.netsuite.com}SearchRow">
 *       &lt;sequence>
 *         &lt;element name="basic" type="{urn:common_2014_2.platform.webservices.netsuite.com}OtherNameCategorySearchRowBasic" minOccurs="0"/>
 *         &lt;element name="userJoin" type="{urn:common_2014_2.platform.webservices.netsuite.com}EmployeeSearchRowBasic" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OtherNameCategorySearchRow", propOrder = {
    "basic",
    "userJoin"
})
public class OtherNameCategorySearchRow
    extends SearchRow
{

    protected OtherNameCategorySearchRowBasic basic;
    protected EmployeeSearchRowBasic userJoin;

    /**
     * Gets the value of the basic property.
     * 
     * @return
     *     possible object is
     *     {@link OtherNameCategorySearchRowBasic }
     *     
     */
    public OtherNameCategorySearchRowBasic getBasic() {
        return basic;
    }

    /**
     * Sets the value of the basic property.
     * 
     * @param value
     *     allowed object is
     *     {@link OtherNameCategorySearchRowBasic }
     *     
     */
    public void setBasic(OtherNameCategorySearchRowBasic value) {
        this.basic = value;
    }

    /**
     * Gets the value of the userJoin property.
     * 
     * @return
     *     possible object is
     *     {@link EmployeeSearchRowBasic }
     *     
     */
    public EmployeeSearchRowBasic getUserJoin() {
        return userJoin;
    }

    /**
     * Sets the value of the userJoin property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmployeeSearchRowBasic }
     *     
     */
    public void setUserJoin(EmployeeSearchRowBasic value) {
        this.userJoin = value;
    }

}
