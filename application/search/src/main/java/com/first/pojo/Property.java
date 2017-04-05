package com.first.pojo;

import java.util.List;

public class Property {

	private String propId;
	
	private String propName;
	
	private List<PropValue> propertiesDetail;

	public String getPropId() {
		return propId;
	}

	public void setPropId(String propId) {
		this.propId = propId;
	}

	public String getPropName() {
		return propName;
	}

	public void setPropName(String propName) {
		this.propName = propName;
	}

	public List<PropValue> getPropertiesDetail() {
		return propertiesDetail;
	}

	public void setPropertiesDetail(List<PropValue> propertiesDetail) {
		this.propertiesDetail = propertiesDetail;
	}

	@Override
	public String toString() {
		return "Property [propId=" + propId + ", propName=" + propName
				+ ", propertiesDetail=" + propertiesDetail + "]";
	}
}
