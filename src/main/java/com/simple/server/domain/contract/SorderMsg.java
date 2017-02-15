package com.simple.server.domain.contract;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.simple.server.domain.IRec;
import com.simple.server.domain.contract.SalesLineMsg;
import com.simple.server.domain.nav.NavSalesLine;
import com.simple.server.domain.nav.NavSorder;

@JsonAutoDetect
@JsonDeserialize(as = SorderMsg.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SorderMsg extends AContract {
	
	private static final long serialVersionUID = 1L;

	protected String outerCustomerId;

	protected String outerSorderId;

	protected String outerUserID;

	protected String sorderNo;

	protected String officialAgreementId;

	protected String privateAgreementId;

	protected String freewareAgreementId;

	protected String customerId;

	protected String customerName;

	protected String salespersonId;

	protected String contactId;

	protected String contact2Id;

	protected String phoneNo;

	protected String phoneNo2;

	protected String email;

	protected String email2;

	protected String address;

	protected String address2;

	protected String address3;

	protected String kladrRegion;

	protected String kladrCity;

	protected String kladrDistrict;

	protected String kladrStreet;

	protected String kladrLocality;

	protected String kladrHouse;

	protected String kladrBuilding;

	protected String kladrFlat;

	protected String kladrVladenie;

	protected String kladrCorpus;

	protected String kladrOffice;

	protected String kladrAdditional;

	protected String deliveryTimeFrom;

	protected String deliveryTimeTo;

	protected String shipmentDate;

	protected String shipmentMethodCode;

	protected String preliminaryOrder;

	protected String sorderSource;

	protected String sorderDate;

	protected String comment;

	protected String wineShopCustomerNo;

	protected String simpleWaters;

	protected String paymentTerms;

	protected String companyName;

	List<SalesLineMsg> slList = new ArrayList();
	
	@Override
	public String getClazz() {
		return SorderMsg.class.getName();
	}

	public String getOuterCustomerId() {
		return outerCustomerId;
	}

	public void setOuterCustomerId(String outerCustomerId) {
		this.outerCustomerId = outerCustomerId;
	}

	public String getOuterSorderId() {
		return outerSorderId;
	}

	public void setOuterSorderId(String outerSorderId) {
		this.outerSorderId = outerSorderId;
	}

	public String getOuterUserID() {
		return outerUserID;
	}

	public void setOuterUserID(String outerUserID) {
		this.outerUserID = outerUserID;
	}

	public String getSorderNo() {
		return sorderNo;
	}

	public void setSorderNo(String sorderNo) {
		this.sorderNo = sorderNo;
	}

	public String getOfficialAgreementId() {
		return officialAgreementId;
	}

	public void setOfficialAgreementId(String officialAgreementId) {
		this.officialAgreementId = officialAgreementId;
	}

	public String getPrivateAgreementId() {
		return privateAgreementId;
	}

	public void setPrivateAgreementId(String privateAgreementId) {
		this.privateAgreementId = privateAgreementId;
	}

	public String getFreewareAgreementId() {
		return freewareAgreementId;
	}

	public void setFreewareAgreementId(String freewareAgreementId) {
		this.freewareAgreementId = freewareAgreementId;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getSalespersonId() {
		return salespersonId;
	}

	public void setSalespersonId(String salespersonId) {
		this.salespersonId = salespersonId;
	}

	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	public String getContact2Id() {
		return contact2Id;
	}

	public void setContact2Id(String contact2Id) {
		this.contact2Id = contact2Id;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public String getPhoneNo2() {
		return phoneNo2;
	}

	public void setPhoneNo2(String phoneNo2) {
		this.phoneNo2 = phoneNo2;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail2() {
		return email2;
	}

	public void setEmail2(String email2) {
		this.email2 = email2;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	public String getAddress3() {
		return address3;
	}

	public void setAddress3(String address3) {
		this.address3 = address3;
	}

	public String getKladrRegion() {
		return kladrRegion;
	}

	public void setKladrRegion(String kladrRegion) {
		this.kladrRegion = kladrRegion;
	}

	public String getKladrCity() {
		return kladrCity;
	}

	public void setKladrCity(String kladrCity) {
		this.kladrCity = kladrCity;
	}

	public String getKladrDistrict() {
		return kladrDistrict;
	}

	public void setKladrDistrict(String kladrDistrict) {
		this.kladrDistrict = kladrDistrict;
	}

	public String getKladrStreet() {
		return kladrStreet;
	}

	public void setKladrStreet(String kladrStreet) {
		this.kladrStreet = kladrStreet;
	}

	public String getKladrLocality() {
		return kladrLocality;
	}

	public void setKladrLocality(String kladrLocality) {
		this.kladrLocality = kladrLocality;
	}

	public String getKladrHouse() {
		return kladrHouse;
	}

	public void setKladrHouse(String kladrHouse) {
		this.kladrHouse = kladrHouse;
	}

	public String getKladrBuilding() {
		return kladrBuilding;
	}

	public void setKladrBuilding(String kladrBuilding) {
		this.kladrBuilding = kladrBuilding;
	}

	public String getKladrFlat() {
		return kladrFlat;
	}

	public void setKladrFlat(String kladrFlat) {
		this.kladrFlat = kladrFlat;
	}

	public String getKladrVladenie() {
		return kladrVladenie;
	}

	public void setKladrVladenie(String kladrVladenie) {
		this.kladrVladenie = kladrVladenie;
	}

	public String getKladrCorpus() {
		return kladrCorpus;
	}

	public void setKladrCorpus(String kladrCorpus) {
		this.kladrCorpus = kladrCorpus;
	}

	public String getKladrOffice() {
		return kladrOffice;
	}

	public void setKladrOffice(String kladrOffice) {
		this.kladrOffice = kladrOffice;
	}

	public String getKladrAdditional() {
		return kladrAdditional;
	}

	public void setKladrAdditional(String kladrAdditional) {
		this.kladrAdditional = kladrAdditional;
	}

	public String getDeliveryTimeFrom() {
		return deliveryTimeFrom;
	}

	public void setDeliveryTimeFrom(String deliveryTimeFrom) {
		this.deliveryTimeFrom = deliveryTimeFrom;
	}

	public String getDeliveryTimeTo() {
		return deliveryTimeTo;
	}

	public void setDeliveryTimeTo(String deliveryTimeTo) {
		this.deliveryTimeTo = deliveryTimeTo;
	}

	public String getShipmentDate() {
		return shipmentDate;
	}

	public void setShipmentDate(String shipmentDate) {
		this.shipmentDate = shipmentDate;
	}

	public String getShipmentMethodCode() {
		return shipmentMethodCode;
	}

	public void setShipmentMethodCode(String shipmentMethodCode) {
		this.shipmentMethodCode = shipmentMethodCode;
	}

	public String getPreliminaryOrder() {
		return preliminaryOrder;
	}

	public void setPreliminaryOrder(String preliminaryOrder) {
		this.preliminaryOrder = preliminaryOrder;
	}

	public String getSorderSource() {
		return sorderSource;
	}

	public void setSorderSource(String sorderSource) {
		this.sorderSource = sorderSource;
	}

	public String getSorderDate() {
		return sorderDate;
	}

	public void setSorderDate(String sorderDate) {
		this.sorderDate = sorderDate;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getWineShopCustomerNo() {
		return wineShopCustomerNo;
	}

	public void setWineShopCustomerNo(String wineShopCustomerNo) {
		this.wineShopCustomerNo = wineShopCustomerNo;
	}

	public String getSimpleWaters() {
		return simpleWaters;
	}

	public void setSimpleWaters(String simpleWaters) {
		this.simpleWaters = simpleWaters;
	}

	public String getPaymentTerms() {
		return paymentTerms;
	}

	public void setPaymentTerms(String paymentTerms) {
		this.paymentTerms = paymentTerms;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public List<SalesLineMsg> getSlList() {
		return slList;
	}

	public void setSlList(List<SalesLineMsg> slList) {
		this.slList = slList;
	}

	@Override
	public void copyFrom(IRec rec) {
		if (rec == null)
			return;
		if (rec instanceof NavSorder) {
			NavSorder nso = (NavSorder) rec;
			this.setAddress(nso.getNavAddress());
			this.setAddress2(nso.getNavAddress2());
			this.setAddress3(nso.getNavAddress3());
			this.setComment(nso.getNavComment1() + nso.getNavComment2() + nso.getNavComment2() + nso.getNavComment3()
					+ nso.getNavComment4());
			this.setContactId(nso.getNavContact());
			this.setContact2Id(nso.getNavContact2());
			this.setCustomerId(nso.getNavCustomerNo());
			this.setCustomerName(nso.getNavCustomerName() + nso.getNavCustomerName2() + nso.getNavCustomerName3());
			this.setDeliveryTimeFrom(nso.getNavDeliveryTimeFrom());
			this.setDeliveryTimeTo(nso.getNavDeliveryTimeTo());
			this.setEmail(nso.getNavEmail());
			this.setFreewareAgreementId(nso.getNavFreewareAgreementNo());
			this.setJuuid(nso.getJuuid());
			this.setKladrAdditional(nso.getNavKladrAdditional());
			this.setKladrBuilding(nso.getNavKladrBuilding());
			this.setKladrCity(nso.getNavKladrCity());
			this.setKladrCorpus(nso.getNavKladrCorpus());
			this.setKladrDistrict(nso.getNavKladrDistrict());
			this.setKladrFlat(nso.getNavKladrFlat());
			this.setKladrHouse(nso.getNavKladrHouse());
			this.setKladrLocality(nso.getNavKladrLocality());
			this.setKladrOffice(nso.getNavKladrOffice());
			this.setKladrRegion(nso.getNavKladrRegion());
			this.setKladrStreet(nso.getNavKladrStreet());
			this.setKladrVladenie(nso.getNavKladrVladenie());
			this.setOfficialAgreementId(nso.getNavOfficialAgreementNo());
			this.setOuterCustomerId(nso.getBtxCustomerNo());
			this.setOuterSorderId(nso.getBtxSorderNo());
			this.setOuterUserID(nso.getBtxUserID());
			this.setPaymentTerms(nso.getNavPaymentTerms());
			this.setPhoneNo(nso.getNavPhoneNo());
			this.setPhoneNo2(nso.getNavPhoneNo2());
			this.setPreliminaryOrder(nso.getNavPreliminaryOrder());
			this.setPrivateAgreementId(nso.getNavPrivateAgreementNo());
			this.setSalespersonId(nso.getNavSalespersonCode());
			this.setShipmentDate(nso.getNavShipmentDate());
			this.setShipmentMethodCode(nso.getNavShipmentMethodCode());
			this.setSimpleWaters(nso.getNavSimpleWaters());
			this.setSorderDate(nso.getNavSorderDate());
			this.setSorderNo(nso.getNavSorderNo());
			this.setSorderSource(nso.getNavSorderSource());
			this.setWineShopCustomerNo(nso.getNavWineShopCustomerNo());

			for (NavSalesLine nsl : nso.getSalesLineList()) {
				SalesLineMsg sl = new SalesLineMsg();
				sl.copyFrom(nsl);
				slList.add(sl);
			}
		}

	}

	@Override
	public String toString() {
		return "SorderMsg [outerCustomerId=" + outerCustomerId + ", outerSorderId=" + outerSorderId + ", outerUserID="
				+ outerUserID + ", sorderNo=" + sorderNo + ", officialAgreementId=" + officialAgreementId
				+ ", privateAgreementId=" + privateAgreementId + ", freewareAgreementId=" + freewareAgreementId
				+ ", customerId=" + customerId + ", customerName=" + customerName + ", salespersonId=" + salespersonId
				+ ", contactId=" + contactId + ", contact2Id=" + contact2Id + ", phoneNo=" + phoneNo + ", phoneNo2="
				+ phoneNo2 + ", email=" + email + ", email2=" + email2 + ", address=" + address + ", address2="
				+ address2 + ", address3=" + address3 + ", kladrRegion=" + kladrRegion + ", kladrCity=" + kladrCity
				+ ", kladrDistrict=" + kladrDistrict + ", kladrStreet=" + kladrStreet + ", kladrLocality="
				+ kladrLocality + ", kladrHouse=" + kladrHouse + ", kladrBuilding=" + kladrBuilding + ", kladrFlat="
				+ kladrFlat + ", kladrVladenie=" + kladrVladenie + ", kladrCorpus=" + kladrCorpus + ", kladrOffice="
				+ kladrOffice + ", kladrAdditional=" + kladrAdditional + ", deliveryTimeFrom=" + deliveryTimeFrom
				+ ", deliveryTimeTo=" + deliveryTimeTo + ", shipmentDate=" + shipmentDate + ", shipmentMethodCode="
				+ shipmentMethodCode + ", preliminaryOrder=" + preliminaryOrder + ", sorderSource=" + sorderSource
				+ ", sorderDate=" + sorderDate + ", comment=" + comment + ", wineShopCustomerNo=" + wineShopCustomerNo
				+ ", simpleWaters=" + simpleWaters + ", paymentTerms=" + paymentTerms + ", companyName=" + companyName
				+ ", slList=" + slList + ", clazz=" + clazz + ", juuid=" + juuid + ", endPointId=" + endPointId
				+ ", operationType=" + operationType + ", serviceIdFrom=" + serviceIdFrom + ", serviceIdTo="
				+ serviceIdTo + ", serviceRoleFrom=" + serviceRoleFrom + ", serviceRoleTo=" + serviceRoleTo
				+ ", serviceOutDatetime=" + serviceOutDatetime + ", serviceInDatetime=" + serviceInDatetime
				+ ", requestInDatetime=" + requestInDatetime + ", messageHeaderValue=" + messageHeaderValue
				+ ", messageBodyValue=" + messageBodyValue + ", logDatetime=" + logDatetime + ", loggerId=" + loggerId
				+ ", methodHandler=" + methodHandler + ", responseURI=" + responseURI + ", responseContentType="
				+ responseContentType + ", responseContractClass=" + responseContractClass + "]";
	}
	
	

}
