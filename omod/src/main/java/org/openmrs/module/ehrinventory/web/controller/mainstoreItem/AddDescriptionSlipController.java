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

package org.openmrs.module.ehrinventory.web.controller.mainstoreItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.Role;
import org.openmrs.api.context.Context;
import org.openmrs.module.hospitalcore.model.InventoryStore;
import org.openmrs.module.hospitalcore.model.InventoryStoreRoleRelation;
import org.openmrs.module.hospitalcore.util.ActionValue;
import org.openmrs.module.ehrinventory.InventoryService;
import org.openmrs.module.ehrinventory.model.InventoryStoreItem;
import org.openmrs.module.ehrinventory.model.InventoryStoreItemTransaction;
import org.openmrs.module.ehrinventory.model.InventoryStoreItemTransactionDetail;
import org.openmrs.module.ehrinventory.web.controller.global.StoreSingleton;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * <p> Class: AddDescriptionSlipController </p>
 * <p> Package: org.openmrs.module.inventory.web.controller.mainstore </p>
 * <p> Create date: Jan 6, 2011 1:37:59 PM </p>
 * <p> Update date: Jan 6, 2011 1:37:59 PM </p>
 **/

@Controller("AddDescriptionItemSlipController")
@RequestMapping("/module/ehrinventory/itemAddDescriptionReceiptSlip.form")
public class AddDescriptionSlipController {
	@RequestMapping(method = RequestMethod.GET)
	public String firstView(Model model) {
		return "/module/ehrinventory/mainstoreItem/itemAddDescriptionReceiptSlip";
	}
	@RequestMapping(method = RequestMethod.POST)
	public String submit(HttpServletRequest request, Model model) {
		String description = request.getParameter("description");
		InventoryService inventoryService = (InventoryService) Context.getService(InventoryService.class);
		Date date = new Date();
		int userId = Context.getAuthenticatedUser().getId();
		//InventoryStore store = inventoryService.getStoreByCollectionRole(new ArrayList<Role>(Context.getAuthenticatedUser().getAllRoles()));
		 List <Role>role=new ArrayList<Role>(Context.getAuthenticatedUser().getAllRoles());
			
			InventoryStoreRoleRelation srl=null;
			Role rl = null;
			for(Role r: role){
				if(inventoryService.getStoreRoleByName(r.toString())!=null){
					srl = inventoryService.getStoreRoleByName(r.toString());	
					rl=r;
				}
			}
			InventoryStore store =null;
			if(srl!=null){
				store = inventoryService.getStoreById(srl.getStoreid());
				
			}
		InventoryStoreItemTransaction transaction = new InventoryStoreItemTransaction();
		transaction.setDescription(description);
		transaction.setCreatedOn(date);
		transaction.setStore(store);
		transaction.setTypeTransaction(ActionValue.TRANSACTION[0]);
		transaction.setCreatedBy(Context.getAuthenticatedUser().getGivenName());
		transaction = inventoryService.saveStoreItemTransaction(transaction);
		
		String fowardParam = "itemReceipt_"+userId;
		List<InventoryStoreItemTransactionDetail> list = (List<InventoryStoreItemTransactionDetail> )StoreSingleton.getInstance().getHash().get(fowardParam);
		if(list != null && list.size() > 0){
			for(int i=0;i< list.size();i++){
				InventoryStoreItemTransactionDetail transactionDetail = list.get(i);
				//save total first
				Integer specificationId = transactionDetail.getSpecification() != null ?transactionDetail.getSpecification().getId() : null;
				InventoryStoreItem storeItem = inventoryService.getStoreItem(store.getId(), transactionDetail.getItem().getId(), specificationId);
				if(storeItem == null){
					storeItem = new InventoryStoreItem();
					storeItem.setCurrentQuantity(transactionDetail.getQuantity());
					storeItem.setReceiptQuantity(transactionDetail.getQuantity());
					storeItem.setItem(transactionDetail.getItem());
					storeItem.setSpecification(transactionDetail.getSpecification());
					storeItem.setStore(store);
					storeItem.setStatusIndent(0);
					storeItem.setReorderQty(0);
					storeItem.setOpeningBalance(0);
					storeItem.setClosingBalance(transactionDetail.getQuantity());
					storeItem.setStatus(0);
					storeItem.setReorderQty(transactionDetail.getItem().getReorderQty());
					storeItem = inventoryService.saveStoreItem(storeItem);
					
				}else{
					storeItem.setOpeningBalance(storeItem.getClosingBalance());
					storeItem.setClosingBalance(storeItem.getClosingBalance()+transactionDetail.getQuantity());
					storeItem.setCurrentQuantity(storeItem.getCurrentQuantity() + transactionDetail.getQuantity());
					storeItem.setReceiptQuantity(transactionDetail.getQuantity());
					storeItem.setReorderQty(transactionDetail.getItem().getReorderQty());
					storeItem = inventoryService.saveStoreItem(storeItem);
				}
				//save transactionDetail
				transactionDetail.setOpeningBalance(storeItem.getOpeningBalance());
				transactionDetail.setClosingBalance(storeItem.getClosingBalance());
				transactionDetail.setTransaction(transaction);
				
				transactionDetail.setAttribute(transactionDetail.getItem().getAttributeName());
				
				inventoryService.saveStoreItemTransactionDetail(transactionDetail);
			}
			StoreSingleton.getInstance().getHash().remove(fowardParam);
			model.addAttribute("message", "Succesfully");
			model.addAttribute("urlS", "itemReceiptsToGeneralStoreList.form");
		}else{
			model.addAttribute("message", "Sorry don't have any receipt to save");
			model.addAttribute("urlS", "itemReceiptsToGeneralStore.form");
		}
	 return "/module/ehrinventory/thickbox/success";
	}
}
