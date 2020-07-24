/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.inventory.web.controller.property.editor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.hospitalcore.model.InventoryStore;
import org.openmrs.module.inventory.InventoryService;

public class StorePropertyEditor  extends PropertyEditorSupport {
	private Log log = LogFactory.getLog(this.getClass());
	public StorePropertyEditor() {
	}
	public void setAsText(String text) throws IllegalArgumentException {
		InventoryService inventoryService = (InventoryService) Context.getService(InventoryService.class);
		if (text != null && text.trim().length() > 0 ) {
			try {
				setValue(inventoryService.getStoreById(NumberUtils.toInt(text)));
			}
			catch (Exception ex) {
				log.error("Error setting text: " + text, ex);
				throw new IllegalArgumentException("Store not found: " + ex.getMessage());
			}
		} else {
			setValue(null);
		}
	}
	
	public String getAsText() {
		InventoryStore s = (InventoryStore) getValue();
		if (s == null ) {
			return null; 
		} else {
			return s.getId()+"";
		}
	}
}
